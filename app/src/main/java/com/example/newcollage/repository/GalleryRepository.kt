package com.example.newcollage.repository

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import com.bumptech.glide.util.Preconditions
import com.bumptech.glide.util.Util
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class GalleryRepository {

    private val projection = arrayOf(MediaStore.Images.Media._ID)

    private val selection =
        MediaStore.Images.Media.WIDTH + " > 190 AND " + MediaStore.Images.Media.HEIGHT + " > 190"

    private val orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC"


    // glide sample里的callbackFlow
    fun loadMediaData(context: Context): Flow<List<Uri>> = callbackFlow {

        val contentResolver = context.contentResolver

        //这里ContentObserver的looper给了个main looper，保证了回调的时候走的是app的主线程
        //由于query需要io线程加载，因此launch一下才不会崩溃

        //looper如果给null，会走系统更新数据库的一个binder的线程回调，不用launcher即可，更简单了

        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                launch {
                    trySend(query(contentResolver))
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )

        trySend(query(contentResolver))

        awaitClose {
            contentResolver.unregisterContentObserver(contentObserver)
        }
    }


    // 查询媒体库
    @WorkerThread
    private fun query(contentResolver: ContentResolver): List<Uri> {
        // Glide自带的工具：检查是否是后台线程，否的话抛出异常
        Preconditions.checkArgument(
            Util.isOnBackgroundThread(),
            "Can only query from a background thread"
        )
        val data = mutableListOf<Uri>()

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            orderBy
        ) ?: return data

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = it.getInt(idIndex)
                val uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                data.add(uri)
            }
        }

        return data
    }

}