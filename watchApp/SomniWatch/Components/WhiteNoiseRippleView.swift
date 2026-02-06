// Copyright (c) Somni. All rights reserved.

import SwiftUI

struct WhiteNoiseRippleView: View {
    var isActive: Bool = true

    @State private var phase: CGFloat = 0

    var body: some View {
        if #available(watchOS 10.0, *) {
            rippleWithShader
        } else {
            rippleFallback
        }
    }

    @ViewBuilder
    private var rippleWithShader: some View {
        TimelineView(.animation) { timeline in
            GeometryReader { proxy in
                Rectangle()
                    .fill(.ultraThinMaterial)
                    .visualEffect { content, _ in
                        content
                            .distortionEffect(
                                ShaderLibrary.ripple(
                                    .float(phase),
                                    .float2(proxy.size.width, proxy.size.height)
                                ),
                                maxSampleOffset: CGSize(width: 8, height: 8)
                            )
                    }
            }
        }
        .onAppear { startRippleAnimation() }
        .onChange(of: isActive) { _, active in
            if active { startRippleAnimation() }
        }
    }

    private var rippleFallback: some View {
        Rectangle()
            .fill(.ultraThinMaterial)
            .overlay {
                Circle()
                    .stroke(.white.opacity(0.3), lineWidth: 1)
                    .scaleEffect(1.0 + phase * 0.1)
                    .blur(radius: 2)
            }
            .onAppear { startRippleAnimation() }
            .onChange(of: isActive) { _, active in
                if active { startRippleAnimation() }
            }
    }

    private func startRippleAnimation() {
        guard isActive else { return }
        withAnimation(.linear(duration: 2).repeatForever(autoreverses: false)) {
            phase = .pi * 2
        }
    }
}

#Preview("Ripple active") {
    ZStack {
        Color.black.ignoresSafeArea()
        WhiteNoiseRippleView(isActive: true)
            .frame(width: 120, height: 120)
    }
}
