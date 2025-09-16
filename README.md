# jon/color-tools

A comprehensive color manipulation library for Clojure and ClojureScript. Provides utilities for color format conversions, color manipulation, palette generation, accessibility checking, and color analysis.

[![Clojars Project](https://img.shields.io/clojars/v/com.github.jramosg/color-tools.svg)](https://clojars.org/com.github.jramosg/color-tools)

## Overview

`jon/color-tools` is designed to be a complete toolkit for working with colors in Clojure applications. Whether you're building web applications, data visualizations, or design tools, this library provides the essential color utilities you need.

### Key Features

- **Type Safety**: Proper `Color` record type for better APIs and type safety
- **Format Conversions**: Convert between HEX, RGB, RGBA, HSL, and HSV color formats
- **Backward Compatible**: All existing string/vector-based APIs continue to work
- **Color Manipulation**: Lighten, darken, saturate, desaturate, invert, and adjust hue
- **Palette Generation**: Create harmonious color palettes using various color theory principles
- **Accessibility**: WCAG-compliant contrast checking and accessible color finding
- **Color Analysis**: Determine color properties like brightness, warmth, and vibrancy
- **Cross-platform**: Works in both Clojure (JVM) and ClojureScript (browser/Node.js)

## Installation

Add the following dependency to your `deps.edn`:

```clojure
{:deps com.github.jramosg/color-tools {:mvn/version "1.0.2.1"}}
```

Or for Leiningen, add to your `project.clj`:

```clojure
[com.github.jramosg/color-tools "1.0.2.1"]
```

## Quick Start

```clojure
(require '[jon.color-tools :as color])

;; Convert between formats
(color/hex->rgb "#ff5733")
;=> [255 87 51]

(color/rgb->hex [255 87 51])
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

;; Convert Color records to different formats
(def red-color (color/color 255 0 0))
(color/->hex red-color)         ;=> "#ff0000"
(color/->rgb red-color)         ;=> [255 0 0]
(color/->hsl red-color)         ;=> [0 100 50]
(color/->rgba red-color)        ;=> [255 0 0 1.0]

;; Colors work seamlessly with all library functions
(color/lighten red-color 0.2)   ;=> Color record (lighter red)
(color/contrast-ratio red-color (color/color "#ffffff"))  ;=> 4.0

;; String representation
(str red-color)                 ;=> "#ff0000"
(str (color/color 255 0 0 0.5)) ;=> "rgba(255,0,0,0.5)"
```

### Traditional Format Conversions (Still Supported)

```clojure
;; HEX to RGB
(color/hex->rgb "#ff5733")
;=> [255 87 51]

;; RGB to HEX
(color/rgb->hex [255 87 51])
;=> "#ff5733"

;; RGB to HSL
(color/rgb->hsl [255 87 51])
;=> [9 100 60]

;; HSL to RGB
(color/hsl->rgb [9 100 60])
;=> [255 87 51]

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

;; Find closest color name
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

### Planned Features (Inspired by these libraries)

**Advanced Color Spaces:**

- Lab/CIE Lab color space for perceptually uniform calculations
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
