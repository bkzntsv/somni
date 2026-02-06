// Copyright (c) Somni. All rights reserved.

import SwiftUI

struct TimerRingView: View {
    let elapsedSeconds: TimeInterval
    let maxSeconds: TimeInterval?
    var lineWidth: CGFloat = 6
    var size: CGFloat = 120
    var showOuterGlow: Bool = true

    private static let defaultCapSeconds: TimeInterval = 3600
    private static let progressAnimationDuration: Double = 0.5
    private static let progressShadowRadius: CGFloat = 4
    private static let progressShadowOpacity: Double = 0.4
    private static let glowBlurRadius: CGFloat = 24
    private static let glowPulseDuration: Double = 6
    private static let glowScaleRange: (CGFloat, CGFloat) = (1, 1.03)
    private static let glowOpacityRange: (Double, Double) = (0.4, 0.7)

    private var progress: CGFloat {
        guard let max = maxSeconds, max > 0 else {
            return min(1.0, elapsedSeconds / Self.defaultCapSeconds)
        }
        return min(1.0, CGFloat(elapsedSeconds / max))
    }

    private var progressGradient: AngularGradient {
        AngularGradient(
            colors: [
                Color(red: 0.65, green: 0.55, blue: 0.98),
                Color(red: 0.55, green: 0.36, blue: 0.96),
                Color(red: 0.38, green: 0.40, blue: 0.94),
            ],
            center: .center
        )
    }

    private var backgroundGradient: AngularGradient {
        AngularGradient(
            colors: [
                .white.opacity(0.2),
                .white.opacity(0.08),
                .white.opacity(0.2),
            ],
            center: .center
        )
    }

    private var outerGlowView: some View {
        Circle()
            .fill(
                RadialGradient(
                    colors: [
                        .clear,
                        Color(red: 79/255, green: 70/255, blue: 229/255).opacity(0.3),
                        .clear,
                    ],
                    center: .center,
                    startRadius: size * 0.27,
                    endRadius: size * 0.5
                )
            )
            .frame(width: size, height: size)
            .blur(radius: Self.glowBlurRadius)
            .scaleEffect(glowScale)
            .opacity(glowOpacity)
    }

    @State private var glowPhase = false

    private var glowScale: CGFloat {
        glowPhase ? Self.glowScaleRange.1 : Self.glowScaleRange.0
    }

    private var glowOpacity: Double {
        glowPhase ? Self.glowOpacityRange.1 : Self.glowOpacityRange.0
    }

    var body: some View {
        ZStack {
            if showOuterGlow {
                outerGlowView
            }

            Circle()
                .stroke(backgroundGradient, lineWidth: lineWidth)
                .frame(width: size, height: size)

            Circle()
                .trim(from: 0, to: progress)
                .stroke(
                    progressGradient,
                    style: StrokeStyle(lineWidth: lineWidth, lineCap: .round)
                )
                .frame(width: size, height: size)
                .rotationEffect(.degrees(-90))
                .shadow(color: .indigo.opacity(Self.progressShadowOpacity), radius: Self.progressShadowRadius)
                .animation(.easeInOut(duration: Self.progressAnimationDuration), value: progress)
        }
        .onAppear {
            guard showOuterGlow else { return }
            withAnimation(
                .easeInOut(duration: Self.glowPulseDuration / 2)
                .repeatForever(autoreverses: true)
            ) {
                glowPhase = true
            }
        }
    }
}

#Preview("Timer ring 5 min") {
    ZStack {
        Color.black.ignoresSafeArea()
        TimerRingView(elapsedSeconds: 5 * 60, maxSeconds: 60 * 60)
    }
}

#Preview("Timer ring 45 min") {
    ZStack {
        Color.black.ignoresSafeArea()
        TimerRingView(elapsedSeconds: 45 * 60, maxSeconds: 60 * 60)
    }
}
