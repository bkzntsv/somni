import SwiftUI

struct TrackerView: View {
    let sleepSessionProvider: SleepSessionProviderProtocol
    let heartRateMonitor: HeartRateMonitorProtocol
    let babyId: String

    @State private var isTouchLocked = true
    @State private var longPressProgress: CGFloat = 0
    @State private var unlockProgressTask: Task<Void, Never>?
    @State private var isScreenActive = true

    private var currentSession: SleepSessionDisplay? {
        sleepSessionProvider.activeSession
    }

    private let longPressDuration: TimeInterval = 2.0
    private static let unlockProgressUpdateIntervalNs: UInt64 = 16_000_000
    private static let timerRingSize: CGFloat = 120
    private static let minTouchTarget: CGFloat = 44
    private static let backgroundSphereDiameterFactor: CGFloat = 1.25
    private static let sphereOpacityActive: Double = 0.6
    private static let sphereOpacityIdle: Double = 1.0
    private static let backgroundOpacity: Double = 0.5
    private static let bottomPadding: CGFloat = 8
    private static let startButtonHorizontalPadding: CGFloat = 24
    private static let secondsPerHour = 3600
    private static let secondsPerMinute = 60
    private static let lockBarWidthCompact: CGFloat = 72
    private static let lockBarHeightCompact: CGFloat = 4
    private static let lockIconExtraSpacing: CGFloat = 14
    private static let stopButtonHeight: CGFloat = 16
    private static let stopButtonHorizontalPadding: CGFloat = 28
    private static let stopButtonBottomPadding: CGFloat = 24

    var body: some View {
        TimelineView(.periodic(from: .now, by: 1.0)) { _ in
            ZStack {
                Color.black
                SleepStateView(isSleeping: Binding.constant(hasActiveSession))
                    .opacity(Self.backgroundOpacity)
                GeometryReader { geometry in
                    let diameter = max(geometry.size.width, geometry.size.height) * Self.backgroundSphereDiameterFactor
                    ZStack {
                        BreathingSphereView(
                            lfoFrequency: heartRateMonitor.lfoFrequency,
                            isScreenActive: isScreenActive,
                            useLiquidStyle: true,
                            size: diameter
                        )
                        .frame(width: diameter, height: diameter)
                    }
                    .frame(width: geometry.size.width, height: geometry.size.height)
                }
                .opacity(hasActiveSession ? Self.sphereOpacityActive : Self.sphereOpacityIdle)
                .ignoresSafeArea()

                if hasActiveSession {
                    ZStack {
                        TimerRingView(elapsedSeconds: elapsedSeconds, maxSeconds: nil)
                            .frame(width: Self.timerRingSize, height: Self.timerRingSize)
                        elapsedTimeLabel
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .ignoresSafeArea()
                }

                if !hasActiveSession {
                    ZStack {
                        startButton
                            .padding(.horizontal, Self.startButtonHorizontalPadding)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }

                if hasActiveSession {
                    VStack {
                        Spacer(minLength: 0)
                        if isTouchLocked {
                            LockIndicatorView(
                                progress: longPressProgress,
                                barWidth: Self.lockBarWidthCompact,
                                barHeight: Self.lockBarHeightCompact,
                                compact: true,
                                iconSpacing: Self.lockIconExtraSpacing
                            )
                            .padding(.bottom, Self.bottomPadding)
                            .transition(.opacity.animation(.easeInOut))
                        } else {
                            stopButton
                                .padding(.horizontal, Self.stopButtonHorizontalPadding)
                                .padding(.bottom, Self.stopButtonBottomPadding)
                                .transition(.move(edge: .bottom).combined(with: .opacity))
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .ignoresSafeArea()
        }
        .onAppear {
            if currentSession != nil {
                isTouchLocked = true
            }
        }
        .onChange(of: currentSession?.id) { _, new in
            if new != nil {
                isTouchLocked = true
                longPressProgress = 0
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
            .font(.system(.title, design: .rounded).weight(.bold))
            .monospacedDigit()
            .foregroundStyle(.white)
            .shadow(color: .black.opacity(0.5), radius: 2, x: 0, y: 1)
            .shadow(color: Color(red: 139/255, green: 92/255, blue: 246/255).opacity(0.3), radius: 8)
            .shadow(color: Color(red: 79/255, green: 70/255, blue: 229/255).opacity(0.2), radius: 16)
    }

    private var elapsedTimeFormatted: String {
        let total = Int(elapsedSeconds)
        let h = total / Self.secondsPerHour
        let m = (total % Self.secondsPerHour) / Self.secondsPerMinute
        let s = total % Self.secondsPerMinute
        if h > 0 {
            return String(format: "%d:%02d:%02d", h, m, s)
        }
        return String(format: "%02d:%02d", m, s)
    }

    private var startButton: some View {
        Button {
            triggerHapticAndStart()
        } label: {
            Image(systemName: "moon.zzz.fill")
                .font(.system(size: 28))
        }
        .buttonStyle(.borderedProminent)
        .tint(.blue)
        .frame(minWidth: Self.minTouchTarget, minHeight: Self.minTouchTarget)
        .accessibilityLabel("Start sleep")
    }

    private var stopButton: some View {
        Button {
            triggerHapticAndStop()
        } label: {
            Text("Stop Sleep")
                .font(.system(size: 14, weight: .semibold))
        }
        .buttonStyle(.borderedProminent)
        .tint(.red)
        .frame(height: Self.stopButtonHeight)
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
                try? await Task.sleep(nanoseconds: Self.unlockProgressUpdateIntervalNs)
            }
        }
    }

    private func cancelUnlockProgress() {
        unlockProgressTask?.cancel()
        unlockProgressTask = nil
        withAnimation(.easeOut(duration: 0.2)) {
            longPressProgress = 0
        }
    }

    private func unlockAndHaptic() {
        withAnimation {
            isTouchLocked = false
        }
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
            withAnimation {
                isTouchLocked = true
            }
        }
    }
}

#Preview("Tracker locked") {
    let provider = MockSleepSessionProvider()
    return TrackerView(
        sleepSessionProvider: provider,
        heartRateMonitor: MockHeartRateMonitor(mockBPM: 72),
        babyId: "baby"
    )
    .task {
        try? await provider.startSession(babyId: "baby")
    }
}

#Preview("Tracker idle") {
    TrackerView(
        sleepSessionProvider: MockSleepSessionProvider(),
        heartRateMonitor: MockHeartRateMonitor(mockBPM: 72),
        babyId: "baby"
    )
}