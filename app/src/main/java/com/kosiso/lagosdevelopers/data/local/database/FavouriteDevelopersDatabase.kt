package com.kosiso.lagosdevelopers.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kosiso.lagosdevelopers.data.local.dao.FavouriteDevelopersDao
import com.kosiso.lagosdevelopers.models.FavouriteDev


@Database(
    entities = [FavouriteDev::class],
    version = 1
)
abstract class FavouriteDevelopersDatabase: RoomDatabase() {

    abstract fun FavouriteDevelopersDao(): FavouriteDevelopersDao

}