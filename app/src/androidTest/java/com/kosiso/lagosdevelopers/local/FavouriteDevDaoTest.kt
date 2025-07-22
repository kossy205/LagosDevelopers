package com.kosiso.lagosdevelopers.local


import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kosiso.lagosdevelopers.models.FavouriteDev
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.kosiso.lagosdevelopers.data.local.dao.FavouriteDevelopersDao
import com.kosiso.lagosdevelopers.data.local.database.FavouriteDevelopersDatabase

@RunWith(AndroidJUnit4::class)
class FavouriteDevDaoTest {

    private lateinit var database: FavouriteDevelopersDatabase
    private lateinit var dao: FavouriteDevelopersDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FavouriteDevelopersDatabase::class.java
        ).build()
        dao = database.FavouriteDevelopersDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertFavDev_replacesOnConflict() = runTest {
        val dev1 = FavouriteDev(
            id = 1,
            login = "dev1",
            avatarUrl = "url1",
            name = "Dev One",
            company = "Company1",
            location = "Lagos",
            email = "dev1@example.com",
            bio = "Bio1",
            twitterUsername = "dev1_twitter",
            publicRepos = 10,
            followers = 100,
            following = 50,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-02T00:00:00Z",
            isFavourite = true
        )
        val dev1Updated = dev1.copy(login = "dev1_updated", avatarUrl = "url1_updated")

        dao.insertFavDev(dev1)
        dao.insertFavDev(dev1Updated)

        val result = dao.getFavDevById(1)
        assertThat(result).isEqualTo(dev1Updated)
    }

    @Test
    fun insertListOfFavDev_replacesOnConflict() = runTest {
        val devList = listOf(
            FavouriteDev(
                id = 1,
                login = "dev1",
                avatarUrl = "url1",
                name = "Dev One",
                company = "Company1",
                location = "Lagos",
                email = "dev1@example.com",
                bio = "Bio1",
                twitterUsername = "dev1_twitter",
                publicRepos = 10,
                followers = 100,
                following = 50,
                createdAt = "2023-01-01T00:00:00Z",
                updatedAt = "2023-01-02T00:00:00Z",
                isFavourite = true
            ),
            FavouriteDev(
                id = 2,
                login = "dev2",
                avatarUrl = "url2",
                name = "Dev Two",
                company = "Company2",
                location = "Abuja",
                email = "dev2@example.com",
                bio = "Bio2",
                twitterUsername = "dev2_twitter",
                publicRepos = 20,
                followers = 200,
                following = 100,
                createdAt = "2023-02-01T00:00:00Z",
                updatedAt = "2023-02-02T00:00:00Z",
                isFavourite = true
            )
        )
        val updatedDevList = devList.map { it.copy(login = "${it.login}_updated", avatarUrl = "${it.avatarUrl}_updated") }

        dao.insertListOfFavDev(devList)
        dao.insertListOfFavDev(updatedDevList)

        val result1 = dao.getFavDevById(1)
        val result2 = dao.getFavDevById(2)
        assertThat(result1).isEqualTo(updatedDevList[0])
        assertThat(result2).isEqualTo(updatedDevList[1])
    }

    @Test
    fun getFavDevById_returnsCorrectDev() = runTest {
        val dev = FavouriteDev(
            id = 1,
            login = "dev1",
            avatarUrl = "url1",
            name = "Dev One",
            company = "Company1",
            location = "Lagos",
            email = "dev1@example.com",
            bio = "Bio1",
            twitterUsername = "dev1_twitter",
            publicRepos = 10,
            followers = 100,
            following = 50,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-02T00:00:00Z",
            isFavourite = true
        )
        dao.insertFavDev(dev)

        val result = dao.getFavDevById(1)
        assertThat(result).isEqualTo(dev)
    }

    @Test
    fun getFavDevById_returnsNullForNonExistentId() = runTest {
        val result = dao.getFavDevById(999)
        assertThat(result).isNull()
    }

    @Test
    fun getAllFavDev_returnsPagingSourceWithCorrectData() = runTest {
        val devList = listOf(
            FavouriteDev(
                id = 1,
                login = "dev1",
                avatarUrl = "url1",
                name = "Dev One",
                company = "Company1",
                location = "Lagos",
                email = "dev1@example.com",
                bio = "Bio1",
                twitterUsername = "dev1_twitter",
                publicRepos = 10,
                followers = 100,
                following = 50,
                createdAt = "2023-01-01T00:00:00Z",
                updatedAt = "2023-01-02T00:00:00Z",
                isFavourite = true
            ),
            FavouriteDev(
                id = 2,
                login = "dev2",
                avatarUrl = "url2",
                name = "Dev Two",
                company = "Company2",
                location = "Abuja",
                email = "dev2@example.com",
                bio = "Bio2",
                twitterUsername = "dev2_twitter",
                publicRepos = 20,
                followers = 200,
                following = 100,
                createdAt = "2023-02-01T00:00:00Z",
                updatedAt = "2023-02-02T00:00:00Z",
                isFavourite = true
            )
        )
        dao.insertListOfFavDev(devList)

        val pagingSource = dao.getAllFavDev()
        val loadResult = pagingSource.load(
            LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertThat(loadResult).isInstanceOf(LoadResult.Page::class.java)
        val page = loadResult as LoadResult.Page
        assertThat(page.data).containsExactlyElementsIn(devList).inOrder()
    }

    @Test
    fun removeFavDev_deletesCorrectDev() = runTest {
        val dev1 = FavouriteDev(
            id = 1,
            login = "dev1",
            avatarUrl = "url1",
            name = "Dev One",
            company = "Company1",
            location = "Lagos",
            email = "dev1@example.com",
            bio = "Bio1",
            twitterUsername = "dev1_twitter",
            publicRepos = 10,
            followers = 100,
            following = 50,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-02T00:00:00Z",
            isFavourite = true
        )
        val dev2 = FavouriteDev(
            id = 2,
            login = "dev2",
            avatarUrl = "url2",
            name = "Dev Two",
            company = "Company2",
            location = "Abuja",
            email = "dev2@example.com",
            bio = "Bio2",
            twitterUsername = "dev2_twitter",
            publicRepos = 20,
            followers = 200,
            following = 100,
            createdAt = "2023-02-01T00:00:00Z",
            updatedAt = "2023-02-02T00:00:00Z",
            isFavourite = true
        )
        dao.insertListOfFavDev(listOf(dev1, dev2))

        dao.removeFavDev(1)

        val result1 = dao.getFavDevById(1)
        val result2 = dao.getFavDevById(2)
        assertThat(result1).isNull()
        assertThat(result2).isEqualTo(dev2)
    }

    @Test
    fun clearFavourites_deletesAllDevs() = runTest {
        val devList = listOf(
            FavouriteDev(
                id = 1,
                login = "dev1",
                avatarUrl = "url1",
                name = "Dev One",
                company = "Company1",
                location = "Lagos",
                email = "dev1@example.com",
                bio = "Bio1",
                twitterUsername = "dev1_twitter",
                publicRepos = 10,
                followers = 100,
                following = 50,
                createdAt = "2023-01-01T00:00:00Z",
                updatedAt = "2023-01-02T00:00:00Z",
                isFavourite = true
            ),
            FavouriteDev (
            id = 2,
            login = "dev2",
            avatarUrl = "url2",
            name = "Dev Two",
            company = "Company2",
            location = "Abuja",
            email = "dev2@example.com",
            bio = "Bio2",
            twitterUsername = "dev2_twitter",
            publicRepos = 20,
            followers = 200,
            following = 100,
            createdAt = "2023-02-01T00:00:00Z",
            updatedAt = "2023-02-02T00:00:00Z",
            isFavourite = true
        )
        )
        dao.insertListOfFavDev(devList)

        dao.clearFavourites()

        val pagingSource = dao.getAllFavDev()
        val loadResult = pagingSource.load(
            LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )
        assertThat(loadResult).isInstanceOf(LoadResult.Page::class.java)
        val page = loadResult as LoadResult.Page
        assertThat(page.data).isEmpty()
    }
}