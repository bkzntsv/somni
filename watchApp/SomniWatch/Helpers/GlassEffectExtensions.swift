// Copyright (c) Somni. All rights reserved.

import SwiftUI

extension View {
    @ViewBuilder
    func glassedEffect(
        in shape: some Shape,
        interactive: Bool = false
    ) -> some View {
        if #available(watchOS 26.0, *) {
            if interactive {
                self.glassEffect(.regular.interactive(), in: shape)
            } else {
                self.glassEffect(.regular, in: shape)
            }
        } else {
            self.background {
                shape.glassed()
            }
        }
    }
}

extension Shape {
    func glassed() -> some View {
        ZStack {
            self.fill(.ultraThinMaterial)
            
            self.fill(
                .linearGradient(
                    colors: [
                        .primary.opacity(0.08),
                        .primary.opacity(0.05),
                        .primary.opacity(0.01),
                        .clear,
                        .clear,
                        .clear
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            
            self.stroke(.primary.opacity(0.2), lineWidth: 0.7)
        }
    }
}
