// Copyright (c) Somni. All rights reserved.

import SwiftUI

struct HeartbeatView: View {
    var pulseInterval: Double = 0.8
    var isActive: Bool = true

    @State private var pulsate = false

    var body: some View {
        ZStack {
            Circle()
                .fill(.pink.opacity(0.3))
                .blur(radius: 20)
                .scaleEffect(pulsate ? 1.2 : 0.8)

            Circle()
                .stroke(
                    LinearGradient(
                        colors: [.white, .clear],
                        startPoint: .top,
                        endPoint: .bottom
                    ),
                    lineWidth: 2
                )
                .scaleEffect(pulsate ? 1.1 : 0.9)
        }
        .animation(
            .timingCurve(0.4, 0, 0.2, 1, duration: pulseInterval)
            .repeatForever(autoreverses: true),
            value: pulsate
        )
        .onAppear { if isActive { pulsate = true } }
        .onChange(of: isActive) { _, active in
            pulsate = active
        }
    }
}

#Preview("Heartbeat active") {
    ZStack {
        Color.black.ignoresSafeArea()
        HeartbeatView(pulseInterval: 0.8, isActive: true)
            .frame(width: 100, height: 100)
    }
}

#Preview("Heartbeat paused") {
    ZStack {
        Color.black.ignoresSafeArea()
        HeartbeatView(pulseInterval: 0.8, isActive: false)
            .frame(width: 100, height: 100)
    }
}
