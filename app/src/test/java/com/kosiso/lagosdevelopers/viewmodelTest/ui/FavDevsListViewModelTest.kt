package com.kosiso.lagosdevelopers.viewmodelTest.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.testing.asSnapshot
import com.google.common.truth.Truth.assertThat
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.ui.favourites_screen.FavouritesListViewModel
import com.kosiso.lagosdevelopers.viewmodelTest.data.repository.FakeMainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavDevsListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FavouritesListViewModel
    private lateinit var fakeRepository: FakeMainRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMainRepository()
        viewModel = FavouritesListViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init calls getAllFavouriteDevs and initializes empty flow`() = runTest {
        advanceUntilIdle()

        assertThat(viewModel.favouriteDevsFlow.value).isNotNull()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()
    }

    @Test
    fun `getAllFavouriteDevs updates flow with repository data`() = runTest {
        val favDev1 = FavouriteDev(
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

        val favDev2 = FavouriteDev(
            id = 2L,
            login = "developer2",
            avatarUrl = "https://example.com/avatar2.jpg",
            name = "Jane Smith",
            company = "StartupXYZ",
            location = "Lagos, Nigeria",
            email = "jane@example.com",
            bio = "Full-stack developer",
            twitterUsername = "janesmith",
            publicRepos = 40,
            followers = 200,
            following = 75,
            createdAt = "2019-06-15T00:00:00Z",
            updatedAt = "2023-12-01T00:00:00Z",
            isFavourite = true
        )

        fakeRepository.addToFavorites(favDev1)
        fakeRepository.addToFavorites(favDev2)

        viewModel.getAllFavouriteDevs()
        advanceUntilIdle()

        val flowSnapshot = fakeRepository.getAllFavDevs().asSnapshot()
        assertThat(flowSnapshot).hasSize(2)
        assertThat(flowSnapshot.map { it.login }).containsExactly("developer1", "developer2")

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(2)
        assertThat(favDevs.map { it.login }).containsExactly("developer1", "developer2")
    }

    @Test
    fun `getAllFavouriteDevs with empty repository returns empty flow`() = runTest {

        viewModel.getAllFavouriteDevs()
        advanceUntilIdle()

        val flowSnapshot = fakeRepository.getAllFavDevs().asSnapshot()
        assertThat(flowSnapshot).isEmpty()
    }

    @Test
    fun `clearAllFavourites removes all favorites and updates flow`() = runTest {
        val favDev1 = FavouriteDev(
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

        val favDev2 = FavouriteDev(
            id = 2L,
            login = "developer2",
            avatarUrl = "https://example.com/avatar2.jpg",
            name = "Jane Smith",
            company = "StartupXYZ",
            location = "Lagos, Nigeria",
            email = "jane@example.com",
            bio = "Full-stack developer",
            twitterUsername = "janesmith",
            publicRepos = 40,
            followers = 200,
            following = 75,
            createdAt = "2019-06-15T00:00:00Z",
            updatedAt = "2023-12-01T00:00:00Z",
            isFavourite = true
        )

        fakeRepository.addToFavorites(favDev1)
        fakeRepository.addToFavorites(favDev2)
        assertThat(fakeRepository.getFavouriteDevs()).hasSize(2)

        viewModel.clearAllFavourites()
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()

        val flowSnapshot = fakeRepository.getAllFavDevs().asSnapshot()
        assertThat(flowSnapshot).isEmpty()
    }

    @Test
    fun `clearAllFavourites with already empty repository works correctly`() = runTest {
        assertThat(fakeRepository.getFavouriteDevs()).isEmpty()

        viewModel.clearAllFavourites()
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()
    }

    @Test
    fun `removeFromFavourites removes specific developer`() = runTest {
        val favDev1 = FavouriteDev(
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

        val favDev2 = FavouriteDev(
            id = 2L,
            login = "developer2",
            avatarUrl = "https://example.com/avatar2.jpg",
            name = "Jane Smith",
            company = "StartupXYZ",
            location = "Lagos, Nigeria",
            email = "jane@example.com",
            bio = "Full-stack developer",
            twitterUsername = "janesmith",
            publicRepos = 40,
            followers = 200,
            following = 75,
            createdAt = "2019-06-15T00:00:00Z",
            updatedAt = "2023-12-01T00:00:00Z",
            isFavourite = true
        )

        fakeRepository.addToFavorites(favDev1)
        fakeRepository.addToFavorites(favDev2)

        assertThat(fakeRepository.getFavouriteDevs()).hasSize(2)

        viewModel.removeFromFavourites(1L)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)
        assertThat(favDevs[0].id).isEqualTo(2L)
        assertThat(favDevs[0].login).isEqualTo("developer2")
    }

    @Test
    fun `removeFromFavourites with non-existent id does not cause error`() = runTest {
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

        assertThat(fakeRepository.getFavouriteDevs()).hasSize(1)

        viewModel.removeFromFavourites(999L)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)
        assertThat(favDevs[0].id).isEqualTo(1L)
    }

    @Test
    fun `removeFromFavourites from empty repository does not cause error`() = runTest {
        assertThat(fakeRepository.getFavouriteDevs()).isEmpty()

        viewModel.removeFromFavourites(1L)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).isEmpty()
    }

    @Test
    fun `repository error during clearAllFavourites is handled gracefully`() = runTest {
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
        fakeRepository.shouldReturnError = true

        viewModel.clearAllFavourites()
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)
    }

    @Test
    fun `repository error during removeFromFavourites is handled gracefully`() = runTest {
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
        fakeRepository.shouldReturnError = true

        viewModel.removeFromFavourites(1L)
        advanceUntilIdle()

        val favDevs = fakeRepository.getFavouriteDevs()
        assertThat(favDevs).hasSize(1)
    }

    @Test
    fun `workflow - add multiple developers then remove some`() = runTest {
        val favDevs = listOf(
            FavouriteDev(
                id = 1L, login = "dev1", avatarUrl = "url1", name = "Dev 1",
                company = "Corp1", location = "Lagos", email = "dev1@test.com",
                bio = "Bio1", twitterUsername = "dev1", publicRepos = 10,
                followers = 100, following = 10, createdAt = "2020-01-01T00:00:00Z",
                updatedAt = "2023-01-01T00:00:00Z", isFavourite = true
            ),
            FavouriteDev(
                id = 2L, login = "dev2", avatarUrl = "url2", name = "Dev 2",
                company = "Corp2", location = "Lagos", email = "dev2@test.com",
                bio = "Bio2", twitterUsername = "dev2", publicRepos = 20,
                followers = 200, following = 20, createdAt = "2020-01-01T00:00:00Z",
                updatedAt = "2023-01-01T00:00:00Z", isFavourite = true
            ),
            FavouriteDev(
                id = 3L, login = "dev3", avatarUrl = "url3", name = "Dev 3",
                company = "Corp3", location = "Lagos", email = "dev3@test.com",
                bio = "Bio3", twitterUsername = "dev3", publicRepos = 30,
                followers = 300, following = 30, createdAt = "2020-01-01T00:00:00Z",
                updatedAt = "2023-01-01T00:00:00Z", isFavourite = true
            )
        )

        fakeRepository.addListOfDevToFavorites(favDevs)

        viewModel.getAllFavouriteDevs()
        advanceUntilIdle()

        assertThat(fakeRepository.getFavouriteDevs()).hasSize(3)

        viewModel.removeFromFavourites(2L)
        advanceUntilIdle()

        val remainingDevs = fakeRepository.getFavouriteDevs()
        assertThat(remainingDevs).hasSize(2)
        assertThat(remainingDevs.map { it.id }).containsExactly(1L, 3L)

        viewModel.clearAllFavourites()
        advanceUntilIdle()

        assertThat(fakeRepository.getFavouriteDevs()).isEmpty()
    }

    @Test
    fun `flow updates correctly after operations`() = runTest {
        viewModel.getAllFavouriteDevs()
        advanceUntilIdle()

        var flowSnapshot = fakeRepository.getAllFavDevs().asSnapshot()
        assertThat(flowSnapshot).isEmpty()

        val favDev = FavouriteDev(
            id = 1L, login = "dev1", avatarUrl = "url1", name = "Dev 1",
            company = "Corp1", location = "Lagos", email = "dev1@test.com",
            bio = "Bio1", twitterUsername = "dev1", publicRepos = 10,
            followers = 100, following = 10, createdAt = "2020-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z", isFavourite = true
        )
        fakeRepository.addToFavorites(favDev)

        viewModel.getAllFavouriteDevs()
        advanceUntilIdle()

        flowSnapshot = fakeRepository.getAllFavDevs().asSnapshot()
        assertThat(flowSnapshot).hasSize(1)
        assertThat(flowSnapshot[0].login).isEqualTo("dev1")
    }
}