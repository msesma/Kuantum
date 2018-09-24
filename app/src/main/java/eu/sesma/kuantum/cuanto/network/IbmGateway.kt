package eu.sesma.kuantum.cuanto.network

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAJob
import eu.sesma.kuantum.cuanto.model.StatusEnum
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeoutException

/**
 * A connection layer to an IBM Quantum Experience server.
 */
class IbmGateway {

    var token: String = ""

    private val cookies = object : CookieJar {
        var cookies = mutableListOf<Cookie>()

        override fun saveFromResponse(p0: HttpUrl?, p1: MutableList<Cookie>) {
            cookies = p1
        }

        override fun loadForRequest(p0: HttpUrl?): MutableList<Cookie> {
            return cookies
        }
    }
    private val interceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttp = OkHttpClient.Builder()
            .cookieJar(cookies)
            .addInterceptor { chain ->
                val resp = chain.proceed(chain.request())
                val body = resp.body()?.string()
                val newBody = ResponseBody.create(resp.body()!!.contentType(), body.orEmpty())

                if (resp.code() == 400)
                    resp.newBuilder().code(200).body(newBody).build()
                else
                    resp.newBuilder().body(newBody).build()
            }
            .addInterceptor(interceptor)
            .build()
    private val gson = GsonBuilder()
            .setLenient()
            .create()
    private val retrofit: Retrofit = Retrofit.Builder()
            .client(okHttp)
            .baseUrl("https://quantumexperience.ng.bluemix.net")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    private val api = retrofit.create(QARetrofitInterfaceC::class.java)

    var devices = listOf<QADevice>()
    val simulator
        get() = devices.firstOrNull { it.simulator }
                ?: throw (IllegalStateException("Simulator not found"))

    suspend fun login(apiToken: String) {
        coroutineScope {
            val result = api.login(apiToken).await()
            token = result.body()?.id.orEmpty()
        }
    }

    suspend fun enumerateDevices() {
        coroutineScope {
            devices = api.listDevices(token).await().body() ?: listOf()
            devices.forEach { it.api = this@IbmGateway }
        }
    }

    suspend fun submitJob(newJob: QAJob): QAJob? = coroutineScope {
        val result = api.sendJob(token, newJob).await()
        if (result.body()?.error == null) result.body() else throw RuntimeException("${result.body()?.error?.message}")
    }


    private suspend fun receiveJob(job: QAJob): QAJob? = coroutineScope {
        val result = api.receiveJob(job.id, token).await()
        if (result.body()?.error == null) result.body()!! else throw RuntimeException("${result.body()?.error?.message}")
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
                    val serverJob = receiveJob(job)
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
