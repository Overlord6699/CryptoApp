package io.horizontalsystems.bankwallet.modules.intro

import androidx.lifecycle.ViewModel
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.ILocalStorage

class IntroViewModel(
    //в конструктор передаётся локальное хранилище
        private val localStorage: ILocalStorage
): ViewModel() {

    //виджеты на входе в приложение
    val vidgets = listOf(
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen1Title,
            R.string.Intro_Wallet_Screen1Description,
            R.drawable.projects,
            R.drawable.projects
        ),
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen2Title,
            R.string.Intro_Wallet_Screen2Description,
            R.drawable.analytics,
            R.drawable.analytics
        ),
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen3Title,
            R.string.Intro_Wallet_Screen3Description,
            R.drawable.security,
            R.drawable.security
        ),
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen4Title,
            R.string.Intro_Wallet_Screen4Description,
            R.drawable.metrics,
            R.drawable.metrics
        ),
        IntroModule.IntroSliderData(
            R.string.Intro_Wallet_Screen5Title,
            R.string.Intro_Wallet_Screen5Description,
            R.drawable.personal,
            R.drawable.personal
        ),
    )

    //реакция на завершение онбординга
    fun onStartClicked() {
        //записывается значение, что онбординг уже был показан
        localStorage.mainShowedOnce = true
    }

}
