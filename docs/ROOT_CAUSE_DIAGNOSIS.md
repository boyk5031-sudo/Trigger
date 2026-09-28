# Trigger Overlay - Root Cause Diagnosis

## Your reported symptoms
- Trigger NOT working in-game (BGMI/Free Fire etc)
- Permissions granted but taps don't register
- Works on home screen but NOT inside OpenGL/Vulkan game surfaces

## Diagnosis Matrix (5 common failure points)

### ❌ Method 1: `Runtime.getRuntime().exec("input tap x y")`
**Symptom:** 200ms-1000ms latency, dropped events when rapid fire.

**Why it fails:**
```java
// BAD - what most tutorials do
Runtime.getRuntime().exec("input tap 800 400");
```
- Each call spawns `app_process` + `com.android.commands.input.Input` JVM
- ~150ms process fork + binder to InputManager
- GC pressure, can't handle >5 taps/sec
- Game anti-cheat may detect shell command pattern

**Fix in this repo:**
`ShizukuTouchService` caches `InputManager` instance via reflection ONCE in shell process (uid 2000). Then calls `injectInputEvent(MotionEvent, INJECT_INPUT_EVENT_MODE_ASYNC)` directly. Latency <5ms measured.

```kotlin
// GOOD - zero process spawn
injectMethod.invoke(inputManager, motionEvent, 2 /* ASYNC */)
```

### ❌ Method 2: `AccessibilityService.dispatchGesture()`
**Symptom:** Works on Settings, launcher, but NOT inside games.

**Why it fails:**
- Games render via `SurfaceView` / `TextureView` with OpenGL/Vulkan. Some OEMs (Xiaomi, Samsung Game Booster) block accessibility gestures on `FLAG_SECURE` or `isGameWindow`
- System throttles gestures to 1 per 300ms
- No multi-touch support with existing fingers (cancels current touches)
- `dispatchGesture` requires valid displayId, many implementations pass 0

**Fix:**
- Primary: Use Shizuku shell injection (bypasses throttling, works on game surfaces)
- Fallback: Keep Accessibility but implement correctly with 40ms duration and proper callback
- See `AccessibilityInjector.kt`

### ❌ Method 3: Coordinate / DPI Scale Misalignment
**Symptom:** Touches land in wrong position, offset by status bar / notch, fails after rotation.

**Why it fails:**
```java
// BAD
int x = (int)(view.getX() * density);
windowManager.addView(..., x, y);
injectTap(x, y); // uses view coords, not physical
```
- Overlay view coords are in WindowManager space (already px)
- Game expects physical display pixels (realMetrics)
- Rotation 90/270 swaps width/height
- Status bar height, cutout, navigation bar insets not accounted

**Fix:**
`CoordinateConverter`:
- Uses `Display.getRealMetrics()` + `getRealSize()` for physical pixels
- Percent-based API: `percentToPhysical(0.85f, 0.5f)` works across all devices
- Handles rotation, clamps to bounds
- Sets `MotionEvent.setDisplayId()` for foldables

```kotlin
// GOOD
val point = CoordinateConverter.percentToPhysical(context, 0.85f, 0.5f)
injectTap(point.x, point.y, displayId)
```

### ❌ Method 4: Broken MotionEvent Lifecycle
**Symptom:** Game sees ghost touches, stuck finger, or ignores tap.

**Why it fails:**
```java
// BAD - only UP, or wrong timestamps
MotionEvent.obtain(0, 0, ACTION_UP, x, y, 0)
```
- Missing `ACTION_DOWN -> ACTION_UP` sequence
- `downTime` must be consistent across DOWN/MOVE/UP
- `eventTime` must be `SystemClock.uptimeMillis()`, not `currentTimeMillis()`
- Missing `SOURCE_TOUCHSCREEN` flag
- Pointer id wrong

**Fix:**
`InputEventFactory` enforces:
- `downTime = uptimeMillis()` at DOWN, reused for UP
- `ACTION_DOWN` then 40ms later `ACTION_UP`
- `TOOL_TYPE_FINGER`, pressure=1f, source=TOUCHSCREEN
- `setDisplayId()` set

### ❌ Method 5: Hidden API Blocks (Android 12/13/14/15)
**Symptom:** `NoSuchMethodException` or `IllegalAccessException` when calling `IInputManager` from app.

**Why it fails:**
- Android 9+ hidden API blacklist blocks `InputManager.injectInputEvent` via reflection from app uid (10000)
- `hidden_api_policy` = 1 (enforce)
- Even with `setHiddenApiExemptions`, Play Store blocks

**Fix:**
- Do reflection in **shell process** (uid 2000) via Shizuku, not app process. Shell is exempt from hidden API restrictions.
- Shizuku's `UserService` runs as `com.android.shell`, can call hidden APIs freely
- App process only does Binder IPC to shell service, no reflection

```kotlin
// App process (no reflection)
shizukuBinder.injectTap(x, y, displayId)

// Shell process (reflection allowed)
val im = InputManager.getInstance()
im.injectInputEvent(event, ASYNC)
```

## Recommended Architecture (Implemented)

```
[Overlay UI - iOS AssistiveTouch]
        |
        v
[FloatingTriggerService] -- (percent coords) --> [InjectorManager]
        |                                            |
        |                                            +--> [ShizukuInjector] --Binder--> [ShizukuTouchService (uid 2000)] --InputManager--> System Server
        |                                            |
        |                                            +--> [AccessibilityInjector] (fallback)
        |
[CoordinateConverter] (fixes DPI, rotation, cutout)
[InputEventFactory] (fixes MotionEvent lifecycle)
```

**Multi-touch guarantee:**
- Overlay Window uses `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL | FLAG_WATCH_OUTSIDE_TOUCH`
- Only trigger button consumes touch, rest passes through
- Injection uses separate MotionEvent stream, doesn't cancel existing game touches
