// Copyright (c) Somni. All rights reserved.

import SwiftUI

struct BabyPulseView: View {
    var pulseAmplitude: CGFloat = 0.05
    var label: String = "👶"
    var isActive: Bool = true

    @State private var startTime = Date.now
    @State private var pulsate = false

    var body: some View {
        if #available(watchOS 10.0, *) {
            pulseWithShader
        } else {
            pulseFallback
        }
    }

    @ViewBuilder
    private var pulseWithShader: some View {
        TimelineView(.animation) { timeline in
            let time = timeline.date.timeIntervalSince(startTime)
            ZStack {
                LinearGradient(
                    colors: [.indigo, .black],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()

                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 140, height: 140)
                    .distortionEffect(
                        ShaderLibrary.liquidRefraction(
                            .float(pulseAmplitude),
                            .float(15.0),
                            .float(time * 2),
                            .float2(140, 140)
                        ),
                        maxSampleOffset: CGSize(width: 20, height: 20)
                    )
                    .overlay {
                        Text(label)
                            .font(.system(size: 50))
                            .scaleEffect(1.0 + pulseAmplitude)
                    }
            }
        }
        .onAppear { if isActive { startTime = Date.now } }
    }

    private var pulseFallback: some View {
        ZStack {
            LinearGradient(
                colors: [.indigo, .black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            Circle()
                .fill(.ultraThinMaterial)
                .frame(width: 140, height: 140)
                .overlay {
                    Text(label)
                        .font(.system(size: 50))
                }
                .scaleEffect(pulsate ? 1.0 + pulseAmplitude : 1.0)
                .animation(
                    .easeInOut(duration: 0.6).repeatForever(autoreverses: true),
                    value: pulsate
                )
        }
        .onAppear {
            if isActive { pulsate = true }
        }
        .onChange(of: isActive) { _, active in
            pulsate = active
        }
    }
}

#Preview("Baby pulse calm") {
    BabyPulseView(pulseAmplitude: 0.05, isActive: true)
}

#Preview("Baby pulse fallback") {
    BabyPulseView(pulseAmplitude: 0.05, isActive: true)
}
