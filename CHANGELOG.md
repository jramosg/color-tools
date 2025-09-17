# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

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
