package com.muratcangzm.nerva.feature.note.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.getKoin
import kotlin.reflect.KClass

@Composable
inline fun <reified VM : ViewModel> rememberKoinNavViewModel(): VM {
    val koin = getKoin()
    val factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return koin.get<VM>() as T
            }
        }
    }
    return viewModel(factory = factory)
}
