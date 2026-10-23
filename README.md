# 🎨 FrameForge — Cinematic 2D Animation Studio for Android Tablets

<p align="center">
  <img src="app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml" width="128" height="128" alt="FrameForge Logo" />
</p>

<p align="center">
  <b>A pro-grade, edge-to-edge full-screen 2D animation powerhouse engineered specifically for Android tablets and active styluses (Samsung S-Pen, Lenovo Precision Pen, Xiaomi Smart Pen).</b>
</p>

<p align="center">
  <a href="https://github.com/RengokuSensei/2d-animation-tab-test/actions"><img src="https://img.shields.io/github/actions/workflow/status/RengokuSensei/2d-animation-tab-test/build-apk.yml?branch=main&style=flat-square&label=Build%20APK" alt="Build Status"></a>
  <a href="https://github.com/RengokuSensei/2d-animation-tab-test/releases/latest"><img src="https://img.shields.io/github/v/release/RengokuSensei/2d-animation-tab-test?style=flat-square&color=blue&label=Latest%20APK" alt="Latest Release"></a>
  <img src="https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024--36)-green?style=flat-square" alt="Platform">
  <img src="https://img.shields.io/badge/Kotlin-100%25-orange?style=flat-square" alt="Kotlin">
  <img src="https://img.shields.io/badge/Stylus-144Hz%20Coalesced-purple?style=flat-square" alt="Stylus Rate">
</p>

---

## 📥 Download & Install APK

You can download the ready-to-install Android APK directly from the **[GitHub Releases Page](https://github.com/RengokuSensei/2d-animation-tab-test/releases/latest)**:

1. **Download**: Grab [`app-debug.apk`](https://github.com/RengokuSensei/2d-animation-tab-test/releases/latest/download/app-debug.apk).
2. **Transfer or Open**: Open the `.apk` file directly on your Android tablet.
3. **Allow Install**: If prompted by Android, enable *"Install unknown apps"* for your browser or file manager.
4. **Launch**: Open **FrameForge** and enjoy an edge-to-edge drawing canvas with zero input latency.

---

## 🌟 The 7 Cinema Systems

FrameForge bridges the gap between freehand tablet sketching (like *Procreate*) and professional non-linear animation editors (like *Procreate Dreams*, *Toon Boom Harmony*, and *Blender Grease Pencil*).

```
   ┌─────────────────────────────────────────────────────────────┐
   │                   FRAMEFORGE STUDIO CANVAS                  │
   │  [⚡ Auto: 600ms]      [☀️ Light Gizmo]      [🧅 Light Table]│
   │                                                             │
   │            (144Hz Coalesced Pressure + Tilt Ink)            │
   │                                                             │
   │   Layer 3 (Foreground)   ────────  Z = +200px (Multiplane)  │
   │   Layer 2 (Character)    ────────  Z = 0px    (Hero Plane)  │
   │   Layer 1 (Background)   ────────  Z = -400px (Parallax)    │
   └─────────────────────────────────────────────────────────────┘
   │  [✂️ Split] [⏳ Stretch] [🧲 Snap] [📈 Graph]  00:04.12 [Play]│
   │  ├──[Clip 1: Intro]──┼──[Clip 2: Jump]──┼──[Clip 3: Land]───┤
   └─────────────────────────────────────────────────────────────┘
```

### 1. 🎬 Precision NLE Timeline Editing Studio
- **Stylus Razor Split Gesture**: Flick or swipe vertically downward across any clip at the playhead with your stylus to instantaneously slice drawing clips, video tracks, or audio stems into two independent clips.
- **Trim Handles (`[` & `]`) with Live Delta**: Grab either edge of a clip to adjust start and end frames with live delta tooltips (`+04f` / `-00:02.12`).
- **Ripple & Roll Editing**: Trim an out-point and automatically pull subsequent clips forward, eliminating awkward blank gaps.
- **Slip & Slide Mode**: Slide the internal cel sequence inside a trimmed window without moving the clip's position on the master timeline.
- **Dynamic Time-Stretch**: Speed-ramp animations from **25% slow-mo** to **400% fast-forward** with linear or cubic keyframe interpolation.
- **Magnetic Snapping (`🧲`)**: Automatically snap clip boundaries and playhead scrubs to adjacent clip edges, scene markers, and audio waveform beat peaks with golden guide lines.

### 2. 🧅 Unified Multi-Layer Onion Skinning & "Shift-and-Trace" Light Table
- **Transform- & 3D-Aware Ghost Compositor**: Ghosts of previous (`t-1`) and future (`t+1`) frames are projected through the exact composite camera matrix ($s = \frac{f}{f + z}$) rather than rendering flat raw bitmaps.
- **Local vs. World Space Mode**: Lock onion skin ghosts to the character's local origin so you can draw secondary limb motion cleanly even while the character translates across the screen.
- **On-Screen Light Table Gizmo**: Temporarily translate, rotate (`⤹ -15°`, `⤸ +15°`), or scale onion skin reference frames right on the canvas. Tap **Reset Pegs** to snap back to true spatial coordinates once your in-between is inked.
- **Per-Layer Onion Pinning (`🧅`)**: Pin specific line-art or layout layers to remain visible in onion skin while ignoring background fills.

### 3. ⚡ Freehand Auto-Advance Frame Engine (Flipbook Mode)
Draw continuous animations without ever lifting your hand to touch the timeline bar:
- **Mode A: Stroke-Lift Auto-Advance (Debounce Timer)**:
  - When you lift your stylus (`ACTION_UP`), a configurable timer starts (**0ms Instant**, **250ms Fast Sketch**, **600ms Multi-Stroke**, **1200ms Detailed**) with an on-screen circular progress indicator.
  - Upon timer expiry, the current cel commits to Frame $N$, Frame $N+1$ is spawned automatically, and the previous frame turns into coral-red onion skin immediately.
- **Mode B: Live Loop Flipbook ("Draw While Playing")**:
  - The timeline plays continuously in a loop at a selected rate (**4, 8, or 12 FPS**) while you sketch directly on the canvas. Each stroke segment is stamped onto the exact frame passing under the playhead.
- **Mode C: Smart Momentum Auto-Clone**:
  - Automatically clones Frame $N$'s geometry into Frame $N+1$ shifted along the velocity vector $(\Delta x, \Delta y)$ of your final stylus flick for fluid projectiles, particles, and kinetic debris.

### 4. 👁️ Psychovisual Scene Transitions
Exploits biological and psychovisual limits of the human visual cortex for seamless cinematic cuts:
- **Saccadic Whip-Pan**: Exploits saccadic masking with directional streak blur and exponential velocity curves over 4–8 frames.
- **Shonen Retinal Impact Strobe**: 1–2 frame stark monochrome threshold flash and speed-lines that temporarily resets retinal edge detection, masking drastic spatial cuts.
- **Automatic Motion Smears**: Synthesizes 2D directional motion smear hulls between fast keyframe poses for fluid 24 FPS playback without tedious in-betweening.
- **Optical-Flow Morph**: Blends contours through a 3×3 deformation lattice.
- **Foveal Gaze Iris Wipe**: Focuses viewer attention directly onto the incoming focal point.

### 5. 📈 Custom Velocity & Cubic Bezier Curve Editor
- Interactive dual-mode graph editor for all animated properties (`Position`, `Rotation`, `Scale`, `Z-Depth`, `Shader Intensity`, `Time Remap`).
- **Value Graph**: Plots explicit property curves over frame time.
- **Velocity Graph**: Displays rate of change with draggable tangent handles $(P_1, P_2)$ for custom ease-in/ease-out acceleration.

### 6. 🌌 2.5D / 3D Multiplane Camera & Depth Staging
- Assign independent Z-depths to art layers (Foreground: $+200\text{px}$, Character: $0\text{px}$, Midground: $-300\text{px}$, Distant Sky: $-1200\text{px}$).
- Real-time parallax camera pans with field-of-view perspective scaling ($s = \frac{f}{f + z}$) and subtle focal plane defocus.

### 7. ☀️ Cinematic VFX Shaders & Interactive Light Gizmo
- **Interactive Light Gizmo (`☀️`)**: Drag the glowing sun/point light directly across the canvas to adjust directional lighting, rim highlights, and cast shadows in real time.
- **Atmospheric Haze & Fog**: Distance-based color absorption.
- **Stylized Grain & Chromatic Aberration**: Adds vintage analog texture and cinematic edge dispersion.

---

## ✍️ Hardware Stylus & Tablet Precision

FrameForge is designed around the raw hardware capabilities of Android tablets:
- **144Hz / 120Hz Coalesced Event Sampling**: Extracts all intermediate historical motion events (`MotionEvent.getHistoricalX/Y`) between display refreshes for silky, jagged-free curves.
- **Hardware Stylus Tilt Detection**: Extracts `MotionEvent.AXIS_TILT` to modulate brush shading width dynamically.
- **Hardware S-Pen Button Eraser Toggle**: Pressing the physical button on the stylus or flipping to the eraser tip (`TOOL_TYPE_ERASER`) instantly toggles erasing without hunting for on-screen menus.
- **Hardware Palm Rejection**: Filters accidental finger touches while stylus tip is hovered or in contact.

---

### 8. 🎨 DaVinci Resolve Pro Color Wheels & ASC CDL
- **Three-Way Color Wheels**: Independent Lift (Shadows), Gamma (Midtones), Gain (Highlights), and Master Offset controls with per-channel RGB balance.
- **ASC CDL Formula**: Exact implementation of $(In \times Slope + Offset)^{Power}$ with color saturation matrix.
- **White Balance (Kelvin)**: Planckian blackbody algorithm converting 2500K (warm tungsten desk lamp) to 9500K (deep azure sky) with Green-Magenta tint axis.
- **Film Emulation Presets**: One-tap Hollywood *Teal & Orange*, *Kodak Vision3 500T*, *Fuji Eterna Cinema*, *Bleach Bypass Silver*, *Film Noir B&W*, *Makoto Shinkai Anime*, and *UPSC Warm Study*.

### 9. 🟩 Industry-Proven Chroma Keying & Matte Tools
- **Studio Keying**: Green screen (`#00FF00`), Blue screen (`#0000FF`), and eyedropper color sampling.
- **Color Similarity & Smoothstep Roll-off**: Prevents jagged aliased edges around hair, cloth, and fine details.
- **Edge Despill Suppression**: Average, Max Adjacent, and Weighted algorithms suppress green spill on skin and props without color degradation.
- **Contact Shadow Recovery**: Automatically recovers and preserves natural floor and prop contact shadows under keyed subjects.
- **Diagnostic B&W Matte Mode**: Inspect the exact alpha silhouette channel in monochrome.

### 10. ✂️ Natron / Silhouette FX Rotoscoping with Per-Vertex Feathering
- **Dual Spline System**: Each vertex has an inner core anchor point $(x, y)$, Bezier curvature tangents, and an independent **feather vector** $(fx, fy)$.
- **Variable Feathering**: Sharp hard footwear (0px feather) and fast-moving motion-blurred hands (35px feather) within the exact same mask shape!
- **Whisker Vector Guides**: Interactive visual whisker lines connecting anchors to their feather handles.
- **Shape Inversion & Keyframe Tweening**: Keyframe vertex positions over time with automatic Hermite smoothstep shape tweening.

### 11. 📈 Glaxnimate / Blender Graph Curve Editor & Dope Sheet
- **Dope Sheet Mode**: Multi-track horizontal keyframe diamond strip $\diamondsuit$ for instant timing, sliding, and stretch adjustments.
- **Bezier Graph Editor**: Dual-coordinate spline graph with sub-frame precision.
- **Tangent Handle Modes**: *Auto-Clamped* (prevents value overshoot), *Aligned* (smooth colinear), *Free* (broken tangents), and *Vector*.

### 12. 🖥️ Distributed Headless GPU Render Farm (Laptop GTX 1650 Offload)
- **Zero Tablet Thermal Throttling**: Offload heavy 4K 60fps renders from the tablet to your laptop/workstation.
- **Python Render Daemon**: Dedicated workstation server (`scripts/frameforge_render_daemon.py`) using NVIDIA NVENC hardware acceleration (`h264_nvenc`).
- **Real-Time Network Telemetry**: Streams live render progress, frame rate (FPS), and ETA directly back to your tablet screen.

---

## 🛠️ Project Structure

```
2d-animation-tab-test/
├── .github/workflows/
│   └── build-apk.yml               # Automated CI build & GitHub Release pipeline
├── app/
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt         # Edge-to-edge Activity entry point
│   │   └── frameforge/
│   │       ├── cinema/             # Color Wheels, Keying, Roto, Tracking, Curves, NLE, Light Gizmo
│   │       ├── engine/             # 144Hz Drawing Engine, IK, Rotoscoping, Procedural
│   │       ├── export/             # Streaming MP4 (MediaCodec) & GIF Encoders
│   │       ├── model/              # Rigging, Timeline, Drawing & Vector Models
│   │       ├── network/            # Headless GPU Render Farm Client & Bridge
│   │       ├── pdf/                # PDF Slide Importer & Animated Handwriting Reveal Engine
│   │       ├── ui/                 # Full-Screen Procreate Canvas, TopBar, SliderHUD, Timeline
│   │       │   └── popovers/       # Color Grading, Chroma Key, Roto, Curves, Render Farm, VFX
│   │       └── viewmodel/          # StudioViewModel state orchestration
│   ├── src/test/java/com/example/  # Comprehensive unit test suite (Robolectric & JUnit)
│   └── build.gradle.kts            # Android Gradle Plugin 9.1 & Compose configuration
└── gradle/
    └── libs.versions.toml          # Centralized version catalog
```

### Running the Headless Render Farm Daemon on Laptop:
```bash
# On your Linux laptop with NVIDIA GPU:
python3 scripts/frameforge_render_daemon.py
```

---

## 🏗️ Building from Source

### Prerequisites
- **JDK 17** (e.g. Eclipse Temurin 17)
- **Android SDK** with Platform API 36 and Build-Tools 36.0.0
- **Gradle 9.3+**

### Local Build
```bash
# Clone the repository
git clone https://github.com/RengokuSensei/2d-animation-tab-test.git
cd 2d-animation-tab-test

# Copy environment template
cp .env.example .env

# Build debug APK
./gradlew assembleDebug --stacktrace

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
This project is open source and available under the **Apache 2.0 License**.
