package com.delphiaconsulting.timestar.event

import android.util.Pair

import com.delphiaconsulting.timestar.data.OrgDefaultEntity
import com.delphiaconsulting.timestar.data.OrgLevelEntity
import com.delphiaconsulting.timestar.data.PunchCategoryEntity

/**
 * Created by dxsier on 1/4/17.
 */
class OnPunchCategoriesLoaded(val punchCategories: Pair<List<PunchCategoryEntity>, List<PunchCategoryEntity>>?, val rootOrgLevel: OrgLevelEntity?, val rootOrgDefault: OrgDefaultEntity?)
