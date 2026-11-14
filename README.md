# jon/color-tools

A color toolkit for Clojure and ClojureScript. Convert formats, manipulate
colors, generate palettes, check contrast, and build accessible theme tokens.

[![Clojars Project](https://img.shields.io/clojars/v/com.github.jramosg/color-tools.svg)](https://clojars.org/com.github.jramosg/color-tools)
[![cljdoc badge](https://cljdoc.org/badge/com.github.jramosg/color-tools)](https://cljdoc.org/d/com.github.jramosg/color-tools)

## [Live Demo](https://color-tools.jonramos.dev/)

Try the color picker, format converter, gradient generator, WCAG contrast
checker, and color harmonies.

## Overview

`jon/color-tools` gives Clojure apps one namespace for common color work:
format conversion, color adjustment, palettes, contrast checks, gradients, and
theme tokens.

### Demo

**[Live Demo](https://color-tools.jonramos.dev/)**

Run the demo from the `demo` directory:

Options:

Prerequisites

- Node.js + npm
- Java (for the ClojureScript build tool)

Quick local options

- Development (recommended for live-reload during editing):

```bash
# from the repository root
cd demo
npm install
npx shadow-cljs watch app
# open http://localhost:8080
```

- Production build (generates `demo/public`):

```bash
# from the repository root
cd demo
npm install
npx shadow-cljs release app
# then serve the generated files:
python3 -m http.server --directory demo/public 8000
# or
npx http-server demo/public -p 8000
# open http://localhost:8000
```

- **Accessible Theme Generator**: WCAG-aware UI tokens from one brand color
- **CSS Variable Export**: Turn generated tokens into frontend-ready custom properties
- **Blending Modes**: Multiply, screen, overlay (Photoshop-style blending)
- **Tints/Shades/Tones**: Design system color scale generation
- **Alpha Blending**: Porter-Duff compositing for transparent colors
- **Color Interpolation**: Gradients in RGB, HSL, or HSV
- **Perceptual Difference**: Delta E calculations in CIE LAB color space
- **Oklab and OKLCh**: Convert between sRGB, Oklab and OKLCh,
  with gamut checks
- **Color Temperature**: Kelvin to RGB conversions for lighting UIs
- **Palette Generation**: Color harmonies from color theory
- **Accessibility**: WCAG-compliant contrast checking and accessible color finding
- **Color Analysis**: Brightness, warmth, and vibrancy checks
- **Cross-platform**: Works in both Clojure (JVM) and ClojureScript (browser/Node.js)
- **Command line**: [`color-tools`](doc/cli.md) runs every feature from a
  terminal, a script or CI

## Installation

Add the following dependency to your `deps.edn`:

```clojure
{:deps com.github.jramosg/color-tools {:mvn/version "1.3.0"}}
```

Or for Leiningen, add to your `project.clj`:

```clojure
[com.github.jramosg/color-tools "1.3.0"]
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
;=> "#ffac99"

(color/darken [255 87 51] 0.3)
;=> [153 28 0]

;; Generate palettes
(color/triadic "#ff5733")
;=> ["#33ff58" "#5833ff"]

;; Check accessibility
(color/accessible? "#000000" "#ffffff")
;=> true

(color/contrast-ratio "#ff5733" "#ffffff")
;=> 3.15

;; Generate accessible UI tokens from a brand color
(def theme (color/accessible-theme "#3498db" {:mode :light :level :aa}))

(:primary theme)
;=> "#3498db"

(:on-primary theme)
;=> "#000000"

(color/theme->css-vars theme)
;=> {"--ct-primary" "#3498db", "--ct-on-primary" "#000000", ...}
```

## Command Line

The whole library is also a command, `color-tools`, for terminals, shell
scripts and CI. It needs no Clojure code and runs on Node.js, Babashka or
the JVM.

```bash
color-tools contrast '#3498db' '#ffffff' --level aa   # exits 1: 3.15:1
color-tools convert '#3498db' --to oklch
color-tools palette '#3498db' --type triadic
color-tools gradient '#ff0000' '#0000ff' --steps 10 --space hsl
color-tools theme '#3498db' --mode dark --format css > theme.css
```

It prints text with color swatches on a terminal, JSON or EDN for scripts,
and reads colors from stdin. See the [CLI guide](doc/cli.md) for
installation, every command and CI recipes.

## Usage Examples

### Working with the Color Record Type

Use `Color` records when you want type safety instead of passing raw vectors or
strings through your code:

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
(color/->hex "#ff0000")         ;=> "#ff0000"  ; passthrough
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

;; Color records work with the same functions as strings and vectors
(color/lighten red-color 0.2)   ;=> Color record (lighter red)
(color/contrast-ratio red-color (color/color "#ffffff"))  ;=> 4.0

;; String representation
(str red-color)                 ;=> "#ff0000"
(str (color/color 255 0 0 0.5)) ;=> "rgba(255,0,0,0.5)"
```

### CSS Color String Support

The parser accepts common CSS color string formats:

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
;; Universal conversion functions accept any supported color input
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
;=> [11 100 60]

(color/->hsl "#ff5733")        ; hex to HSL
;=> [11 100 60]

(color/->hsl "rgba(255, 87, 51, 0.8)") ; CSS RGBA string to HSL
;=> [11 100 60]

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

;; Lighten and darken, preserving the input type
(color/lighten blue 0.2)               ;=> Color record (lighter blue)
(color/lighten "#3498db" 0.2)          ;=> "#8ac4ea"
(color/darken [52 152 219] 0.3)        ;=> [18 67 100]

;; Adjust saturation
(color/saturate blue 0.4)              ;=> Color record (more saturated)
(color/desaturate "#e74c3c" 0.3)       ;=> "#c6675d"

;; Invert colors
(color/invert blue)                    ;=> Color record (inverted)
(color/invert "#ff0000")               ;=> "#00ffff"
```

### Palette Generation

```clojure
;; Complementary colors
(color/complementary "#3498db")
;=> "#db7633"

;; Triadic harmony
(color/triadic "#3498db")
;=> ["#db3398" "#98db33"]

;; Analogous colors
(color/analogous "#3498db" 4 20)  ; 4 colors, 20° apart
;=> ["#3360db" "#3e33db" "#7633db" "#ae33db"]

;; Monochromatic palette
(color/monochromatic "#3498db" 5)
;=> ["#003d66" "#066aad" "#1096ef" "#5db4ee" "#a6d4f2"]

;; Generate palettes by strategy
(color/generate-palette :triadic "#e74c3c")
;=> ["#3ce74d" "#4d3ce7"]

(color/generate-palette :split-complementary "#2ecc71" {:angle 25})
;=> ["#8acc2e" "#2ecc56"]
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
;=> 3.15

;; Find an accessible color by adjusting lightness
(color/find-accessible-color "#3498db" "#e74c3c" :aa)
;=> "#000000"  ; Black is the first candidate that meets AA
```

### Accessible Theme Generation

```clojure
;; Create WCAG-aware UI tokens from one brand color
(def theme (color/accessible-theme "#3498db" {:mode :light :level :aa}))

(:background theme)    ;=> "#f7fbfe"
(:primary theme)       ;=> "#3498db"
(:on-primary theme)    ;=> "#000000"
(:focus-ring theme)    ;=> "#8433db"

;; Use dark mode or stricter contrast
(color/accessible-theme "#ff7a59" {:mode :dark :level :aaa})

;; Export tokens as CSS custom properties
(color/theme->css-vars theme {:prefix "--brand-"})
;=> {"--brand-primary" "#3498db"
;    "--brand-on-primary" "#000000"
;    "--brand-scale-500" "#3498db"
;    ...}
```

### Color Analysis

```clojure
;; Analyze color properties
(color/brightness "#3498db")
;=> 130

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

(color/mix "#ff0000" "#0000ff" 0.25)   ; 75% first color, 25% second
;=> "#bf0040"
```

## API Documentation

Key namespaces:

- `jon.color-tools` - Main API with all color manipulation functions
- `jon.color-tools.cli` - The command-line interface; see the
  [CLI guide](doc/cli.md)

## Development

### Running Tests

The tests live in `.cljc` files and run on both platforms:

```bash
# Clojure (JVM)
clojure -T:build test

# ClojureScript (Node.js)
clojure -M:test-cljs
```

The ClojureScript run needs Node.js on your `PATH`.

Run the CLI from the checkout with `bin/color-tools`, or
`clojure -M:cli`. `clojure -M:npm` compiles it to `npm/color-tools.js`.

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

### Publishing to npm

The CLI is published manually as `@jramosg/color-tools`. You need the
Clojure CLI, Java, Node.js 18 or newer, npm, and an npm account with
publishing access to the `jramosg` organization.

Check that the version in `npm/package.json` matches `build.clj` and the
`version` definition in `src/jon/color_tools/cli.cljc`. For later releases,
update all three together. Each published package version must be unique.

From the repository root, build and smoke-test the CLI:

```bash
clojure -M:npm
test/cli-smoke.sh node npm/color-tools.js
```

The generated `npm/color-tools.js` is ignored by Git; rebuild it for every
release.

Enter the package directory, check your npm login, and preview the package:

```bash
cd npm
npm whoami
npm pack --dry-run
```

Run `npm login` if needed. Confirm the preview includes `color-tools.js`,
then publish:

```bash
npm publish --access public
```

Verify the published version and run the package from the registry:

```bash
npm view @jramosg/color-tools version
npx --yes @jramosg/color-tools help
```

See the [npm publishing documentation](https://docs.npmjs.com/cli/v11/commands/npm-publish/)
for authentication and publishing options.

## Supported Platforms

- **Clojure (JVM)**: Supports the full API
- **ClojureScript**: Supports the full API in browsers and Node.js

## Contributing

Open issues, feature requests, or pull requests on
[GitHub](https://github.com/jramosg/color-tools).

## Acknowledgements

These projects influenced the API:

- [Sass color functions](https://sass-lang.com/documentation/modules/color) - Color manipulation and modern color spaces
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
;=> "#71b7e6"

(color/shade "#3498db" 0.3)                   ; Mix with black
;=> "#246a99"

(color/tone "#3498db" 0.3)                    ; Mix with gray
;=> "#4b91c0"

;; Generate series of variations
(color/tints "#e74c3c" 5)
;=> ["#ec7063" "#f1948a" "#f5b7b1" "#fadbd8" "#ffffff"]

(color/shades "#e74c3c" 3)
;=> ["#9a3328" "#4d1914" "#000000"]

;; Alpha blending with source-over compositing
(color/alpha-blend [255 0 0 0.8] [0 0 255 0.6])
;=> [89 0 166 0.92]

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

;; Generate gradients
(color/gradient ["#ff0000" "#0000ff"] 5 :rgb)
;=> ["#ff0000" "#bf0040" "#800080" "#4000bf" "#0000ff"]

;; Use HSL for more natural color transitions
(color/gradient ["#ff0000" "#ffff00"] 3 :hsl)
;=> ["#ff0000" "#ff8000" "#ffff00"]
```

### Perceptual Color Difference

```clojure
;; Use perceptual distance instead of RGB distance
(color/delta-e "#ff0000" "#fe0000")
;=> 0.37  ; Under 1.0: imperceptible

(color/delta-e "#ff0000" "#0000ff")
;=> 176.3  ; Large difference

;; Check perceptual similarity
(color/perceptually-similar? "#ff0000" "#fe0101")
;=> true  ; Within JND threshold

(color/perceptually-similar? "#ff0000" "#0000ff")
;=> false

;; Convert to LAB color space
(color/rgb->lab [255 0 0])
;=> [53.24 80.09 67.2]
```

### Oklab Conversion

Use `rgb->oklab` with sRGB channels from `0` to `255`. You get `[l a b]`
coordinates with lightness on a 0 to 1 scale. Use `->rgb` first for hex
strings, CSS strings, or `Color` records. The conversion preserves full
precision; use `round-coords` to format coordinates for display.

```clojure
(-> "#336699"
    color/->rgb
    color/rgb->oklab
    (color/round-coords {:decimal-places 4}))
;=> [0.4993 -0.033 -0.093]

;; Convert back to sRGB or hex
(color/oklab->hex [0.4993 -0.033 -0.093])
;=> "#336699"

(color/in-gamut? [0.9 0.3 0.3])
;=> false

;; OKLCh: lightness, chroma, hue in degrees
(color/round-coords (color/rgb->oklch [255 0 0]) {:decimal-places 4})
;=> [0.628 0.2577 29.2339]

(color/oklch->hex [0.628 0.2577 29.23])
;=> "#ff0000"
```

`oklab->rgb` returns unrounded, unclamped channels; `oklab->hex` clamps colors
outside sRGB. See the [API reference](doc/api-reference.md#rgb-oklab)
for axis definitions, gamut handling, and input requirements.

### Color Temperature (Kelvin)

```clojure
;; Convert temperature to color (1000K - 40000K)
(color/kelvin->rgb 1800)      ; Candlelight
;=> [255 126 0]

(color/kelvin->hex 3000)      ; Warm white
;=> "#ffb16e"

(color/kelvin->rgb 6500)      ; Daylight
;=> [255 254 250]

(color/kelvin->color 10000)   ; Cool/blue
;=> Color record

;; Approximate temperature from color
(color/rgb->kelvin (color/kelvin->rgb 1800))
;=> 1805  ; Close to the original 1800K
```

### Planned Features (Inspired by these libraries)

**Color Spaces:**

- Lab/CIE Lab color space for perceptually uniform calculations (added in
  v1.1.0)
- LCH (Lightness, Chroma, Hue) cylindrical color space
- CMYK for print applications
- HWB (Hue, Whiteness, Blackness) color space

Thanks to the Clojure community for the tooling and libraries behind this
project.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and changes.

## License

Copyright © 2025 Jon

Distributed under the Eclipse Public License version 1.0.

The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html

## CI / Deployment

The repository runs GitHub Actions checks on every branch. Deployments to Clojars are triggered in two ways:

### Automatic Deployment

Deployments run from the `master` branch after the Checks workflow completes
and the commit message contains `[deploy]`.

Example:

```bash
git commit -m "Release version 1.1.0 [deploy]"
git push origin master
```

### Manual Deployment

To deploy from GitHub, open the "Deploy to Clojars" workflow and click
"Run workflow".

**Requirements:**

- Ensure `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` are set in the repository secrets
- For automatic deploys, include `[deploy]` in your commit message when pushing to `master`
