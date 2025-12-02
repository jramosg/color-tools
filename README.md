# jon/color-tools

A comprehensive color manipulation library for Clojure and ClojureScript. Provides utilities for color format conversions, color manipulation, palette generation, accessibility checking, and color analysis.

[![Clojars Project](https://img.shields.io/clojars/v/com.github.jramosg/color-tools.svg)](https://clojars.org/com.github.jramosg/color-tools)

## Overview

`jon/color-tools` is designed to be a complete toolkit for working with colors in Clojure applications. Whether you're building web applications, data visualizations, or design tools, this library provides the essential color utilities you need.

### Key Features

- **Type Safety**: Proper `Color` record type for better APIs and type safety
- **Format Conversions**: Convert between HEX, RGB, RGBA, HSL, and HSV color formats
- **Color Manipulation**: Lighten, darken, saturate, desaturate, invert, and adjust hue
- **Palette Generation**: Create harmonious color palettes using various color theory principles
- **Accessibility**: WCAG-compliant contrast checking and accessible color finding
- **Color Analysis**: Determine color properties like brightness, warmth, and vibrancy
- **Cross-platform**: Works in both Clojure (JVM) and ClojureScript (browser/Node.js)

## Installation

Add the following dependency to your `deps.edn`:

```clojure
{:deps com.github.jramosg/color-tools {:mvn/version "1.0.4"}}
```

Or for Leiningen, add to your `project.clj`:

```clojure
[com.github.jramosg/color-tools "1.0.4"]
```

## Quick Start

```clojure
(require '[jon.color-tools :as color])

;; Convert between formats - universal conversion functions
(color/->rgb "#ff5733")        ; hex to RGB
;=> [255 87 51]

(color/->rgb [255 87 51])      ; RGB vector (passthrough)
;=> [255 87 51]

(color/->rgb "rgb(255, 87, 51)") ; CSS RGB string to RGB
;=> [255 87 51]

(color/->hex [255 87 51])      ; RGB to hex
;=> "#ff5733"

(color/->hex "#ff5733")        ; hex (passthrough)
;=> "#ff5733"

(color/->hex "rgba(255, 87, 51, 0.8)") ; CSS RGBA string to hex
;=> "#ff5733"

;; Manipulate colors
(color/lighten "#ff5733" 0.2)
;=> "#ff8566"

(color/darken [255 87 51] 0.3)
;=> [178 61 36]

;; Generate palettes
(color/triadic "#ff5733")
;=> ["#33ff57" "#5733ff"]

;; Check accessibility
(color/accessible? "#000000" "#ffffff")
;=> true

(color/contrast-ratio "#ff5733" "#ffffff")
;=> 3.07
```

## Usage Examples

### Working with the Color Record Type

The library includes a proper `Color` datatype that provides type safety and cleaner APIs:

```clojure
;; Create Color records from various inputs
(color/color 255 0 0)           ; RGB values -> Color record
(color/color "#ff0000")         ; Hex string -> Color record  
(color/color [255 0 0])         ; RGB vector -> Color record
(color/color [255 0 0 0.8])     ; RGBA vector -> Color record
(color/color "rgb(255, 0, 0)")  ; CSS RGB string -> Color record
(color/color "rgba(255, 0, 0, 0.8)") ; CSS RGBA string -> Color record
(color/color (color/color 255 0 0)) ; From existing Color record -> Color record

;; Convert any color type to different formats
(def red-color (color/color 255 0 0))
(color/->hex red-color)         ;=> "#ff0000"
(color/->hex "#ff0000")         ;=> "#ff0000" (passthrough)
(color/->hex [255 0 0])         ;=> "#ff0000"
(color/->hex "rgb(255, 0, 0)")  ;=> "#ff0000"

(color/->rgb red-color)         ;=> [255 0 0]
(color/->rgb "#ff0000")         ;=> [255 0 0]
(color/->rgb [255 0 0])         ;=> [255 0 0] (passthrough)
(color/->rgb "rgb(255, 0, 0)")  ;=> [255 0 0]

(color/->hsl red-color)         ;=> [0 100 50]
(color/->hsl "#ff0000")         ;=> [0 100 50]
(color/->hsl [255 0 0])         ;=> [0 100 50]
(color/->hsl "rgb(255, 0, 0)")  ;=> [0 100 50]

(color/->rgba red-color)        ;=> [255 0 0 1.0]
(color/->rgba [255 0 0 128])    ;=> [255 0 0 128] (RGBA passthrough)
(color/->rgba "rgba(255, 0, 0, 0.5)") ;=> [255 0 0 0.5]

;; Colors work seamlessly with all library functions
(color/lighten red-color 0.2)   ;=> Color record (lighter red)
(color/contrast-ratio red-color (color/color "#ffffff"))  ;=> 4.0

;; String representation
(str red-color)                 ;=> "#ff0000"
(str (color/color 255 0 0 0.5)) ;=> "rgba(255,0,0,0.5)"
```

### CSS Color String Support

The library fully supports CSS color strings with flexible formatting:

```clojure
;; CSS RGB strings - various formats supported
(color/->rgb "rgb(255, 87, 51)")     ; comma-separated
;=> [255 87 51]

(color/->rgb "rgb(255 87 51)")       ; space-separated  
;=> [255 87 51]

(color/->rgb "RGB( 255 , 87 , 51 )") ; case-insensitive, extra spaces
;=> [255 87 51]

;; CSS RGBA strings - with alpha channel
(color/->rgba "rgba(255, 87, 51, 0.8)")  ; with alpha
;=> [255 87 51 0.8]

(color/->rgba "rgba(255 87 51 0.5)")     ; space-separated with alpha
;=> [255 87 51 0.5]

(color/->rgba "RGBA( 255 , 87 , 51 , .7 )") ; decimal alpha, extra spaces
;=> [255 87 51 0.7]

;; CSS strings work with all functions
(color/lighten "rgb(255, 0, 0)" 0.2)     ; manipulation
;=> "#ff6666"

(color/accessible? "rgb(255, 255, 255)" "rgb(0, 0, 0)") ; accessibility  
;=> true

(color/complementary "rgba(255, 0, 0, 0.5)") ; harmony
;=> "#00ffff"
```

### Universal Format Conversions

```clojure
;; Universal conversion functions work with any color input type
(color/->rgb "#ff5733")        ; hex to RGB
;=> [255 87 51]

(color/->rgb [255 87 51])      ; RGB vector (passthrough)
;=> [255 87 51]

(color/->rgb "rgb(255, 87, 51)") ; CSS RGB string to RGB
;=> [255 87 51]

(color/->rgb (color/color 255 87 51))  ; Color record to RGB
;=> [255 87 51]

(color/->hex [255 87 51])      ; RGB to hex
;=> "#ff5733"

(color/->hex "#ff5733")        ; hex (passthrough)
;=> "#ff5733"

(color/->hex "rgb(255, 87, 51)") ; CSS RGB string to hex
;=> "#ff5733"

(color/->hsl [255 87 51])      ; RGB to HSL
;=> [9 100 60]

(color/->hsl "#ff5733")        ; hex to HSL
;=> [9 100 60]

(color/->hsl "rgba(255, 87, 51, 0.8)") ; CSS RGBA string to HSL
;=> [9 100 60]

;; Traditional format-specific functions still available
(color/hex->rgb "#ff5733")     ; specific hex to RGB
;=> [255 87 51]

(color/rgb->hex [255 87 51])   ; specific RGB to hex
;=> "#ff5733"

;; Normalize short hex codes
(color/normalize-hex "#f53")
;=> "#ff5533"
```

### Color Manipulation

```clojure
;; Works with Color records, hex strings, or RGB vectors
(def blue (color/color "#3498db"))

;; Lighten and darken - returns same type as input
(color/lighten blue 0.2)               ;=> Color record (lighter blue)
(color/lighten "#3498db" 0.2)          ;=> "#5dade2" 
(color/darken [52 152 219] 0.3)        ;=> [36 106 153]

;; Adjust saturation  
(color/saturate blue 0.4)              ;=> Color record (more saturated)
(color/desaturate "#e74c3c" 0.3)       ;=> "#d66357"

;; Invert colors
(color/invert blue)                    ;=> Color record (inverted)
(color/invert "#ff0000")               ;=> "#00ffff"
```

### Palette Generation

```clojure
;; Complementary colors
(color/complementary "#3498db")
;=> "#db7634"

;; Triadic harmony
(color/triadic "#3498db")
;=> ["#34db76" "#db3498"]

;; Analogous colors
(color/analogous "#3498db" 4 20)  ; 4 colors, 20° apart
;=> ["#34c6db" "#34dbae" "#3adb34" "#68db34"]

;; Monochromatic palette
(color/monochromatic "#3498db" 5)
;=> ["#3498db" "#5ba7e0" "#7fb6e5" "#a3c5ea" "#c7d4ef"]

;; Generate themed palettes
(color/generate-palette :triadic "#e74c3c")
;=> ["#4ce7a0" "#a04ce7"]

(color/generate-palette :split-complementary "#2ecc71" {:angle 25})
;=> ["#cc2e99" "#2e59cc"]
```

### Accessibility and Contrast

```clojure
;; Check WCAG compliance
(color/accessible? "#ffffff" "#000000")          ; AA standard
;=> true

(color/accessible? "#ffffff" "#777777" :aaa)     ; AAA standard
;=> false

(color/accessible? "#ffffff" "#777777" :aa-large) ; AA Large text
;=> true

;; Calculate contrast ratios
(color/contrast-ratio "#ffffff" "#000000")
;=> 21.0

(color/contrast-ratio "#3498db" "#ffffff")
;=> 3.14

;; Find accessible colors automatically
(color/find-accessible-color "#3498db" "#e74c3c" :aa)
;=> "#b71c1c"  ; Darkened version that meets AA standards
```

### Color Analysis

```clojure
;; Analyze color properties
(color/brightness "#3498db")
;=> 123

(color/dark? "#2c3e50")
;=> true

(color/light? "#ecf0f1")
;=> true

(color/warm? "#e74c3c")    ; Red colors are warm
;=> true

(color/cool? "#3498db")    ; Blue colors are cool
;=> true

(color/vibrant? "#e74c3c") ; High saturation
;=> true

(color/muted? "#95a5a6")   ; Low saturation
;=> true

;; Color distance and similarity
(color/color-distance "#ff0000" "#ff0011")
;=> 17.0

(color/similar? "#ff0000" "#ff0011" 20)
;=> true
```

### Working with Color Names

```clojure
;; Convert color names to hex
(color/name->hex "red")
;=> "#ff0000"

(color/name->hex "navy")
;=> "#000080"

;; Find closest color name from any color input type
(color/->name "#ff1100")              ; hex string
;=> "red"

(color/->name [255 17 0])             ; RGB vector
;=> "red"

(color/->name (color/color 255 17 0)) ; Color record
;=> "red"

;; Legacy function still available
(color/hex->name "#ff1100")
;=> "red"
```

### Color Mixing

```clojure
;; Mix two colors
(color/mix "#ff0000" "#0000ff")        ; 50/50 mix
;=> "#800080"

(color/mix "#ff0000" "#0000ff" 0.25)   ; 25% first color, 75% second
;=> "#4000bf"
```

## API Documentation

Key namespaces:

- `jon.color-tools` - Main API with all color manipulation functions

## Development

### Running Tests

```bash
clojure -T:build test
```

### Code Quality

#### Formatting
Format all Clojure files using cljfmt:
```bash
clojure -Tcljfmt fix
```

#### Linting
Lint the codebase using clj-kondo:
```bash
clojure -Sdeps '{:deps {clj-kondo/clj-kondo {:mvn/version "RELEASE"}}}' -M -m clj-kondo.main --lint src test
```

### Building

```bash
# Run CI pipeline and build JAR
clojure -T:build ci

# Install locally
clojure -T:build install

# Deploy to Clojars (requires CLOJARS_USERNAME and CLOJARS_PASSWORD)
clojure -T:build deploy
```

## Supported Platforms

- **Clojure (JVM)**: Full support for all features
- **ClojureScript**: Full support for all features in browser and Node.js environments

## Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests on [GitHub](https://github.com/jon/color-tools).

## Acknowledgements

This library draws inspiration from various color manipulation libraries across different programming languages:

- [Sass color functions](https://sass-lang.com/documentation/modules/color) - Advanced color manipulation and modern color space support
- [Chroma.js](https://gka.github.io/chroma.js/) for JavaScript - Color scales, interpolation, and Delta E calculations
- [colorsys](https://github.com/vaab/colour) for Python - Color space conversions and color picking algorithms

### Color Blending and Transparency

```clojure
;; Blending modes for design work
(color/blend-multiply "#ff0000" "#0000ff")    ; Darkens
;=> "#000000"

(color/blend-screen "#800000" "#000080")      ; Lightens
;=> "#800080"

(color/blend-overlay "#ff0000" "#808080")     ; Combines multiply/screen
;=> "#ff0000"

;; Tints, shades, and tones for design systems
(color/tint "#3498db" 0.3)                    ; Mix with white
;=> "#6fb3e5"

(color/shade "#3498db" 0.3)                   ; Mix with black
;=> "#236a91"

(color/tone "#3498db" 0.3)                    ; Mix with gray
;=> "#4d98c3"

;; Generate series of variations
(color/tints "#e74c3c" 5)
;=> ["#eb6757" "#ef8272" "#f39c8d" "#f7b7a8" "#fbd2c3"]

(color/shades "#e74c3c" 3)
;=> ["#b83d30" "#892d24" "#5a1e18"]

;; Alpha blending with proper compositing
(color/alpha-blend [255 0 0 0.8] [0 0 255 0.6])
;=> [102 0 153 0.92]

(color/with-alpha "#ff0000" 0.5)
;=> "rgba(255,0,0,0.5)"
```

### Color Interpolation and Gradients

```clojure
;; Interpolate between colors
(color/interpolate ["#ff0000" "#0000ff"] 0.5 :rgb)
;=> "#800080"  ; Middle point

;; Multi-color interpolation
(color/interpolate ["#ff0000" "#00ff00" "#0000ff"] 0.5 :hsl)
;=> "#00ff00"  ; At the green midpoint

;; Generate smooth gradients
(color/gradient ["#ff0000" "#0000ff"] 5 :rgb)
;=> ["#ff0000" "#bf003f" "#80007f" "#4000bf" "#0000ff"]

;; Use HSL for more natural color transitions
(color/gradient ["#ff0000" "#ffff00"] 3 :hsl)
;=> ["#ff0000" "#ff8000" "#ffff00"]
```

### Perceptual Color Difference

```clojure
;; More accurate than RGB distance
(color/delta-e "#ff0000" "#fe0000")
;=> 1.2  ; Small difference (barely perceptible)

(color/delta-e "#ff0000" "#0000ff")
;=> 176.5  ; Large difference

;; Check perceptual similarity
(color/perceptually-similar? "#ff0000" "#fe0101")
;=> true  ; Within JND threshold

(color/perceptually-similar? "#ff0000" "#0000ff")
;=> false

;; Convert to LAB color space
(color/rgb->lab [255 0 0])
;=> [53.24 80.09 67.20]
```

### Color Temperature (Kelvin)

```clojure
;; Convert temperature to color (1000K - 40000K)
(color/kelvin->rgb 1800)      ; Candlelight
;=> [255 147 41]

(color/kelvin->hex 3000)      ; Warm white
;=> "#ffd6aa"

(color/kelvin->rgb 6500)      ; Daylight
;=> [255 249 253]

(color/kelvin->color 10000)   ; Cool/blue
;=> Color record

;; Approximate temperature from color
(color/rgb->kelvin [255 147 41])
;=> 1856  ; Close to original 1800K
```

### Planned Features (Inspired by these libraries)

**Advanced Color Spaces:**

- ✅ Lab/CIE Lab color space for perceptually uniform calculations (ADDED in v1.1.0)
- LCH (Lightness, Chroma, Hue) cylindrical color space
- OKLab/OKLCh modern perceptually uniform color spaces
- CMYK for print applications
- HWB (Hue, Whiteness, Blackness) color space

Special thanks to the Clojure community for excellent tooling and libraries that made this project possible.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and changes.

## License

Copyright © 2025 Jon

Distributed under the Eclipse Public License version 1.0.

The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html

## CI / Deployment

The repository runs GitHub Actions checks on every branch. Deployments to Clojars are triggered in two ways:

### Automatic Deployment
Deployments only run from the `master` branch after the Checks workflow completes successfully **and** the commit message contains `[deploy]`.

Example:
```bash
git commit -m "Release version 1.0.4 [deploy]"
git push origin master
```

### Manual Deployment
You can manually trigger deployment from the GitHub Actions UI by going to the "Deploy to Clojars" workflow and clicking "Run workflow".

**Requirements:**
- Ensure `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` are set in the repository secrets
- For automatic deploys, include `[deploy]` in your commit message when pushing to `master`
