package com.kosiso.lagosdevelopers.models


import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class LagosDeveloper(
    val id: Long,
    val login: String,
    @SerializedName("avatar_url") val avatarUrl: String
): Parcelable