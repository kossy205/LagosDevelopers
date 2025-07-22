package com.kosiso.lagosdevelopers.viewmodelTest.data.repository


import androidx.paging.PagingData
import com.kosiso.lagosdevelopers.data.repository.MainRepository
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.DeveloperDetails
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMainRepository : MainRepository {


    private val developers = mutableListOf<LagosDeveloper>()
    private val favouriteDevs = mutableListOf<FavouriteDev>()
    private val developerDetailsMap = mutableMapOf<String, DeveloperDetails>()

    var shouldReturnError = false
    var errorMessage = "Test error"
    var shouldReturnEmptyDetails = false
    var networkError = false

    init {
        setupDefaultData()
    }

    private fun setupDefaultData() {
        developers.addAll(
            listOf(
                LagosDeveloper(
                    id = 1L,
                    login = "developer1",
                    avatarUrl = "https://example.com/avatar1.jpg"
                ),
                LagosDeveloper(
                    id = 2L,
                    login = "developer2",
                    avatarUrl = "https://example.com/avatar2.jpg"
                ),
                LagosDeveloper(
                    id = 3L,
                    login = "developer3",
                    avatarUrl = "https://example.com/avatar3.jpg"
                )
            )
        )

        developerDetailsMap["developer1"] = DeveloperDetails(
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
            updatedAt = "2023-01-01T00:00:00Z"
        )

        developerDetailsMap["developer2"] = DeveloperDetails(
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
            updatedAt = "2023-12-01T00:00:00Z"
        )

        developerDetailsMap["developer3"] = DeveloperDetails(
            id = 3L,
            login = "developer3",
            avatarUrl = "https://example.com/avatar3.jpg",
            name = "Mike Johnson",
            company = "InnovateLabs",
            location = "Lagos, Nigeria",
            email = "mike@example.com",
            bio = "Mobile app developer",
            twitterUsername = "mikejohnson",
            publicRepos = 15,
            followers = 80,
            following = 30,
            createdAt = "2021-03-10T00:00:00Z",
            updatedAt = "2024-01-15T00:00:00Z"
        )
    }

    override suspend fun getDevelopers(): Flow<PagingData<LagosDeveloper>> {
        return if (shouldReturnError) {
            flowOf(PagingData.empty())
        } else {
            flowOf(PagingData.from(developers))
        }
    }

    override suspend fun getDeveloperDetails(login: String): DevResponseState<DeveloperDetails> {
        return when {
            shouldReturnError -> DevResponseState.Error(errorMessage)
            networkError -> DevResponseState.NoInternet("No internet connection")
            shouldReturnEmptyDetails -> DevResponseState.Error("Developer not found")
            else -> {
                val details = developerDetailsMap[login]
                if (details != null) {
                    DevResponseState.Success(details)
                } else {
                    DevResponseState.Error("Developer not found")
                }
            }
        }
    }

    override fun getAllFavDevs(): Flow<PagingData<FavouriteDev>> {
        return flowOf(PagingData.from(favouriteDevs))
    }

    override suspend fun addToFavorites(dev: FavouriteDev) {
        if (!shouldReturnError) {

            favouriteDevs.removeAll { it.id == dev.id }
            favouriteDevs.add(dev)
        }
    }

    override suspend fun addListOfDevToFavorites(devs: List<FavouriteDev>) {
        if (!shouldReturnError) {
            devs.forEach { dev ->
                favouriteDevs.removeAll { it.id == dev.id }
                favouriteDevs.add(dev)
            }
        }
    }

    override suspend fun getFavDevById(id: Long): FavouriteDev? {
        return if (shouldReturnError) {
            null
        } else {
            favouriteDevs.find { it.id == id }
        }
    }

    override suspend fun removeFavDev(id: Long) {
        if (!shouldReturnError) {
            favouriteDevs.removeAll { it.id == id }
        }
    }

    override suspend fun clearFavourites() {
        if (!shouldReturnError) {
            favouriteDevs.clear()
        }
    }

    fun addDeveloper(developer: LagosDeveloper) {
        developers.add(developer)
    }

    fun clearDevelopers() {
        developers.clear()
    }

    fun getFavouriteDevs(): List<FavouriteDev> = favouriteDevs.toList()

    fun getDevelopersList(): List<LagosDeveloper> = developers.toList()


    fun reset() {
        shouldReturnError = false
        errorMessage = "Test error"
        shouldReturnEmptyDetails = false
        networkError = false
        developers.clear()
        favouriteDevs.clear()
        developerDetailsMap.clear()
        setupDefaultData()
    }
}