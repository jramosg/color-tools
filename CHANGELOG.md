# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## Unreleased
### Fixed
- The demo converter rejected `rgba()` without an alpha, such as
  `rgba(52, 152, 219)`. CSS treats `rgba()` as an alias of `rgb()`, so it now
  reads it as RGBA with an alpha of 1.
- The demo converter showed "Detected RGB" for invalid input that names
  another format, such as `hsl(400, 50%, 50%)`, with a color made from its
  numbers. Input that starts with a format name now gets only that format's
  parser.
- The demo converter said "No valid color yet" for input that will never be
  valid. Empty input now says "Paste a color to convert" and anything else
  that fails to parse says "Not a valid color".
- `rgb->lab` converted with the sRGB matrix rounded to 4 digits, which put
  Lab values off by up to 0.02: red came out as `[53.233 80.109 67.220]`
  instead of the reference `[53.241 80.092 67.203]`. It now uses the full
  matrix and matches reference values to 0.001. `delta-e` and
  `perceptually-similar?` build on it, so their results shift by a similar
  amount.

## [1.3.0] - 2026-11-13
### Added
- `rgb->oklab` converts sRGB vectors with channels from 0 to 255 into Oklab
  `[l a b]` coordinates at full precision, with lightness on a 0 to 1 scale
- `round-coords` rounds each value in a coordinate vector to a number of
  decimal places or significant digits, for display and export
- `oklab->rgb` converts Oklab coordinates back to sRGB at full precision,
  without clamping, as the exact inverse of `rgb->oklab`
- `oklab->hex` converts Oklab coordinates to a hex string, clamping colors
  outside sRGB
- `in-gamut?` checks whether Oklab coordinates fall inside the sRGB gamut
- `clamp-rgb` clamps computed RGB channels to 0 to 255 before `->hex` or
  `color`
- `oklab->oklch`, `oklch->oklab`, `rgb->oklch`, `oklch->rgb` and `oklch->hex`
  convert to and from OKLCh, the lightness, chroma and hue form CSS `oklch()`
  uses
- The demo shows Oklab and OKLCh in the color picker, and its converter
  accepts `oklab()` and `oklch()` input

### Changed
- Bump Clojure from 1.12.1 to 1.12.6
- Improved the Accessible Theme Generator demo layout for clearer token,
  contrast, and color scale previews
- Expanded cljdoc documentation for accessible theme generation, CSS variable
  export, public API coverage, and workflow examples

### Fixed
- `accessible?`, `find-accessible-color`, `get-contrast-text` and
  `accessible-theme` compared the contrast ratio after rounding it to 2
  decimal places, so pairs just below a WCAG threshold passed. They now
  compare the unrounded ratio. `contrast-ratio` still returns a rounded value.
  `accessible-theme` output changes for brands near a threshold: `#006ffb`
  now gets black `:on-primary` text instead of white (4.4999:1).
- The demo contrast checker showed WCAG levels as passing for pairs just below
  the threshold. It now uses `accessible?`.
- The demo color picker showed every color as `hsl(0°, 0%, 0%)` and
  `hsv(0°, 0%, 0%)`
- `round` overflowed a long on the JVM for large values at high precision:
  `(round 40000.0 15)` returned `9223.37`. It now returns the value unchanged
  once no fractional digit is left at the requested precision.
- `valid-hex?` returned a regex match vector or `nil` instead of `true` or
  `false`. Code that used it as a condition is unaffected; code that read the
  match vector or tested for `nil` relied on undocumented behavior.
- `kelvin->rgb` made every temperature above 6600 K too blue: it scaled red
  and green by 255 instead of Helland's coefficients. 10000 K now gives
  `[202 218 255]` instead of `[156 193 255]`.
- `rgb->kelvin` returned wrong temperatures: it skipped the conversion to CIE
  xy chromaticity and had two coefficient signs flipped, so a 2700 K warm white
  came back as 24514 K. It now returns 6504 K for sRGB white and `nil` for
  colors far from the black-body curve, as documented.
- Documented example outputs in the README and guides didn't match the
  library in about 100 places. They are corrected, and a test now checks every
  documented result.

## [1.2.0] - 2026-06-23
### Added
- **Accessible Theme Generator**: Generate production-ready UI color tokens
  from a single brand color
  - `accessible-theme` - Builds light or dark theme tokens with WCAG-aware
    foreground colors, semantic roles, focus ring, border, and color scale
  - `theme->css-vars` - Exports generated theme tokens as CSS custom
    properties for direct frontend use
- **Demo Theme Generator**: Interactive demo panel for previewing generated
  accessible themes, contrast pairs, color scales, and CSS variable export
- **cljdoc support**: Added explicit cljdoc article ordering, Clojure and
  ClojureScript language metadata, and a README badge

### Changed
- Replaced optional destructuring arities with explicit multi-arity
  definitions while preserving existing call behavior

## [1.1.0] - 2025-12-02
### Added
- **Color Blending Modes**: Professional blending operations for design workflows
  - `blend-multiply` - Multiply blend mode (darkens)
  - `blend-screen` - Screen blend mode (lightens)
  - `blend-overlay` - Overlay blend mode (combines multiply and screen)
- **Tints, Shades, and Tones**: Essential design system utilities
  - `tint` - Mix color with white (lightens while preserving hue)
  - `shade` - Mix color with black (darkens while preserving hue)
  - `tone` - Mix color with gray (reduces saturation)
  - `tints`, `shades`, `tones` - Generate series of variations
- **Alpha Blending and Compositing**: Proper transparency support
  - `alpha-blend` - Porter-Duff alpha compositing
  - `with-alpha` - Set alpha channel of any color
- **Color Interpolation**: Smooth color transitions
  - `interpolate` - Interpolate between multiple colors in RGB, HSL, or HSV space
  - `gradient` - Generate gradients with n steps between colors
  - Supports hue wrapping for natural color transitions
- **Perceptual Color Difference**: More accurate color comparison
  - `rgb->lab` - Convert to CIE L\*A\*B\* color space
  - `delta-e` - Calculate perceptual difference using CIE76 Delta E formula
  - `perceptually-similar?` - Check similarity using JND (Just Noticeable Difference)
- **Color Temperature (Kelvin)**: Physical light color representation
  - `kelvin->rgb`, `kelvin->hex`, `kelvin->color` - Convert temperature to color
  - `rgb->kelvin` - Approximate color temperature from RGB
  - Supports 1000K-40000K range using Tanner Helland's algorithm

### Improved
- **Better Color Science**: Added LAB color space for perceptually uniform calculations
- **Enhanced Documentation**: Comprehensive examples and usage patterns for new features
- **Test Coverage**: Extensive tests for all new blending, interpolation, and temperature functions

## [1.0.4] - 2025-09-17
### Added
- **CSS Color String Support**: Full support for CSS `rgb()` and `rgba()` color strings
  - `valid-css-rgb?` and `valid-css-rgba?` validation functions
  - `parse-css-rgb` and `parse-css-rgba` parsing functions
  - `normalize-css-color-string` for flexible formatting support
- **Flexible CSS Format Support**: Accepts various CSS color string formats:
  - Comma-separated: `"rgb(255, 0, 0)"`, `"rgba(255, 0, 0, 0.5)"`
  - Space-separated: `"rgb(255 0 0)"`, `"rgba(255 0 0 0.5)"`
  - Case-insensitive: `"RGB(255, 0, 0)"`, `"RGBA(255, 0, 0, 1)"`
  - Flexible whitespace: `"rgb( 255 , 0 , 0 )"`, `"rgba( 255  0  0  0.8 )"`
  - Decimal alpha values: `"rgba(255, 0, 0, .5)"`
- **Enhanced Color Input Handling**: CSS color strings now work seamlessly with all library functions
  - Color factory function: `(color "rgb(255, 0, 0)")`
  - Universal conversion functions: `(->rgb "rgba(255, 0, 0, 0.8)")`
  - Color manipulation: `(lighten "rgb(255, 0, 0)" 0.2)`
  - All accessibility, analysis, and harmony functions
- **Enhanced Color Factory**: `color` function now accepts any color input type, including existing Color records
- **Universal Color Name Function**: New `->name` function that finds closest color names from any color input type
- **Unified Color Input Handling**: Internal `normalize-color-input` function provides consistent color type handling across all functions
- **Comprehensive Test Coverage**: Extensive tests for all CSS color string formats and edge cases

### Improved
- **Cross-platform Parsing**: Added `parse-float` utility for consistent number parsing
- **Better Error Messages**: Enhanced error reporting for invalid color string formats
- **Input Validation**: Stricter validation of CSS color string formats with helpful error messages
- **Simplified API**: All color manipulation functions now work seamlessly with any color input type
- **Code Consistency**: Refactored internal implementations to use unified color handling
- **Better Type Safety**: Enhanced validation and error handling for color inputs
- **Performance**: Reduced conditional logic through unified color processing

### Technical Details
- Made `normalize-color-input` private for internal use
- Enhanced `parse-int` utility function for cross-platform compatibility
- Improved RGBA vector handling in color factory function
- Consolidated color conversion logic for better maintainability

## [1.0.3] - 2025-09-16
### Added
- **Color** record type
- `get-contrast-text` function to determine best contrast text color
- **workflow** automation with GitHub Actions for CI/CD

## 0.1.0 - 2025-08-31
### Added
- Initial release of jon/color-tools library
- **Color Format Conversions**: Support for HEX, RGB, RGBA, HSL, and HSV format conversions
  - `hex->rgb`, `rgb->hex`, `hex->rgba`, `rgba->hex`
  - `rgb->hsl`, `hsl->rgb`, `hex->hsl`, `hsl->hex`
  - `rgb->hsv`, `hsv->rgb`
- **Color Validation**: Validation functions for all supported color formats
  - `valid-hex?`, `valid-rgb?`, `valid-rgba?`, `valid-hsl?`
- **Color Manipulation**: Comprehensive color adjustment functions
  - `lighten`, `darken`, `saturate`, `desaturate`
  - `adjust-hue`, `invert`, `grayscale`
- **Color Mixing and Blending**:
  - `mix` - blend two colors with configurable ratio
- **Accessibility and Contrast**:
  - `luminance`, `contrast-ratio`, `accessible?`
  - `find-accessible-color` - automatically find WCAG-compliant colors
- **Color Harmony and Palettes**:
  - `complementary`, `triadic`, `tetradic`, `analogous`
  - `monochromatic`, `split-complementary`
  - `generate-palette` with multiple strategies
- **Color Analysis**:
  - `brightness`, `dark?`, `light?`, `vibrant?`, `muted?`
  - `warm?`, `cool?`, `temperature`
  - `color-distance`, `similar?`
- **Color Names**: Built-in color name dictionary with conversion utilities
  - `name->hex`, `hex->name`
- **Cross-platform Support**: Works in both Clojure (JVM) and ClojureScript
- **Comprehensive Test Suite**: Extensive test coverage for all functionality
