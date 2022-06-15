/*
 * Copyright (c) 2022 Touchlab
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package co.touchlab.kermit.bugsnag.internal

import co.touchlab.kermit.bugsnag.BugsnagStackframe
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.memScoped
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.objc.class_copyMethodList
import kotlin.math.min
import kotlinx.cinterop.alloc
import kotlinx.cinterop.plus
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.objc.method_getName
import platform.objc.sel_getName


@Serializable
data class CrashReport(val frames:List<StackFrame>)

@Serializable
data class StackFrame(
    val symbolAddress:Long?,
    val frameAddress:Long?,
    val machoVmAddress:Long?,
    val machoLoadAddress:Long?,
    val machoFile:String?,
    val machoUuid:String?,
    val function: String?
    )

object CrashStorage {
    fun writeCrashReport(t: Throwable){
        val report = crashReportFromThrowable(t)
        writeCrashReport(report)
    }

    fun readCrashes(): List<CrashReport> {
        val path = iosDirPath("crashes")
        val crashReports = try {
            val json = NSString.stringWithContentsOfFile("$path/acrash.json")
            listOf(Json.decodeFromString(CrashReport.serializer(), json as String))
        } catch (t:Throwable) {
            emptyList()
        }
        return crashReports
    }

    private fun crashReportFromThrowable(t:Throwable):CrashReport{
        val addresses = t.getStackTraceAddresses()
        val stackFrames = BugsnagStackframe.stackframesWithCallStackReturnAddresses(addresses.map { NSNumber(long = it) }) as List<BugsnagStackframe>
        val framesByFrameAddress = stackFrames.filter { it.frameAddress != null }.associateBy {it.frameAddress!!.longValue}
        val functions = t.getStackTrace()
        val arrayLength = min(addresses.size, functions.size)
        val frames = (0 until arrayLength).mapNotNull { index ->
            val frame = framesByFrameAddress.get(addresses[index])
            if (frame != null) {
                StackFrame(
                    symbolAddress = frame.symbolAddress?.longValue,
                    frameAddress = frame.frameAddress?.longValue,
                    machoVmAddress =  frame.machoVmAddress?.longValue,
                    machoLoadAddress =  frame.machoLoadAddress?.longValue,
                    machoFile = frame.machoFile,
                    machoUuid = frame.machoUuid,
                    functions[index].substring(59)
                )
            } else {
                null
            }
        }

        return CrashReport(frames)
    }

    private fun writeCrashReport(crashReport: CrashReport){
        val jsonData = Json.encodeToString(CrashReport.serializer(), crashReport)

        val jsonDataNs: NSString = jsonData as NSString
        val path = iosDirPath("crashes")
        jsonDataNs.writeToFile("$path/acrash.json", true)
    }

    private fun iosDirPath(folder:String):String{
        val paths = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true);
        val documentsDirectory = paths[0] as String;

        val databaseDirectory = "$documentsDirectory/$folder"

        val fileManager = NSFileManager.defaultManager()

        if (!fileManager.fileExistsAtPath(databaseDirectory))
            fileManager.createDirectoryAtPath(databaseDirectory, true, null, null); //Create folder

        return databaseDirectory
    }
}
