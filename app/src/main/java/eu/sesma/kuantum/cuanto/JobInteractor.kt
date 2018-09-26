package eu.sesma.kuantum.cuanto

import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAError
import eu.sesma.kuantum.cuanto.model.QAJob
import eu.sesma.kuantum.cuanto.model.StatusEnum
import eu.sesma.kuantum.cuanto.network.Either
import eu.sesma.kuantum.cuanto.network.IbmProvider
import eu.sesma.kuantum.cuanto.network.QAsm
import kotlinx.coroutines.experimental.runBlocking
import timber.log.Timber
import java.util.*


object JobInteractor {

    private var token: String = ""
    private var devices: List<QADevice> = emptyList()
    private val qex = IbmProvider()

    val simulator
        get() = devices.first { it.simulator }
    var lastError: String = ""

    @Synchronized
    fun init(apiKey: String): Boolean {
        if (devices.isNotEmpty()) return true

        runBlocking {
            val result = qex.login(apiKey)
            when (result) {
                is Either.Left -> {
                    lastError = "Login error: ${result.v}"
                    Timber.d(lastError)
                }
                is Either.Right -> token = result.v
            }
        }
        if (token.isEmpty()) return false
        Timber.d("Got token: $token")

        runBlocking {
            val result = qex.getDevices(token)
            when (result) {
                is Either.Left -> {
                    lastError = "Get devices error: ${result.v}"
                    Timber.d(lastError)
                }
                is Either.Right -> devices = result.v
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

        return runBlocking {
            val result = qex.submitJob(token, job)
            when (result) {
                is Either.Left -> {
                    lastError = "Submit job error: ${result.v}"
                    Timber.d(lastError)
                    QAJob(error = QAError(message = result.v))
                }
                is Either.Right -> result.v
            }
        }
    }

    //TODO Fix thing this ASAP
    fun onStatus(job: QAJob,
                 timeoutSeconds: Int,
                 onCompleted: (QAJob) -> Unit,
                 onError: (String) -> Unit) {
        job ?: return
        val jobStart = Calendar.getInstance()
        var sleep = 1
        var horribleWayOfDoingThings = true
        do {
            val elapsed = (Calendar.getInstance().timeInMillis - jobStart.timeInMillis) / 1000.0

            if (sleep > timeoutSeconds) {
                onError("timeout waiting for a completed job: ${elapsed}s")
                break
            } else {
                Thread.sleep((sleep * 1000.0).toLong())
                sleep++
            }

            runBlocking {
                val result = qex.receiveJob(token, job)
                when (result) {
                    is Either.Left -> {
                        lastError = "Get status error: ${result.v}"
                        Timber.d(lastError)
                        onError(result.v)
                    }
                    is Either.Right -> {
                        onCompleted(result.v)
                        if (result.v.status == StatusEnum.COMPLETED) {
                            horribleWayOfDoingThings = false
                        }
                    }
                }
            }

        } while (horribleWayOfDoingThings)
    }
}