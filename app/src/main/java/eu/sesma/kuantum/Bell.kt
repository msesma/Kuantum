package eu.sesma.kuantum

import android.util.Log
import pl.qus.qotlin.*

class Bell {

    private val TAG = Bell::class.java.simpleName

    fun run() {
        val qex = Qotlin()

        qex.login("28978ff305e3cf767284a4c89a5724cf090f9a03699d685e8f5ab2394ce0d0caa736b27938f0d7b26f675347a4509cb5c3c67aebd97dde3c7e5a663947e676b8")

        qex.enumerateDevices()

        Log.d(TAG, "Currently available:")
        qex.devices.forEach {
            println(it)
        }

        Log.d(TAG, "\nRunning Bell state experiment")
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

        Log.d(TAG, "Running quantum Fourier transform")
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