// Copyright (c) Somni. All rights reserved.

import SwiftUI

/// Touch lock indicator: lock icon, "Hold to unlock", and progress (design §12, requirement 19.6).
struct LockIndicatorView: View {

    /// 0...1 for long-press progress.
    let progress: CGFloat

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: "lock.fill")
                .font(.title2)
                .foregroundStyle(.white.opacity(0.8))

            Text("Hold to unlock")
                .font(.caption2)
                .foregroundStyle(.white.opacity(0.6))

            ProgressView(value: progress, total: 1.0)
                .progressViewStyle(.linear)
                .frame(width: 80)
                .tint(.white.opacity(0.8))
        }
        .padding()
    }
}

// MARK: - Previews

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
