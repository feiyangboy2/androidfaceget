# Android Face Detection Design

## Goal

Create a minimal Android app that detects faces in realtime camera frames with MediaPipe and draws face boxes over the camera preview.

## Architecture

The app uses CameraX for camera lifecycle and frame delivery. `MainActivity` owns permission checks, camera binding, and UI callbacks. `FaceDetectorHelper` owns MediaPipe Face Detector setup and live-stream inference. `OverlayView` draws bounding boxes, and `BoxMapper` maps MediaPipe image coordinates into the `PreviewView` center-crop coordinate space.

## Components

- `MainActivity`: requests camera permission, starts CameraX preview, configures `ImageAnalysis`, switches front/back cameras, and updates status text.
- `FaceDetectorHelper`: loads `face_detection_short_range.tflite`, creates a `FaceDetector` in `RunningMode.LIVE_STREAM`, converts CameraX RGBA frames into `MPImage`, and returns async results.
- `OverlayView`: receives `FaceDetectorResult` objects and draws one box per detection.
- `BoxMapper`: pure Kotlin coordinate mapper for testable preview scaling behavior.

## Data Flow

CameraX sends the newest RGBA frame to `ImageAnalysis`. `FaceDetectorHelper` rotates and mirrors the bitmap as needed, then calls MediaPipe `detectAsync`. The result listener passes the detection result and input image size back to `MainActivity`, which updates `OverlayView` on the main thread.

## Error Handling

Camera permission denial updates the status label and does not start camera binding. Camera binding and MediaPipe runtime errors are caught and displayed in the status label. The analyzer always closes each `ImageProxy` in a `finally` block.

## Testing

`BoxMappingTest` covers the coordinate transform for wide views, tall views, and invalid frame sizes. The user asked to skip local compilation, so Gradle tests are included but not executed in this session.

## Dependencies

- Android Gradle Plugin `8.6.1`
- Gradle wrapper `8.7`
- Kotlin Android plugin `2.0.21`
- CameraX `1.4.2`
- MediaPipe Tasks Vision `0.10.33`
