// Copyright (c) Somni. All rights reserved.

import Foundation
import Combine

final class MockHeartRateMonitor: HeartRateMonitorProtocol {

    private static let updateInterval: TimeInterval = 5.0
    private static let minBPM = 40
    private static let maxBPM = 200

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
        self.mockBPM = min(Self.maxBPM, max(Self.minBPM, mockBPM))
    }

    func requestAuthorization() async throws {}

    func startMonitoring() async {
        subject.send(mockBPM)
        await MainActor.run {
            timer = Timer.scheduledTimer(withTimeInterval: Self.updateInterval, repeats: true) { [weak self, mockBPM] _ in
                self?.subject.send(mockBPM)
            }
            if let timer = timer {
                RunLoop.current.add(timer, forMode: .common)
            }
        }
    }

    func stopMonitoring() {
        timer?.invalidate()
        timer = nil
    }
}
