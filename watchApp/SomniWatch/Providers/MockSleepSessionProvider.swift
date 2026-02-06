// Copyright (c) Somni. All rights reserved.

import Foundation

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

    func reset() {
        activeSession = nil
    }
}
