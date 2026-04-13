# Android Face Get

Android realtime face detection demo using CameraX and MediaPipe Tasks Vision.

## What it does

- Opens the camera preview.
- Runs MediaPipe Face Detector on live CameraX frames.
- Draws bounding boxes over detected faces.
- Supports front/back camera switching.
- Requests camera permission at runtime.

## Project Notes

- Main package: `com.example.androidfaceget`
- Model asset: `app/src/main/assets/face_detection_short_range.tflite`
- MediaPipe API: `com.google.mediapipe:tasks-vision`
- Camera stack: CameraX `PreviewView`, `Preview`, `ImageAnalysis`, and `ProcessCameraProvider`

## Build

Open this folder in Android Studio and sync Gradle, then run the `app` configuration.

The project includes Gradle wrapper `8.7`, so Android Studio should use the project wrapper during sync. For command-line checks:

```bash
./gradlew :app:assembleDebug
```

## References

- MediaPipe Face Detector Android docs: https://ai.google.dev/edge/mediapipe/solutions/vision/face_detector/android
- MediaPipe Android sample: https://github.com/google-ai-edge/mediapipe-samples/tree/main/examples/face_detector/android
- CameraX docs: https://developer.android.com/media/camera/camerax
