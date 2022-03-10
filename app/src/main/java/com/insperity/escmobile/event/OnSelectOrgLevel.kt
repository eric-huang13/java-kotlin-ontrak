package com.insperity.escmobile.event

import com.insperity.escmobile.data.OrgItemEntity
import com.insperity.escmobile.data.OrgLevelEntity

class OnSelectOrgLevel(val orgLevel: OrgLevelEntity, val orgItems: List<OrgItemEntity>)