# Introduction to jon/color-tools

`jon/color-tools` is a comprehensive color manipulation library for Clojure and ClojureScript that provides everything you need to work with colors in your applications.

## What is color-tools?

Color manipulation is a common need in many applications - from web development to data visualization, design tools, and accessibility compliance. `jon/color-tools` provides a unified, functional API for all your color needs.

## Design Philosophy

The library follows these core principles:

### Functional and Pure
All functions are pure with no side effects. Colors in, colors out.

### Cross-Platform
Works identically in Clojure (JVM) and ClojureScript (browser/Node.js) environments.

### Format Agnostic
Functions accept colors in multiple formats (hex strings, RGB vectors) and return results in the same format, making it easy to integrate into existing codebases.

### Comprehensive
From basic format conversions to advanced color theory and accessibility compliance - all in one library.

## Core Concepts

### Color Formats

The library supports several color formats:

- **HEX**: Strings like `"#ff5733"` or `"#f53"`
- **RGB**: Vectors like `[255, 87, 51]` with values 0-255
- **RGBA**: Vectors like `[255, 87, 51, 0.8]` with alpha 0-1
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

### Accessibility Focus

Built-in support for WCAG (Web Content Accessibility Guidelines) compliance:

- Contrast ratio calculations
- Accessibility checking for AA and AAA standards
- Automatic accessible color finding

## Common Use Cases

### Web Development
- Theme generation and color scheme management
- Accessibility compliance checking
- Dynamic color manipulation based on user preferences

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

The quickest way to get started is to require the namespace and try some basic operations:

```clojure
(require '[jon.color-tools :as color])

;; Convert formats
(color/hex->rgb "#3498db")
;=> [52 152 219]

;; Manipulate colors
(color/lighten "#3498db" 0.2)
;=> "#5dade2"

;; Generate palettes
(color/analogous "#3498db" 3)
;=> ["#34c6db" "#34dbae" "#3adb34"]

;; Check accessibility
(color/accessible? "#3498db" "#ffffff")
;=> false ; Doesn't meet AA standards

(color/contrast-ratio "#3498db" "#ffffff")  
;=> 3.14 ; Needs 4.5 for AA
```

## Architecture

The library is organized into several functional areas:

### Validation and Normalization
Functions to validate color formats and normalize values (e.g., expanding 3-digit hex codes).

### Format Conversions
Bidirectional conversion functions between all supported color formats.

### Color Manipulation
Functions to modify colors: lighten, darken, saturate, adjust hue, invert, etc.

### Color Analysis
Functions to analyze color properties: brightness, warmth, vibrancy, etc.

### Palette Generation
Functions implementing color theory for generating harmonious color combinations.

### Accessibility
Functions for WCAG compliance checking and accessible color generation.

### Color Mixing and Blending
Functions for combining colors mathematically.

## Performance Considerations

The library is designed for performance:

- Pure functions enable easy caching and memoization
- Minimal external dependencies
- Efficient algorithms for color space conversions
- No unnecessary object creation

## Examples and Tutorials

For comprehensive examples and tutorials, see the main [README.md](../README.md) file.

For advanced usage patterns and best practices, explore the examples in the test suite.
