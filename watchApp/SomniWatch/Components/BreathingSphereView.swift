// Copyright (c) Somni. All rights reserved.

import SwiftUI

/// Pulsating sphere synchronized with LFO (heart rate). Dark-mode optimized (design §12, requirements 19.1–19.2).
struct BreathingSphereView: View {

    let lfoFrequency: Float
    let isScreenActive: Bool

    private var animationDuration: Double {
        guard lfoFrequency > 0 else { return 1.0 }
        return 1.0 / Double(lfoFrequency)
    }

    @State private var scale: CGFloat = 1.0
    @State private var brightness: Double = 0.5
    @State private var isAnimating = false

    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()

            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            Color.blue.opacity(brightness),
                            Color.purple.opacity(brightness * 0.5),
                        ],
                        center: .center,
                        startRadius: 0,
                        endRadius: 80
                    )
                )
                .frame(width: 80, height: 80)
                .scaleEffect(scale)
                .animation(
                    .easeInOut(duration: animationDuration).repeatForever(autoreverses: true),
                    value: scale
                )
        }
        .onAppear { startBreathingAnimation() }
        .onChange(of: isScreenActive) { _, active in
            if active {
                startBreathingAnimation()
            } else {
                stopBreathingAnimation()
            }
        }
    }

    private func startBreathingAnimation() {
        guard isScreenActive else { return }
        isAnimating = true
        scale = 1.1
        brightness = 0.7
    }

    private func stopBreathingAnimation() {
        isAnimating = false
        scale = 1.0
        brightness = 0.5
    }
}

// MARK: - Previews

#Preview("Breathing sphere active") {
    BreathingSphereView(lfoFrequency: 100, isScreenActive: true)
}

#Preview("Breathing sphere paused") {
    BreathingSphereView(lfoFrequency: 100, isScreenActive: false)
}
