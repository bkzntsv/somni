// Copyright (c) Somni. All rights reserved.

import SwiftUI

struct SleepStateView: View {
    @Binding var isSleeping: Bool

    var body: some View {
        ZStack {
            LinearGradient(
                colors: isSleeping
                    ? [.indigo.opacity(0.8), .black]
                    : [.orange.opacity(0.5), .black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            sleepStateCanvas
        }
        .animation(.spring(response: 0.6, dampingFraction: 0.8), value: isSleeping)
    }

    @ViewBuilder
    private var sleepStateCanvas: some View {
        GeometryReader { geo in
            let size = geo.size
            let center = CGPoint(x: size.width / 2, y: size.height / 2)
            ZStack {
                ForEach(0..<3, id: \.self) { index in
                    Circle()
                        .fill(
                            isSleeping
                                ? Color.indigo.opacity(0.25)
                                : Color.orange.opacity(0.2)
                        )
                        .frame(width: 60 + CGFloat(index) * 20, height: 60 + CGFloat(index) * 20)
                        .blur(radius: 15)
                        .position(center)
                }
            }
            .frame(width: size.width, height: size.height)
        }
    }
}

#Preview("Sleeping") {
    SleepStateView(isSleeping: .constant(true))
}

#Preview("Awake") {
    SleepStateView(isSleeping: .constant(false))
}
