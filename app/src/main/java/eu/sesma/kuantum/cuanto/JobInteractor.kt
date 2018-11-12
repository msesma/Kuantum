package eu.sesma.kuantum.cuanto

import arrow.core.Either
import eu.sesma.kuantum.cuanto.model.QADevice
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


    suspend fun init(apiKey: String): Boolean {
        if (devices.isNotEmpty()) return true

        async {
            val result = qex.login(apiKey)
            when (result) {
                is Either.Left -> {
                    lastError = "Login error: ${result.a}"
                    Timber.d(lastError)
                }
                is Either.Right -> token = result.b
            }
        }.await()
        if (token.isEmpty()) return false
        Timber.d("Got token: $token")

        async {
            val result = qex.getDevices(token)
            when (result) {
                is Either.Left -> {
                    lastError = "Get devices error: ${result.a}"
                    Timber.d(lastError)
                }
                is Either.Right -> devices = result.b
            }
        }.await()
        Timber.d("Devices: ")
        devices.forEach { Timber.d(it.toString()) }

        return devices.isNotEmpty()
    }

    suspend fun submitJob(device: QADevice,
                          shots: Int = 1,
                          maxCredits: Int = 1,
                          vararg sources: QAsm): Either<String, QAJob> {
        val job = QAJob(
                backend = device,
                shots = shots,
                maxCredits = maxCredits,
                qasms = listOf(*sources))

        return async { qex.submitJob(token, job) }.await()
    }

    suspend fun onStatus(job: Either<String, QAJob>,
                         timeoutSeconds: Int): Either<String, QAJob> {
        if (job.isLeft()) return job
        val repeatJob = Job(coroutineJob)
        lateinit var result: Either<String, QAJob>
        async (repeatJob) {
            repeat(timeoutSeconds) {
                result = qex.receiveJob(token, job)
                result.fold({
                    lastError = "Get status result: $it"
                    Timber.d(lastError)
                    delay(TimeUnit.SECONDS.toMillis(1))
                    yield()
                }, {
                    repeatJob.cancel()
                })
            }
        }.await()
        return result
    }

    fun onDestroy() {
        coroutineJob.cancel()
    }
}