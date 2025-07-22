package com.kosiso.lagosdevelopers.viewmodelTest.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kosiso.lagosdevelopers.models.LagosDeveloper

class FakeDeveloperPagingSource(
    private val developers: List<LagosDeveloper>
) : PagingSource<Int, LagosDeveloper>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LagosDeveloper> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // Ensure we don't go out of bounds
            val start = page * pageSize
            val end = minOf(start + pageSize, developers.size)

            // Handle edge case where start is beyond the list size
            val data = if (start >= developers.size) {
                emptyList()
            } else {
                developers.subList(start, end)
            }

            LoadResult.Page(
                data = data,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (end < developers.size) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LagosDeveloper>): Int? {
        // Return the page closest to the current position
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
