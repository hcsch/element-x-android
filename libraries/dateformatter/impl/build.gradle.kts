/*
 * Copyright (c) 2022 New Vector Ltd
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

// TODO: Remove once https://youtrack.jetbrains.com/issue/KTIJ-19369 is fixed
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("io.element.android-library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.anvil)
}

anvil {
    generateDaggerFactories.set(true)
}

android {
    namespace = "io.element.android.libraries.dateformatter.impl"

    dependencies {
        anvil(projects.anvilcodegen)
        implementation(libs.dagger)
        implementation(projects.libraries.di)
        implementation(projects.anvilannotations)

        api(projects.libraries.dateformatter.api)
        api(libs.datetime)

        testImplementation(libs.test.junit)
        testImplementation(libs.test.truth)
        testImplementation(projects.libraries.dateformatter.test)
    }
}
