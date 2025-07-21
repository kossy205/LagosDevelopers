package com.kosiso.lagosdevelopers.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.gson.Gson
import com.kosiso.lagosdevelopers.data.local.dao.FavouriteDevelopersDao
import com.kosiso.lagosdevelopers.data.local.database.FavouriteDevelopersDatabase
import com.kosiso.lagosdevelopers.data.remote.ApiHelper
import com.kosiso.lagosdevelopers.data.remote.api.LagosDevsApiService
import com.kosiso.lagosdevelopers.data.repository.MainRepository
import com.kosiso.lagosdevelopers.data.repository.MainRepositoryImpl
import com.kosiso.lagosdevelopers.di.Constants.BASE_URL
import com.kosiso.lagosdevelopers.di.Constants.DATABASE_NAME
import com.kosiso.lagosdevelopers.ui.theme.LagosDevelopersTheme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        FavouriteDevelopersDatabase::class.java,
        DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideFavouriteDevelopersDao(db: FavouriteDevelopersDatabase) = db.FavouriteDevelopersDao()


    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit) =
        retrofit.create(LagosDevsApiService::class.java)

    @Singleton
    @Provides
    fun provideApiResponseHelper(): ApiHelper = ApiHelper()

    @Singleton
    @Provides
    fun provideMainRepo(
        favouriteDevelopersDao: FavouriteDevelopersDao,
        apiService: LagosDevsApiService,
        apiHelper: ApiHelper
    ): MainRepository {
        return MainRepositoryImpl(
            favouriteDevelopersDao,
            apiService,
            apiHelper
        )
    }
}