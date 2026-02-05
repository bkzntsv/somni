// Copyright (c) Somni. All rights reserved.

import Foundation

/// Mock sleep session provider for SwiftUI Previews without HealthKit or KMP.
@MainActor
@Observable
final class MockSleepSessionProvider: SleepSessionProviderProtocol, @unchecked Sendable {

    private(set) var activeSession: SleepSessionDisplay?

    func startSession(babyId: String) async throws {
        activeSession = SleepSessionDisplay(
            id: UUID().uuidString,
            babyId: babyId,
            startTime: Date(),
            endTime: nil,
            isActive: true
        )
    }

    func endSession(sessionId: String) async throws {
        guard activeSession?.id == sessionId else { return }
        activeSession = nil
    }

    /// Resets to no active session (for Previews that need "idle" state).
    func reset() {
        activeSession = nil
    }
}
