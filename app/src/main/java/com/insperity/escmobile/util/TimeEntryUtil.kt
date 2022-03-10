package com.insperity.escmobile.util

object TimeEntryUtil {
    const val APPROVE_ACTION_KEY = "approve"
    const val UNAPPROVE_ACTION_KEY = "unapprove"
    const val EMPLOYEE_ACTION_KEY = "employee"
    const val SUPERVISOR_ACTION_KEY = "supervisor"
    const val LOAD_BATCH_SIZE = 10

    val NEEDS_APPROVAL_MASK_STATUS_ARRAY = intArrayOf(40, 32, 36, 37, 38, 39, 44, 45, 46, 47)
    val NO_ACTION_REQUIRED_MASK_STATUS_ARRAY = intArrayOf(0, 4, 5, 6, 7, 48, 56, 52, 53, 54, 55, 60, 61, 62, 63)

    val ENABLED_CHECKBOX_STATUS_ARRAY = intArrayOf(40, 56, 44, 45, 46, 47, 60, 61, 62, 63)
    val DISABLED_CHECKBOX_STATUS_ARRAY = intArrayOf(0, 4, 5, 7, 32, 37, 36, 38, 39)
    val DONE_CHECK_MARK_STATUS_ARRAY = intArrayOf(6, 48, 52, 53, 54, 55)

    val EMPLOYEE_APPROVED_STATUS_ARRAY = intArrayOf(6, 7, 38, 39, 46, 47, 54, 55, 62, 63)
    val SUPERVISOR_APPROVED_STATUS_ARRAY = intArrayOf(48, 56, 52, 53, 54, 55, 60, 61, 62, 63)
    val EMPLOYEE_CAN_CHANGE_STATUS_ARRAY = intArrayOf(5, 7, 37, 39, 45, 47, 53, 55, 61, 63)
    val SUPERVISOR_CAN_CHANGE_STATUS_ARRAY = intArrayOf(40, 56, 44, 45, 46, 47, 60, 61, 62, 63)

    val SUPERVISOR_CAN_APPROVE_ARRAY = intArrayOf(48, 56, 52, 54, 55, 60, 62, 63)
    val SUPERVISOR_CAN_UNAPPROVE_ARRAY = intArrayOf(56, 60, 62, 63)
    val SUPERVISOR_EMP_APP_DISABLED_ARRAY = intArrayOf(0, 32, 40, 48, 56)
}
