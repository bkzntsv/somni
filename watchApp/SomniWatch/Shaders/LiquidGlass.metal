// Copyright (c) Somni. All rights reserved.
// Liquid Glass / pulse shaders for watchOS. Add this file to the Watch App target in Xcode.

#include <metal_stdlib>
using namespace metal;

/// Искажение пространства «линза/капля» — синхронизация с пульсом.
/// position — координата пикселя, size — размер view в точках.
[[ stitchable ]] float2 liquidRefraction(
    float2 position,
    float amplitude,
    float frequency,
    float time,
    float2 size
) {
    float2 uv = position / size;
    float2 center = float2(0.5, 0.5);
    float dist = distance(uv, center);
    float wave = sin(dist * frequency - time) * amplitude;
    return position + (uv - center) * wave * 100.0;
}

/// Рябь для белого шума: волновое искажение от центра (center = size * 0.5).
[[ stitchable ]] float2 ripple(
    float2 position,
    float phase,
    float2 size
) {
    float2 center = size * 0.5;
    float2 uv = position - center;
    float dist = length(uv);
    float wave = sin(dist * 0.05 - phase) * 4.0;
    float2 dir = normalize(uv + 0.001);
    return position + dir * wave;
}
