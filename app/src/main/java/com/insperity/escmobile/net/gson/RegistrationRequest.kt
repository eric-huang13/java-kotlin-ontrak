package com.insperity.escmobile.net.gson

import android.os.Build

class RegistrationRequest(val onboardingCode: String, val deviceName: String = "${Build.MANUFACTURER} ${Build.MODEL}")
