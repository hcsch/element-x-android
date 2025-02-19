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

package io.element.android.features.createroom.impl.addpeople

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.selectusers.api.SelectionMode
import io.element.android.features.selectusers.api.aListOfSelectedUsers
import io.element.android.features.selectusers.api.aSelectUsersState

open class AddPeopleStateProvider : PreviewParameterProvider<AddPeopleState> {
    override val values: Sequence<AddPeopleState>
        get() = sequenceOf(
            aAddPeopleState(),
            aAddPeopleState().copy(
                selectUsersState = aSelectUsersState().copy(
                    selectedUsers = aListOfSelectedUsers(),
                    selectionMode = SelectionMode.Multiple,
                )
            ),
            aAddPeopleState().copy(
                selectUsersState = aSelectUsersState().copy(
                    selectedUsers = aListOfSelectedUsers(),
                    isSearchActive = true,
                    selectionMode = SelectionMode.Multiple,
                )
            )
        )
}

fun aAddPeopleState() = AddPeopleState(
    selectUsersState = aSelectUsersState(),
    eventSink = {}
)
