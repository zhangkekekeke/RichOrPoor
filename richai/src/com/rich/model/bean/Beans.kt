package com.rich.model.bean

/**
 * 万得全A指数
 */
data class WindABean(
        val time: String,
        val closePrice: Float,
        val pe: Float,
)

data class WindJsonList(
        val datas: List<Pair<String, Float>>
)

/**
 * 国债收益率
 */
data class BondsBean(
        val time: String,
        val earningRate: Float,
)

data class BondsList(
        val datas: List<BondsBean>
)