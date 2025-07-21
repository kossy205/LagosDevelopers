package com.kosiso.lagosdevelopers.models

import com.google.gson.annotations.SerializedName


data class LagosDeveloperApiResponse(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("incomplete_results") val incompleteResults: String,
    @SerializedName("items") val items: List<LagosDeveloper>
)