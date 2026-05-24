package com.meditrack.di

import android.content.Context
import androidx.room.Room
import com.meditrack.data.local.MediTrackDatabase
import com.meditrack.data.local.dao.AppointmentDao
import com.meditrack.data.local.dao.MedicationDao
import com.meditrack.data.local.dao.VitalsDao
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
    fun provideDatabase(@ApplicationContext context: Context): MediTrackDatabase =
        Room.databaseBuilder(context, MediTrackDatabase::class.java, "meditrack.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMedicationDao(db: MediTrackDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideVitalsDao(db: MediTrackDatabase): VitalsDao = db.vitalsDao()

    @Provides
    fun provideAppointmentDao(db: MediTrackDatabase): AppointmentDao = db.appointmentDao()
}
