// Copyright (c) Somni. All rights reserved.

import Foundation

/// Represents a sleep session for UI display (platform-agnostic).
struct SleepSessionDisplay: Identifiable {
    let id: String
    let babyId: String
    let startTime: Date
    var endTime: Date?
    var durationMinutes: Int? { durationMinutes(from: startTime, to: endTime ?? Date()) }
    let isActive: Bool

    var elapsedTimeInterval: TimeInterval {
        Date().timeIntervalSince(startTime)
    }

    private func durationMinutes(from start: Date, to end: Date) -> Int? {
        guard end >= start else { return nil }
        return Int(end.timeIntervalSince(start) / 60)
    }
}

/// Protocol for providing current sleep session state (real or mock for Previews).
protocol SleepSessionProviderProtocol: AnyObject {
    var activeSession: SleepSessionDisplay? { get }
    func startSession(babyId: String) async throws
    func endSession(sessionId: String) async throws
}
