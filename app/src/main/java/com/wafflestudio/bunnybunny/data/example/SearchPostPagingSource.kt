package com.wafflestudio.bunnybunny.data.example

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.wafflestudio.bunnybunny.lib.network.api.BunnyApi
import com.wafflestudio.bunnybunny.lib.network.dto.GoodsPostPreview
import retrofit2.HttpException
import java.io.IOException


/**
 * Sample page-keyed PagingSource, which uses Int page number to load pages.
 *
 * Loads Items from network requests via Retrofit to a backend service.
 *
 * Note that the key type is Int, since we're using page number to load a page.
 */

data class SearchParam(
    val keyword:String,
    val cur: Long?,
    val count: Int,
)

class SearchPostPagingSource(
    val api: BunnyApi,
    val token:String,
    val distance:Int,
    val areaId:Int,
) : PagingSource<SearchParam, GoodsPostPreview>() {
    override suspend fun load(params: LoadParams<SearchParam>): LoadResult<SearchParam, GoodsPostPreview> {

        // Retrofit calls that return the body type throw either IOException for network
        // failures, or HttpException for any non-2xx HTTP status codes. This code reports all
        // errors to the UI, but you can inspect/wrap the exceptions to provide more context.
        return try {
            // Key may be null during a refresh, if no explicit key is passed into Pager
            // construction. Use 0 as default, because our API is indexed started at index 0
            val pageNumber = params.key ?: SearchParam(params.key!!.keyword, 0,0)

            // Suspending network load via Retrofit. This doesn't need to be wrapped in a
            // withContext(Dispatcher.IO) { ... } block since Retrofit's Coroutine
            // CallAdapter dispatches on a worker thread.
            val response = api.searchPostList(
                authToken = token,
                keyword=params.key!!.keyword,
                cur = params.key?.cur,
                distance = distance,
                areaId = areaId,
                count = params.key?.count,
            )

            // Since 0 is the lowest page number, return null to signify no more pages should
            // be loaded before it.
            val prevKey = if (pageNumber.count > 0) SearchParam(params.key!!.keyword,params.key?.cur, params.key?.count ?: 0) else null

            // This API defines that it's out of data when a page returns empty. When out of
            // data, we return `null` to signify no more pages should be loaded
            val nextKey = if (response.data.isNotEmpty()) SearchParam(params.key!!.keyword,response.cur,response.count ?: 0) else null
            LoadResult.Page(
                data = response.data,
                prevKey = prevKey,
                nextKey = nextKey
            )

        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<SearchParam, GoodsPostPreview>): SearchParam? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey
                ?: state.closestPageToPosition(it)?.nextKey
        }
    }
}