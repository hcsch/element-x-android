/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.rageshake.impl.bugreport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.rageshake.api.reporter.BugReporter
import io.element.android.features.rageshake.api.reporter.BugReporterListener
import io.element.android.features.rageshake.api.reporter.ReportType
import io.element.android.features.rageshake.api.crash.CrashDataStore
import io.element.android.features.rageshake.impl.logs.VectorFileLogger
import io.element.android.features.rageshake.api.screenshot.ScreenshotHolder
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BugReportPresenter @Inject constructor(
    private val bugReporter: BugReporter,
    private val crashDataStore: CrashDataStore,
    private val screenshotHolder: ScreenshotHolder,
    private val appCoroutineScope: CoroutineScope,
) : Presenter<BugReportState> {

    private class BugReporterUploadListener(
        private val sendingProgress: MutableState<Float>,
        private val sendingAction: MutableState<Async<Unit>>
    ) : BugReporterListener {

        override fun onUploadCancelled() {
            sendingProgress.value = 0f
            sendingAction.value = Async.Uninitialized
        }

        override fun onUploadFailed(reason: String?) {
            sendingProgress.value = 0f
            sendingAction.value = Async.Failure(Exception(reason))
        }

        override fun onProgress(progress: Int) {
            sendingProgress.value = progress.toFloat() / 100
            sendingAction.value = Async.Loading()
        }

        override fun onUploadSucceed(reportUrl: String?) {
            sendingProgress.value = 0f
            sendingAction.value = Async.Success(Unit)
        }
    }

    @Composable
    override fun present(): BugReportState {
        val screenshotUri = rememberSaveable {
            mutableStateOf(
                screenshotHolder.getFileUri()
            )
        }
        val crashInfo: String by crashDataStore
            .crashInfo()
            .collectAsState(initial = "")

        val sendingProgress = remember {
            mutableStateOf(0f)
        }
        val sendingAction: MutableState<Async<Unit>> = remember {
            mutableStateOf(Async.Uninitialized)
        }
        val formState: MutableState<BugReportFormState> = remember {
            mutableStateOf(BugReportFormState.Default)
        }
        val uploadListener = BugReporterUploadListener(sendingProgress, sendingAction)

        fun handleEvents(event: BugReportEvents) {
            when (event) {
                BugReportEvents.SendBugReport -> appCoroutineScope.sendBugReport(formState.value, crashInfo.isNotEmpty(), uploadListener)
                BugReportEvents.ResetAll -> appCoroutineScope.resetAll()
                is BugReportEvents.SetDescription -> updateFormState(formState) {
                    copy(description = event.description)
                }
                is BugReportEvents.SetCanContact -> updateFormState(formState) {
                    copy(canContact = event.canContact)
                }
                is BugReportEvents.SetSendCrashLog -> updateFormState(formState) {
                    copy(sendCrashLogs = event.sendCrashlog)
                }
                is BugReportEvents.SetSendLog -> updateFormState(formState) {
                    copy(sendLogs = event.sendLog)
                }
                is BugReportEvents.SetSendScreenshot -> updateFormState(formState) {
                    copy(sendScreenshot = event.sendScreenshot)
                }
                BugReportEvents.ClearError -> {
                    sendingProgress.value = 0f
                    sendingAction.value = Async.Uninitialized
                }
            }
        }

        return BugReportState(
            hasCrashLogs = crashInfo.isNotEmpty(),
            sendingProgress = sendingProgress.value,
            sending = sendingAction.value,
            formState = formState.value,
            screenshotUri = screenshotUri.value,
            eventSink = ::handleEvents
        )
    }

    private fun updateFormState(formState: MutableState<BugReportFormState>, operation: BugReportFormState.() -> BugReportFormState) {
        formState.value = operation(formState.value)
    }

    private fun CoroutineScope.sendBugReport(
        formState: BugReportFormState,
        hasCrashLogs: Boolean,
        listener: BugReporterListener,
    ) = launch {
        bugReporter.sendBugReport(
            reportType = ReportType.BUG_REPORT,
            withDevicesLogs = formState.sendLogs,
            withCrashLogs = hasCrashLogs && formState.sendCrashLogs,
            withKeyRequestHistory = false,
            withScreenshot = formState.sendScreenshot,
            theBugDescription = formState.description,
            serverVersion = "",
            canContact = formState.canContact,
            customFields = emptyMap(),
            listener = listener
        )
    }

    private fun CoroutineScope.resetAll() = launch {
        screenshotHolder.reset()
        crashDataStore.reset()
        VectorFileLogger.getFromTimber()?.reset()
    }
}
