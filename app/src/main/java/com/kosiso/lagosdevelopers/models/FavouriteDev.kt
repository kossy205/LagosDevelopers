package com.kosiso.lagosdevelopers.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "favourite_devs")
data class FavouriteDev(
    @PrimaryKey
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val name: String?,
    val company: String?,
    val location: String?,
    val email: String?,
    val bio: String?,
    val twitterUsername: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String,
    val updatedAt: String,
    val isFavourite: Boolean = true
)

