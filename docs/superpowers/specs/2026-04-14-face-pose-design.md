# Face Pose Design

## Goal

Add realtime face orientation labels to the existing CameraX preview, distinguishing front face, left side face, right side face, looking up, and looking down.

## Architecture

Replace the realtime MediaPipe Face Detector helper with a Face Landmarker helper. Face Landmarker still provides face landmarks for drawing a box, and it can also output facial transformation matrices. A pure Kotlin `FacePoseEstimator` converts the first matrix for each face into approximate yaw, pitch, and roll angles, then maps the angles into a user-facing orientation label.

## Components

- `FaceLandmarkerHelper`: loads `face_landmarker.task`, runs MediaPipe in `RunningMode.LIVE_STREAM`, and returns `FaceLandmarkerResult`.
- `FacePoseEstimator`: extracts approximate yaw, pitch, and roll from a 4x4 transform matrix and classifies the orientation.
- `OverlayView`: computes each face box from normalized landmarks and draws the orientation label above the box.
- `MainActivity`: sends CameraX frames to `FaceLandmarkerHelper` and displays face count, pose label, and inference time.

## Classification Rules

The first dominant posture wins:

- `pitch >= 18` degrees: `面朝下`
- `pitch <= -18` degrees: `面朝上`
- `yaw >= 25` degrees: `右侧脸`
- `yaw <= -25` degrees: `左侧脸`
- otherwise: `正脸`

Front camera mirroring reverses yaw before classification, so left/right labels match what the person is doing from the phone user's perspective. These thresholds are intentionally conservative and can be tuned after device testing.

## Error Handling

If a frame has no faces, the overlay clears and the status text says `Faces: 0`. If the transform matrix is missing, the app still draws the face box and labels the pose as `未知`.

## Testing

Add unit tests for the pure Kotlin pose classifier and matrix-to-angle conversion. The user previously asked to avoid full local compilation, so verification focuses on unit tests and Gradle project configuration rather than `assembleDebug`.

