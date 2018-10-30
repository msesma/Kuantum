package eu.sesma.kuantum.cuanto

import arrow.core.Either
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAError
import eu.sesma.kuantum.cuanto.model.QAJob
import eu.sesma.kuantum.cuanto.network.IbmProvider
import eu.sesma.kuantum.cuanto.network.QAsm
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class JobInteractor(private val qex: IbmProvider) : CoroutineScope {

    private val coroutineJob = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + coroutineJob +
            CoroutineExceptionHandler { _, exception -> println("Caught $exception") }

    private var token: String = ""
    var devices: List<QADevice> = emptyList()
    var lastError: String = ""


    fun init(apiKey: String): Boolean {
        if (devices.isNotEmpty()) return true

        runBlocking(coroutineContext) {
            val result = qex.login(apiKey)
            when (result) {
                is Either.Left -> {
                    lastError = "Login error: ${result.a}"
                    Timber.d(lastError)
                }
                is Either.Right-> token = result.b
            }
        }
        if (token.isEmpty()) return false
        Timber.d("Got token: $token")

        runBlocking(coroutineContext) {
            val result = qex.getDevices(token)
            when (result) {
                is Either.Left -> {
                    lastError = "Get devices error: ${result.a}"
                    Timber.d(lastError)
                }
                is Either.Right -> devices = result.b
            }
        }
        Timber.d("Devices: ")
        devices.forEach { Timber.d(it.toString()) }

        return devices.isNotEmpty()
    }

    fun submitJob(device: QADevice,
                  shots: Int = 1,
                  maxCredits: Int = 1,
                  vararg sources: QAsm): QAJob {
        val job = QAJob(backend = device, shots = shots, maxCredits = maxCredits, qasms = listOf(*sources))

        return runBlocking(coroutineContext) {
            val result = qex.submitJob(token, job)
            when (result) {
                is Either.Left -> {
                    lastError = "Submit job error: ${result.a}"
                    Timber.d(lastError)
                    QAJob(error = QAError(message = result.a))
                }
                is Either.Right -> result.b
            }
        }
    }

    fun onStatus(job: QAJob,
                 timeoutSeconds: Int,
                 onCompleted: (QAJob) -> Unit,
                 onError: (String) -> Unit) {
        val repeatJob = Job(coroutineJob)
        launch(repeatJob) {
            repeat(timeoutSeconds) {
                val result = qex.receiveJob(token, job)
                when (result) {
                    is Either.Left -> {
                        lastError = "Get status result: ${result.a}"
                        Timber.d(lastError)
                        onError(result.a)
                        delay(TimeUnit.SECONDS.toMillis(1))
                    }
                    is Either.Right -> {
                        onCompleted(result.b)
                        repeatJob.cancel()
                    }
                }
            }
        }
    }

    fun onDestroy() {
        coroutineJob.cancel()
    }
}