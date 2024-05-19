package io.horizontalsystems.bankwallet.modules.chart

import io.horizontalsystems.chartview.models.ChartIndicator
import io.horizontalsystems.chartview.models.ChartPoint

data class ChartPointsContainer(
    val items: List<ChartPoint>,
    val isMovementChart: Boolean = true,
    val indicators: Map<String, ChartIndicator> = mapOf(),
)
