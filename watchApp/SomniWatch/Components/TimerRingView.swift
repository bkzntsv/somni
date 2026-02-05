// Copyright (c) Somni. All rights reserved.

import SwiftUI

/// Circular progress ring for elapsed session time. Arc-based, dark-mode friendly.
struct TimerRingView: View {

    /// Elapsed time in seconds.
    let elapsedSeconds: TimeInterval
    /// Optional max duration for ring fill (e.g. 24h). If nil, ring shows elapsed only (no full circle).
    let maxSeconds: TimeInterval?
    /// Line width of the ring.
    var lineWidth: CGFloat = 6
    /// Size of the view (ring diameter).
    var size: CGFloat = 120

    private var progress: CGFloat {
        guard let max = maxSeconds, max > 0 else {
            return min(1.0, elapsedSeconds / 3600) // default: cap at 1h for display
        }
        return min(1.0, CGFloat(elapsedSeconds / max))
    }

    var body: some View {
        ZStack {
            // Background ring
            Circle()
                .stroke(Color.white.opacity(0.2), lineWidth: lineWidth)
                .frame(width: size, height: size)

            // Progress arc
            Circle()
                .trim(from: 0, to: progress)
                .stroke(
                    Color.blue.opacity(0.9),
                    style: StrokeStyle(lineWidth: lineWidth, lineCap: .round)
                )
                .frame(width: size, height: size)
                .rotationEffect(.degrees(-90))
                .animation(.easeInOut(duration: 0.5), value: progress)
        }
    }
}

// MARK: - Previews

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
