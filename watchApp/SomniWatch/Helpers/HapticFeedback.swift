// Copyright (c) Somni. All rights reserved.

import WatchKit

enum HapticFeedback {

    /// Light tap for button taps and selections.
    static func lightTap() {
        WKInterfaceDevice.current().play(.click)
    }

    /// Success (e.g. session started/stopped).
    static func success() {
        WKInterfaceDevice.current().play(.success)
    }

    /// Start (e.g. begin tracking).
    static func start() {
        WKInterfaceDevice.current().play(.start)
    }

    /// Stop (e.g. end tracking).
    static func stop() {
        WKInterfaceDevice.current().play(.stop)
    }

    /// Notification (e.g. lock/unlock).
    static func notification() {
        WKInterfaceDevice.current().play(.notification)
    }
}
