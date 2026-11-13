# Introduction to jon/color-tools

`jon/color-tools` is a color toolkit for Clojure and ClojureScript.

## Library Scope

Clojure apps often need format conversion, palette generation, contrast checks,
and theme tokens. `jon/color-tools` puts those operations in one namespace.

## Design Philosophy

The library follows these core principles:

### Functional and Pure
Every function is pure: it takes colors and returns new colors without side
effects.

### Cross-Platform
The same API runs in Clojure (JVM) and ClojureScript (browser and Node.js).

### Format Agnostic
Functions accept colors in multiple formats, including hex strings and RGB
vectors, and return the same shape when the operation can preserve it.

### Broad API
One namespace covers format conversions, color theory, accessibility, blending,
gradients, and theme tokens.

## Core Concepts

### Color Formats

The library supports several color formats:

- **HEX**: Strings like `"#ff5733"` or `"#f53"`
- **RGB**: Vectors like `[255, 87, 51]` with values 0-255
- **RGBA**: Vectors like `[255, 87, 51, 0.8]` with alpha 0-1
- **CSS RGB**: CSS color strings like `"rgb(255, 87, 51)"` or `"rgb(255 87 51)"`
- **CSS RGBA**: CSS color strings like `"rgba(255, 87, 51, 0.8)"` or `"rgba(255 87 51 0.8)"`
- **HSL**: Vectors like `[9, 100, 60]` (Hue 0-360, Saturation/Lightness 0-100)
- **HSV**: Vectors like `[9, 80, 100]` (Hue 0-360, Saturation/Value 0-100)

### Format-Preserving Operations

Most manipulation functions preserve the input format:

```clojure
(lighten "#ff5733" 0.2)  ; Returns hex string
;=> "#ff8566"

(lighten [255 87 51] 0.2) ; Returns RGB vector  
;=> [255 133 102]
```

### Color Theory Integration

The library implements color theory concepts like:

- **Complementary colors**: Colors opposite on the color wheel
- **Triadic harmonies**: Three colors equally spaced on the color wheel
- **Analogous colors**: Colors adjacent on the color wheel
- **Monochromatic palettes**: Variations of a single hue
- **Accessible theme tokens**: UI roles generated from one brand color

### Accessibility Focus

Built-in support for WCAG (Web Content Accessibility Guidelines) compliance:

- Contrast ratio calculations
- Accessibility checking for AA and AAA standards
- Automatic accessible color finding
- Accessible light and dark theme generation
- CSS custom property export for frontend apps

## Common Use Cases

### Web Development
- Theme generation and color scheme management
- Accessibility compliance checking
- Dynamic color manipulation based on user preferences
- CSS variable generation for app-wide design tokens

### Data Visualization
- Generating color scales and palettes
- Ensuring sufficient contrast for readability
- Creating harmonious color schemes for charts and graphs

### Design Tools
- Color picker implementations
- Palette generation and management
- Color format conversions for different design software

### Accessibility
- Ensuring text meets contrast requirements
- Finding accessible alternatives for problematic color combinations
- Automated accessibility testing

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
;=> "#5dade2"

(color/lighten "rgb(52, 152, 219)" 0.2) ; CSS string input
;=> "#5dade2"

;; Generate palettes
(color/analogous "#3498db" 3)
;=> ["#34c6db" "#34dbae" "#3adb34"]

;; Check accessibility
(color/accessible? "#3498db" "#ffffff")
;=> false ; Doesn't meet AA standards

(color/contrast-ratio "#3498db" "#ffffff")  
;=> 3.14 ; Needs 4.5 for AA

;; Generate app-ready accessible theme tokens
(def theme (color/accessible-theme "#3498db" {:mode :light :level :aa}))

(:on-primary theme)
;=> "#000000"

(color/theme->css-vars theme)
;=> {"--ct-primary" "#3498db", "--ct-on-primary" "#000000", ...}
```

## Architecture

The namespace groups functions by task:

### Validation and Normalization
Validate color formats and normalize values, such as expanding 3-digit hex
codes.

### Format Conversions
Convert between supported color formats.

### Color Manipulation
Modify colors with `lighten`, `darken`, `saturate`, `adjust-hue`, `invert`,
and related functions.

### Color Analysis
Analyze brightness, warmth, vibrancy, and related properties.

### Palette Generation
Generate color harmonies and palette variants.

### Accessibility
Check WCAG contrast and find accessible alternatives.

### Design Tokens
Generate accessible UI theme tokens and export CSS custom properties.

### Color Mixing and Blending
Combine colors with mixing, blending, and alpha compositing.

## Performance Considerations

The implementation keeps runtime overhead low:

- Pure functions work well with caching and memoization
- Minimal external dependencies
- Direct algorithms for color space conversions
- No unnecessary object creation

## Examples and Tutorials

For examples and installation notes, see [README.md](../README.md).

For design system, accessibility, and lighting workflows, see the workflow
guide. The test suite has more examples.
