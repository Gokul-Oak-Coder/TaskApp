package com.example.taskappjetpack.data.data.remote.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState.Loading.endOfPaginationReached
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.taskappjetpack.data.data.remote.UnsplashApi
import com.example.taskappjetpack.data.data.remote.local.dao.UnsplashDatabase
import com.example.taskappjetpack.model.UnSplashImage
import com.example.taskappjetpack.model.UnsplashRemoteKeys
import com.example.taskappjetpack.util.Constants.ITEMS_PER_PAGE
import javax.inject.Inject


@ExperimentalPagingApi
class UnsplashRemoteMediator @Inject constructor(
    private val unsplashApi: UnsplashApi,
    private val unsplashDatabase: UnsplashDatabase
): RemoteMediator<Int, UnSplashImage>() {

    private val unsplashImageDao = unsplashDatabase.unsplashImageDao()
    private val unsplashRemoteKeysDao = unsplashDatabase.unsplashRemoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UnSplashImage>
    ): MediatorResult {
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPage?.minus(1) ?: 1
                }

                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevPage = remoteKeys?.prevPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    prevPage
                }

                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextPage = remoteKeys?.nextPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = remoteKeys != null
                        )
                    nextPage
                }
            }
            val response = unsplashApi.getAllImages(page = currentPage, perPage = ITEMS_PER_PAGE)
            val endOfPaginationReached = response.isEmpty()

            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1

            unsplashDatabase.withTransaction {
                if (loadType == LoadType.REFRESH){
                    unsplashImageDao.deleteAllImages()
                unsplashRemoteKeysDao.deleteAllRemoteKeys()
                }
                val keys = response.map { unSplashImage ->
                    UnsplashRemoteKeys(
                        id = unSplashImage.id,
                        prevPage = prevPage,
                        nextPage = nextPage
                )
            }
            unsplashRemoteKeysDao.addAllRemoteKeys(remoteKeys = keys)
            unsplashImageDao.addImages(images = response)
        }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
        private suspend fun getRemoteKeyClosestToCurrentPosition(
            state: PagingState<Int, UnSplashImage>
        ): UnsplashRemoteKeys? {
            return state.anchorPosition?.let { position ->
                state.closestItemToPosition(position)?.id?.let { id ->
                    unsplashRemoteKeysDao.getRemoteKeys(id = id)
                }
            }
        }

        private suspend fun getRemoteKeyForFirstItem(
            state: PagingState<Int, UnSplashImage>
        ): UnsplashRemoteKeys? {
            return state.pages.firstOrNull() { it.data.isNotEmpty() }?.data?.firstOrNull()
                ?.let { unSplashImage ->
                    unsplashRemoteKeysDao.getRemoteKeys(id = unSplashImage.id)
                }
        }

        private suspend fun getRemoteKeyForLastItem(
            state: PagingState<Int, UnSplashImage>
        ): UnsplashRemoteKeys? {
            return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
                ?.let { unSplashImage ->
                    unsplashRemoteKeysDao.getRemoteKeys(id = unSplashImage.id)
                }
        }
}
