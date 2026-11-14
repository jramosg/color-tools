# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## Unreleased
### Added
- `oklab` builds an `Oklab` record from `l`, `a` and `b`. Alpha defaults
  to 1.0 ([#7](https://github.com/jramosg/color-tools/issues/7)).
- `->oklab-space` converts a color to an `Oklab` record, which keeps alpha.
  `->rgb-space` converts it back to a `Color` and clamps out-of-gamut
  channels ([#7](https://github.com/jramosg/color-tools/issues/7)).
- `oklab->rgb`, `oklab->hex`, `in-gamut?` and `oklab->oklch` accept an
  `Oklab` record as well as an `[l a b]` vector, and ignore its alpha.
  `color`, `->rgb`, `->hex`, `->css` and the other color functions accept
  an `Oklab` record too
  ([#7](https://github.com/jramosg/color-tools/issues/7)).

- `color-tools`, a command-line interface to the whole library: convert
  formats, adjust, mix and blend colors, generate palettes, gradients and
  accessible themes, and check WCAG contrast without writing Clojure. See the
  [CLI guide](doc/cli.md).
  - Runs on Node.js (`npx color-tools`), Babashka
    (`bbin install io.github.jramosg/color-tools`) and the JVM
    (`clojure -M -m jon.color-tools.main`)
  - Reads hex, names, `rgb()`, `rgba()`, `hsl()`, `hsv()`, `oklab()`,
    `oklch()`, bare channels and temperatures such as `6500k`; `-` reads
    colors from stdin
  - Prints text with terminal swatches, pretty-printed JSON and EDN, or CSS
    custom properties for themes; `--to` picks the color format
  - `theme --mode both --format css` writes a light theme and a dark one
    that follows `prefers-color-scheme`, or a selector of your choice
  - `color-tools completions bash|zsh|fish` prints a shell completion script
  - Exits 0 on success, 1 when a check such as `contrast --level aa` fails
    and 2 on invalid input, for use in scripts and CI
  - Every public function has a command; a test keeps the
    [coverage table](doc/cli.md#library-coverage) current
### Fixed
- `mix` preserves fractional RGB channels at a ratio of 0 or 1 before
  output rounding ([#156](https://github.com/jramosg/color-tools/issues/156)).
- Hex and Color interpolation stabilize computed half-channel ties before
  rounding. The HSV midpoint of `#001801` and `#003002` is now `#002402`
  on every runtime; vector results keep their full precision
  ([#142](https://github.com/jramosg/color-tools/issues/142)).
- `with-alpha` converts JVM Ratio opacities to doubles in vector results
  ([#158](https://github.com/jramosg/color-tools/issues/158)).
- `hex->rgba` converts JVM Ratio opacities to doubles in RGBA vectors
  ([#164](https://github.com/jramosg/color-tools/issues/164)).

- `alpha-blend` keeps RGB channels in range when floating-point
  compositing would otherwise push a channel above 255
  ([#159](https://github.com/jramosg/color-tools/issues/159)).

## [1.4.0] - 2026-11-17
### Added
- `rgb->hsl`, `rgb->hsv`, `hsl->rgb` and `hsv->rgb` accept
  `{:full-precision? true}` in a second argument to preserve computed
  doubles. `(color input options)` and `(->color r g b a options)` accept
  the same flag to keep fractional RGB channels. Existing defaults stay
  rounded or truncated until 2.0
  ([#8](https://github.com/jramosg/color-tools/issues/8),
  [#38](https://github.com/jramosg/color-tools/issues/38)).
- `->css` exports a color as CSS `rgba()`, with per-call `:decimal-places`
  or `:significant-digits` options. `with-alpha` and `alpha-blend` accept
  the same options for translucent string output; passing both options
  throws ([#8](https://github.com/jramosg/color-tools/issues/8)).
- A Precision section in the API reference explains output rounding and
  how to adopt precise calculations and adapt exact comparisons from 1.3
  ([#8](https://github.com/jramosg/color-tools/issues/8)).
- `get-contrast-text` takes a WCAG level: `(get-contrast-text bg :aa-large)`.
  It returns white when white meets the level, black when only black does,
  and the one with more contrast when neither does, the rule Material UI and
  Bootstrap use ([#54](https://github.com/jramosg/color-tools/issues/54)).

### Changed
- `mix`, `interpolate`, `gradient` and `alpha-blend` retain fractional
  vector channels. Hex and legacy `Color` results round once to 8-bit
  channels. HSL and HSV interpolation uses full precision internally, so
  gradients may change where whole-coordinate rounding shifted a step
  ([#8](https://github.com/jramosg/color-tools/issues/8)).
- `->rgb` and `->rgba` preserve fractional channels from vectors and CSS
  strings, including when used by luminance and Delta E calculations
  ([#8](https://github.com/jramosg/color-tools/issues/8)).
- `lighten`, `darken`, `saturate`, `desaturate` and `adjust-hue` work on
  full-precision HSL and round once, to 8-bit channels. They used to round
  HSL to whole numbers first, which moved a color by up to 3 per channel
  even at zero adjustment: `(adjust-hue "#123456" 0)` returned `"#123354"`.
  Results can differ from 1.3 by that rounding error, so
  `(complementary "#3498db")` is now `"#db7734"`, an exact channel swap,
  instead of `"#db7633"`. `complementary`, `triadic`, `tetradic`,
  `analogous`, `split-complementary` and the `:secondary`, `:accent` and
  `:focus-ring` tokens of `accessible-theme` follow
  ([#100](https://github.com/jramosg/color-tools/issues/100)).
- CSS numeric output preserves fractional channels by default, and CSS
  parsers accept decimal channels and exponent notation for round trips
  ([#8](https://github.com/jramosg/color-tools/issues/8)).
- `alpha-blend` strings keep fractional channels, rounded to 3 decimal
  places unless you pass `:decimal-places` or `:significant-digits`. 1.3
  rounded them to whole numbers, and unrounded results such as
  `179.8571428571429` are not useful in a stylesheet
  ([#127](https://github.com/jramosg/color-tools/issues/127)).
- `get-contrast-text` aims for `:aa` (4.5:1) by default, as `accessible?`
  does, instead of 3:1. About one color in five now gets black text where it
  got white, on mid-tones such as `#3498db`. Pass `:aa-large` to keep the 1.3
  result ([#54](https://github.com/jramosg/color-tools/issues/54)).
- `accessible-theme` picks `:on-primary`, `:on-secondary` and `:on-accent`
  with `get-contrast-text` at the theme's level, so the two agree
  ([#54](https://github.com/jramosg/color-tools/issues/54)).
- `find-accessible-color` returns the variant of the target nearest to it in
  lightness that meets the level, instead of black whenever black passes
  ([#35](https://github.com/jramosg/color-tools/issues/35)).
- `accessible-theme` builds on that search, so two tokens keep their color
  where they used to fall back to black or white: `:focus-ring` keeps its
  hue and `:on-muted` its slate tint, at the nearest lightness that meets the
  level ([#35](https://github.com/jramosg/color-tools/issues/35)).

### Fixed
- `accessible?`, `find-accessible-color`, `get-contrast-text` and
  `accessible-theme` checked any unknown level, such as a mistyped `:AAA`,
  as `:aa`, and could report a pair as accessible that is not. Unknown
  levels now throw, as does a theme `:mode` other than `:light` or `:dark`.
  `nil` still means the default
  ([#99](https://github.com/jramosg/color-tools/issues/99)).
- Hex export no longer narrows channels to a 32-bit float before rounding,
  which could round a channel just below a half-integer up instead of down.
- On the JVM, `(with-alpha "#ff0000" 1)` returned `rgba(255,0,0,1)` for an
  integer alpha of 1. It returns the hex string, as it does for `1.0`
  ([#96](https://github.com/jramosg/color-tools/issues/96)).
- On the JVM, `round` and `round-int` returned 0 for NaN. NaN now stays
  NaN, as in ClojureScript and `round-coords`
  ([#97](https://github.com/jramosg/color-tools/issues/97)).
- `accessible-theme` could return a dark `:muted` surface on which no text
  color meets `:aaa`, so `:on-muted` missed the level it promises. `:muted`
  now darkens until the level is reachable
  ([#52](https://github.com/jramosg/color-tools/issues/52)).
- `round` and `round-int` overflowed when rounding large numbers to zero
  decimal places. Values outside the 32-bit integer range now remain
  doubles; ordinary zero-decimal results still return integers
  ([#89](https://github.com/jramosg/color-tools/issues/89)).
- The documentation of `accessible-theme` said every `:on-*` token meets the
  requested level. On `:primary`, `:secondary` and `:accent` that can't hold
  at `:aaa` for mid-tone colors, and the documentation now says so
  ([#52](https://github.com/jramosg/color-tools/issues/52)).
- `hsv->rgb` returned a color for out-of-range input, such as a hue of 400
  or a saturation of 150. It now checks `valid-hsv?`, as `hsl->rgb` checks
  `valid-hsl?` ([#101](https://github.com/jramosg/color-tools/issues/101)).
- On ClojureScript, `rgb->hsl`, `rgb->hsv`, `hsl->rgb` and `hsv->rgb` rounded
  a value that is exactly halfway down whenever floating point landed a hair
  under it, while the JVM rounded it up. That affects about one color in 300:
  `(rgb->hsl [0 24 1])` was `[122 100 5]` there and `[123 100 5]` on the JVM.
  Both platforms now round halves up, as the JVM always did
  ([#134](https://github.com/jramosg/color-tools/issues/134)).
- `gradient` gave different colors on the JVM and in ClojureScript for some
  inputs, in every color space: about one random color pair in 70. Both
  platforms now agree
  ([#137](https://github.com/jramosg/color-tools/issues/137)).
- `split-complementary` added the base hue twice, producing incorrect
  palettes for nonzero base hues. It now rotates by 180 degrees plus or
  minus the requested angle
  ([#86](https://github.com/jramosg/color-tools/issues/86)).
- The demo converter rejected `rgba()` without an alpha, such as
  `rgba(52, 152, 219)`. CSS treats `rgba()` as an alias of `rgb()`, so it now
  reads it as RGBA with an alpha of 1.
- `monochromatic` ignored the saturation of the base color, so a gray gave a
  red palette and a muted color a vivid one. Saturation now follows the
  base ([#98](https://github.com/jramosg/color-tools/issues/98)).
- `vibrant?`, `muted?`, `warm?`, `cool?`, `temperature` and `monochromatic`
  read hue and saturation after rounding them to whole numbers, so a color
  at 60.24% saturation was not vibrant at the default threshold of 60, and
  one at hue 60.24 degrees counted as warm. They now use the exact values.
  `monochromatic` steps can differ from 1.3 by one channel level
  ([#128](https://github.com/jramosg/color-tools/issues/128)).
- One-step `gradient` and `monochromatic` calls divided by zero. They now
  return the first gradient stop unchanged or the first monochromatic
  variation, while zero steps still returns an empty sequence
  ([#87](https://github.com/jramosg/color-tools/issues/87)).
- The demo converter showed "Detected RGB" for invalid input that names
  another format, such as `hsl(400, 50%, 50%)`, with a color made from its
  numbers. Input that starts with a format name now gets only that format's
  parser ([#37](https://github.com/jramosg/color-tools/issues/37)).
- The demo converter said "No valid color yet" for input that will never be
  valid. Empty input now says "Paste a color to convert" and anything else
  that fails to parse says "Not a valid color".
- `luminance` used the obsolete sRGB cutoff of 0.03928 instead of WCAG's
  current 0.04045. Fractional channels between the cutoffs now use the
  correct linear branch
  ([#88](https://github.com/jramosg/color-tools/issues/88)).
- `rgb->lab` converted with the sRGB matrix rounded to 4 digits, which put
  Lab values off by up to 0.02. It now uses the full matrix and matches
  reference values to 0.001. `delta-e` and `perceptually-similar?` build on
  it, so their results shift by a similar amount. It also uses CIE's exact
  216/24389 and 24389/27 in place of the rounded 0.008856 and 7.787, and
  white and grays are neutral: `(rgb->lab [255 255 255])` is
  `[100.0 0.0 0.0]`. `rgb->kelvin` uses the same matrix, so sRGB white is
  6503 K instead of 6504 K and very cool colors above 14000 K can move by
  10 K or more
  ([#36](https://github.com/jramosg/color-tools/issues/36),
  [#102](https://github.com/jramosg/color-tools/issues/102)).

### Adapting from 1.3
- Computed vectors can now contain fractions, such as
  `[127.5 127.5 127.5]` instead of `[128 128 128]`. Exact comparisons can
  change; Clojure's `=` also distinguishes integers from integral doubles.
  Compare computed coordinates with a tolerance, or use `mapv round-int`
  when an integration needs integer channels. Existing HSL/HSV conversion
  and record-construction defaults are retained; full precision is opt-in.
  See the [precision guide](doc/api-reference.md#precision).

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
