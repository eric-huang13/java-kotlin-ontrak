package com.delphiaconsulting.timestar.store

import com.delphiaconsulting.timestar.event.OnEmployeeBatchProcessed
import com.delphiaconsulting.timestar.event.OnEmployeeLoadingProgressUpdate
import rx.subjects.PublishSubject

interface TimeEntryStore {

    val onEmployeeLoadingProgressUpdateSubject: PublishSubject<OnEmployeeLoadingProgressUpdate>
    val onEmployeeBatchProcessedSubject: PublishSubject<OnEmployeeBatchProcessed>
}