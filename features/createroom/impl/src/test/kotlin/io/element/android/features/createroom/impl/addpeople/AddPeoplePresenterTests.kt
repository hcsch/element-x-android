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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.createroom.impl.addpeople

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.selectusers.api.SelectUsersPresenterArgs
import io.element.android.features.selectusers.impl.DefaultSelectUsersPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddPeoplePresenterTests {

    private lateinit var presenter: AddPeoplePresenter

    @Before
    fun setup() {
        val selectUsersFactory = object : DefaultSelectUsersPresenter.DefaultSelectUsersFactory {
            override fun create(args: SelectUsersPresenterArgs) = DefaultSelectUsersPresenter(args)
        }
        presenter = AddPeoplePresenter(selectUsersFactory)
    }

    @Test
    fun `present - initial state`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState)
        }
    }
}
