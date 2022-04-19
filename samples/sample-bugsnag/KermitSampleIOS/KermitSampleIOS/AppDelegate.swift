//
//  AppDelegate.swift
//  KermitSampleIOS

// Copyright (c) 2021 Touchlab
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

import UIKit
import shared
import Bugsnag

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        let config = BugsnagConfiguration.loadConfig()
        
        config.addOnSendError { (event) -> Bool in
            
            event.addMetadata("Acme4 Co.", key:"name", section:"account")
            event.addMetadata(true, key:"paying_customer", section:"account")

            let crashes = HelperKt.readAllCrashes()
            
            if(event.errors[0].stacktrace.count > 8){
                let st = event.errors[0].stacktrace
                for bsframe in st {
                    print("bsframe \(bsframe.description)")
                }
                event.errors[0].stacktrace.removeSubrange(0...8)
            }
                
            let stackCount = event.errors[0].stacktrace.count
            
            if(!event.errors[0].stacktrace.isEmpty){
                let crashReport = crashes[0]
                for frame in crashReport.frames {
                    let bsframe = BugsnagStackframe()
//                    bsframe.symbolAddress = NSNumber(value: frame.address)
//                    bsframe.frameAddress = NSNumber(value: frame.address)
//                    bsframe.machoVmAddress = NSNumber(value: frame.address)
                    bsframe.machoLoadAddress = NSNumber(value: frame.address)
                    bsframe.method = frame.function
                    event.errors[0].stacktrace.append(bsframe)
//                    stacktrace.insert(bsframe, at: 0)
                    print("inserted \(bsframe.symbolAddress)")
                }
            }
            
            let newStackCound = event.errors[0].stacktrace.count
            print("stackCount \(stackCount) newStackCound \(newStackCound)")
            
//            if(event.errors[0].stacktrace.count > 8){
//                event.errors[0].stacktrace.removeSubrange(0...8)
//                event.errors[0].stacktrace.insert(BugsnagStackframe, at: <#T##Int#>)
//            }
            
            // Return `false` if you'd like to stop this error being reported
            return true
        }
        
        Bugsnag.start(with: config)
        
        let path = HelperKt.iosDirPath(folder: "crashes")
        print("Crash path \(path)")
        
//        Bugsnag.notify(NSException()) { (event) -> Bool in
//            event.addMetadata("Acme2 Co.", key:"name", section:"account")
//            event.addMetadata(true, key:"paying_customer", section:"account")
//
//            return true
//        }
        HelperKt.startKermit()
        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }
}

