package com.delphiaconsulting.timestar.dagger.modules

import com.google.gson.*
import com.delphiaconsulting.timestar.BuildConfig
import com.delphiaconsulting.timestar.net.gson.DollarList
import com.delphiaconsulting.timestar.net.gson.HoursList
import com.delphiaconsulting.timestar.net.gson.PunchList
import com.delphiaconsulting.timestar.net.gson.deserializer.DollarListDeserializer
import com.delphiaconsulting.timestar.net.gson.deserializer.HoursListDeserializer
import com.delphiaconsulting.timestar.net.gson.deserializer.PunchListDeserializer
import com.delphiaconsulting.timestar.net.service.LegalService
import com.delphiaconsulting.timestar.util.ToStringConverterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class MainNetModule {

    companion object {
        private const val INSPERITY_FLAVOR_NAME = "insperity"
    }

    @Provides
    @Singleton internal fun provideGson(): Gson {
        val builder = GsonBuilder()
        if (BuildConfig.FLAVOR == INSPERITY_FLAVOR_NAME) {
            builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
        }
        return builder.registerTypeAdapter(DateTime::class.java, JsonDeserializer<DateTime> { json, _, _ -> DateTime.parse(json.asString) })
                .registerTypeAdapter(HoursList.ReferenceData::class.java, HoursListDeserializer())
                .registerTypeAdapter(PunchList.ReferenceData::class.java, PunchListDeserializer())
                .registerTypeAdapter(DollarList.ReferenceData::class.java, DollarListDeserializer())
                .registerTypeAdapter(DateTime::class.java, JsonSerializer<DateTime> { src, _, _ -> JsonPrimitive(ISODateTimeFormat.dateTime().print(src)) })
                .setPrettyPrinting()
                .create()
    }

    @Provides
    @Singleton internal fun provideRetrofit(gson: Gson, client: OkHttpClient): Retrofit = Retrofit.Builder()
            .addConverterFactory(ToStringConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .baseUrl(BuildConfig.ENDPOINT)
            .client(client)
            .build()

    @Provides
    @Singleton internal fun provideTimePunchService(retrofit: Retrofit): LegalService = retrofit.create(LegalService::class.java)
}