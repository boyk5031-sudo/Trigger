# iOS Design System Implementation

## Philosophy
The Trigger app mimics iOS 17 Settings + Control Center + AssistiveTouch.

## Color Palette (from iOS Human Interface Guidelines)

- **System Background Light:** `#F2F2F7` - grouped table background
- **Card Light:** `#FFFFFF` - cell background
- **Separator:** `#C6C6C8` - 0.5pt divider
- **Label Primary:** `#000000`
- **Secondary Label:** `#8E8E93`
- **iOS Blue:** `#007AFF` - interactive
- **Green:** `#34C759` - toggle on, success
- **Red:** `#FF3B30` - destructive, error
- **Orange:** `#FF9500` - warning
- **Indigo:** `#5856D6`

Dark mode:
- Background `#000000`
- Card `#1C1C1E`
- Variant `#2C2C2E`

## Typography
- **Large Title:** 34sp Bold, 0.37 spacing (SF Pro Display)
- **Title 2:** 28sp SemiBold
- **Headline:** 17sp SemiBold -0.41 spacing
- **Body:** 17sp Regular -0.41 spacing
- **Callout:** 16sp
- **Subhead:** 15sp -0.24
- **Footnote:** 13sp
- **Caption 2:** 11sp

## Components

### IOSSection
- Uppercase 13sp header, 6dp bottom padding
- Card: 12dp rounded, 1dp shadow, white background
- Footer: 13sp secondary, 6dp top padding
- Padding: 16dp horizontal, 8dp vertical between sections

### IOSListRow
- 28dp icon box, 6dp rounded, colored background (blue/orange/etc)
- 18dp white icon inside
- Title 17sp, subtitle 14sp secondary
- Chevron `›` 20sp #C7C7CC light
- Separator 0.5dp from leading edge (56dp if icon, 16dp otherwise)
- Height 44pt minimum (iOS standard)

### IOSToggle
- Track 51x31 dp
- Thumb 27dp white with shadow
- On: #34C759, Off: #E9E9EB
- Animation 0.3s spring

### IOSSlider
- Track 4dp height, active #007AFF, inactive #E5E5EA
- Thumb 28dp white with shadow
- Value label trailing, secondary color

### IOSButton
- 12dp rounded
- Filled: blue background white text, 17sp semibold, 14dp vertical
- Outlined: light gray background blue text
- Destructive: red
- Pressed: 0.9 opacity + 0.95 scale

### IOSStatusCard
- 12dp rounded, colored background 12% alpha
- 10dp dot, green/red
- Title 15sp medium, status 13sp secondary

### IOSLargeTitle
- 34sp bold, 20dp horizontal padding
- Subtitle 15sp secondary

## Overlay (AssistiveTouch Clone)

- **Container:** 140dp default, user resizable 80-200dp
- **Outer:** 85% of container, white 180 alpha, 1dp black 60 alpha stroke, 12dp elevation, oval
- **Inner:** 40% of container, blue #007AFF, 6dp elevation
- **Blur:** Simulated via semi-transparent white + elevation, real blur would need `RenderEffect` API 31+
- **Animations:**
  - Down: 0.9x scale 100ms
  - Up: 1f scale 200ms overshoot
  - Tap: 0.85x 80ms → 1f 250ms bounce, inner 1.2x 80ms → 1f 200ms
  - Hold: inner red #FF3B30, pulse 1f→1.1f→1f 500ms infinite
- **Drag:** Touch slop detection, update WindowManager.LayoutParams.x/y
- **Snap:** ValueAnimator to edge 20dp margin, 300ms overshoot interpolator
- **Haptic:** LIGHT 20ms, MEDIUM 40ms via Vibrator

## Screen Layout

- Scaffold with background #F2F2F7
- Scrollable Column
- Large Title at top
- Hero card: blue #007AFF, 20dp rounded, 48dp icon box white 20% alpha, Start button white pill
- Sections stacked with 8dp vertical gap
- Footer centered 12sp secondary

## Implementation Notes

- Use Material3 but override colors to iOS palette
- No Material ripple, use clickable with iOS spring
- Shadows via `Modifier.shadow()` not elevation where possible
- Icons: Material filled, but in colored boxes like iOS Settings
- All interactions have haptic feedback
