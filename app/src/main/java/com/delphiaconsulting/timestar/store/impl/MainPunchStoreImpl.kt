package com.delphiaconsulting.timestar.store.impl

import android.content.Context
import android.util.Pair
import com.delphiaconsulting.timestar.R
import com.delphiaconsulting.timestar.action.Action
import com.delphiaconsulting.timestar.action.Actions
import com.delphiaconsulting.timestar.action.Keys
import com.delphiaconsulting.timestar.data.*
import com.delphiaconsulting.timestar.dispatcher.Dispatcher
import com.delphiaconsulting.timestar.event.*
import com.delphiaconsulting.timestar.net.gson.*
import com.delphiaconsulting.timestar.store.MainStore
import com.delphiaconsulting.timestar.store.PunchStore
import com.delphiaconsulting.timestar.util.Preferences
import com.delphiaconsulting.timestar.util.PunchStatus
import com.delphiaconsulting.timestar.util.PunchStatus.*
import com.delphiaconsulting.timestar.util.Triple
import com.delphiaconsulting.timestar.view.common.AdapterItem
import com.delphiaconsulting.timestar.view.common.PunchItem
import com.delphiaconsulting.timestar.view.common.PunchSectionItem
import com.delphiaconsulting.timestar.view.service.OfflinePunchJobService
import net.danlew.android.joda.DateUtils
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by dxsier on 12/27/16.
 */

open class MainPunchStoreImpl(dispatcher: Dispatcher, bus: EventBus, protected var preferences: Preferences, private val context: Context, private val daoSession: DaoSession) : MainStore(dispatcher, bus), PunchStore {

    companion object {
        const val ORG_LEVEL_SELECTION_THRESHOLD = 15
    }

    protected open val punchCategoryEntityDao: PunchCategoryEntityDao
        get() = daoSession.punchCategoryEntityDao
    protected open val orgLevelEntityDao: OrgLevelEntityDao
        get() = daoSession.orgLevelEntityDao
    protected open val orgItemEntityDao: OrgItemEntityDao
        get() = daoSession.orgItemEntityDao
    protected open val orgDefaultEntityDao: OrgDefaultEntityDao
        get() = daoSession.orgDefaultEntityDao
    protected open val punchEntityDao: PunchEntityDao
        get() = daoSession.punchEntityDao
    protected open val orgLevelSelectionEntityDao: OrgLevelSelectionEntityDao
        get() = daoSession.orgLevelSelectionEntityDao

    @Suppress("UNCHECKED_CAST")
    override fun onActionReceived(action: Action) {
        when (action.type) {
            Actions.PUNCH_SYNC_DATA_RECEIVED -> {
                val categoryPair = action.getByKey(Keys.PUNCH_CATEGORIES) as Pair<Response<ServerTime>, Response<PunchCategoryList>>
                val orgLevelPair = action.getByKey(Keys.ORG_LEVELS_ITEMS) as Pair<Response<OrgLevelList>, Response<OrgHierarchyList>>
                val defaultsPair = action.getByKey(Keys.DEFAULTS_DATA) as Pair<Response<OrgDefaultList>, Response<OrgDefaultList>>
                if (categoryPair.first.message != null || categoryPair.second.message != null || orgLevelPair.first.message != null || orgLevelPair.second.message != null || defaultsPair.first.message != null || defaultsPair.second.message != null){
                    emitChange(OnPunchDataError())
                    return
                }
                preferences.serverTime = if (categoryPair.first.data != null) categoryPair.first.data.time * 1000 else DateTime.now().millis
                preferences.serverTimeSetTime = System.currentTimeMillis()
                preferences.allowOrgLevelDefaultsSwitch = categoryPair.second.data != null && categoryPair.second.data.allowTransfers == 1
                savePunchCategoriesStream(categoryPair.second.data)
                saveOrgLevelStream(orgLevelPair.first.data, orgLevelPair.second.data)
                saveOrgDefaultsStream(defaultsPair.first.data, defaultsPair.second.data)
            }
            Actions.PUNCHES_DATA_RECEIVED -> {
                val punchesResponse = action.getByKey(Keys.PUNCHES_DATA) as Response<PunchList>
                if (punchesResponse.message != null) {
                    emitChange(OnPunchDataError())
                    return
                }
                savePunchesStream(punchesResponse.data?.content?.punch)
            }
            Actions.ONLINE_PUNCH_SUBMITTED -> {
                val response = action.getByKey(Keys.SUBMITTED_PUNCH) as Response<SubmittedPunch>
                response.message?.let {
                    emitChange(OnPunchSubmissionError(it.message))
                    return
                }
                emitChange(OnTrackPunchEvent(false, "Submit", "Success"))
            }
            Actions.SCHEDULE_OFFLINE_PUNCH_SUBMISSION -> {
                val punchToSubmit = action.getByKey(Keys.OFFLINE_PUNCH_SUBMISSION) as PunchToSubmit
                savePunchObservable(null, punchToSubmit, NOT_SYNCED_STATUS)
                        .doOnError { e -> punchSubmissionError(true, e, true) }
                        .doOnCompleted {
                            emitChange(OnOfflinePunchStored())
                            emitChange(OnTrackPunchEvent(true, "Submit", "Success"))
                            OfflinePunchJobService.schedule(context)
                        }
                        .subscribe()
            }
            Actions.OFFLINE_PUNCHES_SUBMITTED -> {
                val offlinePunchesPair = action.getByKey(Keys.SUBMITTED_PUNCH) as Pair<Response<SubmittedPunch>, List<Long>>
                if (offlinePunchesPair.first.data?.success != 1) {
                    emitChange(OnOfflinePunchesSyncFailed())
                    return
                }
                updatePunchStatuses(offlinePunchesPair.second)
            }
            Actions.OFFLINE_PUNCHES_SUBMISSION_ERROR -> {
                logError(action)
                emitChange(OnOfflinePunchesSyncFailed())
            }
            Actions.ONLINE_PUNCH_SUBMISSION_ERROR -> {
                val throwable = action.getByKey(Keys.ERROR) as Throwable
                punchSubmissionError(false, throwable, false)
            }
            Actions.PUNCH_SYNC_ERROR, Actions.PUNCHES_DATA_ERROR -> onBaseDataSavingError(action.getByKey(Keys.ERROR) as Throwable)
            else -> logOnActionNotCaught(action.type)
        }
    }

    private fun savePunchCategoriesStream(punchCategoryList: PunchCategoryList?) {
        if (punchCategoryList == null) {
            emitChange(OnNoPunchDataError())
            return
        }
        punchCategoryEntityDao.deleteAll()
        Observable.from(punchCategoryList.punchCategories)
                .filter { it.webPunchFlag == 1 }
                .concatMap { punchCat ->
                    punchCategoryEntityDao.queryBuilder().orderDesc(PunchCategoryEntityDao.Properties.Order).limit(1).rx().unique()
                            .map { if (it == null) 1 else it.order + 1 }
                            .map { PunchCategoryEntity(punchCat.punchCategory, punchCat.name, punchCat.description, punchCat.defaultOrgLevelsFlag == 1, it) }
                            .concatMap { punchCategoryEntityDao.rx().insert(it) }
                }
                .doOnError { onBaseDataSavingError(it) }
                .subscribe()
    }

    private fun saveOrgLevelStream(orgLevelList: OrgLevelList?, orgHierarchyList: OrgHierarchyList?) {
        if (orgLevelList == null || orgHierarchyList == null) {
            emitChange(OnNoPunchDataError())
            return
        }
        orgLevelEntityDao.deleteAll()
        orgItemEntityDao.deleteAll()
        preferences.quickOrgLevelSelection = true
        Observable.from(orgHierarchyList.map)
                .filter { it.depth != 0L }
                .concatMap { mapItem ->
                    Observable.from(mapItem.items.entries)
                            .map { Pair(mapItem.depth, it) }
                }
                .filter { it.second.key.toLong() < 1 }
                .switchMap { pair ->
                    Observable.from(orgHierarchyList.lists)
                            .filter { it.hash == pair.second.value }
                            .map { it.list }
                            .map { Pair(pair.first, it) }
                }
                .toList()
                .concatMap { saveOrgLevelStream(orgLevelList, orgHierarchyList, it) }
                .doOnError { onBaseDataSavingError(it) }
                .subscribe()
    }

    private fun saveOrgLevelStream(orgLevelList: OrgLevelList, orgHierarchyList: OrgHierarchyList?, restrictedOrgLevels: List<Pair<Long, List<Int>>>): Observable<*> = Observable.from(orgLevelList.orgLevelDepths)
            .filter { isOrgLevelRestricted(it, restrictedOrgLevels) }
            .concatMap { orgLevel ->
                orgLevelEntityDao.rx().insert(OrgLevelEntity(orgLevel.depth, orgLevel.name, null))
                        .map { Pair(it, orgLevel.items) }
            }
            .toSortedList { left, right -> left.first.id.compareTo(right.first.id) }
            .concatMapIterable { setLevelNextInHierarchy(it) }
            .concatMap { pair ->
                orgLevelEntityDao.rx().update(pair.first)
                        .concatMap { orgLevelEntity ->
                            if (pair.second.size > ORG_LEVEL_SELECTION_THRESHOLD) {
                                preferences.quickOrgLevelSelection = false
                            }
                            Observable.from(pair.second)
                                    .map { Pair(orgLevelEntity.id, it) }
                        }
            }
            .concatMap { saveOrgItemStream(it.first, it.second, orgHierarchyList, restrictedOrgLevels) }

    private fun isOrgLevelRestricted(orgLevel: OrgLevelList.OrgLevel, restrictedOrgLevels: List<Pair<Long, List<Int>>>) = restrictedOrgLevels.none { it.first == orgLevel.depth }

    private fun setLevelNextInHierarchy(pairs: List<Pair<OrgLevelEntity, List<OrgLevelList.OrgItem>>>): List<Pair<OrgLevelEntity, List<OrgLevelList.OrgItem>>> {
        var previousOrgLevelEntity: OrgLevelEntity? = null
        for (pair in pairs) {
            if (previousOrgLevelEntity != null) {
                previousOrgLevelEntity.next = pair.first
            }
            previousOrgLevelEntity = pair.first
        }
        return pairs
    }

    private fun saveOrgItemStream(orgLevelId: Long, orgItem: OrgLevelList.OrgItem, orgHierarchyList: OrgHierarchyList?, restrictedOrgLevels: List<Pair<Long, List<Int>>>): Observable<*> = Observable.from(orgHierarchyList?.map)
            .filter { it.depth == orgLevelId }
            .concatMapIterable { it.items.entries }
            .filter { orgItem.orgLevelId == it.key.toLong() }
            .map { it.value }
            .switchMap { hash ->
                Observable.from(orgHierarchyList?.lists)
                        .filter { it.hash == hash }
                        .map { it.list }
                        .concatMap { getOrgItemNextValues(orgLevelId, it, restrictedOrgLevels) }
                        .concatMapIterable { it }
                        .map { it.toLong() }
                        .toList()
            }
            .concatMap { nextOrgItemIds ->
                orgItemEntityDao.queryBuilder().orderDesc(OrgItemEntityDao.Properties.Order).limit(1).rx().unique()
                        .map { if (it == null) 1 else it.order + 1 }
                        .map { OrgItemEntity(orgItem.orgLevelId, orgItem.label, orgLevelId, null, it).setNextOrgItemIds(nextOrgItemIds) }
                        .concatMap { orgItemEntityDao.rx().insert(it) }
            }

    private fun getOrgItemNextValues(orgLevelId: Long, values: List<Int>, restrictedOrgLevels: List<Pair<Long, List<Int>>>): Observable<List<Int>> {
        if (values.size == 1 && values[0] < 1) {
            val nextOrgLevelId = orgLevelId + 1
            restrictedOrgLevels.filter { it.first == nextOrgLevelId }.forEach { return getOrgItemNextValues(nextOrgLevelId, it.second, restrictedOrgLevels) }
        }
        return Observable.just(values)
    }

    private fun saveOrgDefaultsStream(mainOrgDefaultTree: OrgDefaultList?, orgDefaultList: OrgDefaultList?) {
        orgDefaultEntityDao.deleteAll()
        if (mainOrgDefaultTree == null) {
            saveOrgDefaultsStream(orgDefaultList)
            return
        }
        Observable.from(mainOrgDefaultTree.orgHierarchy)
                .concatMap { orgDefaultEntityDao.rx().insert(OrgDefaultEntity(null, it.depth, it.orgLevelId, null, true)) }.toList()
                .concatMapIterable { setDefaultNextInHierarchy(it) }
                .concatMap { orgDefaultEntityDao.rx().update(it) }
                .doOnError { onBaseDataSavingError(it) }
                .doOnCompleted { saveOrgDefaultsStream(orgDefaultList) }
                .subscribe()
    }

    private fun saveOrgDefaultsStream(orgDefaultList: OrgDefaultList?) {
        if (orgDefaultList == null) return
        Observable.from(orgDefaultList.orgLevelDefaults)
                .map { it.defaults }
                .concatMap { it ->
                    Observable.from(it).filter { orgLevelEntityDao.load(it.depth) != null }
                            .concatMap { orgDefaultEntityDao.rx().insert(OrgDefaultEntity(null, it.depth, it.orgLevelId, null, false)) }.toList()
                }
                .concatMapIterable { setDefaultNextInHierarchy(it) }
                .concatMap { orgDefaultEntityDao.rx().update(it) }
                .doOnError { onBaseDataSavingError(it) }
                .subscribe()
    }

    private fun setDefaultNextInHierarchy(orgDefaultEntities: List<OrgDefaultEntity>): List<OrgDefaultEntity> {
        var previousOrgDefaultEntity: OrgDefaultEntity? = null
        for (orgDefaultEntity in orgDefaultEntities) {
            if (previousOrgDefaultEntity != null) {
                previousOrgDefaultEntity.next = orgDefaultEntity
            }
            previousOrgDefaultEntity = orgDefaultEntity
        }
        return orgDefaultEntities
    }

    private fun savePunchesStream(punches: List<PunchList.Punch>?) {
        punchEntityDao.queryBuilder().where(PunchEntityDao.Properties.SyncStatus.notEq(NOT_SYNCED_STATUS)).buildDelete().executeDeleteWithoutDetachingEntities()
        punches?.let { it ->
            Observable.from(it)
                    .concatMap { punch ->
                        Observable.from(punch.orgLevels.entries)
                                .map { PunchToSubmit.OrgLevelSelection(it.key.split("rgLevelDepth")[1].toLong(), it.value.value.toLong()) }
                                .toList()
                                .map { Pair(punch.punchId.value.toInt(), PunchToSubmit(punch.punchCategory.value.toLong(), punch.comment.value, it, punch.timestamp)) }
                                .concatMap { savePunchObservable(it.first, it.second, ARCHIVED_STATUS) }
                    }
                    .doOnError { onBaseDataSavingError(it) }
                    .doOnCompleted { emitChange(OnPunchesDataSaved()) }
                    .subscribe()
        }
    }

    private fun savePunchObservable(punchExternalId: Int?, punchData: PunchToSubmit, @PunchStatus punchStatus: Int): Observable<*> =
            punchCategoryEntityDao.queryBuilder()
                    .where(PunchCategoryEntityDao.Properties.Id.eq(punchData.punchCategory)).rx().unique()
                    .filter { it != null }
                    .switchMap {
                        if (punchStatus != ARCHIVED_STATUS) {
                            emitChange(OnTrackPunchEvent(punchExternalId == null, "Type", String.format("%s:%s", it.id, it.description)))
                            emitChange(OnTrackPunchEvent(punchExternalId == null, "Comment", (!punchData.comment.isEmpty()).toString()))
                        }
                        punchEntityDao.rx().insert(PunchEntity(null, punchExternalId, punchData.timestamp, punchData.comment, punchStatus, it.id))
                    }
                    .switchMap { punch ->
                        Observable.from(punchData.rawOrgLevels)
                                .concatMap {
                                    Observable.zip(orgLevelEntityDao.queryBuilder().where(OrgLevelEntityDao.Properties.Id.eq(it.depth)).rx().unique().onErrorReturn { null },
                                            orgItemEntityDao.queryBuilder().where(OrgItemEntityDao.Properties.Id.eq(it.orgLevelId)).rx().unique().onErrorReturn { null })
                                    { orgLevelEnt, orgItemEnt -> orgLevelEnt?.let { OrgLevelSelectionEntity(null, punch.id, orgLevelEnt.id, if (orgItemEnt == null) 0L else orgItemEnt.id) } }
                                }
                                .concatMap { if (it == null) Observable.empty() else orgLevelSelectionEntityDao.rx().insert(it) }
                                .toList()
                                .map { punch }
                    }

    private fun onBaseDataSavingError(throwable: Throwable) {
        logError(throwable)
        emitChange(OnPunchDataError())
    }

    protected open fun punchSubmissionError(offline: Boolean, throwable: Throwable, notify: Boolean) {
        logError(throwable)
        emitChange(OnTrackPunchEvent(offline, "Submit", String.format("Fail:%s", throwable.message)))
        if (notify) {
            emitChange(OnPunchSubmissionError())
        }
    }

    private fun updatePunchStatuses(updatedPunchIds: List<Long>) = punchEntityDao.queryBuilder()
            .where(PunchEntityDao.Properties.SyncStatus.eq(NOT_SYNCED_STATUS), PunchEntityDao.Properties.Id.`in`(updatedPunchIds)).rx()
            .oneByOne()
            .onBackpressureBuffer()
            .concatMap {
                it.syncStatus = SYNCED_STATUS
                punchEntityDao.rx().update(it)
            }
            .doOnCompleted { emitChange(OnOfflinePunchesSynced()) }
            .subscribe()

    override fun getPunchCategories() {
        Observable.zip(punchCategoryEntityDao.queryBuilder().orderAsc(PunchCategoryEntityDao.Properties.Order).rx().list()
                .switchMap { it ->
                    Observable.zip(Observable.from(it).filter { it.defaultOrgLevel }.toList(),
                            Observable.from(it).filter { !it.defaultOrgLevel }.toList())
                    { first, second -> Pair<List<PunchCategoryEntity>, List<PunchCategoryEntity>>(first, second) }
                },
                orgLevelEntityDao.queryBuilder().orderAsc(OrgLevelEntityDao.Properties.Id).rx().oneByOne().first()
                        .onErrorReturn {
                            Timber.e("getPunchCategories::orgLevelEntityDao -> %s", it.message)
                            null
                        },
                orgDefaultEntityDao.queryBuilder().where(OrgDefaultEntityDao.Properties.MainDefaultRoot.eq(true))
                        .orderAsc(OrgDefaultEntityDao.Properties.Id)
                        .rx().oneByOne().first()
                        .onErrorReturn {
                            Timber.e("getPunchCategories::orgDefaultEntityDao -> %s", it.message)
                            null
                        }) { first, second, third -> Triple(first, second, third) }
                .doOnError {
                    logError(it)
                    emitChange(OnPunchCategoriesLoadError())
                }
                .subscribeOn(Schedulers.computation())
                .subscribe { emitChange(OnPunchCategoriesLoaded(it.first, it.second, it.third)) }
    }

    override fun getOfflinePunchesToSync() {
        punchEntityDao.queryBuilder().where(PunchEntityDao.Properties.SyncStatus.eq(NOT_SYNCED_STATUS)).rx()
                .oneByOne()
                .concatMap { punch ->
                    Observable.from(punch.orgLevels)
                            .filter { it.orgItemId != 0L }
                            .map { PunchToSubmit.OrgLevelSelection(it.orgLevelId, it.orgItemId) }
                            .toList()
                            .map { PunchToSubmit(punch.punchCategoryId, punch.comment, it, punch.datetime, punch.id) }
                }
                .toList()
                .map { PunchesToSubmit(it) }
                .subscribeOn(Schedulers.computation())
                .subscribe { emitChange(OnOfflinePunchesToSubmit(it)) }
    }

    @Synchronized
    override fun getPunches() {
        Observable.zip(punchEntityDao.queryBuilder().orderDesc(PunchEntityDao.Properties.Datetime).orderAsc(PunchEntityDao.Properties.ExternalId).rx().list(),
                punchEntityDao.queryBuilder().where(PunchEntityDao.Properties.SyncStatus.eq(NOT_SYNCED_STATUS)).rx().list().map { it.isEmpty() })
        { punchEntities, empty ->
            if (!empty) {
                OfflinePunchJobService.schedule(context)
                emitChange(OnPendingOfflinePunchesSync())
            }
            return@zip punchEntities
        }
                .concatMapIterable { it }
                .filter { Days.daysBetween(DateTime(it.datetime).toLocalDate(), DateTime.now().toLocalDate()).days <= 1 }
                .toList()
                .concatMap { it ->
                    Observable.zip(
                            Observable.from(it).filter { DateUtils.isToday(DateTime(it.datetime)) }.map { PunchItem(it) }.toList().map { attachDateSection(it) },
                            Observable.from(it).filter { !DateUtils.isToday(DateTime(it.datetime)) }.map { PunchItem(it) }.toList().map { attachDateSection(it) })
                    { todayPunches, yesterdayPunches ->
                        val array = ArrayList(todayPunches)
                        array.addAll(yesterdayPunches)
                        array
                    }
                }
                .subscribeOn(Schedulers.computation())
                .subscribe { emitChange(OnPunchesLoaded(it)) }
    }

    override fun getLatestPunch() {
        punchEntityDao.queryBuilder().orderDesc(PunchEntityDao.Properties.Datetime).rx().oneByOne().first()
                .subscribe({ emitStickyChange(OnLatestPunchFound(it)) }, { emitStickyChange(OnNoPunchesAvailableError()) })
    }

    override fun getNextDefaultFor(orgLevelId: Long, orgItemId: Long, isMainDefault: Boolean): OrgDefaultEntity? = orgDefaultEntityDao.queryBuilder()
            .where(OrgDefaultEntityDao.Properties.MainDefaultRoot.eq(isMainDefault), OrgDefaultEntityDao.Properties.OrgLevelId.eq(orgLevelId), OrgDefaultEntityDao.Properties.OrgItemId.eq(orgItemId))
            .rx().oneByOne().first()
            .map { orgDefaultEntity -> orgDefaultEntity?.next }
            .onErrorReturn { null }
            .toBlocking()
            .single()

    private fun attachDateSection(punchItems: List<PunchItem>?): List<AdapterItem> {
        val adapterItems = ArrayList<AdapterItem>()
        if (punchItems == null || punchItems.isEmpty()) {
            return adapterItems
        }
        adapterItems.add(PunchSectionItem(punchSectionDateFormatter(punchItems[0].punchEntity.datetime)))
        adapterItems.addAll(punchItems)
        return adapterItems
    }

    private fun punchSectionDateFormatter(timestamp: Long): String {
        val date = DateTime(timestamp)
        val dateHeader = context.getString(if (DateTime.now().dayOfWeek == date.dayOfWeek) R.string.today_punch_text else R.string.yesterday_punch_text)
        return String.format("%s, %s", dateHeader, date.toString(DateTimeFormat.forPattern("EEEE  MM/dd/yyyy")))
    }
}

