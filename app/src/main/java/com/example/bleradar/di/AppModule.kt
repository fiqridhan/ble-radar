package com.example.bleradar.di

import android.content.Context
import androidx.room.Room
import com.example.bleradar.ble.BleScanner
import com.example.bleradar.storage.BleRadarDatabase
import com.example.bleradar.storage.DeviceHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideScanner(@ApplicationContext context: Context): BleScanner = BleScanner(context)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BleRadarDatabase =
        Room.databaseBuilder(context, BleRadarDatabase::class.java, "ble-radar.db").build()

    @Provides
    fun provideHistoryDao(database: BleRadarDatabase): DeviceHistoryDao = database.deviceHistoryDao()
}
