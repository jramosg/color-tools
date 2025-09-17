# Tutorial: Working with Colors in Clojure

This tutorial will walk you through the main features of `jon/color-tools` with practical examples.

## Setup

First, add the library to your project and require it:

```clojure
(require '[jon.color-tools :as color])
```

## Chapter 1: Color Formats and Conversions

### Understanding Color Formats

Colors can be represented in many ways. This library supports the most common formats:

```clojure
;; HEX - Web standard, strings starting with #
"#ff5733"  ; Full 6-digit
"#f53"     ; Short 3-digit (expanded to #ff5533)

;; RGB - Red, Green, Blue values 0-255
[255 87 51]

;; RGBA - RGB with Alpha channel 0-1
[255 87 51 0.8]

;; CSS RGB - CSS color strings with flexible formatting
"rgb(255, 87, 51)"     ; Comma-separated (standard)
"rgb(255 87 51)"       ; Space-separated (modern CSS)
"RGB( 255 , 87 , 51 )" ; Case-insensitive, extra spaces

;; CSS RGBA - CSS color strings with alpha channel
"rgba(255, 87, 51, 1)"    ; Comma-separated with alpha
"rgba(255 87 51 0.8)"     ; Space-separated with alpha
"rgba(255, 87, 51, .5)"   ; Decimal alpha values

;; HSL - Hue (0-360), Saturation (0-100), Lightness (0-100)
[9 100 60]

;; HSV - Hue (0-360), Saturation (0-100), Value (0-100)
[9 80 100]
```

### Basic Conversions

The library provides two approaches for conversions: format-specific functions and universal conversion functions.

#### Universal Conversion Functions (Recommended)

These functions work with any color input type:

```clojure
;; Convert any color type to RGB
(color/->rgb "#ff5733")                ; hex to RGB
;=> [255 87 51]

(color/->rgb "rgb(255, 87, 51)")       ; CSS RGB to RGB
;=> [255 87 51]

(color/->rgb "rgba(255, 87, 51, 0.8)") ; CSS RGBA to RGB (ignores alpha)
;=> [255 87 51]

(color/->rgb [255 87 51])              ; RGB passthrough
;=> [255 87 51]

;; Convert any color type to HEX
(color/->hex [255 87 51])              ; RGB to hex
;=> "#ff5733"

(color/->hex "rgb(255, 87, 51)")       ; CSS RGB to hex
;=> "#ff5733"

(color/->hex "#ff5733")                ; hex passthrough
;=> "#ff5733"

;; Convert any color type to HSL
(color/->hsl "#ff5733")                ; hex to HSL
;=> [9 100 60]

(color/->hsl "rgb(255, 87, 51)")       ; CSS RGB to HSL
;=> [9 100 60]
```

#### Format-Specific Functions (Legacy)

Traditional format-specific conversions are still available:

```clojure
;; HEX to RGB
(color/hex->rgb "#ff5733")
;=> [255 87 51]

;; RGB to HEX
(color/rgb->hex [255 87 51])
;=> "#ff5733"

;; Parse CSS color strings
(color/parse-css-rgb "rgb(255, 87, 51)")
;=> [255 87 51]

(color/parse-css-rgba "rgba(255, 87, 51, 0.8)")
;=> [255 87 51 0.8]

;; RGB to HSL for color manipulation
(color/rgb->hsl [255 87 51])
;=> [9 100 60]

;; Chain conversions
(-> "#3498db"
    color/hex->rgb
    color/rgb->hsl)
;=> [204 70 53]
```

### Validation

Always validate colors when accepting user input:

```clojure
(color/valid-hex? "#ff5733")   ;=> true
(color/valid-hex? "invalid")   ;=> false

(color/valid-rgb? [255 87 51]) ;=> true
(color/valid-rgb? [300 87 51]) ;=> false (out of range)

(color/valid-hsl? [204 70 53]) ;=> true
```

## Chapter 2: Color Manipulation

### Lightness and Darkness

Adjust the lightness of colors. The functions work with any input format and preserve the input type:

```clojure
(def blue "#3498db")
(def blue-css "rgb(52, 152, 219)")

;; Make it 20% lighter - works with any format
(color/lighten blue 0.2)         ; hex input -> hex output
;=> "#5dade2"

(color/lighten blue-css 0.2)     ; CSS RGB input -> hex output  
;=> "#5dade2"

(color/lighten [52 152 219] 0.2) ; RGB vector input -> RGB vector output
;=> [93 173 226]

;; Make it 30% darker  
(color/darken blue 0.3)
;=> "#2471a3"

(color/darken blue-css 0.3)      ; CSS input also works
;=> "#2471a3"

;; Chain operations - seamless format handling
(-> blue
    (color/lighten 0.1)
    (color/darken 0.05))
;=> "#4fa3d6"
```

### Saturation

Control the intensity of colors. These functions also work with CSS color strings:

```clojure
(def orange "#ff8c00")
(def orange-css "rgb(255, 140, 0)")

;; More vivid - works with any input format
(color/saturate orange 0.2)      ; hex input
;=> "#ff7700"

(color/saturate orange-css 0.2)  ; CSS RGB input
;=> "#ff7700"

;; More muted
(color/desaturate orange 0.4)
;=> "#cc9633"

(color/desaturate "rgba(255, 140, 0, 0.8)" 0.4) ; CSS RGBA input
;=> "#cc9633"

;; Complete desaturation = grayscale
(color/desaturate orange 1.0)
;=> "#999999"
```

### Hue Shifting

Rotate colors around the color wheel:

```clojure
(def red "#e74c3c")

;; Shift 60 degrees toward orange
(color/adjust-hue red 60)
;=> "#e7e73c"

;; Complement (180 degrees)
(color/adjust-hue red 180)
;=> "#3ce7e7"

;; Or use the convenience function
(color/complementary red)
;=> "#3ce7e7"
```

## Chapter 3: Accessibility and Contrast

### Understanding Contrast Ratios

WCAG defines minimum contrast ratios for accessibility:
- **AA Normal**: 4.5:1 minimum
- **AA Large**: 3:1 minimum  
- **AAA Normal**: 7:1 minimum

```clojure
;; Check contrast ratio
(color/contrast-ratio "#000000" "#ffffff")
;=> 21.0  ; Perfect contrast

(color/contrast-ratio "#3498db" "#ffffff") 
;=> 3.14  ; Not enough for AA normal text

(color/contrast-ratio "#2c3e50" "#ffffff")
;=> 12.63 ; Excellent contrast
```

### Accessibility Checking

```clojure
(def background "#f8f9fa")
(def text-color "#6c757d")

;; Check different standards
(color/accessible? background text-color :aa)
;=> false

(color/accessible? background text-color :aa-large)  
;=> true

(color/accessible? background text-color :aaa)
;=> false
```

### Finding Accessible Colors

Automatically find accessible alternatives:

```clojure
(def bg "#3498db")
(def desired-text "#e74c3c")

;; Find an accessible version of the text color
(color/find-accessible-color bg desired-text :aa)
;=> "#b71c1c"  ; Darker red that meets AA standards

;; For large text (lower requirements)
(color/find-accessible-color bg desired-text :aa-large)
;=> "#c62828"  ; Slightly lighter option
```

## Chapter 4: Palette Generation

### Color Harmony

Generate harmonious color combinations based on color theory:

```clojure
(def base-color "#e74c3c")

;; Complementary (opposite on color wheel)
(color/complementary base-color)
;=> "#3ce7e7"

;; Triadic (three colors forming triangle)
(color/triadic base-color)
;=> ["#4ce73c" "#3c4ce7"]

;; Split complementary (base + two colors near complement)
(color/split-complementary base-color)
;=> ["#3ce77b" "#3c7be7"]

;; Analogous (adjacent colors)
(color/analogous base-color 4 30)  ; 4 colors, 30° apart
;=> ["#e7743c" "#e7ac3c" "#e7e43c" "#afe73c"]
```

### Monochromatic Palettes

Create palettes using variations of a single hue:

```clojure
(color/monochromatic "#3498db" 5)
;=> ["#2980b9" "#3498db" "#5dade2" "#85c1e9" "#aed6f1"]
```

### Theme Generation

Use the palette generator for different strategies:

```clojure
;; Generate a triadic theme
(color/generate-palette :triadic "#e74c3c")
;=> ["#4ce73c" "#3c4ce7"]

;; Generate analogous theme with custom options
(color/generate-palette :analogous "#3498db" {:count 5 :angle 20})
;=> ["#3498db" "#34b8db" "#34dbd8" "#34dbaf" "#34db87"]

;; Random palette for experimentation
(color/generate-palette :random nil {:count 3})
;=> ["#a47f3c" "#2e8b57" "#ff6347"]
```

## Chapter 5: Color Analysis

### Basic Properties

Analyze color characteristics:

```clojure
(def navy "#2c3e50")
(def coral "#ff6b6b")

;; Brightness (perceived luminance)
(color/brightness navy)   ;=> 60  (dark)
(color/brightness coral)  ;=> 122 (medium)

;; Boolean checks
(color/dark? navy)        ;=> true
(color/light? coral)      ;=> false
(color/warm? coral)       ;=> true (red family)
(color/cool? navy)        ;=> true (blue family)
```

### Vibrancy and Saturation

```clojure
(def vibrant "#ff1744")  ; Hot pink
(def muted "#8d6e63")    ; Brown grey

(color/vibrant? vibrant)  ;=> true
(color/muted? muted)      ;=> true

;; Custom thresholds
(color/vibrant? "#ff6b6b" 70)  ; 70% saturation threshold
;=> false
```

### Color Distance

Compare colors mathematically:

```clojure
;; Euclidean distance in RGB space
(color/color-distance "#ff0000" "#ff0011")
;=> 17.0

;; Check similarity within threshold
(color/similar? "#ff0000" "#ff0011" 20)
;=> true

(color/similar? "#ff0000" "#00ff00" 50)  
;=> false  ; Too different
```

## Chapter 6: Real-World Examples

### Example 1: Theme Generator

```clojure
(defn generate-app-theme [primary-color]
  (let [complementary (color/complementary primary-color)
        analogous (color/analogous primary-color 2 30)]
    {:primary primary-color
     :secondary (first analogous)
     :accent (second analogous)
     :danger "#e74c3c"
     :success "#27ae60"
     :warning "#f39c12"
     :info "#3498db"
     :light (color/lighten primary-color 0.4)
     :dark (color/darken primary-color 0.3)}))

(generate-app-theme "#9b59b6")
;=> {:primary "#9b59b6"
;    :secondary "#59b69b" 
;    :accent "#5959b6"
;    :danger "#e74c3c"
;    ...}
```

### Example 2: Accessibility Checker

```clojure
(defn check-color-accessibility [bg-color text-colors]
  (for [text-color text-colors]
    (let [ratio (color/contrast-ratio bg-color text-color)]
      {:color text-color
       :ratio ratio
       :aa-normal (>= ratio 4.5)
       :aa-large (>= ratio 3.0)
       :aaa-normal (>= ratio 7.0)})))

(check-color-accessibility "#ffffff" 
                          ["#000000" "#333333" "#666666" "#999999"])
;=> ({:color "#000000", :ratio 21.0, :aa-normal true, :aa-large true, :aaa-normal true}
;    {:color "#333333", :ratio 12.63, :aa-normal true, :aa-large true, :aaa-normal true}
;    {:color "#666666", :ratio 5.74, :aa-normal true, :aa-large true, :aaa-normal false}
;    {:color "#999999", :ratio 2.85, :aa-normal false, :aa-large false, :aaa-normal false})
```

### Example 3: Dynamic Color Scaling

```clojure
(defn create-color-scale [start-color end-color steps]
  (for [i (range steps)]
    (let [ratio (/ i (dec steps))]
      (color/mix start-color end-color ratio))))

(create-color-scale "#3498db" "#e74c3c" 5)
;=> ("#3498db" "#5d7bc9" "#855fb7" "#ad42a4" "#e74c3c")
```

## Next Steps

- Explore the complete API reference
- Try building your own color utilities
- Experiment with different color harmonies
- Build accessibility checking into your applications
- Create dynamic themes based on user preferences

## Tips and Best Practices

1. **Always validate user input** when accepting colors from external sources
2. **Consider accessibility** from the start of your design process
3. **Use semantic color names** in your application (primary, secondary, etc.)
4. **Test color combinations** across different devices and lighting conditions
5. **Provide fallbacks** for color-blind users
6. **Cache expensive operations** like palette generation when possible
