// Copyright (c) Somni. All rights reserved.

import SwiftUI

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
