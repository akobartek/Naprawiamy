package pl.sokolowskibartlomiej.naprawiamy.apicalls

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*

object RetrofitClient {

    private val mMoshi = Moshi.Builder()
        .add(Date::class.java, DateJsonAdapter().nullSafe())
        .build()

    const val BASE_API_URL = "http://192.168.0.100:5001/"
    private var apiClient: Retrofit? = null
    val naprawiamyApi: NaprawiamyApi = getClient().create(NaprawiamyApi::class.java)
    val authorizedNaprawiamyApi: NaprawiamyApi =
        getAuthorizationClient().create(NaprawiamyApi::class.java)

    private fun getClient(): Retrofit {
        if (apiClient === null) {
            apiClient = Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .addConverterFactory(MoshiConverterFactory.create(mMoshi))
                .client(getOkHttpClient())
                .build()
        }
        return apiClient!!
    }

    private fun getAuthorizationClient(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_API_URL)
        .addConverterFactory(MoshiConverterFactory.create(mMoshi))
        .client(getAuthorizationOkHttpClient())
        .build()

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder()
                    .build()
                chain.proceed(request)
            }
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }


    private fun getAuthorizationOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder()
                    .addHeader("Authorization", "Bearer ${PreferencesManager.getBearerToken()}")
                    .build()

                chain.proceed(request)
            }
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DateJsonAdapter : JsonAdapter<Date>() {

    private val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
    private val sdFormat = SimpleDateFormat(dateFormat, Locale.US)

    @Synchronized
    @Throws(Exception::class)
    override fun fromJson(reader: JsonReader): Date {
        val string = reader.nextString()
        return sdFormat.parse(string)
    }

    @Synchronized
    @Throws(Exception::class)
    override fun toJson(writer: JsonWriter, value: Date?) {
        writer.value(sdFormat.format(value))
    }

}