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

package io.element.android.features.roomlist.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryPlaceholders
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val extendedRangeSize = 40

class RoomListPresenter @Inject constructor(
    private val client: MatrixClient,
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
    private val sessionVerificationService: SessionVerificationService,
) : Presenter<RoomListState> {

    @Composable
    override fun present(): RoomListState {
        val matrixUser: MutableState<MatrixUser?> = remember {
            mutableStateOf(null)
        }
        var filter by rememberSaveable { mutableStateOf("") }
        val roomSummaries by client
            .roomSummaryDataSource
            .roomSummaries()
            .collectAsState()

        Timber.v("RoomSummaries size = ${roomSummaries.size}")

        val filteredRoomSummaries: MutableState<ImmutableList<RoomListRoomSummary>> = remember {
            mutableStateOf(persistentListOf())
        }
        LaunchedEffect(Unit) {
            initialLoad(matrixUser)
        }

        // Session verification status (unknown, not verified, verified)
        val sessionVerifiedStatus by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        var verificationPromptDismissed by rememberSaveable { mutableStateOf(false) }
        // We combine both values to only display the prompt if the session is not verified and it wasn't dismissed
        val displayVerificationPrompt by remember {
            derivedStateOf { sessionVerifiedStatus == SessionVerifiedStatus.NotVerified && !verificationPromptDismissed }
        }

        // Current verification flow status, if any (initial, requesting, accepted, etc.)
        val currentVerificationFlowStatus by sessionVerificationService.verificationFlowState.collectAsState()
        // We only care about the 'Finished' state to display the 'verification success' message
        val presentVerificationSuccessfulMessage = remember {
            derivedStateOf { currentVerificationFlowStatus == VerificationFlowState.Finished }
        }

        fun handleEvents(event: RoomListEvents) {
            when (event) {
                is RoomListEvents.UpdateFilter -> filter = event.newFilter
                is RoomListEvents.UpdateVisibleRange -> updateVisibleRange(event.range)
                RoomListEvents.DismissRequestVerificationPrompt -> verificationPromptDismissed = true
                RoomListEvents.ClearSuccessfulVerificationMessage -> sessionVerificationService.reset()
            }
        }

        LaunchedEffect(roomSummaries, filter) {
            filteredRoomSummaries.value = updateFilteredRoomSummaries(roomSummaries, filter)
        }

        return RoomListState(
            matrixUser = matrixUser.value,
            roomList = filteredRoomSummaries.value,
            filter = filter,
            presentVerificationSuccessfulMessage = presentVerificationSuccessfulMessage.value,
            displayVerificationPrompt = displayVerificationPrompt,
            eventSink = ::handleEvents
        )
    }

    private suspend fun updateFilteredRoomSummaries(roomSummaries: List<RoomSummary>?, filter: String): ImmutableList<RoomListRoomSummary> {
        if (roomSummaries.isNullOrEmpty()) {
            return RoomListRoomSummaryPlaceholders.createFakeList(16).toImmutableList()
        }
        val mappedRoomSummaries = mapRoomSummaries(roomSummaries)
        return if (filter.isEmpty()) {
            mappedRoomSummaries
        } else {
            mappedRoomSummaries.filter { it.name.contains(filter, ignoreCase = true) }
        }.toImmutableList()
    }

    private fun CoroutineScope.initialLoad(matrixUser: MutableState<MatrixUser?>) = launch {
        val userAvatarUrl = client.loadUserAvatarURLString().getOrNull()
        val userDisplayName = client.loadUserDisplayName().getOrNull()
        val avatarData =
            AvatarData(
                id = client.sessionId.value,
                name = userDisplayName,
                url = userAvatarUrl,
                size = AvatarSize.SMALL
            )
        matrixUser.value = MatrixUser(
            id = UserId(client.sessionId.value),
            username = userDisplayName ?: client.sessionId.value,
            avatarData = avatarData,
        )
    }

    private fun updateVisibleRange(range: IntRange) {
        if (range.isEmpty()) return
        val midExtendedRangeSize = extendedRangeSize / 2
        val extendedRangeStart = (range.first - midExtendedRangeSize).coerceAtLeast(0)
        // Safe to give bigger size than room list
        val extendedRangeEnd = range.last + midExtendedRangeSize
        val extendedRange = IntRange(extendedRangeStart, extendedRangeEnd)
        client.roomSummaryDataSource.setSlidingSyncRange(extendedRange)
    }

    private suspend fun mapRoomSummaries(
        roomSummaries: List<RoomSummary>
    ): List<RoomListRoomSummary> {
        return roomSummaries.parallelMap { roomSummary ->
            when (roomSummary) {
                is RoomSummary.Empty -> RoomListRoomSummaryPlaceholders.create(roomSummary.identifier)
                is RoomSummary.Filled -> {
                    val avatarData = AvatarData(
                        id = roomSummary.identifier(),
                        name = roomSummary.details.name,
                        url = roomSummary.details.avatarURLString
                    )
                    RoomListRoomSummary(
                        id = roomSummary.identifier(),
                        name = roomSummary.details.name,
                        hasUnread = roomSummary.details.unreadNotificationCount > 0,
                        timestamp = lastMessageTimestampFormatter.format(roomSummary.details.lastMessageTimestamp),
                        lastMessage = roomSummary.details.lastMessage?.let { message ->
                            roomLastMessageFormatter.processMessageItem(message.event, roomSummary.details.isDirect)
                        }.orEmpty(),
                        avatarData = avatarData,
                    )
                }
            }
        }
    }
}
