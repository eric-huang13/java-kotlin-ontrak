package com.delphiaconsulting.timestar.event

import com.delphiaconsulting.timestar.data.OrgItemEntity
import com.delphiaconsulting.timestar.data.OrgLevelEntity

class OnSelectOrgLevel(val orgLevel: OrgLevelEntity, val orgItems: List<OrgItemEntity>)