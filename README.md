# Android Face Get

Android realtime face pose demo using CameraX and MediaPipe Tasks Vision.

## What it does

- Opens the camera preview.
- Runs MediaPipe Face Landmarker on live CameraX frames.
- Draws bounding boxes over detected faces from face landmarks.
- Shows realtime pose labels: `正脸`, `左侧脸`, `右侧脸`, `面朝上`, `面朝下`.
- Supports front/back camera switching.
- Requests camera permission at runtime.

## Project Notes

- Main package: `com.example.androidfaceget`
- Model asset: `app/src/main/assets/face_landmarker.task`
- MediaPipe API: `com.google.mediapipe:tasks-vision`
- Camera stack: CameraX `PreviewView`, `Preview`, `ImageAnalysis`, and `ProcessCameraProvider`

## Build

Open this folder in Android Studio and sync Gradle, then run the `app` configuration.

The project includes Gradle wrapper `8.7`, so Android Studio should use the project wrapper during sync. For command-line checks:

```bash
./gradlew :app:assembleDebug
```

## References

- MediaPipe Face Landmarker Android docs: https://ai.google.dev/edge/mediapipe/solutions/vision/face_landmarker/android
- MediaPipe Android sample: https://github.com/google-ai-edge/mediapipe-samples/tree/main/examples/face_landmarker/android
- CameraX docs: https://developer.android.com/media/camera/camerax
