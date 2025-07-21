package com.kosiso.lagosdevelopers.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kosiso.lagosdevelopers.models.FavouriteDev
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDevelopersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavDev(favDev: FavouriteDev)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListOfFavDev(favDevs: List<FavouriteDev>)

    @Query("SELECT * FROM favourite_devs WHERE :id = id")
    suspend fun getFavDevById(id: Long): FavouriteDev

    @Query("SELECT * FROM favourite_devs")
    fun getAllFavDev(): PagingSource<Int, FavouriteDev>

    @Query("DELETE FROM favourite_devs WHERE :id = id")
    suspend fun removeFavDev(id: Long)

    @Query("DELETE FROM favourite_devs")
    suspend fun clearFavourites()

}