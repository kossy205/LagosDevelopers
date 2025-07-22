package com.kosiso.lagosdevelopers.viewmodelTest.ui


import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.kosiso.lagosdevelopers.data.remote.NetworkUtils
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import com.kosiso.lagosdevelopers.ui.developer_details_screen.DeveloperDetailsViewModel
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
class DevDetailsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DeveloperDetailsViewModel
    private lateinit var fakeRepository: FakeMainRepository
    private val mockContext = mockk<Context>()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock NetworkUtils static method
        mockkObject(NetworkUtils)

        // Create fake repository
        fakeRepository = FakeMainRepository()

        viewModel = DeveloperDetailsViewModel(fakeRepository, mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state has correct default values`() {
        // Then - initial state should be loading and not favourite
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Loading::class.java)
        assertThat(viewModel.isFavourite.value).isFalse()
    }

    @Test
    fun `getDeveloperDetails with existing favorite returns favorite data`() = runTest {
        // Given - add a developer to favorites first
        val favDev = FavouriteDev(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg",
            name = "John Doe",
            company = "Tech Corp",
            location = "Lagos, Nigeria",
            email = "john@example.com",
            bio = "Software developer",
            twitterUsername = "johndoe",
            publicRepos = 25,
            followers = 150,
            following = 50,
            createdAt = "2020-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z",
            isFavourite = true
        )
        fakeRepository.addToFavorites(favDev)

        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        // When
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should return favorite data with isFavourite = true
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Success::class.java)
        val successState = viewModel.developerState.value as DevResponseState.Success
        assertThat(successState.data.isFavourite).isTrue()
        assertThat(successState.data.name).isEqualTo("John Doe")
        assertThat(viewModel.isFavourite.value).isTrue()
    }

    @Test
    fun `getDeveloperDetails with no local data and internet available fetches from API`() = runTest {
        // Given
        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        // When
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should fetch from API and return data with isFavourite = false
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Success::class.java)
        val successState = viewModel.developerState.value as DevResponseState.Success
        assertThat(successState.data.isFavourite).isFalse()
        assertThat(successState.data.name).isEqualTo("John Doe")
        assertThat(successState.data.login).isEqualTo("developer1")
        assertThat(viewModel.isFavourite.value).isFalse()
    }

    @Test
    fun `getDeveloperDetails with no internet connection returns NoInternet state`() = runTest {
        // Given
        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns false

        // When
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should return NoInternet state
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.NoInternet::class.java)
        val noInternetState = viewModel.developerState.value as DevResponseState.NoInternet
        assertThat(noInternetState.message).isEqualTo("No internet connection")
        assertThat(viewModel.isFavourite.value).isFalse() // Should remain false
    }

    @Test
    fun `getDeveloperDetails with API error returns Error state`() = runTest {
        // Given
        val lagosDev = LagosDeveloper(
            id = 999L, // Non-existent developer
            login = "nonexistent",
            avatarUrl = "https://example.com/avatar.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        // When
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should return Error state
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Error::class.java)
        val errorState = viewModel.developerState.value as DevResponseState.Error
        assertThat(errorState.message).isEqualTo("Error fetching developer details")
        assertThat(viewModel.isFavourite.value).isFalse()
    }

    @Test
    fun `getDeveloperDetails with repository error returns Error state`() = runTest {
        // Given
        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true
        fakeRepository.shouldReturnError = true
        fakeRepository.errorMessage = "API is down"

        // When
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should return Error state
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Error::class.java)
        val errorState = viewModel.developerState.value as DevResponseState.Error
        assertThat(errorState.message).isEqualTo("Error fetching developer details")
        assertThat(viewModel.isFavourite.value).isFalse()
    }

    @Test
    fun `removeFromFavourites removes developer from favorites`() = runTest {
        // Given - add a developer to favorites first
        val favDev = FavouriteDev(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg",
            name = "John Doe",
            company = "Tech Corp",
            location = "Lagos, Nigeria",
            email = "john@example.com",
            bio = "Software developer",
            twitterUsername = "johndoe",
            publicRepos = 25,
            followers = 150,
            following = 50,
            createdAt = "2020-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z",
            isFavourite = true
        )
        fakeRepository.addToFavorites(favDev)

        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        // When
        viewModel.removeFromFavourites(lagosDev)
        advanceUntilIdle()

        // Then - developer should be removed from favorites
        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()
    }

    @Test
    fun `insertIntoFavourites with success state adds developer to favorites`() = runTest {
        // Given - first get developer details to set success state
        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        // Get developer details first to populate the state
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Verify state is success
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Success::class.java)

        // When
        viewModel.insertIntoFavourites(lagosDev)
        advanceUntilIdle()

        // Then - developer should be added to favorites
        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)
        assertThat(favDevs[0].login).isEqualTo("developer1")
        assertThat(favDevs[0].name).isEqualTo("John Doe")
    }

    @Test
    fun `insertIntoFavourites with non-success state does not add to favorites`() = runTest {
        // Given - state is initially Loading (not Success)
        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        // Verify initial state is Loading
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Loading::class.java)

        // When
        viewModel.insertIntoFavourites(lagosDev)
        advanceUntilIdle()

        // Then - nothing should be added to favorites
        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()
    }

    @Test
    fun `setIsFavourite updates isFavourite state`() {
        // Given - initial state is false
        assertThat(viewModel.isFavourite.value).isFalse()

        // When
        viewModel.setIsFavourite(true)

        // Then
        assertThat(viewModel.isFavourite.value).isTrue()

        // When
        viewModel.setIsFavourite(false)

        // Then
        assertThat(viewModel.isFavourite.value).isFalse()
    }

    @Test
    fun `getDeveloperDetails correctly maps all fields from API response`() = runTest {
        // Given
        val lagosDev = LagosDeveloper(
            id = 2L,
            login = "developer2",
            avatarUrl = "https://example.com/avatar2.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        // When
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - verify all fields are correctly mapped
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Success::class.java)
        val successState = viewModel.developerState.value as DevResponseState.Success
        val developer = successState.data

        assertThat(developer.id).isEqualTo(2L)
        assertThat(developer.login).isEqualTo("developer2")
        assertThat(developer.avatarUrl).isEqualTo("https://example.com/avatar2.jpg")
        assertThat(developer.name).isEqualTo("Jane Smith")
        assertThat(developer.company).isEqualTo("StartupXYZ")
        assertThat(developer.location).isEqualTo("Lagos, Nigeria")
        assertThat(developer.email).isEqualTo("jane@example.com")
        assertThat(developer.bio).isEqualTo("Full-stack developer")
        assertThat(developer.twitterUsername).isEqualTo("janesmith")
        assertThat(developer.publicRepos).isEqualTo(40)
        assertThat(developer.followers).isEqualTo(200)
        assertThat(developer.following).isEqualTo(75)
        assertThat(developer.createdAt).isEqualTo("2019-06-15T00:00:00Z")
        assertThat(developer.updatedAt).isEqualTo("2023-12-01T00:00:00Z")
        assertThat(developer.isFavourite).isFalse()
    }

    @Test
    fun `getDeveloperDetails correctly maps all fields from local database`() = runTest {
        // Given - add a custom favorite with all fields
        val customFavDev = FavouriteDev(
            id = 100L,
            login = "customdev",
            avatarUrl = "https://custom.com/avatar.jpg",
            name = "Custom Developer",
            company = "Custom Corp",
            location = "Lagos, Nigeria",
            email = "custom@example.com",
            bio = "Custom bio",
            twitterUsername = "customdev",
            publicRepos = 99,
            followers = 999,
            following = 99,
            createdAt = "2022-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            isFavourite = true
        )
        fakeRepository.addToFavorites(customFavDev)

        val lagosDev = LagosDeveloper(
            id = 100L,
            login = "customdev",
            avatarUrl = "https://custom.com/avatar.jpg"
        )

        // When
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - verify all fields are correctly mapped from local database
        assertThat(viewModel.developerState.value).isInstanceOf(DevResponseState.Success::class.java)
        val successState = viewModel.developerState.value as DevResponseState.Success
        val developer = successState.data

        assertThat(developer.id).isEqualTo(100L)
        assertThat(developer.login).isEqualTo("customdev")
        assertThat(developer.avatarUrl).isEqualTo("https://custom.com/avatar.jpg")
        assertThat(developer.name).isEqualTo("Custom Developer")
        assertThat(developer.company).isEqualTo("Custom Corp")
        assertThat(developer.location).isEqualTo("Lagos, Nigeria")
        assertThat(developer.email).isEqualTo("custom@example.com")
        assertThat(developer.bio).isEqualTo("Custom bio")
        assertThat(developer.twitterUsername).isEqualTo("customdev")
        assertThat(developer.publicRepos).isEqualTo(99)
        assertThat(developer.followers).isEqualTo(999)
        assertThat(developer.following).isEqualTo(99)
        assertThat(developer.createdAt).isEqualTo("2022-01-01T00:00:00Z")
        assertThat(developer.updatedAt).isEqualTo("2024-01-01T00:00:00Z")
        assertThat(developer.isFavourite).isTrue()
        assertThat(viewModel.isFavourite.value).isTrue()
    }

    @Test
    fun `workflow - add to favorites then get details returns favorite data`() = runTest {
        // Given
        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        // When - first get details (not in favorites)
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should not be favorite
        assertThat(viewModel.isFavourite.value).isFalse()

        // When - add to favorites
        viewModel.insertIntoFavourites(lagosDev)
        advanceUntilIdle()

        // When - get details again
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should now be favorite
        assertThat(viewModel.isFavourite.value).isTrue()
        val successState = viewModel.developerState.value as DevResponseState.Success
        assertThat(successState.data.isFavourite).isTrue()
    }

    @Test
    fun `workflow - remove from favorites then get details returns non-favorite data`() = runTest {
        // Given - start with favorite
        val favDev = FavouriteDev(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg",
            name = "John Doe",
            company = "Tech Corp",
            location = "Lagos, Nigeria",
            email = "john@example.com",
            bio = "Software developer",
            twitterUsername = "johndoe",
            publicRepos = 25,
            followers = 150,
            following = 50,
            createdAt = "2020-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z",
            isFavourite = true
        )
        fakeRepository.addToFavorites(favDev)

        val lagosDev = LagosDeveloper(
            id = 1L,
            login = "developer1",
            avatarUrl = "https://example.com/avatar1.jpg"
        )

        every { NetworkUtils.isInternetAvailable(mockContext) } returns true

        // When - get details (should be favorite)
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should be favorite
        assertThat(viewModel.isFavourite.value).isTrue()

        // When - remove from favorites
        viewModel.removeFromFavourites(lagosDev)
        advanceUntilIdle()

        // When - get details again
        viewModel.getDeveloperDetails(lagosDev)
        advanceUntilIdle()

        // Then - should not be favorite anymore (fetched from API)
        assertThat(viewModel.isFavourite.value).isFalse()
        val successState = viewModel.developerState.value as DevResponseState.Success
        assertThat(successState.data.isFavourite).isFalse()
    }
}