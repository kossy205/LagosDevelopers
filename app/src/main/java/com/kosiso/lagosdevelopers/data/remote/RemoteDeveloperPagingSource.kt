package com.kosiso.lagosdevelopers.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kosiso.lagosdevelopers.data.remote.api.LagosDevsApiService
import com.kosiso.lagosdevelopers.models.LagosDeveloper
import retrofit2.HttpException
import java.io.IOException

class RemoteDeveloperPagingSource (
    private val apiService: LagosDevsApiService,
    private val query: String = "lagos"
) : PagingSource<Int, LagosDeveloper>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LagosDeveloper> {
        return try {
            val page = params.key ?: 1
            val response = apiService.getDevelopers(query, page, params.loadSize)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val items = body.items
                    LoadResult.Page(
                        data = items,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (items.isEmpty() || items.size < params.loadSize) null else page + 1
                    )
                } else {
                    LoadResult.Error(Exception("unable to get devs, try again"))
                }
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LagosDeveloper>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}