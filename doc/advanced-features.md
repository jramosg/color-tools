# Advanced Features Guide

This guide covers the advanced color manipulation features added in version 1.1.0.

## Table of Contents

- [Color Blending Modes](#color-blending-modes)
- [Tints, Shades, and Tones](#tints-shades-and-tones)
- [Alpha Blending and Compositing](#alpha-blending-and-compositing)
- [Color Interpolation and Gradients](#color-interpolation-and-gradients)
- [Perceptual Color Difference](#perceptual-color-difference)
- [Color Temperature (Kelvin)](#color-temperature-kelvin)

## Color Blending Modes

Blending modes are essential for design work, allowing you to combine colors in different ways similar to popular design tools like Photoshop or Figma.

### Multiply

Multiply darkens colors by multiplying the color channels. It's useful for creating shadows and darkening effects.

```clojure
(require '[jon.color-tools :as color])

;; Multiply red and blue creates black
(color/blend-multiply "#ff0000" "#0000ff")
;=> "#000000"

;; Multiply with semi-bright colors
(color/blend-multiply "#ff8800" "#88ff00")
;=> "#885500"

;; Works with RGB vectors too
(color/blend-multiply [255 128 64] [128 255 200])
;=> [127 127 50]
```

**Use cases:**
- Creating shadow effects
- Darkening images or backgrounds
- Layer compositing in design tools

### Screen

Screen lightens colors by inverting, multiplying, and inverting again. It's the opposite of multiply.

```clojure
;; Screen brightens dark colors
(color/blend-screen "#800000" "#000080")
;=> "#800080"

;; Screen with bright colors creates very bright results
(color/blend-screen "#ff8800" "#88ff00")
;=> "#ffff00"
```

**Use cases:**
- Creating highlight effects
- Lightening images
- Glow and light effects

### Overlay

Overlay combines multiply and screen based on the base color's brightness. Dark areas get darker (multiply), light areas get lighter (screen).

```clojure
;; Overlay creates high contrast
(color/blend-overlay "#ff0000" "#808080")
;=> "#ff0000"

;; Works great for texture overlays
(color/blend-overlay "#3498db" "#f0f0f0")
;=> "#6db4e8"
```

**Use cases:**
- Adding texture to surfaces
- Increasing contrast
- Dramatic color effects

### Preserving Input Format

All blending functions preserve the input format of the base color:

```clojure
;; Hex input → hex output
(color/blend-multiply "#ff0000" "#0000ff")
;=> "#000000"

;; Vector input → vector output
(color/blend-multiply [255 0 0] [0 0 255])
;=> [0 0 0]

;; Color record input → Color record output
(def red-color (color/color 255 0 0))
(def blue-color (color/color 0 0 255))
(color/blend-multiply red-color blue-color)
;=> #Color{:r 0 :g 0 :b 0 :a 1.0}
```

## Tints, Shades, and Tones

These are fundamental concepts in color theory and design systems. They create color variations while maintaining color harmony.

### Understanding the Difference

- **Tint**: Color + White (lightens while preserving hue)
- **Shade**: Color + Black (darkens while preserving hue)  
- **Tone**: Color + Gray (reduces saturation/vibrancy)

### Creating Single Variations

```clojure
(def base-color "#3498db")  ; A nice blue

;; Create a tint (lighter version)
(color/tint base-color 0.3)
;=> "#6fb3e5"

;; Create a shade (darker version)
(color/shade base-color 0.3)
;=> "#236a91"

;; Create a tone (less saturated version)
(color/tone base-color 0.3)
;=> "#4d98c3"
```

The amount parameter (0-1) controls how much white/black/gray to mix:
- 0.0 = no change
- 0.5 = 50% mixed
- 1.0 = pure white/black/gray

### Generating Color Scales

Creating a series of tints, shades, or tones is perfect for design systems and UI palettes:

```clojure
;; Generate 5 progressively lighter tints
(color/tints "#e74c3c" 5)
;=> ["#eb6757" "#ef8272" "#f39c8d" "#f7b7a8" "#fbd2c3"]

;; Generate 5 progressively darker shades
(color/shades "#e74c3c" 5)
;=> ["#b83d30" "#892d24" "#5a1e18" "#2b0f0c" "#000000"]

;; Generate 5 progressively less saturated tones
(color/tones "#e74c3c" 5)
;=> ["#dd6b5e" "#ce8980" "#bfa8a2" "#b0c6c4" "#a1e5e6"]
```

### Building Design Systems

```clojure
;; Create a complete color palette for a brand
(def brand-primary "#2ecc71")

(def palette
  {:primary brand-primary
   :primary-light (color/tint brand-primary 0.2)
   :primary-lighter (color/tint brand-primary 0.4)
   :primary-dark (color/shade brand-primary 0.2)
   :primary-darker (color/shade brand-primary 0.4)
   :primary-muted (color/tone brand-primary 0.3)})

palette
;=> {:primary "#2ecc71"
;    :primary-light "#58d68d"
;    :primary-lighter "#82e0a9"
;    :primary-dark "#25a25a"
;    :primary-darker "#1c7943"
;    :primary-muted "#4db67a"}
```

## Alpha Blending and Compositing

Proper alpha blending is essential for working with transparency and creating layered interfaces.

### Porter-Duff Alpha Compositing

The `alpha-blend` function implements the standard Porter-Duff "source over" compositing algorithm:

```clojure
;; Blend semi-transparent blue over opaque red
(color/alpha-blend [255 0 0 1.0] [0 0 255 0.5])
;=> [127 0 127 1.0]

;; The formula: 
;; a_out = a_overlay + a_base * (1 - a_overlay)
;; c_out = (c_overlay * a_overlay + c_base * a_base * (1 - a_overlay)) / a_out
```

### Setting Alpha Channels

```clojure
;; Add transparency to a color
(color/with-alpha "#ff0000" 0.5)
;=> "rgba(255,0,0,0.5)"

;; Works with all color types
(color/with-alpha [255 0 0] 0.7)
;=> [255 0 0 0.7]

(color/with-alpha (color/color 255 0 0) 0.3)
;=> Color record with alpha 0.3
```

### Layering Colors

```clojure
;; Simulate layered UI elements
(def background "#ffffff")
(def overlay1 (color/with-alpha "#3498db" 0.3))
(def overlay2 (color/with-alpha "#e74c3c" 0.3))

;; Layer overlay1 on background
(def layer1 (color/alpha-blend background overlay1))

;; Then layer overlay2 on top
(def final (color/alpha-blend layer1 overlay2))
```

### Practical Example: Glass Morphism

```clojure
(defn create-glass-effect [background-color glass-tint alpha blur]
  (let [tinted (color/tint background-color glass-tint)
        with-transparency (color/with-alpha tinted alpha)]
    {:background with-transparency
     :backdrop-filter (str "blur(" blur "px)")
     :border "1px solid rgba(255,255,255,0.18)"}))

(create-glass-effect "#1a1a1a" 0.1 0.15 10)
;=> {:background "rgba(37,37,37,0.15)"
;    :backdrop-filter "blur(10px)"
;    :border "1px solid rgba(255,255,255,0.18)"}
```

## Color Interpolation and Gradients

Create smooth color transitions for animations, data visualizations, and UI effects.

### Basic Interpolation

```clojure
;; Find the color halfway between red and blue
(color/interpolate ["#ff0000" "#0000ff"] 0.5 :rgb)
;=> "#800080"  ; Purple

;; Position can be any value from 0 to 1
(color/interpolate ["#ff0000" "#0000ff"] 0.25 :rgb)
;=> "#bf003f"  ; More red than blue

(color/interpolate ["#ff0000" "#0000ff"] 0.75 :rgb)
;=> "#4000bf"  ; More blue than red
```

### Multi-Color Interpolation

```clojure
;; Interpolate through multiple colors
(def colors ["#ff0000" "#00ff00" "#0000ff"])

(color/interpolate colors 0.0 :rgb)   ; Start
;=> "#ff0000"

(color/interpolate colors 0.5 :rgb)   ; Middle (at green)
;=> "#00ff00"

(color/interpolate colors 1.0 :rgb)   ; End
;=> "#0000ff"
```

### Color Space Matters

Different color spaces produce different results:

```clojure
(def red "#ff0000")
(def yellow "#ffff00")

;; RGB interpolation (direct component mixing)
(color/interpolate [red yellow] 0.5 :rgb)
;=> "#ff8000"  ; Orange (halfway in RGB space)

;; HSL interpolation (through hue wheel)
(color/interpolate [red yellow] 0.5 :hsl)
;=> "#ff8000"  ; Orange (through hue 0→60)

;; For colors across the hue wheel, HSL is more natural
(def red "#ff0000")
(def cyan "#00ffff")

(color/interpolate [red cyan] 0.5 :rgb)
;=> "#808080"  ; Gray (RGB mixes to gray)

(color/interpolate [red cyan] 0.5 :hsl)
;=> "#00ff80"  ; Green-cyan (natural hue progression)
```

### Creating Gradients

```clojure
;; Simple 2-color gradient
(color/gradient ["#ff0000" "#0000ff"] 5 :rgb)
;=> ["#ff0000" "#bf003f" "#80007f" "#4000bf" "#0000ff"]

;; Multi-color gradient
(color/gradient ["#ff0000" "#00ff00" "#0000ff"] 7 :hsl)
;=> ["#ff0000" "#aaaa00" "#55ff00" "#00ff55" "#00aaaa" "#0055ff" "#0000ff"]

;; Fine-grained gradient for smooth transitions
(color/gradient ["#3498db" "#e74c3c"] 20 :rgb)
;=> Twenty colors from blue to red
```

### Practical Examples

**Heat Map Colors:**
```clojure
(def heat-map-gradient
  (color/gradient ["#0000ff" "#00ffff" "#00ff00" "#ffff00" "#ff0000"] 100 :rgb))

(defn value->color [value min-val max-val]
  (let [normalized (/ (- value min-val) (- max-val min-val))
        index (int (* normalized 99))]
    (nth heat-map-gradient index)))

(value->color 50 0 100)  ; Mid-range value
;=> "#00ff00"  ; Green
```

**Animated Color Transitions:**
```clojure
(defn animate-color-transition [start-color end-color duration-ms current-time-ms]
  (let [progress (/ current-time-ms duration-ms)
        clamped (max 0 (min 1 progress))]
    (color/interpolate [start-color end-color] clamped :hsl)))

(animate-color-transition "#3498db" "#e74c3c" 1000 500)
;=> Color at 50% of animation
```

## Perceptual Color Difference

RGB distance doesn't match how humans perceive color differences. The CIE Delta E formula provides perceptually uniform measurements.

### Why Delta E?

```clojure
;; These have similar RGB distances
(color/color-distance "#ff0000" "#ff0100")
;=> 1.0

(color/color-distance "#00ff00" "#00ff01")
;=> 1.0

;; But humans perceive them differently!
(color/delta-e "#ff0000" "#ff0100")
;=> 0.8  ; Imperceptible

(color/delta-e "#00ff00" "#00ff01")
;=> 0.4  ; Even less perceptible (green is more sensitive)
```

### Understanding Delta E Values

- **< 1.0**: Imperceptible difference (colors appear identical)
- **1.0 - 2.0**: Barely perceptible (experts might notice)
- **2.0 - 10.0**: Noticeable difference (most people can see it)
- **> 10.0**: Very different colors

### Checking Similarity

```clojure
;; Using default threshold (2.3 JND - Just Noticeable Difference)
(color/perceptually-similar? "#ff0000" "#fe0101")
;=> true

(color/perceptually-similar? "#ff0000" "#0000ff")
;=> false

;; Custom threshold for stricter matching
(color/perceptually-similar? "#ff0000" "#fe0000" 1.0)
;=> false  ; Stricter than default
```

### LAB Color Space

Delta E uses the CIE L\*a\*b\* color space:

```clojure
;; Convert to LAB
(color/rgb->lab [255 0 0])
;=> [53.24 80.09 67.20]
;; L: lightness (0-100)
;; a: green-red axis
;; b: blue-yellow axis

(color/rgb->lab [0 255 0])
;=> [87.73 -86.18 83.18]
;; Negative a = more green
```

### Practical Applications

**Color Palette Deduplication:**
```clojure
(defn deduplicate-colors [colors threshold]
  (reduce (fn [acc color]
            (if (some #(color/perceptually-similar? color % threshold) acc)
              acc
              (conj acc color)))
          []
          colors))

(deduplicate-colors ["#ff0000" "#fe0101" "#ff0100" "#0000ff"] 2.0)
;=> ["#ff0000" "#0000ff"]  ; Similar reds merged
```

**Finding Closest Match:**
```clojure
(defn find-closest-color [target palette]
  (apply min-key
         #(color/delta-e target %)
         palette))

(def brand-colors ["#3498db" "#e74c3c" "#2ecc71" "#f39c12"])
(find-closest-color "#3a9edb" brand-colors)
;=> "#3498db"  ; Closest brand color
```

## Color Temperature (Kelvin)

Color temperature represents the color of light emitted by a black body at a given temperature, measured in Kelvin.

### Common Color Temperatures

- **1800K**: Candlelight (warm orange)
- **2700K**: Incandescent bulb (warm white)
- **3000K**: Warm white LED
- **4000K**: Neutral white
- **5000K**: Cool white
- **6500K**: Daylight (slightly cool)
- **10000K**: Clear blue sky (very cool)

### Converting Temperature to Color

```clojure
;; Candlelight
(color/kelvin->rgb 1800)
;=> [255 147 41]

(color/kelvin->hex 1800)
;=> "#ff9329"

;; Warm white
(color/kelvin->rgb 3000)
;=> [255 214 170]

;; Daylight
(color/kelvin->rgb 6500)
;=> [255 249 253]

;; Cool blue
(color/kelvin->rgb 10000)
;=> [201 218 255]
```

### Approximating Temperature from Color

```clojure
;; Works best for colors on the black body curve
(color/rgb->kelvin [255 147 41])
;=> 1856  ; Close to 1800K

(color/rgb->kelvin [255 249 253])
;=> 6453  ; Close to 6500K

;; Returns nil for non-black-body colors
(color/rgb->kelvin [255 0 0])
;=> nil  ; Pure red isn't on the curve
```

### Valid Range

The functions support temperatures from 1000K to 40000K:

```clojure
(color/kelvin->rgb 1000)   ; OK
(color/kelvin->rgb 40000)  ; OK

(color/kelvin->rgb 500)    ; Exception: out of range
(color/kelvin->rgb 50000)  ; Exception: out of range
```

### Practical Applications

**Adjustable Lighting UI:**
```clojure
(defn create-light-controls [kelvin]
  {:color (color/kelvin->hex kelvin)
   :label (cond
            (< kelvin 2500) "Warm"
            (< kelvin 4000) "Neutral Warm"
            (< kelvin 5500) "Neutral"
            (< kelvin 7000) "Cool"
            :else "Very Cool")
   :slider-value kelvin})

(create-light-controls 3000)
;=> {:color "#ffd6aa"
;    :label "Neutral Warm"
;    :slider-value 3000}
```

**Time-of-Day Lighting:**
```clojure
(defn time-based-temperature [hour]
  (cond
    (< hour 6)   1800   ; Night - candlelight
    (< hour 9)   3000   ; Early morning - warm
    (< hour 17)  5500   ; Day - neutral/cool
    (< hour 20)  3500   ; Evening - warm
    :else        2200)) ; Night - very warm

(defn get-ambient-color [hour]
  (color/kelvin->hex (time-based-temperature hour)))

(get-ambient-color 14)  ; 2 PM
;=> "#ffffff"  ; Bright daylight color
```

**Mood Lighting:**
```clojure
(def mood-presets
  {:romantic   (color/kelvin->hex 1800)  ; Candlelight
   :relaxing   (color/kelvin->hex 2700)  ; Warm cozy
   :productive (color/kelvin->hex 5000)  ; Cool white
   :energizing (color/kelvin->hex 6500)}) ; Daylight

mood-presets
;=> {:romantic "#ff9329"
;    :relaxing "#ffc58f"
;    :productive "#ffeef0"
;    :energizing "#fff9fd"}
```

## Conclusion

These advanced features provide professional-grade color manipulation capabilities for design systems, data visualization, lighting applications, and more. They build on the solid foundation of the color-tools library while adding sophisticated operations that match industry-standard tools.

For complete API documentation, see [api-reference.md](api-reference.md).
