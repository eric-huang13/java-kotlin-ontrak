package com.delphiaconsulting.timestar.event

class OnSupervisorAccessFlagsReceived(val canApprove: Boolean, val canUnapprove: Boolean, val empAppDisabled: Boolean, val dollarsDisabled: Boolean)