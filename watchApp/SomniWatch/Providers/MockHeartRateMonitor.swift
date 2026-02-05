// Copyright (c) Somni. All rights reserved.

import Foundation
import Combine

/// Mock heart rate monitor for SwiftUI Previews without HealthKit.
final class MockHeartRateMonitor: HeartRateMonitorProtocol {

    private let subject = PassthroughSubject<Int, Never>()
    private var timer: Timer?
    private let mockBPM: Int

    var currentHeartRate: Int? { mockBPM }
    var heartRatePublisher: AnyPublisher<Int, Never> { subject.eraseToAnyPublisher() }

    var lfoFrequency: Float {
        guard let bpm = currentHeartRate else { return 100 }
        return Self.mapHeartRateToLFO(bpm)
    }

    init(mockBPM: Int = 72) {
        self.mockBPM = min(200, max(40, mockBPM))
    }

    func requestAuthorization() async throws {
        // No-op for mock
    }

    func startMonitoring() async {
        subject.send(mockBPM)
        timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self, mockBPM] _ in
            self?.subject.send(mockBPM)
        }
        RunLoop.current.add(timer!, forMode: .common)
    }

    func stopMonitoring() {
        timer?.invalidate()
        timer = nil
    }
}
