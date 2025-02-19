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

package io.element.android.libraries.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.SystemGrey4Dark
import io.element.android.libraries.designsystem.SystemGrey6Light
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.previews.ColorListPreview
import kotlinx.collections.immutable.persistentMapOf

/**
 * Room list.
 */
@Composable
fun MaterialTheme.roomListRoomName() = colorScheme.primary

@Composable
fun MaterialTheme.roomListRoomMessage() = colorScheme.secondary

@Composable
fun MaterialTheme.roomListRoomMessageDate() = colorScheme.secondary

@Composable
fun MaterialTheme.roomListUnreadIndicator() = colorScheme.primary

@Composable
fun ElementColors.roomListPlaceHolder() = if (isLight) SystemGrey6Light else SystemGrey4Dark

@Preview
@Composable
internal fun ColorAliasesLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun ColorAliasesDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    ColorListPreview(
        backgroundColor = Color.Black,
        foregroundColor = Color.White,
        colors = persistentMapOf(
            "roomListRoomName" to MaterialTheme.roomListRoomName(),
            "roomListRoomMessage" to MaterialTheme.roomListRoomMessage(),
            "roomListRoomMessageDate" to MaterialTheme.roomListRoomMessageDate(),
            "roomListUnreadIndicator" to MaterialTheme.roomListUnreadIndicator(),
            "roomListPlaceHolder" to ElementTheme.colors.roomListPlaceHolder(),
        )
    )
}
