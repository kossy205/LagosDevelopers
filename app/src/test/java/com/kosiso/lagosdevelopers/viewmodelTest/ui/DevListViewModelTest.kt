package com.kosiso.lagosdevelopers.viewmodelTest.ui


import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.kosiso.lagosdevelopers.data.remote.NetworkUtils
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import com.kosiso.lagosdevelopers.ui.developer_list_screen.DevelopersListViewModel
import com.kosiso.lagosdevelopers.viewmodelTest.data.repository.FakeMainRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevelopersListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DevelopersListViewModel
    private lateinit var fakeRepository: FakeMainRepository
    private val mockContext = mockk<Context>()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(NetworkUtils)

        fakeRepository = FakeMainRepository()

        viewModel = DevelopersListViewModel(fakeRepository, mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init calls getLagosDevs and repository has default data`() = runTest {

        advanceUntilIdle()

        val developers = fakeRepository.getDevelopersList()
        assertThat(developers).hasSize(3)
        assertThat(developers[0].login).isEqualTo("developer1")
        assertThat(developers[1].login).isEqualTo("developer2")
        assertThat(developers[2].login).isEqualTo("developer3")

        assertThat(viewModel.lagosDevsFlow.value).isNotEqualTo(PagingData.empty<LagosDeveloper>())
    }

    @Test
    fun `getLagosDevs updates repository flow with new data`() = runTest {
        val customDev = LagosDeveloper(
            id = 99L,
            login = "customdev",
            avatarUrl = "https://example.com/custom.jpg"
        )
        fakeRepository.clearDevelopers()
        fakeRepository.addDeveloper(customDev)

        viewModel.getLagosDevs()
        advanceUntilIdle()


        val developers = fakeRepository.getDevelopersList()
        assertThat(developers).hasSize(1)
        assertThat(developers[0].login).isEqualTo("customdev")
        assertThat(developers[0].id).isEqualTo(99L)


        val flowSnapshot = fakeRepository.getDevelopers().asSnapshot()
        assertThat(flowSnapshot).hasSize(1)
        assertThat(flowSnapshot[0].login).isEqualTo("customdev")
    }

    @Test
    fun `addToFavourites with internet connection successfully adds to favorites`() = runTest {

        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true


        viewModel.addToFavourites(lagosDev)
        advanceUntilIdle()


        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)

        val addedDev = favDevs[0]
        assertThat(addedDev.id).isEqualTo(1L)
        assertThat(addedDev.login).isEqualTo("developer1")
        assertThat(addedDev.name).isEqualTo("John Doe")
        assertThat(addedDev.company).isEqualTo("Tech Corp")
        assertThat(addedDev.location).isEqualTo("Lagos, Nigeria")
        assertThat(addedDev.isFavourite).isEqualTo(true)


        assertThat(viewModel.noNetworkState.value).isInstanceOf(DevResponseState.Loading::class.java)
    }

    @Test
    fun `addToFavourites without internet connection sets no internet state`() = runTest {

        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns false


        viewModel.addToFavourites(lagosDev)
        advanceUntilIdle()


        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()


        assertThat(viewModel.noNetworkState.value).isInstanceOf(DevResponseState.NoInternet::class.java)
        val noInternetState = viewModel.noNetworkState.value as DevResponseState.NoInternet
        assertThat(noInternetState.message).isEqualTo("No internet connection")
    }

    @Test
    fun `addToFavourites with internet but failed developer details does not add to favorites`() = runTest {

        val lagosDev = LagosDeveloper(
            id = 999L,
            login = "nonexistent",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        viewModel.addToFavourites(lagosDev)
        advanceUntilIdle()


        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()

        assertThat(viewModel.noNetworkState.value).isInstanceOf(DevResponseState.Loading::class.java)
    }

    @Test
    fun `addToFavourites with repository error does not add to favorites`() = runTest {
        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true
        fakeRepository.shouldReturnError = true
        fakeRepository.errorMessage = "API is down"

        viewModel.addToFavourites(lagosDev)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()

        assertThat(viewModel.noNetworkState.value).isInstanceOf(DevResponseState.Loading::class.java)
    }

    @Test
    fun `initial state has correct default values`() {
        assertThat(viewModel.noNetworkState.value).isInstanceOf(DevResponseState.Loading::class.java)


        val developers = fakeRepository.getDevelopersList()
        assertThat(developers).hasSize(3)
    }

    @Test
    fun `addToFavourites creates correct FavouriteDev object from DeveloperDetails`() = runTest {

        val lagosDev = LagosDeveloper(
            id = 2L,
            login = "developer2",
            avatarUrl = "https://example.com/avatar2.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        viewModel.addToFavourites(lagosDev)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)

        val addedDev = favDevs[0]
        assertThat(addedDev.id).isEqualTo(2L)
        assertThat(addedDev.login).isEqualTo("developer2")
        assertThat(addedDev.avatarUrl).isEqualTo("https://example.com/avatar2.jpg")
        assertThat(addedDev.name).isEqualTo("Jane Smith")
        assertThat(addedDev.company).isEqualTo("StartupXYZ")
        assertThat(addedDev.location).isEqualTo("Lagos, Nigeria")
        assertThat(addedDev.email).isEqualTo("jane@example.com")
        assertThat(addedDev.bio).isEqualTo("Full-stack developer")
        assertThat(addedDev.twitterUsername).isEqualTo("janesmith")
        assertThat(addedDev.publicRepos).isEqualTo(40)
        assertThat(addedDev.followers).isEqualTo(200)
        assertThat(addedDev.following).isEqualTo(75)
        assertThat(addedDev.createdAt).isEqualTo("2019-06-15T00:00:00Z")
        assertThat(addedDev.updatedAt).isEqualTo("2023-12-01T00:00:00Z")
        assertThat(addedDev.isFavourite).isEqualTo(true)
    }

    @Test
    fun `addToFavourites multiple developers adds all to favorites`() = runTest {

        val dev1 = LagosDeveloper(id = 1L, login = "developer1", avatarUrl = "url1")
        val dev2 = LagosDeveloper(id = 2L, login = "developer2", avatarUrl = "url2")

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        viewModel.addToFavourites(dev1)
        viewModel.addToFavourites(dev2)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(2)
        assertThat(favDevs.map { it.login }).containsExactly("developer1", "developer2")
    }

    @Test
    fun `addToFavourites same developer twice updates existing entry`() = runTest {
        val dev1 = LagosDeveloper(id = 1L, login = "developer1", avatarUrl = "url1")

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        viewModel.addToFavourites(dev1)
        viewModel.addToFavourites(dev1)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)
        assertThat(favDevs[0].login).isEqualTo("developer1")
    }

    @Test
    fun `repository flow returns correct paging data`() = runTest {
        val flowSnapshot = fakeRepository.getDevelopers().asSnapshot()

        assertThat(flowSnapshot).hasSize(3)
        assertThat(flowSnapshot.map { it.login }).containsExactly("developer1", "developer2", "developer3")
    }

    @Test
    fun `empty repository returns empty paging data`() = runTest {
        fakeRepository.clearDevelopers()

        val flowSnapshot = fakeRepository.getDevelopers().asSnapshot()

        assertThat(flowSnapshot).isEmpty()
    }
}

