package com.insperity.escmobile.util

/**
 * Created by dxsier on 12/5/16.
 */

class Triple<out F, out S, out T>(val first: F?, val second: S?, val third: T?) {

    companion object {
        fun <A, B, C> create(a: A, b: B, c: C) = Triple(a, b, c)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Triple<*, *, *>) {
            return false
        }
        val p = other as Triple<*, *, *>?
        return p!!.first == first && p.second == second && p.third == third
    }

    override fun hashCode() = (first?.hashCode() ?: 0) xor (second?.hashCode() ?: 0) xor (third?.hashCode() ?: 0)
}