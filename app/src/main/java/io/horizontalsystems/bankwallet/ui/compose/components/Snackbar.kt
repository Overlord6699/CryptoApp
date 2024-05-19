package io.horizontalsystems.bankwallet.ui.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import io.horizontalsystems.core.helpers.HUDManager

@Composable
fun SnackbarError(errorMessage: String) {
    HUDManager.showErrorMessage(LocalView.current, errorMessage)
}
