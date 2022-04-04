package com.delphiaconsulting.timestar.store

/**
 * Created by qktran on 11/22/16.
 */

interface LegalStore {

    fun getPageTitle(id: Int): String?

    fun getPageContent(id: Int): String?
}
