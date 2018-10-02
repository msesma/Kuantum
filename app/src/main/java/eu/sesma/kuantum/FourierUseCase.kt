package eu.sesma.kuantum

import eu.sesma.kuantum.cuanto.*
import timber.log.Timber

class FourierUseCase(private val interactor: JobInteractor,
                     private val console: (String) -> Unit) {

    fun run() {
        val device = interactor.simulator

        Timber.d("Running quantum Fourier transform")
        val qasm = qasm {
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
        }
        val jobFt = interactor.submitJob(
                device = device,
                shots = 256,
                maxCredits = 100,
                sources = *arrayOf(qasm))
        console(jobFt.toString())

        interactor.onStatus(jobFt, 500, { finishedJob ->
            finishedJob.qasms?.forEach { qasm ->
                Timber.d(qasm.result.toString())
                console(qasm.result.toString())
            }
        }, { error -> Timber.d(error) })
    }
}