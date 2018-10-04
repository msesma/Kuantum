package eu.sesma.kuantum.experiments

import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.model.QAData
import eu.sesma.kuantum.cuanto.network.QAsm
import timber.log.Timber


abstract class Experiment(private val interactor: JobInteractor,
                          private val result: (QAData?) -> Unit) {

    abstract val describe: String
    abstract val qasm: QAsm

    fun run() {
        Timber.d(describe)

        val device = interactor.simulator
        val jobQ = interactor.submitJob(
                device = device,
                shots = 1024,
                maxCredits = 1,
                sources = *arrayOf(qasm))

        interactor.onStatus(jobQ, 60,
                { finishedJob ->
                    finishedJob.qasms?.forEach { qasm ->
                        Timber.d(qasm.result.toString())
                        result(qasm.result?.data)
                    }
                }, { error -> Timber.d(error) }
        )
    }

    override fun toString(): String = describe
}