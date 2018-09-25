package eu.sesma.kuantum.cuanto

import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAJob
import eu.sesma.kuantum.cuanto.model.StatusEnum
import eu.sesma.kuantum.cuanto.network.Either
import eu.sesma.kuantum.cuanto.network.IbmProvider
import eu.sesma.kuantum.cuanto.network.QAsm
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import java.util.concurrent.TimeoutException


object IbmRepository {

    private var token: String = ""
    private lateinit var devices: List<QADevice>
    val simulator
        get() = devices.first { it.simulator }
    private val qex = IbmProvider()

    fun init(apiKey: String): Boolean {
        if (devices.isNotEmpty()) return true

        runBlocking {
            val result = qex.login(apiKey)
            when (result) {
                is Either.Left -> Timber.d("Error loging in: ${result.t}")
                is Either.Right -> token = result.t
            }
        }
        if (token.isEmpty()) return false
        Timber.d("Got token: $token")

        runBlocking {
            devices = qex.getDevices(token)
        }
        Timber.d("Devices: ")
        devices.forEach { Timber.d(it.toString()) }

        return devices.isNotEmpty()
    }

    fun submitJob(device: QADevice, shots: Int = 1, maxCredits: Int = 1, vararg sources: QAsm): QAJob? {
        val job = QAJob(backend = device, shots = shots, maxCredits = maxCredits, qasms = listOf(*sources))
        var jobResult: QAJob? = null
        runBlocking {
            jobResult = qex.submitJob(token, job)
        }
        return jobResult
    }

    fun onStatus(job: QAJob?, timeoutSeconds: Int, onCompleted: (QAJob) -> Unit,
                 onError: (Throwable) -> Unit = {}) {
        job ?: return
        onJobStatus(job, timeoutSeconds, onCompleted, onError)
    }

    /**
     *
     * Callback for receiving results of a job
     *
     * @param job a job to fetch from the server
     * @param maxTime fail if this waiting time is reached
     */
    fun onJobStatus(
            job: QAJob,
            maxTime: Int,
            onCompleted: (QAJob) -> Unit,
            onError: (Throwable) -> Unit = {}) {

        val jobStart = Calendar.getInstance()
        var sleep = 1
        var horribleWayOfDoingThings = true //TODO Fix thing this ASAP
        do {
            val elapsed = (Calendar.getInstance().timeInMillis - jobStart.timeInMillis) / 1000.0

            if (sleep > maxTime) {
                onError(TimeoutException("timeout waiting for a completed job: ${elapsed}s"))
                break
            } else {
                try {
                    Thread.sleep((sleep * 1000.0).toLong())
                    sleep++
                } catch (e: InterruptedException) {
                    onError(e)
                    break
                }
            }

            try {
                runBlocking {
                    val serverJob = qex.receiveJob(token, job)
                    if (serverJob?.status == StatusEnum.COMPLETED) {
                        onCompleted(serverJob)
                        horribleWayOfDoingThings = false
                    }
                }
            } catch (ex: Exception) {
                onError(ex)
                break
            }
        } while (horribleWayOfDoingThings)
    }


}