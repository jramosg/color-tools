# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

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
