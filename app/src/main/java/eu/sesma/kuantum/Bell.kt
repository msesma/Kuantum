package eu.sesma.kuantum

import eu.sesma.kuantum.cuanto.*
import eu.sesma.kuantum.cuanto.network.IbmProvider

class Bell(private val apiKey: String, private val qex: IbmProvider) {

    fun run() {
        Timber.d("Running Bell state experiment.")

        if (!IbmRepository.init(apiKey)) {
            Timber.d("Cannot contact IBM Quantum Experience")
            return
        }
        val device = IbmRepository.simulator

        val jobBell = IbmRepository.submitJob(device, 256, 1, qasm {
            qreg(2)
            creg(5)
            h(0)
            cx(0, 1)
            measure(0, 1)
            measure(1, 1)
        })
        IbmRepository.onStatus(jobBell, 60,
                { finishedJob ->
                    finishedJob.qasms?.forEach { qasm ->
                        Timber.d(qasm.result.toString())
                    }
                },
                {
                    Timber.d("Job failed")
                }
        )

        Timber.d("Running quantum Fourier transform")
        val jobFt = IbmRepository.submitJob(device, 256, 100, qasm {
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
        IbmRepository.onStatus(jobFt, 500, { finishedJob ->
            finishedJob.qasms?.forEach { qasm ->
                Timber.d(qasm.result.toString())
            }
        }, {

        })
    }
}