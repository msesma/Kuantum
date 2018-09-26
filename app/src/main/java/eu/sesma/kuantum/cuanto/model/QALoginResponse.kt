package eu.sesma.kuantum.cuanto.model

import java.util.*

data class QALoginResponse(val id: String = "",
                           val ttl: Long = 0L,
                           val created: Date? = null,
                           val userId: String = "",
                           val error: QAError? = null)