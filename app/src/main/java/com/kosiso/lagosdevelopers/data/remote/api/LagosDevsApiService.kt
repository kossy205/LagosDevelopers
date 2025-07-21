package com.kosiso.lagosdevelopers.data.remote.api

import com.kosiso.lagosdevelopers.models.DeveloperDetails
import com.kosiso.lagosdevelopers.models.LagosDeveloperApiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface LagosDevsApiService {

    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"
    )
    @GET("search/users")
    suspend fun getDevelopers(
        @Query("q") query: String = "lagos",
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<LagosDeveloperApiResponse>

    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28"
    )
    @GET("users/{login}")
    suspend fun getDeveloperDetails(
        @Path("login") username: String
    ): Response<ResponseBody>

}