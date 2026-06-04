package com.punchlist.app.di

import android.content.Context
import androidx.room.Room
import com.punchlist.app.data.local.PunchlistDatabase
import com.punchlist.app.data.local.dao.PunchItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PunchlistDatabase =
        Room.databaseBuilder(context, PunchlistDatabase::class.java, "punchlist.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePunchItemDao(db: PunchlistDatabase): PunchItemDao = db.punchItemDao()
}
