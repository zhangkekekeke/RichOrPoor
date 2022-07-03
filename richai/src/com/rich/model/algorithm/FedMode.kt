package com.rich.model.algorithm

import com.rich.model.bean.BondsBean
import com.rich.model.bean.WindABean

/**
 * 股债性价比 业务
 */
class FedMode {

    fun convertToFed(windPrice: List<WindABean>, bondsList: List<BondsBean>) {

    }
}

/**
 * 股债性价比数据
 */
data class FedBean(
        val time: String,
        val value: Float,
        val closePrice: Int,
)