package com.kosiso.lagosdevelopers.viewmodelTest.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kosiso.lagosdevelopers.models.FavouriteDev

class FakeFavouriteDevPagingSource(
    private val favoriteDevs: List<FavouriteDev>
) : PagingSource<Int, FavouriteDev>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FavouriteDev> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // Ensure we don't go out of bounds
            val start = page * pageSize
            val end = minOf(start + pageSize, favoriteDevs.size)

            // Handle edge case where start is beyond the list size
            val data = if (start >= favoriteDevs.size) {
                emptyList()
            } else {
                favoriteDevs.subList(start, end)
            }

            LoadResult.Page(
                data = data,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (end < favoriteDevs.size) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FavouriteDev>): Int? {
        // Return the page closest to the current position
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}