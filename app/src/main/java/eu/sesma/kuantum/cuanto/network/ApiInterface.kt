package eu.sesma.kuantum.cuanto.network

import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.model.QAJob
import eu.sesma.kuantum.cuanto.model.QALoginResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    /**
     * Logs in to the server. Creates an access token for the current session.
     * @return an access token - z pola id
     */
    @FormUrlEncoded
    @POST("api/users/loginWithToken")
    fun login(@Field("apiToken") apiToken: String): Deferred<Response<QALoginResponse>>

    /**
     * Returns the devices (backends) on the server.
     * @return a map of devices keyed with their names, null for none
     */
    @GET("api/backends")
    fun listDevices(@Query("access_token") accessToken: String): Deferred<Response<List<QADevice>>>

    /**
     * Sends a new job.
     * @param newJob a new job, with one or more tasks
     * @return the jobs just sent, with new ids attached
     */
    @POST("api/Jobs")
    fun sendJob(@Query("access_token") accessToken: String,
                @Body newJob: QAJob): Deferred<Response<QAJob>>

    /**
     * Receives a given job from the server, with its status and possible computation results.
     * @param job a job to fetch from the server
     * @return a description of the same job, with its current status and a possible result
     */
    @GET("api/Jobs/{jobId}")
    fun receiveJob(@Path("jobId") jobId: String,
                   @Query("access_token") accessToken: String): Deferred<Response<QAJob>>
}