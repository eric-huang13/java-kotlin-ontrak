package com.insperity.escmobile.event

import android.util.Pair

import com.insperity.escmobile.data.OrgDefaultEntity
import com.insperity.escmobile.data.OrgLevelEntity
import com.insperity.escmobile.data.PunchCategoryEntity

/**
 * Created by dxsier on 1/4/17.
 */
class OnPunchCategoriesLoaded(val punchCategories: Pair<List<PunchCategoryEntity>, List<PunchCategoryEntity>>?, val rootOrgLevel: OrgLevelEntity?, val rootOrgDefault: OrgDefaultEntity?)
