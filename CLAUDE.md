# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Project Overview

BlurView is an Android library that provides a `BlurLayout` container for applying real-time blur effects to view backgrounds. It uses a native RenderScript-style toolkit for high-performance image processing.

## Architecture

### Kotlin Layer (`app/src/main/java/com/dbv/cutstom/blurview/`)

- **BlurLayout.kt** - A FrameLayout container that captures views behind a target child, applies blur, and sets the result as the target's background. Key features:
  - `CornerRadiusProvider` and `BlurRadiusProvider` interfaces for custom views
  - XML attributes: `blurLayout_targetChildId`, `blurLayout_blurRadius`, `blurLayout_targetChildBackgroundCornerRadius`
  - Auto-initializes blur via `Toolkit.blur()` in `onAttachedToWindow`

- **Toolkit.kt** - Kotlin wrapper around the native RenderScript toolkit. Provides operations: blend, blur, colorMatrix, convolve, histogram, histogramDot, lut, lut3d, resize, yuvToRgb. Blur radius accepts values 1-25.

### Native Layer (`app/src/main/cpp/`)

C++ implementation of image processing operations with ARM NEON/AdvSIMD optimizations:

- **RenderScriptToolkit.cpp/.h** - Main toolkit class with thread pool management
- **TaskProcessor.cpp/.h** - Tiles operations across pool threads
- **JniEntryPoints.cpp** - JNI bridge between Kotlin and native code
- Architecture-specific assembly:
  - `*_neon.S` - ARM32 NEON implementations
  - `*_advsimd.S` - ARM64 Advanced SIMD implementations

### Build Configuration

- Min SDK: 24, Target SDK: 36
- NDK: 29.0.13846066 rc3
- JVM Target: 17
- Native library output: `renderscript-toolkit`

## Key Implementation Details

- BlurLayout works by drawing all children behind the target to a bitmap, applying blur via native code, then setting the blurred bitmap as the target's background
- The toolkit uses a thread pool for parallel processing (configurable thread count)
- Native code uses NEON intrinsics for ARM optimization; x86 support is partial