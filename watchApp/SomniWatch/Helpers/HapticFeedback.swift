// Copyright (c) Somni. All rights reserved.

import WatchKit

enum HapticFeedback {

    static func lightTap() {
        WKInterfaceDevice.current().play(.click)
    }

    static func success() {
        WKInterfaceDevice.current().play(.success)
    }

    static func start() {
        WKInterfaceDevice.current().play(.start)
    }

    static func stop() {
        WKInterfaceDevice.current().play(.stop)
    }

    static func notification() {
        WKInterfaceDevice.current().play(.notification)
    }
}
