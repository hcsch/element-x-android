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

package io.element.android.features.rageshake.crash.ui

import io.element.android.features.rageshake.crash.CrashDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

const val A_CRASH_DATA = "Some crash data"

class FakeCrashDataStore(
    crashData: String = "",
    appHasCrashed: Boolean = false,
) : CrashDataStore {
    private val appHasCrashedFlow = MutableStateFlow(appHasCrashed)
    private val crashDataFlow = MutableStateFlow(crashData)

    override fun setCrashData(crashData: String) {
        crashDataFlow.value = crashData
    }

    override suspend fun resetAppHasCrashed() {
        appHasCrashedFlow.value = false
    }

    override fun appHasCrashed(): Flow<Boolean> = appHasCrashedFlow

    override fun crashInfo(): Flow<String> = crashDataFlow

    override suspend fun reset() {
        appHasCrashedFlow.value = false
        crashDataFlow.value = ""
    }
}