// Copyright (c) Somni. All rights reserved.

import SwiftUI

struct LockIndicatorView: View {
    let progress: CGFloat
    var barWidth: CGFloat = 88
    var barHeight: CGFloat = 5
    var compact: Bool = false
    var iconSpacing: CGFloat = 0

    private static let spacing: CGFloat = 6
    private static let trackOpacity: CGFloat = 0.4
    private static let trackOpacityCompact: CGFloat = 0.35

    var body: some View {
        VStack(spacing: Self.spacing) {
            Image(systemName: "lock.fill")
                .font(compact ? .title3 : .title2)
                .foregroundStyle(.white)

            if iconSpacing > 0 {
                Spacer().frame(height: iconSpacing)
            }

            Text("Hold to unlock")
                .font(.caption2)
                .foregroundStyle(compact ? Color.secondary : Color.white.opacity(0.85))
            ZStack(alignment: .leading) {
                Capsule()
                    .fill(.white.opacity(compact ? Self.trackOpacityCompact : Self.trackOpacity))
                    .frame(width: barWidth, height: barHeight)
                Capsule()
                    .fill(.white)
                    .frame(width: barWidth * progress, height: barHeight)
            }
            .animation(.linear(duration: 0.1), value: progress)
        }
        .padding(.vertical, compact ? 6 : 8)
        .padding(.horizontal, compact ? 0 : 12)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Screen locked. Hold to unlock.")
        .accessibilityValue(progress >= 1 ? "Unlocking" : "\(Int(progress * 100)) percent")
    }
}

#Preview("Lock 0%") {
    ZStack {
        Color.black.ignoresSafeArea()
        LockIndicatorView(progress: 0)
    }
}

#Preview("Lock 60%") {
    ZStack {
        Color.black.ignoresSafeArea()
        LockIndicatorView(progress: 0.6)
    }
}
