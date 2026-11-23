# Introduction to jon/color-tools

`jon/color-tools` is a color toolkit for Clojure and ClojureScript.

## Library Scope

Convert color formats, generate palettes, check contrast and build theme
tokens from the `jon.color-tools` namespace.

## Design Philosophy

### Color Operations

Transform colors without mutating the input. Random color and palette
generation use runtime randomness; other color calculations are deterministic.

### Cross-Platform
Run the API on Clojure (JVM), ClojureScript (browser or Node.js), or Babashka.

### Format Agnostic
Pass hex strings, CSS RGB/RGBA strings, vectors or colorspace records.
Manipulation functions preserve the input shape where their return type
allows it; see each function's API entry for exceptions.

### Broad API
One namespace covers format conversions, color theory, accessibility, blending,
gradients, and theme tokens.

## Core Concepts

### Color Formats

Choose the representation that matches your input:

- **HEX**: Strings like `"#ff5733"` or `"#f53"`
- **RGB**: Vectors like `[255, 87, 51]` with values 0-255
- **RGBA**: Vectors like `[255, 87, 51, 0.8]` with alpha 0-1
- **CSS RGB**: CSS color strings like `"rgb(255, 87, 51)"` or `"rgb(255 87 51)"`
- **CSS RGBA**: CSS color strings like `"rgba(255, 87, 51, 0.8)"` or `"rgba(255 87 51 0.8)"`
- **HSL**: Vectors like `[9, 100, 60]` (Hue 0-360, Saturation/Lightness 0-100)
- **HSV**: Vectors like `[9, 80, 100]` (Hue 0-360, Saturation/Value 0-100)

Convert HSL and HSV vectors with `hsl->rgb` and `hsv->rgb` before using
functions that take RGB input. For Oklab and OKLCh coordinates and records,
see the [API reference](api-reference.md#rgb-oklab).

### Format-Preserving Operations

Most manipulation functions preserve the input format:

```clojure
(lighten "#ff5733" 0.2)  ; Returns hex string
;=> "#ffab99"

(lighten [255 87 51] 0.2) ; Returns RGB vector  
;=> [255 171 153]
```

### Color Theory Integration

Generate color relationships with these palette functions:

- **Complementary colors**: Colors opposite on the color wheel
- **Triadic harmonies**: Three colors equally spaced on the color wheel
- **Analogous colors**: Colors adjacent on the color wheel
- **Monochromatic palettes**: Variations of a single hue
- **Accessible theme tokens**: UI roles generated from one brand color

### Accessibility Focus

Use WCAG contrast calculations to check text and choose foreground colors:

- Contrast ratio calculations
- Accessibility checking for AA and AAA standards
- Automatic accessible color finding
- Accessible light and dark theme generation
- CSS custom property export for frontend apps

## Common Use Cases

Build theme CSS from a brand color, generate chart palettes, or check text
against a background. The [workflow guide](workflow-guide.md) includes
examples for UI themes, lighting controls and color matching.

## Getting Started

Require the namespace and call the functions:

```clojure
(require '[jon.color-tools :as color])

;; Convert formats
(color/->rgb "#3498db")          ; hex to RGB
;=> [52 152 219]

(color/->rgb "rgb(52, 152, 219)") ; CSS string to RGB
;=> [52 152 219]

(color/->hex [52 152 219])       ; RGB to hex
;=> "#3498db"

;; Manipulate colors
(color/lighten "#3498db" 0.2)           ; hex input
;=> "#8bc4ea"

(color/lighten "rgb(52, 152, 219)" 0.2) ; CSS string input
;=> "#8bc4ea"

;; Generate palettes
(color/analogous "#3498db" 3)
;=> ["#3445db" "#7734db" "#cb34db"]

;; Check accessibility
(color/accessible? "#3498db" "#ffffff")
;=> false ; Doesn't meet AA standards

(color/contrast-ratio "#3498db" "#ffffff")  
;=> 3.15  ; Needs 4.5 for AA

;; Generate theme tokens
(def theme (color/accessible-theme "#3498db" {:mode :light :level :aa}))

(:on-primary theme)
;=> "#000000"

(color/theme->css-vars theme)
;=> {"--ct-primary" "#3498db", "--ct-on-primary" "#000000", ...}
```

## Performance Considerations

Keep intermediate color coordinates at full precision and round at display
or export. Measure repeated conversions and palette generation with your
own inputs before adding a cache. Cache deterministic calculations when the
same inputs recur; random generators produce new samples on each call.

## Examples and Tutorials

- [README](../README.md): installation and quick start.
- [Tutorial](tutorial.md): conversions, adjustments and contrast checks.
- [API reference](api-reference.md): input types, return values and precision.
