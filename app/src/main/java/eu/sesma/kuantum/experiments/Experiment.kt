package eu.sesma.kuantum.experiments

import arrow.core.Either
import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAJob
import eu.sesma.kuantum.cuanto.network.QAsm
import timber.log.Timber


abstract class Experiment(private val interactor: JobInteractor) {

    abstract val describe: String
    abstract val qasm: QAsm

    suspend fun run(device: QADevice): Either<String, QAJob> {
        Timber.d(describe)

        val result = interactor.submitJob(
                device = device,
                shots = 1024,
                maxCredits = 1,
                sources = *arrayOf(qasm))

        return interactor.onStatus(result, 60)
    }

    override fun toString(): String = describe
}