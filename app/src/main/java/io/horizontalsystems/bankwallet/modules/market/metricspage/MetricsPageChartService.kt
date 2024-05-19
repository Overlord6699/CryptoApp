package io.horizontalsystems.bankwallet.modules.market.metricspage

import io.horizontalsystems.bankwallet.core.managers.CurrencyManager
import io.horizontalsystems.bankwallet.entities.Currency
import io.horizontalsystems.bankwallet.modules.chart.AbstractChartService
import io.horizontalsystems.bankwallet.modules.chart.ChartPointsContainer
import io.horizontalsystems.bankwallet.modules.market.tvl.GlobalMarketRepository
import io.horizontalsystems.bankwallet.modules.metricchart.MetricsType
import io.horizontalsystems.chartview.ChartViewType
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class MetricsPageChartService(
    //менеджер для монет
    override val currencyManager: CurrencyManager,
    //тип метрики
    private val type: MetricsType,
    //хранилище данных для аналитики
    private val repository: GlobalMarketRepository,
    //наследование абстрактного сервиса графиков
) : AbstractChartService() {
    //период графика
    override val initialChartInterval: HsTimePeriod = HsTimePeriod.Day1
    //интервалы между точками
    override val chartIntervals = HsTimePeriod.values().toList()
    //вид графика
    override val chartViewType = ChartViewType.Line

    //функция возвращает контейнер точек
    override fun getItems(
        interval: HsTimePeriod,
        currency: Currency,
    ): Single<ChartPointsContainer> {
        //получение точек для графика
        return repository
            .getGlobalMarketPointsForGraphic(
                currency.code,
                interval,
                type
            ).map {
                //маппинг данных для отображения графика
                ChartPointsContainer(it)
            }
    }
}
