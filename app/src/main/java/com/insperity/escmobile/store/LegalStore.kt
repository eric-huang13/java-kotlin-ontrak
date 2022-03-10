package com.insperity.escmobile.store

/**
 * Created by qktran on 11/22/16.
 */

interface LegalStore {

    fun getPageTitle(id: Int): String?

    fun getPageContent(id: Int): String?
}
