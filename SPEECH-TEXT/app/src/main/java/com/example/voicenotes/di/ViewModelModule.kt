package com.example.voicenotes.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voicenotes.ui.auth.AuthViewModel
import com.example.voicenotes.ui.notes.NotesViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.multibindings.IntoMap

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {
    
    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    @ViewModelScoped
    abstract fun bindAuthViewModel(viewModel: AuthViewModel): ViewModel
    
    @Binds
    @IntoMap
    @ViewModelKey(NotesViewModel::class)
    @ViewModelScoped
    abstract fun bindNotesViewModel(viewModel: NotesViewModel): ViewModel
}

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelFactoryModule {
    @Provides
    fun provideViewModelFactory(
        creators: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val creator = creators[modelClass] ?: creators.entries.firstOrNull {
                    modelClass.isAssignableFrom(it.key)
                }?.value ?: throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                
                @Suppress("UNCHECKED_CAST")
                return creator as T
            }
        }
    }
}
