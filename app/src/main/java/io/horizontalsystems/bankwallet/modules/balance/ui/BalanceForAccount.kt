package io.horizontalsystems.bankwallet.modules.balance.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.providers.Translator
import io.horizontalsystems.bankwallet.core.slideFromBottom
import io.horizontalsystems.bankwallet.core.slideFromRight
import io.horizontalsystems.bankwallet.core.utils.ModuleField
import io.horizontalsystems.bankwallet.entities.ViewState
import io.horizontalsystems.bankwallet.modules.backupalert.BackupAlert
import io.horizontalsystems.bankwallet.modules.balance.AccountViewItem
import io.horizontalsystems.bankwallet.modules.balance.BalanceModule
import io.horizontalsystems.bankwallet.modules.balance.BalanceViewModel
import io.horizontalsystems.bankwallet.modules.contacts.screen.ConfirmationBottomSheet
import io.horizontalsystems.bankwallet.modules.manageaccount.dialogs.BackupRequiredDialog
import io.horizontalsystems.bankwallet.modules.manageaccounts.ManageAccountsModule
import io.horizontalsystems.bankwallet.modules.qrscanner.QRScannerActivity
import io.horizontalsystems.bankwallet.modules.swap.settings.Caution
import io.horizontalsystems.bankwallet.modules.walletconnect.WCAccountTypeNotSupportedDialog
import io.horizontalsystems.bankwallet.modules.walletconnect.list.WalletConnectListViewModel
import io.horizontalsystems.bankwallet.modules.walletconnect.version2.WC2Manager
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.TranslatableString
import io.horizontalsystems.bankwallet.ui.compose.components.AppBar
import io.horizontalsystems.bankwallet.ui.compose.components.MenuItem
import io.horizontalsystems.bankwallet.ui.compose.components.title3_leah
import io.horizontalsystems.core.helpers.HUDManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BalanceForAccount(navController: NavController, accountViewItem: AccountViewItem) {
    //сам виджет баланса
    val viewModel = viewModel<BalanceViewModel>(factory = BalanceModule.Factory())
    //локальный контекст приложения
    val context = LocalContext.current
    //состояние нижней панели при ошибке
    val errorBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    //сканер QR-кода для пополнения
    val qrScannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //сканирование адреса
            viewModel.handleScannedData(result.data?.getStringExtra(ModuleField.SCAN_ADDRESS) ?: "")
        }
    }
    //отображение ошибки при необходимости
    viewModel.uiState.errorMessage?.let { message ->
        val view = LocalView.current
        HUDManager.showErrorMessage(view, text = message)
        viewModel.errorShown()
    }

    //отображение ошибки синхронизации и
    //повторная попытка получить данные
    when (viewModel.connectionResult) {
        WalletConnectListViewModel.ConnectionResult.Error -> {
            LaunchedEffect(viewModel.connectionResult) {
                //отображение ошибки спустя 300 миллисекунд
                coroutineScope.launch {
                    delay(300)
                    errorBottomSheetState.show()
                }
            }
            viewModel.onHandleRoute()
        }

        else -> Unit
    }


    BackupAlert(navController)
    ModalBottomSheetLayout(
        sheetState = errorBottomSheetState,
        sheetBackgroundColor = ComposeAppTheme.colors.transparent,
        sheetContent = {
            ConfirmationBottomSheet(
                title = stringResource(R.string.WalletConnect_Title),
                text = stringResource(R.string.WalletConnect_Error_InvalidUrl),
                iconPainter = painterResource(R.drawable.ic_wallet_connect_24),
                iconTint = ColorFilter.tint(ComposeAppTheme.colors.jacob),
                confirmText = stringResource(R.string.Button_TryAgain),
                cautionType = Caution.Type.Warning,
                cancelText = stringResource(R.string.Button_Cancel),
                onConfirm = {
                    coroutineScope.launch {
                        errorBottomSheetState.hide()
                        qrScannerLauncher.launch(QRScannerActivity.getScanQrIntent(context, true))
                    }
                },
                onClose = {
                    coroutineScope.launch { errorBottomSheetState.hide() }
                }
            )
        }
    ) {
        Column {
            AppBar(
                title = {
                    BalanceTitleRow(navController, accountViewItem.name)
                },
                menuItems = buildList {
                    add(
                        MenuItem(
                            title = TranslatableString.ResString(R.string.Nfts_Title),
                            icon = R.drawable.ic_nft_24,
                            onClick = {
                                navController.slideFromRight(R.id.nftsFragment)
                            }
                        )
                    )
                    if (accountViewItem.type.supportsWalletConnect) {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.WalletConnect_NewConnect),
                                icon = R.drawable.ic_qr_scan_20,
                                onClick = {
                                    when (val state = viewModel.getWalletConnectSupportState()) {
                                        WC2Manager.SupportState.Supported -> {
                                            qrScannerLauncher.launch(QRScannerActivity.getScanQrIntent(context, true))
                                        }

                                        WC2Manager.SupportState.NotSupportedDueToNoActiveAccount -> {
                                            navController.slideFromBottom(R.id.wcErrorNoAccountFragment)
                                        }

                                        is WC2Manager.SupportState.NotSupportedDueToNonBackedUpAccount -> {
                                            val text = Translator.getString(R.string.WalletConnect_Error_NeedBackup)
                                            navController.slideFromBottom(
                                                R.id.backupRequiredDialog,
                                                BackupRequiredDialog.prepareParams(state.account, text)
                                            )
                                        }

                                        is WC2Manager.SupportState.NotSupported -> {
                                            navController.slideFromBottom(
                                                R.id.wcAccountTypeNotSupportedDialog,
                                                WCAccountTypeNotSupportedDialog.prepareParams(state.accountTypeDescription)
                                            )
                                        }
                                    }
                                }
                            )
                        )
                    }
                }
            )

            //получение состояния экрана
            val uiState = viewModel.uiState


            Crossfade(uiState.viewState, label = "") { viewState ->
                when (viewState) {
                    ViewState.Success -> {
                        //получение данных монет
                        val currencyViewItems = uiState.balanceViewItems

                        //отображение списка токенов
                        if (currencyViewItems.isNotEmpty()) {
                            BalanceItems(
                                //данные монет
                                currencyViewItems,
                                //виджет
                                viewModel,
                                //виджет аккаунта
                                accountViewItem,
                                //контроллер навигации
                                navController,
                                //состояние UI
                                uiState,
                                //конечное состояние UI
                                viewModel.totalUiState
                            )
                        } else {
                            //отображение пустого экрана с подсказкой по добавлению токенов
                            BalanceItemsEmpty(navController)
                        }
                    }

                    ViewState.Loading,
                    is ViewState.Error,
                    null -> {
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceTitleRow(
    navController: NavController,
    title: String
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                navController.slideFromBottom(
                    R.id.manageAccountsFragment,
                    ManageAccountsModule.prepareParams(ManageAccountsModule.Mode.Switcher)
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        title3_leah(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(weight = 1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_down_24),
            contentDescription = null,
            tint = ComposeAppTheme.colors.grey
        )
    }
}