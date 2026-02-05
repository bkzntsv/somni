// Copyright (c) Somni. All rights reserved.

import Foundation
import Combine

/// Protocol for heart rate monitoring (real HealthKit or mock for Previews).
protocol HeartRateMonitorProtocol: AnyObject {
    /// Current heart rate in BPM, or nil if not available.
    var currentHeartRate: Int? { get }
    /// Publisher for heart rate updates (e.g. every 5 seconds when monitoring).
    var heartRatePublisher: AnyPublisher<Int, Never> { get }
    /// LFO frequency in Hz derived from heart rate (80–150 Hz for 60–100 BPM).
    var lfoFrequency: Float { get }
    func requestAuthorization() async throws
    func startMonitoring() async
    func stopMonitoring()
}

extension HeartRateMonitorProtocol {
    /// Maps heart rate 60–100 BPM to LFO 80–150 Hz (design.md).
    static func mapHeartRateToLFO(_ heartRate: Int) -> Float {
        let clamped = min(100, max(60, heartRate))
        let normalized = Float(clamped - 60) / 40.0
        return 80.0 + normalized * 70.0
    }
}
