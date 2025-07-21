package com.kosiso.lagosdevelopers.models

import com.google.gson.annotations.SerializedName


data class DeveloperDetails(
    val id: Long,
    val login: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    val name: String?,
    val company: String?,
    val location: String?,
    val email: String?,
    val bio: String?,
    @SerializedName("twitter_username") val twitterUsername: String?,
    @SerializedName("public_repos") val publicRepos: Int,
    val followers: Int,
    val following: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

