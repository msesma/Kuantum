package eu.sesma.kuantum.cuanto.network

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAJob
import kotlinx.coroutines.experimental.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A connection layer to an IBM Quantum Experience server.
 */
class IbmProvider {

    companion object {
        private const val URL = "https://quantumexperience.ng.bluemix.net"
        private const val UNKNOWN = "Server returned an unknown error"
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttp = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    private val gson = GsonBuilder().create()
    private val retrofit: Retrofit = Retrofit.Builder()
            .client(okHttp)
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    private val api = retrofit.create(ApiInterface::class.java)

    suspend fun login(apiToken: String): Either<String, String> = coroutineScope {
        val result = api.login(apiToken).await()
        when {
            result.code() != 200 -> Either.Left(result.code().toString())
            result.body()?.id?.isNotEmpty() == true -> Either.Right(result.body()?.id.orEmpty())
            else -> Either.Left(result.body()?.error?.message ?: UNKNOWN)
        }
    }

    suspend fun getDevices(token: String): List<QADevice> = coroutineScope {
        api.listDevices(token).await().body() ?: listOf()
    }

    suspend fun submitJob(token: String, newJob: QAJob): QAJob? = coroutineScope {
        val result = api.sendJob(token, newJob).await()
        if (result.body()?.error == null) result.body() else throw RuntimeException("${result.body()?.error?.message}")
    }

    suspend fun receiveJob(token: String, job: QAJob): QAJob? = coroutineScope {
        val result = api.receiveJob(job.id, token).await()
        if (result.body()?.error == null) result.body() else throw RuntimeException("${result.body()?.error?.message}")
    }


    /**
     *
     * Callback for receiving results of a job
     *
     * @param job a job to fetch from the server
     * @param maxTime fail if this waiting time is reached
     */
//    fun onJobStatus(
//            job: QAJob,
//            maxTime: Int,
//            onCompleted: (QAJob) -> Unit,
//            onError: (Throwable) -> Unit = {}) {
//
//        val jobStart = Calendar.getInstance()
//        var sleep = 1
//        var horribleWayOfDoingThings = true //TODO Fix thing this ASAP
//        do {
//            val elapsed = (Calendar.getInstance().timeInMillis - jobStart.timeInMillis) / 1000.0
//
//            if (sleep > maxTime) {
//                onError(TimeoutException("timeout waiting for a completed job: ${elapsed}s"))
//                break
//            } else {
//                try {
//                    Thread.sleep((sleep * 1000.0).toLong())
//                    sleep++
//                } catch (e: InterruptedException) {
//                    onError(e)
//                    break
//                }
//            }
//
//            try {
//                runBlocking {
//                    val serverJob = receiveJob(job)
//                    if (serverJob?.status == StatusEnum.COMPLETED) {
//                        onCompleted(serverJob)
//                        horribleWayOfDoingThings = false
//                    }
//                }
//            } catch (ex: Exception) {
//                onError(ex)
//                break
//            }
//        } while (horribleWayOfDoingThings)
//    }
}
