// Copyright (c) Somni. All rights reserved.

import SwiftUI

/// Root content: Tracker with mock providers. Replace with real KMP/HealthKit when integrating.
struct ContentView: View {

    private let sleepProvider = MockSleepSessionProvider()
    private let heartRateMonitor = MockHeartRateMonitor(mockBPM: 72)
    private let babyId = "default-baby"

    var body: some View {
        TrackerView(
            sleepSessionProvider: sleepProvider,
            heartRateMonitor: heartRateMonitor,
            babyId: babyId
        )
    }
}

#Preview("Content") {
    ContentView()
}

// Test on device/simulator: 41mm, 45mm, Ultra (task 1.4.8)
// Use Scheme → Run on Apple Watch Series 9 (41mm) / (45mm) / Ultra 2
