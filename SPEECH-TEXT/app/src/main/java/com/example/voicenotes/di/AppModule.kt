package com.example.voicenotes.di

import android.content.Context
import com.example.voicenotes.data.repository.AuthRepository
import com.example.voicenotes.data.repository.AuthRepositoryImpl
import com.example.voicenotes.data.repository.NotesRepository
import com.example.voicenotes.data.repository.NotesRepositoryImpl
import com.example.voicenotes.util.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Firebase
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth
    
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore
    
    // Repositories
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        preferenceManager: PreferenceManager
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore, preferenceManager)
    }
    
    @Provides
    @Singleton
    fun provideNotesRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): NotesRepository {
        return NotesRepositoryImpl(firestore, auth)
    }
    
    // Preferences
    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context).apply {
            setup(context)
        }
    }
}
