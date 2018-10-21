package eu.sesma.kuantum.cuanto.network

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAJob
import eu.sesma.kuantum.cuanto.model.StatusEnum
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

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

    suspend fun getDevices(token: String): Either<String, List<QADevice>> = coroutineScope {
        val result = api.listDevices(token).await()
        when {
            result.code() != 200 -> Either.Left(result.code().toString())
            result.body() == null -> Either.Left(UNKNOWN)
            else -> Either.Right(result.body() ?: emptyList())
        }
    }

    suspend fun submitJob(token: String, newJob: QAJob): Either<String, QAJob> = coroutineScope {
        val result = api.sendJob(token, newJob).await()
        when {
            result.code() != 200 -> Either.Left(result.code().toString())
            result.body()?.error == null -> Either.Right(result.body() ?: QAJob())
            else -> Either.Left(result.body()?.error?.message ?: UNKNOWN)
        }
    }

    suspend fun receiveJob(token: String, job: QAJob): Either<String, QAJob> = coroutineScope {
        val result = api.receiveJob(job.id, token).await()
        Timber.d("${result.code()}, ${result.body()?.error}, ${result.body()}")
        when {
            result.code() != 200 -> Either.Left(result.code().toString())
            result.body()?.status == StatusEnum.RUNNING -> Either.Left("Running")
            result.body()?.status == StatusEnum.COMPLETED -> Either.Right(result.body() ?: QAJob())
            else -> Either.Left(result.body()?.error?.message ?: UNKNOWN)
        }
    }
}
