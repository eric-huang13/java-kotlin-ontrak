package com.delphiaconsulting.timestar.util

/**
 * Created by dxsier on 12/5/16.
 */

class Quadruple<out F, out S, out T, out Q>(val first: F, val second: S, val third: T, val fourth: Q) {

    companion object {
        fun <A, B, C, D> create(a: A, b: B, c: C, d: D) = Quadruple(a, b, c, d)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Quadruple<*, *, *, *>) {
            return false
        }
        val p = other as Quadruple<*, *, *, *>?
        return p!!.first == first && p.second == second && p.third == third && p.fourth == fourth
    }

    override fun hashCode() = (first?.hashCode() ?: 0) xor (second?.hashCode() ?: 0) xor (third?.hashCode() ?: 0) xor (fourth?.hashCode() ?: 0)
}