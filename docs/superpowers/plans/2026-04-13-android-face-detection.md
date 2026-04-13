# Android Face Detection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a minimal Android app that detects faces in realtime with MediaPipe and draws bounding boxes over CameraX preview.

**Architecture:** CameraX provides preview and RGBA analysis frames. MediaPipe Face Detector runs in `LIVE_STREAM` mode and returns async results. A custom overlay maps image-space boxes into the preview coordinate space and draws them.

**Tech Stack:** Kotlin, Android Gradle Plugin, CameraX, MediaPipe Tasks Vision, JUnit 4.

---

### Task 1: Project Skeleton

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `.gitignore`
- Create: `app/build.gradle`

- [x] **Step 1: Add Gradle settings**

Create repositories for `google`, `mavenCentral`, and `gradlePluginPortal`; include `:app`.

- [x] **Step 2: Add Android/Kotlin plugins**

Use `com.android.application` `8.6.1` and `org.jetbrains.kotlin.android` `2.0.21`.

- [x] **Step 3: Add app dependencies**

Add CameraX `1.4.2`, MediaPipe Tasks Vision `0.10.33`, AndroidX Activity/Core/Lifecycle, and JUnit `4.13.2`.

### Task 2: UI and Manifest

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/styles.xml`

- [x] **Step 1: Add camera permission and launcher activity**

Declare `android.permission.CAMERA`, camera hardware feature, and `MainActivity`.

- [x] **Step 2: Add preview layout**

Use a full-screen `PreviewView`, full-screen `OverlayView`, status text, and camera switch button.

### Task 3: Testable Box Mapping

**Files:**
- Create: `app/src/test/java/com/example/androidfaceget/BoxMappingTest.kt`
- Create: `app/src/main/java/com/example/androidfaceget/BoxMapping.kt`

- [x] **Step 1: Write mapping tests**

Cover square-to-wide, wide-to-tall, and invalid-size mapping.

- [x] **Step 2: Implement `BoxMapper.mapCenterCrop`**

Use center-crop scale `max(viewWidth / imageWidth, viewHeight / imageHeight)` and symmetric offsets.

### Task 4: MediaPipe Detection and CameraX

**Files:**
- Create: `app/src/main/java/com/example/androidfaceget/FaceDetectorHelper.kt`
- Create: `app/src/main/java/com/example/androidfaceget/MainActivity.kt`
- Create: `app/src/main/java/com/example/androidfaceget/OverlayView.kt`
- Create: `app/src/main/assets/face_detection_short_range.tflite`

- [x] **Step 1: Add model asset**

Download `blaze_face_short_range.tflite` into assets as `face_detection_short_range.tflite`.

- [x] **Step 2: Implement MediaPipe helper**

Load the model with `BaseOptions`, use CPU delegate, set `RunningMode.LIVE_STREAM`, and call `detectAsync`.

- [x] **Step 3: Implement activity**

Request camera permission, bind CameraX preview and analysis, switch cameras, and forward detector results to the overlay.

- [x] **Step 4: Implement overlay**

Draw one bounding box per MediaPipe detection using `BoxMapper`.

### Task 5: Repository Setup

**Files:**
- Modify: local Git repository
- Create: GitHub repository `androidfaceget`

- [x] **Step 1: Initialize Git**

Run: `git init -b main`

- [x] **Step 2: Commit files**

Run: `git add . && git commit -m "feat: add mediapipe face detection android app"`

- [x] **Step 3: Create GitHub repository**

Use the provided token through stdin, create or reuse `androidfaceget`, set `origin`, and push `main`.
