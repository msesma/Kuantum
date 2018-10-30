package eu.sesma.kuantum.experiments

import arrow.core.Either
import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.model.QAData
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.network.QAsm
import timber.log.Timber


abstract class Experiment(private val interactor: JobInteractor,
                          private val result: (Either<String, QAData>) -> Unit) {

    abstract val describe: String
    abstract val qasm: QAsm

    fun run(device: QADevice) {
        Timber.d(describe)

        val jobQ = interactor.submitJob(
                device = device,
                shots = 1024,
                maxCredits = 1,
                sources = *arrayOf(qasm))
        if (jobQ.error != null) {
            result(Either.Left(jobQ.error.toString()))
            return
        }

        interactor.onStatus(jobQ, 60,
                { finishedJob ->
                    finishedJob.qasms?.forEach { qasm ->
                        Timber.d(qasm.result.toString())
                        result(Either.Right(qasm.result?.data ?: QAData()))
                    }
                },
                { error ->
                    Timber.d(error)
                    result(Either.Left(error))
                }
        )
    }

    override fun toString(): String = describe
}