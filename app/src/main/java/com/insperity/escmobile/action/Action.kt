package com.insperity.escmobile.action

import java.util.*

class Action private constructor(val type: String, private val data: HashMap<Int, Any>) {

    companion object {

        fun type(type: String) = Builder().with(type)
    }

    fun getByKey(key: Int) = data[key]

    class Builder {
        private lateinit var type: String
        private lateinit var data: HashMap<Int, Any>

        fun with(type: String): Builder {
            this.type = type
            this.data = HashMap()
            return this
        }

        fun bundle(key: Int, value: Any): Builder {
            data.put(key, value)
            return this
        }

        fun build() = Action(type, data)
    }
}