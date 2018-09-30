package eu.sesma.kuantum

import eu.sesma.kuantum.cuanto.*
import timber.log.Timber

class BellUseCase(private val interactor: JobInteractor) {

    fun run() {
        val device = interactor.simulator

        Timber.d("Running BellUseCase state experiment.")
        val jobBell = interactor.submitJob(device, 256, 1, qasm {
            qreg(2)
            creg(5)
            h(0)
            cx(0, 1)
            measure(0, 1)
            measure(1, 1)
        })

        interactor.onStatus(jobBell, 60,
                { finishedJob ->
                    finishedJob.qasms?.forEach { qasm ->
                        Timber.d(qasm.result.toString())
                    }
                }, { error -> Timber.d(error) }
        )
    }
}