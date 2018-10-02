package eu.sesma.kuantum

import eu.sesma.kuantum.cuanto.*
import timber.log.Timber

class BellUseCase(private val interactor: JobInteractor,
                  private val console: (String) -> Unit) {

    fun run() {
        val device = interactor.simulator

        Timber.d("Running BellUseCase state experiment.")
        val qasm = qasm {
            qreg(2)
            creg(5)
            h(0)
            cx(0, 1)
            measure(0, 1)
            measure(1, 1)
        }
        val jobBell = interactor.submitJob(
                device = device,
                shots = 256,
                maxCredits = 1,
                sources = *arrayOf(qasm))
        console(jobBell.toString())

        interactor.onStatus(jobBell, 60,
                { finishedJob ->
                    finishedJob.qasms?.forEach { qasm ->
                        Timber.d(qasm.result.toString())
                        console(qasm.result.toString())
                    }
                }, { error -> Timber.d(error) }
        )
    }
}