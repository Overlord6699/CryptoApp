package io.horizontalsystems.bankwallet.modules.swap

import io.horizontalsystems.bankwallet.core.ILocalStorage
import io.horizontalsystems.bankwallet.modules.swap.SwapMainModule.Dex
import io.horizontalsystems.bankwallet.modules.swap.SwapMainModule.ISwapProvider
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SwapMainService(
    //информация о токене для обмена
    tokenFrom: Token?,
    //список провайдеров обмена
    private val providers: List<ISwapProvider>,
    //локальное хранилище
    private val localStorage: ILocalStorage
) {
    //dex для обмена
    var dex: Dex = getDex(tokenFrom)
        private set

    private val _providerUpdatedFlow =
        MutableSharedFlow<ISwapProvider>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val providerUpdatedFlow = _providerUpdatedFlow.asSharedFlow()

    //список провайдеров, поддерживающих эту сеть обмена
    val availableProviders: List<ISwapProvider>
        get() = providers.filter { it.supports(dex.blockchainType) }.sortedBy { it.title }

    //установка провайдера и сохранение
    fun setProvider(provider: ISwapProvider) {
        if (dex.provider.id != provider.id) {
            dex = Dex(dex.blockchain, provider)
            _providerUpdatedFlow.tryEmit(provider)

            localStorage.setSwapProviderId(dex.blockchainType, provider.id)
        }
    }

    //получение DEX для обмена
    private fun getDex(tokenFrom: Token?): Dex {
        //получение сети обмена
        val blockchain = getBlockchainForToken(tokenFrom)
        //попытка получить провайдера для сети
        val provider = getSwapProvider(blockchain.type) ?: throw IllegalStateException("No provider found for ${blockchain.name}")

        return Dex(blockchain, provider)
    }

    //получение выбранного провайдера (по умолчанию это 1inch)
    private fun getSwapProvider(blockchainType: BlockchainType): ISwapProvider? {
        val providerId = localStorage.getSwapProviderId(blockchainType)
            ?: SwapMainModule.OneInchProvider.id

        return providers.firstOrNull { it.id == providerId }
    }

    private fun getBlockchainForToken(token: Token?) = when (token?.blockchainType) {
        BlockchainType.Ethereum,
        BlockchainType.BinanceSmartChain,
        BlockchainType.Polygon,
        BlockchainType.Avalanche,
        BlockchainType.Optimism,
        BlockchainType.Gnosis,
        BlockchainType.Fantom,
        BlockchainType.ArbitrumOne -> token.blockchain
        null -> Blockchain(BlockchainType.Ethereum, "Ethereum", null) // todo: find better solution
        else -> throw IllegalStateException("Swap not supported for ${token.blockchainType}")
    }

}
