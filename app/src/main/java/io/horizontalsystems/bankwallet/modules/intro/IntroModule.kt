package io.horizontalsystems.bankwallet.modules.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.core.App

object IntroModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IntroViewModel(App.localStorage) as T
        }
    }

    data class IntroSliderData(
        val title: Int,  //заголовок
        val description: Int,  //описание
        val lightImage: Int, //светлая картинка
        val darkImage: Int  //тёмная картинка
    )

}
