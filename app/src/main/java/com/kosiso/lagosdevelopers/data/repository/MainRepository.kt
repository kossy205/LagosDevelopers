package com.kosiso.lagosdevelopers.data.repository

import androidx.paging.PagingData
import com.kosiso.lagosdevelopers.data.state.DevResponseState
import com.kosiso.lagosdevelopers.models.DeveloperDetails
import com.kosiso.lagosdevelopers.models.FavouriteDev
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import kotlinx.coroutines.flow.Flow


interface MainRepository {
    suspend fun getDevelopers(): Flow<PagingData<LagosDeveloper>>

    suspend fun getDeveloperDetails(login: String): DevResponseState<DeveloperDetails>


    // local db
    fun getAllFavDevs(): Flow<PagingData<FavouriteDev>>

    suspend fun addToFavorites(dev: FavouriteDev)

    suspend fun addListOfDevToFavorites(devs: List<FavouriteDev>)

    suspend fun getFavDevById(id: Long): FavouriteDev?

    suspend fun removeFavDev(id: Long)

    suspend fun clearFavourites()
}