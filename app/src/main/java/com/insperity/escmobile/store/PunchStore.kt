package com.insperity.escmobile.store

import com.insperity.escmobile.data.OrgDefaultEntity

/**
 * Created by dxsier on 11/18/16.
 */

interface PunchStore {
    fun getPunchCategories()

    fun getOfflinePunchesToSync()

    fun getPunches()

    fun getLatestPunch()

    fun getNextDefaultFor(orgLevelId: Long, orgItemId: Long, isMainDefault: Boolean): OrgDefaultEntity?
}
