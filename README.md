# BlurView

A lightweight Android library for applying real-time blur effects to view backgrounds using native RenderScript Toolkit.

[![](https://jitpack.io/v/aspect-dev/BlurView.svg)](https://jitpack.io/#aspect-dev/BlurView)

## Features

- Real-time blur effect for view backgrounds
- Native C++ implementation with ARM NEON/AdvSIMD optimizations
- Customizable blur radius (1-25)
- Support for rounded corners
- Minimal API surface

## Installation

Add JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.aspect-dev:BlurView:1.0.0")
}
```

## Usage

### XML

```xml
<com.dbv.blurview.BlurLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:blurLayout_blurRadius="15"
    app:blurLayout_targetChildId="@id/blurredCard"
    app:blurLayout_targetChildBackgroundCornerRadius="16dp">

    <ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/background" />

    <androidx.cardview.widget.CardView
        android:id="@+id/blurredCard"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_gravity="center" />

</com.dbv.blurview.BlurLayout>
```

### XML Attributes

| Attribute | Description | Default |
|-----------|-------------|---------|
| `blurLayout_blurRadius` | Blur intensity (1-25) | Required |
| `blurLayout_targetChildId` | ID of the child view to blur | Single child |
| `blurLayout_targetChildBackgroundCornerRadius` | Corner radius for blurred background | 0dp |

### Programmatic

```kotlin
blurLayout.setBlurredBackgroundForTargetChild(
    targetChild = cardView,
    blurRadius = 15,
    targetChildBackgroundCornerRadius = 16f.dp
)
```

### Dynamic Blur Radius

Implement `BlurRadiusProvider` interface on your custom view:

```kotlin
class MyBlurredView : View, BlurLayout.BlurRadiusProvider {
    override fun provideBlurRadius(): Int = 20
}
```

### Dynamic Corner Radius

Implement `CornerRadiusProvider` interface:

```kotlin
class MyBlurredView : View, BlurLayout.CornerRadiusProvider {
    override fun provideCornerRadius(): Float = 24f
}
```

## How It Works

1. `BlurLayout` captures all children rendered behind the target view
2. The captured bitmap is processed through native blur algorithms
3. The blurred bitmap is set as the target view's background

## Requirements

- Min SDK: 24
- Target SDK: 36

## License

```
Copyright 2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Native RenderScript Toolkit code is from [Android Open Source Project](https://android.googlesource.com/platform/frameworks/rs/) under Apache 2.0 License.
