package eu.sesma.kuantum.cuanto.model

data class QAError(
        val name: String = "",
        var status: Int = 0,
        var message: String = "",
        val statusCode: Int = 0,
        var code: String = ""
)
