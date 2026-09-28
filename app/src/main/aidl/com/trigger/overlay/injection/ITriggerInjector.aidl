package com.trigger.overlay.injection;

// High-speed binder interface that runs in Shizuku shell context (uid 2000)
// All coordinates MUST be physical display pixels (not dp, not view coords)
interface ITriggerInjector {
    // Simple tap: DOWN -> UP with minimal latency
    void injectTap(float x, float y, int displayId);

    // Low-level lifecycle for custom gestures / hold / swipe
    void injectDown(float x, float y, int displayId, long downTime);
    void injectMove(float x, float y, int displayId, long downTime);
    void injectUp(float x, float y, int displayId, long downTime);

    // Multi-touch: inject second finger (for games that need 2 pointers)
    void injectPointerDown(int pointerId, float x, float y, int displayId, long downTime);
    void injectPointerUp(int pointerId, float x, float y, int displayId, long downTime);

    // Ping & cleanup
    int getVersion();
    void destroy();
}
