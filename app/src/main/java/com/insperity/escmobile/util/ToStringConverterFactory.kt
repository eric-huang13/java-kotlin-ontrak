package com.insperity.escmobile.util

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Created by dxsier on 12/9/16.
 */

class ToStringConverterFactory : Converter.Factory() {

    companion object {
        internal val MEDIA_TYPE = MediaType.parse("text/plain")

        fun create() = ToStringConverterFactory()
    }

    override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
        return if (String::class.java != type) {
            null
        } else Converter<ResponseBody, String> { value -> value.string() }
    }

    override fun requestBodyConverter(type: Type?, parameterAnnotations: Array<Annotation>?, methodAnnotations: Array<Annotation>?, retrofit: Retrofit?): Converter<*, RequestBody>? {
        return if (String::class.java != type) {
            null
        } else Converter<String, RequestBody> { value -> RequestBody.create(MEDIA_TYPE, value) }
    }
}