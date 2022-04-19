/*
 * Copyright (c) 2021 Touchlab
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package co.touchlab.kermitsample

import co.touchlab.kermit.Logger
import co.touchlab.kermit.bugsnag.BugsnagLogWriter
import co.touchlab.kermit.bugsnag.internal.CrashReport
import co.touchlab.kermit.bugsnag.internal.CrashStorage
import co.touchlab.kermit.bugsnag.setupBugsnagExceptionHook
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

@Suppress("unused")
fun startKermit(){
    //You may want to us a non-global logger in production, but this will work fine.
    Logger.addLogWriter(BugsnagLogWriter())
    setupBugsnagExceptionHook(Logger)
}

fun readAllCrashes():List<CrashReport> = CrashStorage.readCrashes()
fun iosDirPath(folder:String):String{
    val paths = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true);
    val documentsDirectory = paths[0] as String;

    val databaseDirectory = "$documentsDirectory/$folder"

    val fileManager = NSFileManager.defaultManager()

    if (!fileManager.fileExistsAtPath(databaseDirectory))
        fileManager.createDirectoryAtPath(databaseDirectory, true, null, null); //Create folder

    return databaseDirectory
}