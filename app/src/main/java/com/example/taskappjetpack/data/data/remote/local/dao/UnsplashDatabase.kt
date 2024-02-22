package com.example.taskappjetpack.data.data.remote.local.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.taskappjetpack.model.UnSplashImage
import com.example.taskappjetpack.model.UnsplashRemoteKeys

@Database(entities = [UnSplashImage::class, UnsplashRemoteKeys::class], version = 1)
abstract class UnsplashDatabase: RoomDatabase() {

    abstract fun unsplashImageDao(): UnsplashImageDao
    abstract fun unsplashRemoteKeysDao(): UnsplashRemoteKeysDao
}