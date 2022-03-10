package com.insperity.escmobile.dagger.components


import com.insperity.escmobile.view.activity.MainTimeEntryActivity
import com.insperity.escmobile.view.activity.MainTimeEntrySupervisorActivity
import com.insperity.escmobile.view.activity.MainTimeOffApprovalDetailsActivity
import com.insperity.escmobile.view.adapter.TimeOffRequestDatesEditAdapter
import com.insperity.escmobile.view.fragment.*
import com.insperity.escmobile.view.service.OfflinePunchJobService
import com.insperity.escmobile.view.service.PunchDataService
import com.insperity.escmobile.view.widget.ClockView
import com.insperity.escmobile.view.widget.PayPeriodView
import com.insperity.escmobile.view.widget.PunchOrgLevelView

interface MainAppComponent {

    fun inject(punchFragment: PunchFragment)
    fun inject(clockView: ClockView)
    fun inject(punchCategoriesFragment: PunchCategoriesFragment)
    fun inject(recentPunchesFragment: RecentPunchesFragment)
    fun inject(punchOrgLevelView: PunchOrgLevelView)
    fun inject(punchDataService: PunchDataService)
    fun inject(offlinePunchJobService: OfflinePunchJobService)
    fun inject(timeOffBalanceFragment: TimeOffBalanceFragment)
    fun inject(timeOffRequestListFragment: TimeOffRequestsFragment)
    fun inject(timeOffRequestDetailsFragment: TimeOffRequestDetailsFragment)
    fun inject(timeOffSubmitFragment: TimeOffSubmitFragment)
    fun inject(timeOffAddDateFragment: TimeOffAddDateFragment)
    fun inject(timeOffRequestDatesEditAdapter: TimeOffRequestDatesEditAdapter)
    fun inject(webFragment: WebFragment)
    fun inject(aboutFragment: AboutFragment)
    fun inject(timeOffApprovalsFragment: TimeOffApprovalsFragment)
    fun inject(timeOffApprovalTabFragment: TimeOffApprovalTabFragment)
    fun inject(mainTimeOffApprovalDetailsActivity: MainTimeOffApprovalDetailsActivity)
    fun inject(timeOffApprovalDetailsFragment: TimeOffApprovalDetailsFragment)
    fun inject(timeOffCalendarFragment: TimeOffCalendarFragment)
    fun inject(timeOffApprovalResolutionFragment: TimeOffApprovalResolutionFragment)
    fun inject(payPeriodView: PayPeriodView)
    fun inject(mainTimeEntryActivity: MainTimeEntryActivity)
    fun inject(timeEntryBaseFragment: TimeEntryBaseFragment)
    fun inject(timeEntryDetailFragment: TimeEntryDetailFragment)
    fun inject(mainTimeEntrySupervisorActivity: MainTimeEntrySupervisorActivity)
    fun inject(timeEntryEmployeeListFragment: TimeEntryEmployeeListFragment)
    fun inject(orgItemsSearchFragment: OrgItemsSearchFragment)
}
