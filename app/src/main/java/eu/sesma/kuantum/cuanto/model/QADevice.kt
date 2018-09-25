package eu.sesma.kuantum.cuanto.model

import eu.sesma.kuantum.cuanto.network.IbmProvider
import eu.sesma.kuantum.cuanto.network.QAsm
import kotlinx.coroutines.experimental.runBlocking
import java.util.Date

class QADevice(var name: String) {
    var status: QADeviceStatus? = null
    var serialNumber: String = ""
    var description: String = ""
    var id: String = ""
    var topologyId: String = ""
    var simulator: Boolean = false
    var nQubits: Int = 0
    //var couplingMap: Array<IntArray>? = null // TODO - can't decode as it is either array or string

    var chipName: String = ""
    var onlineDate: Date? = null
    var gateSet: String = ""
    var basisGates: String = ""
    var version: String = ""
    var url: String = ""
    var allowQObject: Boolean = false

    @Transient
    var api: IbmProvider? = null

    override fun toString(): String {
        return "$name - $description, status: $status, real:${!simulator}, qbits:$nQubits, gates:$basisGates"
    }

    fun submitJob(shots: Int = 1, maxCredits: Int = 1, vararg sources: QAsm): QAJob {
        var job = QAJob(backend = this, shots = shots, maxCredits = maxCredits, qasms = listOf(*sources))
        runBlocking {
            job = api?.submitJob(job)?.apply {
                this.api = this@QADevice.api
            } ?: throw(IllegalStateException("You have to obtain device instance from IbmProvider instance"))
        }
        return job
    }

}

enum class QADeviceStatus {
    on, off, standby
}