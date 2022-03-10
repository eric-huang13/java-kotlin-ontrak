package com.insperity.escmobile.util

/**
 * Created by dxsier on 1/23/17.
 */

object TokenUtil {

    fun formatItaAuthToken(token: String) = if (token.isEmpty()) token else String.format("Bearer %s", token)
}
