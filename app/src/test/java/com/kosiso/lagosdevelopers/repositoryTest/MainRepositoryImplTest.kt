package com.kosiso.lagosdevelopers.repositoryTest


import android.util.Log
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import com.kosiso.lagosdevelopers.data.local.dao.FavouriteDevelopersDao
import com.kosiso.lagosdevelopers.data.remote.ApiHelper
import com.kosiso.lagosdevelopers.data.remote.RemoteDeveloperPagingSource
import com.kosiso.lagosdevelopers.data.remote.api.LagosDevsApiService
import com.kosiso.lagosdevelopers.data.repository.MainRepositoryImpl
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.DeveloperDetails
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloperApiResponse
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MainRepositoryImplTest {

    private val dao: FavouriteDevelopersDao = mockk()
    private val apiService: LagosDevsApiService = mockk()
    private val apiHelper: ApiHelper = mockk()
    private lateinit var repository: MainRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = MainRepositoryImpl(dao, apiService, apiHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getDevelopers_returnsPagingDataFlow() = runTest {
        val pagingSource = mockk<RemoteDeveloperPagingSource>()

        val mockApiResponse = mockk<LagosDeveloperApiResponse>()

        coEvery { apiService.getDevelopers(
            any(),
            page = 1,
            perPage = 30
        ) } returns Response.success(mockApiResponse)

        val flow = repository.getDevelopers()
        val pagingData = flow.first()

        assertThat(pagingData).isInstanceOf(PagingData::class.java)
    }

    @Test
    fun getDeveloperDetails_returnsSuccessWhenApiCallSucceeds() = runTest {
        val login = "dev1"
        val developerDetails = DeveloperDetails(
            id = 1L,
            login = login,
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
            updatedAt = "2023-01-02T00:00:00Z"
        )

        val jsonResponse = buildJsonObject {
            put("id", 1L)
            put("login", login)
            put("avatar_url", "url1")
            put("name", "Dev One")
            put("company", "Company1")
            put("location", "Lagos")
            put("email", "dev1@example.com")
            put("bio", "Bio1")
            put("twitter_username", "dev1_twitter")
            put("public_repos", 10)
            put("followers", 100)
            put("following", 50)
            put("created_at", "2023-01-01T00:00:00Z")
            put("updated_at", "2023-01-02T00:00:00Z")
        }.toString()

        val response = Response.success(jsonResponse.toResponseBody("application/json".toMediaType()))

        coEvery {
            apiHelper.safeApiCall<DeveloperDetails>(any())
        } returns DevResponseState.Success(developerDetails)

        coEvery { apiService.getDeveloperDetails(login) } returns response

        val result = repository.getDeveloperDetails(login)

        assertThat(result).isInstanceOf(DevResponseState.Success::class.java)
        val success = result as DevResponseState.Success
        assertThat(success.data).isEqualTo(developerDetails)

        coVerify { apiHelper.safeApiCall<DeveloperDetails>(any()) }
    }

    @Test
    fun getDeveloperDetails_returnsErrorWhenApiCallFailsWithHttpException() = runTest {
        val login = "dev1"
        val errorMessage = "Not Found"


        coEvery {
            apiHelper.safeApiCall<DeveloperDetails>(any())
        } returns DevResponseState.Error("http error: $errorMessage")

        val result = repository.getDeveloperDetails(login)

        assertThat(result).isInstanceOf(DevResponseState.Error::class.java)
        assertThat((result as DevResponseState.Error).message).isEqualTo("http error: $errorMessage")

        coVerify { apiHelper.safeApiCall<DeveloperDetails>(any()) }
    }

    @Test
    fun getDeveloperDetails_returnsErrorWhenApiCallFailsWithIOException() = runTest {
        val login = "dev1"
        val errorMessage = "Network error"

        coEvery {
            apiHelper.safeApiCall<DeveloperDetails>(any())
        } returns DevResponseState.Error(errorMessage)

        val result = repository.getDeveloperDetails(login)

        assertThat(result).isInstanceOf(DevResponseState.Error::class.java)
        assertThat((result as DevResponseState.Error).message).isEqualTo(errorMessage)

        coVerify { apiHelper.safeApiCall<DeveloperDetails>(any()) }
    }

    @Test
    fun getAllFavDevs_returnsPagingDataFlow() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0


        val pagingSource = mockk<PagingSource<Int, FavouriteDev>>()
        every { dao.getAllFavDev() } returns pagingSource
        coEvery { pagingSource.load(any()) } returns PagingSource.LoadResult.Page(
            data = emptyList(),
            prevKey = null,
            nextKey = null
        )
        every { pagingSource.registerInvalidatedCallback(any()) } just Runs
        every { pagingSource.getRefreshKey(any()) } returns null

        val flow = repository.getAllFavDevs()
        val pagingData = flow.first()

        assertThat(pagingData).isInstanceOf(PagingData::class.java)
        coVerify { dao.getAllFavDev() }

        unmockkStatic(Log::class)
    }

    @Test
    fun addToFavorites_callsDaoInsert() = runTest {
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
        coEvery { dao.insertFavDev(dev) } returns Unit

        repository.addToFavorites(dev)

        coVerify { dao.insertFavDev(dev) }
    }

    @Test
    fun addToFavorites_handlesException() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

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
        coEvery { dao.insertFavDev(dev) } throws Exception("Database error")

        repository.addToFavorites(dev)
        coVerify { dao.insertFavDev(dev) }


        unmockkStatic(Log::class)
    }

    @Test
    fun addListOfDevToFavorites_callsDaoInsertList() = runTest {
        val devs = listOf(
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
        coEvery { dao.insertListOfFavDev(devs) } returns Unit

        repository.addListOfDevToFavorites(devs)

        coVerify { dao.insertListOfFavDev(devs) }
    }

    @Test
    fun getFavDevById_returnsDevFromDao() = runTest {
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
        coEvery { dao.getFavDevById(1) } returns dev

        val result = repository.getFavDevById(1)

        assertThat(result).isEqualTo(dev)
        coVerify { dao.getFavDevById(1) }
    }

    @Test
    fun getFavDevById_returnsNullOnException() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        coEvery { dao.getFavDevById(1) } throws Exception("Database error")

        val result = repository.getFavDevById(1)
        assertThat(result).isNull()
        coVerify { dao.getFavDevById(1) }

        unmockkStatic(Log::class)
    }

    @Test
    fun removeFavDev_callsDaoRemove() = runTest {
        coEvery { dao.removeFavDev(1) } returns Unit

        repository.removeFavDev(1)

        coVerify { dao.removeFavDev(1) }
    }

    @Test
    fun clearFavourites_callsDaoClear() = runTest {
        coEvery { dao.clearFavourites() } returns Unit

        repository.clearFavourites()

        coVerify { dao.clearFavourites() }
    }
}