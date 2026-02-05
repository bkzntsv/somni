// Copyright (c) Somni. All rights reserved.

import SwiftUI

/// Main tracking interface: timer ring, breathing sphere, start/stop, touch lock (design §12, requirements 6, 19).
struct TrackerView: View {

    let sleepSessionProvider: SleepSessionProviderProtocol
    let heartRateMonitor: HeartRateMonitorProtocol
    let babyId: String

    @State private var isTouchLocked = true
    @State private var longPressProgress: CGFloat = 0
    @State private var unlockProgressTask: Task<Void, Never>?
    @State private var elapsedTimer: Timer?
    @State private var isScreenActive = true
    @State private var tick = 0

    private var currentSession: SleepSessionDisplay? {
        sleepSessionProvider.activeSession
    }

    private let minTouchSize: CGFloat = 44
    private let longPressDuration: TimeInterval = 2.0

    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()

            BreathingSphereView(
                lfoFrequency: heartRateMonitor.lfoFrequency,
                isScreenActive: isScreenActive
            )
            .opacity(hasActiveSession ? 0.9 : 0.6)

            VStack(spacing: 8) {
                if hasActiveSession {
                    TimerRingView(
                        elapsedSeconds: elapsedSeconds,
                        maxSeconds: nil
                    )
                    .frame(width: 100, height: 100)
                    .id(tick)

                    elapsedTimeLabel
                        .id(tick)
                        .accessibilityLabel("Elapsed time")
                        .accessibilityValue(elapsedTimeFormatted)

                    if isTouchLocked {
                        LockIndicatorView(progress: longPressProgress)
                            .transition(.opacity)
                            .accessibilityLabel("Screen locked")
                            .accessibilityHint("Hold for 2 seconds to unlock")
                    } else {
                        stopButton
                    }
                } else {
                    startButton
                }
            }
        }
        .onAppear {
            if currentSession != nil {
                isTouchLocked = true
                startElapsedTimer()
            }
        }
        .onChange(of: currentSession?.id) { _, new in
            if new != nil {
                isTouchLocked = true
                longPressProgress = 0
                startElapsedTimer()
            } else {
                stopElapsedTimer()
            }
        }
        .onLongPressGesture(minimumDuration: longPressDuration, pressing: { pressing in
            guard isTouchLocked, hasActiveSession else { return }
            if pressing {
                startUnlockProgress()
            } else {
                cancelUnlockProgress()
            }
        }, perform: {
            guard isTouchLocked else { return }
            unlockAndHaptic()
        })
    }

    private var hasActiveSession: Bool { currentSession != nil }

    private var elapsedSeconds: TimeInterval {
        guard let session = currentSession else { return 0 }
        return session.elapsedTimeInterval
    }

    private var elapsedTimeLabel: some View {
        Text(elapsedTimeFormatted)
            .font(.system(.title2, design: .rounded).monospacedDigit())
            .foregroundStyle(.white.opacity(0.9))
    }

    private var elapsedTimeFormatted: String {
        let total = Int(elapsedSeconds)
        let h = total / 3600
        let m = (total % 3600) / 60
        let s = total % 60
        if h > 0 {
            return String(format: "%d:%02d:%02d", h, m, s)
        }
        return String(format: "%d:%02d", m, s)
    }

    private var startButton: some View {
        Button {
            triggerHapticAndStart()
        } label: {
            Text("Start sleep")
                .font(.headline)
                .frame(minWidth: minTouchSize, minHeight: minTouchSize)
        }
        .buttonStyle(.borderedProminent)
        .tint(.blue)
        .accessibilityLabel("Start sleep session")
        .accessibilityHint("Starts tracking your baby's sleep")
    }

    private var stopButton: some View {
        Button {
            triggerHapticAndStop()
        } label: {
            Text("Stop")
                .font(.headline)
                .frame(minWidth: minTouchSize, minHeight: minTouchSize)
        }
        .buttonStyle(.bordered)
        .tint(.red)
        .accessibilityLabel("Stop sleep session")
        .accessibilityHint("Ends the current sleep tracking")
    }

    private func startUnlockProgress() {
        cancelUnlockProgress()
        let start = Date()
        unlockProgressTask = Task { @MainActor in
            while !Task.isCancelled {
                let elapsed = Date().timeIntervalSince(start)
                if elapsed >= longPressDuration {
                    longPressProgress = 1.0
                    return
                }
                longPressProgress = CGFloat(elapsed / longPressDuration)
                try? await Task.sleep(nanoseconds: 50_000_000)
            }
        }
    }

    private func cancelUnlockProgress() {
        unlockProgressTask?.cancel()
        unlockProgressTask = nil
        longPressProgress = 0
    }

    private func unlockAndHaptic() {
        isTouchLocked = false
        HapticFeedback.notification()
    }

    private func triggerHapticAndStart() {
        HapticFeedback.start()
        Task { @MainActor in
            try? await sleepSessionProvider.startSession(babyId: babyId)
        }
    }

    private func triggerHapticAndStop() {
        guard let session = currentSession else { return }
        HapticFeedback.stop()
        Task { @MainActor in
            try? await sleepSessionProvider.endSession(sessionId: session.id)
            isTouchLocked = true
        }
    }

    private func startElapsedTimer() {
        stopElapsedTimer()
        elapsedTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            guard sleepSessionProvider.activeSession != nil else { return }
            tick += 1
        }
        RunLoop.main.add(elapsedTimer!, forMode: .common)
    }

    private func stopElapsedTimer() {
        elapsedTimer?.invalidate()
        elapsedTimer = nil
    }
}

// MARK: - Previews

#Preview("Tracker idle") {
    TrackerView(
        sleepSessionProvider: MockSleepSessionProvider(),
        heartRateMonitor: MockHeartRateMonitor(mockBPM: 72),
        babyId: "preview-baby"
    )
}

#Preview("Tracker active") {
    let provider = MockSleepSessionProvider()
    return TrackerView(
        sleepSessionProvider: provider,
        heartRateMonitor: MockHeartRateMonitor(mockBPM: 68),
        babyId: "preview-baby"
    )
    .task {
        try? await provider.startSession(babyId: "preview-baby")
    }
}
