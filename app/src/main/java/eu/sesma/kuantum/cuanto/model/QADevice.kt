package eu.sesma.kuantum.cuanto.model

import java.util.*

data class QADevice(var name: String,
                    var status: QADeviceStatus? = null,
                    var serialNumber: String = "",
                    var description: String = "",
                    var id: String = "",
                    var topologyId: String = "",
                    var simulator: Boolean = false,
                    var nQubits: Int = 0,
        //var couplingMap: Array<IntArray>? = null // TODO - can'v decode as it is either array or string

                    var chipName: String = "",
                    var onlineDate: Date? = null,
                    var gateSet: String = "",
                    var basisGates: String = "",
                    var version: String = "",
                    var url: String = "",
                    var allowQObject: Boolean = false) {

    override fun toString()= "$name - $description, status: $status, real:${!simulator}, qbits:$nQubits, gates:$basisGates"
}

enum class QADeviceStatus {
    ON, OFF, STANDBY
}