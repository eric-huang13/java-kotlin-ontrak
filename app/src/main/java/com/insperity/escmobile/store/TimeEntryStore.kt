package com.insperity.escmobile.store

import com.insperity.escmobile.event.OnEmployeeBatchProcessed
import com.insperity.escmobile.event.OnEmployeeLoadingProgressUpdate
import rx.subjects.PublishSubject

interface TimeEntryStore {

    val onEmployeeLoadingProgressUpdateSubject: PublishSubject<OnEmployeeLoadingProgressUpdate>
    val onEmployeeBatchProcessedSubject: PublishSubject<OnEmployeeBatchProcessed>
}