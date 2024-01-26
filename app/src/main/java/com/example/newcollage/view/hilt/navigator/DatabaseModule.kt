package com.example.newcollage.view.hilt.navigator

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.newcollage.view.hilt.data.AppDatabase
import com.example.newcollage.view.hilt.data.LogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// 告诉这个模块，所需要的context为SingletonComponent提供的，
// 即hilt处理这个模块的时候，会把application context当作当前模块的context
@InstallIn(SingletonComponent::class)
//这个类可以被hilt识别为模块
@Module
object DatabaseModule {

    // LogDao为接口，没有构造方法，一般用Provides来告诉hilt如何生成这种接口
    // 但是hilt还不知道AppDatabase如何生成，需要处理AppDatabase
    @Provides
    fun provideLogDao(database: AppDatabase): LogDao {
        return database.logDao()
    }

    // 构造AppDatabase的时候，需要传入context
    // 由于这个模块的作用范围是SingletonComponent，即可以提供ApplicationContext
    // 所以直接用@ApplicationContext即可
    // @Singleton保证了hilt管理的AppDatabase都是单例
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "logging.db"
        ).build()
    }

}