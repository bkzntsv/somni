// Copyright (c) Somni. All rights reserved.

import SwiftUI

extension Shape {
    func glassed() -> some View {
        self
            .fill(.thinMaterial)
            .overlay {
                self.fill(
                    .linearGradient(
                        colors: [
                            .white.opacity(0.15),
                            .white.opacity(0.10),
                            .white.opacity(0.05),
                            .clear,
                            .clear,
                            .clear
                        ],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
            }
            .overlay {
                self.stroke(.white.opacity(0.35), lineWidth: 1.2)
            }
    }
}

struct BreathingSphereView: View {
    let lfoFrequency: Float
    let isScreenActive: Bool
    var useLiquidStyle: Bool = true
    var size: CGFloat? = nil

    private static let defaultSphereDiameter: CGFloat = 80
    private static let brightness: Double = 0.55
    private let visualEffectBlurMax: CGFloat = 2
    private let visualEffectHueSpeed: Double = 10

    private var effectiveDiameter: CGFloat { size ?? Self.defaultSphereDiameter }

    @State private var shaderStartTime = Date()

    var body: some View {
        Group {
            if useLiquidStyle {
                sphereWithVisualEffect
            } else {
                sphereWithoutShader
            }
        }
        .onAppear { shaderStartTime = Date() }
    }

    private var sphereWithoutShader: some View {
        ZStack {
            Color.black
                .ignoresSafeArea()
            mainCircle
        }
    }

    @ViewBuilder
    private var sphereWithVisualEffect: some View {
        TimelineView(.animation) { timeline in
            let time = timeline.date.timeIntervalSince(shaderStartTime)

            ZStack {
                Color.black
                    .ignoresSafeArea()
                mainCircle
                    .visualEffect { content, _ in
                        content
                            .hueRotation(.degrees(time * visualEffectHueSpeed))
                            .blur(radius: abs(CGFloat(sin(time))) * visualEffectBlurMax)
                    }
            }
        }
    }

    @ViewBuilder
    private var mainCircle: some View {
        if useLiquidStyle {
            if #available(watchOS 26.0, *) {
                circleBase
                    .glassEffect(.regular.interactive(), in: Circle())
            } else {
                circleBase
                    .background {
                        Circle().glassed()
                    }
            }
        } else {
            circleBase
        }
    }
    
    private var circleBase: some View {
        Circle()
            .fill(
                RadialGradient(
                    colors: [
                        Color.blue.opacity(Self.brightness),
                        Color.purple.opacity(Self.brightness * 0.5),
                    ],
                    center: .center,
                    startRadius: 0,
                    endRadius: effectiveDiameter / 2
                )
            )
            .frame(width: effectiveDiameter, height: effectiveDiameter)
    }
}

#Preview("Breathing sphere active") {
    BreathingSphereView(lfoFrequency: 100, isScreenActive: true)
}

#Preview("Breathing sphere paused") {
    BreathingSphereView(lfoFrequency: 100, isScreenActive: false)
}

#Preview("Breathing background") {
    BreathingSphereView(lfoFrequency: 100, isScreenActive: true, useLiquidStyle: true, size: 200)
}
