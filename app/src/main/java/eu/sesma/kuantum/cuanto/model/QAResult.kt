package eu.sesma.kuantum.cuanto.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class QAResult(var date: Date? = null,
                    var data: QAData? = null) {
    override fun toString() = "$date\n$data"
}

data class QAData(@SerializedName("creg_labels")
                  var cregLabels: String = "",
                  var additionalData: QAAdditional? = null,
                  var time: Double = 0.toDouble(),
                  var counts: Map<String, Int>? = null) {

    override fun toString() = "$cregLabels time=$time counts=$counts"
}

data class QAAdditional(internal var seed: Long = 0)
