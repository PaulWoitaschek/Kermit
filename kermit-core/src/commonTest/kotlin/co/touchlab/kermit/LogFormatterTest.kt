/*
 * Copyright (c) 2022 Touchlab
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package co.touchlab.kermit

import kotlin.test.Test
import kotlin.test.assertEquals

class LogFormatterTest {
    @Test
    fun defaultFormatter() {
        val testLogWriter = TestStringLogWriter(DefaultLogFormatter)
        testLogWriter.log(Severity.Info, "A Log Message", "Some Tag")
        testLogWriter.log(Severity.Warn, "A Log Message", "")
        assertEquals(testLogWriter.logList[0], "Info: (Some Tag) A Log Message")
        assertEquals(testLogWriter.logList[1], "Warn: A Log Message")
    }

    @Test
    fun noTagFormatter() {
        val testLogWriter = TestStringLogWriter(NoTagLogFormatter)
        testLogWriter.log(Severity.Info, "A Log Message", "Some Tag")
        testLogWriter.log(Severity.Warn, "A Log Message", "")
        assertEquals(testLogWriter.logList[0], "Info: A Log Message")
        assertEquals(testLogWriter.logList[1], "Warn: A Log Message")
    }

    @Test
    fun simpleFormatter() {
        val testLogWriter = TestStringLogWriter(SimpleLogFormatter)
        testLogWriter.log(Severity.Info, "A Log Message", "Some Tag")
        testLogWriter.log(Severity.Warn, "A Log Message", "")
        assertEquals(testLogWriter.logList[0], "A Log Message")
        assertEquals(testLogWriter.logList[1], "A Log Message")
    }
}

internal class TestStringLogWriter(private val logFormatter: LogFormatter) : LogWriter() {
    val logList = mutableListOf<String>()

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        logList.add(logFormatter.formatMessage(severity, message, tag))
    }
}