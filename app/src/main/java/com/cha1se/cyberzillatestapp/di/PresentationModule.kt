package com.cha1se.cyberzillatestapp.di

import com.cha1se.cyberzillatestapp.presentation.viewmodels.MainActivityViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel<MainActivityViewModel> { MainActivityViewModel(eventsRepositoryImpl = get()) }
}