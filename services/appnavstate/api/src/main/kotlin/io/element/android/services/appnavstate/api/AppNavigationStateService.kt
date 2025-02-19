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

package io.element.android.services.appnavstate.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.core.ThreadId
import kotlinx.coroutines.flow.StateFlow

interface AppNavigationStateService {
    val appNavigationStateFlow: StateFlow<AppNavigationState>

    fun onNavigateToSession(sessionId: SessionId)
    fun onLeavingSession()

    fun onNavigateToSpace(spaceId: SpaceId)
    fun onLeavingSpace()

    fun onNavigateToRoom(roomId: RoomId)
    fun onLeavingRoom()

    fun onNavigateToThread(threadId: ThreadId)
    fun onLeavingThread()
}
