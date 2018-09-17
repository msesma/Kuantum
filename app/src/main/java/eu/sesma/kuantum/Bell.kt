package eu.sesma.kuantum

import eu.sesma.kuantum.cuanto.*
import eu.sesma.kuantum.cuanto.network.IbmGateway
import timber.log.Timber

class Bell(private val token: String, private val qex: IbmGateway) {

    fun run() {
        val qex = IbmGateway()

        qex.login(token)

        qex.enumerateDevices()

        Timber.d("Currently available:")
        qex.devices.forEach {
            println(it)
        }

        Timber.d("\nRunning Bell state experiment")
        qex.simulator
                .submitJob(256, 1, qasm {
                    qreg(2)
                    creg(5)
                    h(0)
                    cx(0, 1)
                    measure(0, 1)
                    measure(1, 1)
                })
                .onStatus(60,
                        { finishedJob ->
                            finishedJob.qasms?.forEach { qasm ->
                                println(qasm.result)
                            }
                        },
                        {
                            println("Job failed")
                        }
                )

        Timber.d("Running quantum Fourier transform")
        //qex.devices.firstOrNull { !it.simulator }
        qex.simulator
                .submitJob(256, 100, qasm {
                    // quantum Fourier transform
                    qreg(4)
                    creg(4)
                    x(0)
                    x(2)
                    barrier()
                    h(0)
                    cu1(Math.PI / 2, 1, 0)
                    h(1)
                    cu1(Math.PI / 4, 2, 0)
                    cu1(Math.PI / 2, 2, 1)
                    h(2)
                    cu1(Math.PI / 8, 3, 0)
                    cu1(Math.PI / 4, 3, 1)
                    cu1(Math.PI / 2, 3, 2)
                    h(3)
                    measure()
                })
                .onStatus(500, { finishedJob ->
                    finishedJob.qasms?.forEach { qasm ->
                        println(qasm.result)
                    }
                }, {

                })
    }
}