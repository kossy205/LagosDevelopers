package com.kosiso.lagosdevelopers.data.repository


import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kosiso.lagosdevelopers.data.local.dao.FavouriteDevelopersDao
import com.kosiso.lagosdevelopers.data.remote.ApiHelper
import com.kosiso.lagosdevelopers.data.remote.RemoteDeveloperPagingSource
import com.kosiso.lagosdevelopers.data.remote.api.LagosDevsApiService
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.DeveloperDetails
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import javax.inject.Inject


class MainRepositoryImpl @Inject constructor(
    val favDevDao: FavouriteDevelopersDao,
    val apiService: LagosDevsApiService,
    val apiHelper: ApiHelper
): MainRepository {
    override suspend fun getDevelopers(): Flow<PagingData<LagosDeveloper>> {
        return withContext(Dispatchers.IO){
            Pager(
                config = PagingConfig(
                    pageSize = 30,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { RemoteDeveloperPagingSource(apiService) }
            ).flow
        }
    }

    override suspend fun getDeveloperDetails(login: String): DevResponseState<DeveloperDetails> {
        return apiHelper.safeApiCall {
            val response = apiService.getDeveloperDetails(login)
            if (response.isSuccessful) {
                val body = response.body()?.string()
                if (body != null) {
                    val json = Json.parseToJsonElement(body).jsonObject
                    val developerDetails = DeveloperDetails(
                        id = json["id"]!!.jsonPrimitive.long,
                        login = json["login"]!!.jsonPrimitive.content,
                        avatarUrl = json["avatar_url"]!!.jsonPrimitive.content,
                        name = json["name"]?.jsonPrimitive?.contentOrNull  ?: "not provided",
                        company = json["company"]?.jsonPrimitive?.contentOrNull  ?: "not provided",
                        location = json["location"]?.jsonPrimitive?.contentOrNull  ?: "not provided",
                        email = json["email"]?.jsonPrimitive?.contentOrNull  ?: "not provided",
                        bio = json["bio"]?.jsonPrimitive?.contentOrNull  ?: "not provided",
                        twitterUsername = json["twitter_username"]?.jsonPrimitive?.contentOrNull  ?: "not provided",
                        publicRepos = json["public_repos"]!!.jsonPrimitive.int,
                        followers = json["followers"]!!.jsonPrimitive.int,
                        following = json["following"]!!.jsonPrimitive.int,
                        createdAt = json["created_at"]!!.jsonPrimitive.content,
                        updatedAt = json["updated_at"]!!.jsonPrimitive.content
                    )
                    DevResponseState.Success(developerDetails)
                } else {
                    DevResponseState.Error("unable to get dev details, try again")
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown API error"
                Log.e("get Developer Details", errorMessage)
                DevResponseState.Error(errorMessage)

            }
        }
    }



    override fun getAllFavDevs(): Flow<PagingData<FavouriteDev>> {
        return Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = { favDevDao.getAllFavDev() }
            ).flow
    }

    override suspend fun addToFavorites(dev: FavouriteDev) {
        withContext(Dispatchers.IO){
            try {
                favDevDao.insertFavDev(dev)
            } catch (e: Exception) {
                Log.e("add To Favorites", "error adding to favourite", e)
            }
        }
    }

    override suspend fun addListOfDevToFavorites(devs: List<FavouriteDev>) {
        withContext(Dispatchers.IO){
            try {
                favDevDao.insertListOfFavDev(devs)
            } catch (e: Exception) {
                Log.e("add List Of Dev To Favorites", "error adding list to favourite", e)
            }
        }
    }

    override suspend fun getFavDevById(id: Long): FavouriteDev? {
        return withContext(Dispatchers.IO){
            try {
                favDevDao.getFavDevById(id)
            } catch (e: Exception) {
                Log.e("get Fav Dev By Id", "Error getting fav by id", e)
                null
            }
        }
    }

    override suspend fun removeFavDev(id: Long) {
        withContext(Dispatchers.IO){
            try {
                favDevDao.removeFavDev(id)
            } catch (e: Exception) {
                Log.e("remove Fav Dev", "error removing fav", e)
            }
        }
    }

    override suspend fun clearFavourites() {
        withContext(Dispatchers.IO){
            try {
                favDevDao.clearFavourites()
            } catch (e: Exception) {
                Log.e("clear Favourites", "error clearing all fav", e)
            }
        }
    }
}