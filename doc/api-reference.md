# API Reference

This document provides a comprehensive reference for all functions in `jon/color-tools`.

## Validation Functions

### `valid-hex?`
```clojure
(valid-hex? hex) => boolean
```
Check if string is a valid hex color.

**Parameters:**
- `hex` - String to validate

**Returns:** `true` if valid hex color, `false` otherwise

**Examples:**
```clojure
(valid-hex? "#ff5733")   ;=> true
(valid-hex? "#f53")      ;=> true  
(valid-hex? "ff5733")    ;=> true
(valid-hex? "invalid")   ;=> false
```

### `valid-rgb?`
```clojure
(valid-rgb? rgb) => boolean
```
Check if vector is a valid RGB color.

**Parameters:**
- `rgb` - Vector of [r g b] values

**Returns:** `true` if valid RGB (all values 0-255), `false` otherwise

### `valid-rgba?`
```clojure
(valid-rgba? rgba) => boolean
```
Check if vector is a valid RGBA color.

**Parameters:**
- `rgba` - Vector of [r g b a] values

**Returns:** `true` if valid RGBA (RGB 0-255, alpha 0-1), `false` otherwise

### `valid-hsl?`
```clojure
(valid-hsl? hsl) => boolean
```
Check if vector is a valid HSL color.

**Parameters:**
- `hsl` - Vector of [h s l] values

**Returns:** `true` if valid HSL (h: 0-360, s,l: 0-100), `false` otherwise

## Format Conversion Functions

### `normalize-hex`
```clojure
(normalize-hex hex) => string
```
Normalize hex color to 6 characters with #.

**Parameters:**
- `hex` - Hex color string

**Returns:** Normalized 6-character hex string

**Examples:**
```clojure
(normalize-hex "#f53")     ;=> "#ff5533"
(normalize-hex "ff5733")   ;=> "#ff5733"
```

### `hex->rgb`
```clojure
(hex->rgb hex) => [r g b]
```
Convert hex color to RGB vector.

**Parameters:**
- `hex` - Valid hex color string

**Returns:** Vector of [red green blue] values (0-255)

**Examples:**
```clojure
(hex->rgb "#ff5733")  ;=> [255 87 51]
(hex->rgb "#f53")     ;=> [255 85 51]
```

### `rgb->hex`
```clojure
(rgb->hex rgb) => string
```
Convert RGB vector to hex string.

**Parameters:**
- `rgb` - Vector of [r g b] values (0-255)

**Returns:** Hex color string

**Examples:**
```clojure
(rgb->hex [255 87 51])  ;=> "#ff5733"
```

### `hex->rgba`
```clojure
(hex->rgba hex alpha) => [r g b a]
```
Convert hex color to RGBA vector with specified alpha.

**Parameters:**
- `hex` - Valid hex color string
- `alpha` - Alpha value (0-1)

**Returns:** Vector of [red green blue alpha]

### `rgba->hex`
```clojure
(rgba->hex rgba) => string
```
Convert RGBA vector to hex string (ignoring alpha).

**Parameters:**
- `rgba` - Vector of [r g b a] values

**Returns:** Hex color string

### `rgb->hsl`
```clojure
(rgb->hsl rgb) => [h s l]
```
Convert RGB to HSL color space.

**Parameters:**
- `rgb` - Vector of [r g b] values (0-255)

**Returns:** Vector of [hue saturation lightness] (h: 0-360, s,l: 0-100)

### `hsl->rgb`
```clojure
(hsl->rgb hsl) => [r g b]
```
Convert HSL to RGB color space.

**Parameters:**
- `hsl` - Vector of [h s l] values

**Returns:** Vector of [red green blue] values (0-255)

### `hex->hsl`
```clojure
(hex->hsl hex) => [h s l]
```
Convert hex color to HSL.

**Parameters:**
- `hex` - Valid hex color string

**Returns:** Vector of [hue saturation lightness]

### `hsl->hex`
```clojure
(hsl->hex hsl) => string
```
Convert HSL to hex color.

**Parameters:**
- `hsl` - Vector of [h s l] values

**Returns:** Hex color string

### `rgb->hsv`
```clojure
(rgb->hsv rgb) => [h s v]
```
Convert RGB to HSV color space.

### `hsv->rgb`
```clojure
(hsv->rgb hsv) => [r g b]
```
Convert HSV to RGB color space.

## Color Manipulation Functions

### `lighten`
```clojure
(lighten color amount) => color
```
Lighten a color by a percentage.

**Parameters:**
- `color` - Color (hex string or RGB vector)
- `amount` - Amount to lighten (0-1)

**Returns:** Color in same format as input

**Examples:**
```clojure
(lighten "#3498db" 0.2)    ;=> "#5dade2"
(lighten [52 152 219] 0.2) ;=> [93 173 226]
```

### `darken`
```clojure
(darken color amount) => color
```
Darken a color by a percentage.

**Parameters:**
- `color` - Color (hex string or RGB vector)
- `amount` - Amount to darken (0-1)

**Returns:** Color in same format as input

### `saturate`
```clojure
(saturate color amount) => color
```
Increase saturation of a color by a percentage.

**Parameters:**
- `color` - Color (hex string or RGB vector)
- `amount` - Amount to saturate (0-1)

**Returns:** Color in same format as input

### `desaturate`
```clojure
(desaturate color amount) => color
```
Decrease saturation of a color by a percentage.

### `adjust-hue`
```clojure
(adjust-hue color degrees) => color
```
Adjust the hue of a color by degrees.

**Parameters:**
- `color` - Color (hex string or RGB vector)
- `degrees` - Degrees to shift hue (-360 to 360)

**Returns:** Color in same format as input

### `invert`
```clojure
(invert color) => color
```
Invert a color (each RGB component becomes 255 - component).

### `grayscale`
```clojure
(grayscale color) => color
```
Convert a color to grayscale using luminance weights.

## Color Analysis Functions

### `luminance`
```clojure
(luminance color) => number
```
Calculate relative luminance of a color (0-1).

**Parameters:**
- `color` - Color (hex string or RGB vector)

**Returns:** Relative luminance value (0-1)

### `contrast-ratio`
```clojure
(contrast-ratio color1 color2) => number
```
Calculate contrast ratio between two colors.

**Parameters:**
- `color1` - First color
- `color2` - Second color

**Returns:** Contrast ratio (1-21)

**Examples:**
```clojure
(contrast-ratio "#000000" "#ffffff")  ;=> 21.0
(contrast-ratio "#3498db" "#ffffff")  ;=> 3.14
```

### `accessible?`
```clojure
(accessible? color1 color2 & [level]) => boolean
```
Check if two colors meet WCAG accessibility standards.

**Parameters:**
- `color1` - First color
- `color2` - Second color
- `level` - Optional accessibility level (`:aa`, `:aa-large`, `:aaa`)

**Returns:** `true` if colors meet specified standard

### `brightness`
```clojure
(brightness color) => number
```
Calculate perceived brightness of a color (0-255).

### `dark?`
```clojure
(dark? color) => boolean
```
Check if a color is dark (brightness < 128).

### `light?`
```clojure
(light? color) => boolean
```
Check if a color is light (brightness >= 128).

### `warm?`
```clojure
(warm? color) => boolean
```
Check if a color is warm (red/orange/yellow family).

### `cool?`
```clojure
(cool? color) => boolean
```
Check if a color is cool (blue/green/purple family).

### `vibrant?`
```clojure
(vibrant? color & [threshold]) => boolean
```
Check if a color is vibrant (high saturation).

**Parameters:**
- `color` - Color to check
- `threshold` - Optional saturation threshold (default 60)

### `muted?`
```clojure
(muted? color & [threshold]) => boolean
```
Check if a color is muted (low saturation).

## Palette Generation Functions

### `complementary`
```clojure
(complementary color) => color
```
Get the complementary color (opposite on color wheel).

### `triadic`
```clojure
(triadic color) => [color color]
```
Get triadic colors (120° and 240° from base).

### `tetradic`
```clojure
(tetradic color) => [color color color]
```
Get tetradic colors (90°, 180°, 270° from base).

### `analogous`
```clojure
(analogous color & [count angle]) => [color ...]
```
Generate analogous colors.

**Parameters:**
- `color` - Base color
- `count` - Number of colors to generate (default 3)
- `angle` - Angle between colors in degrees (default 30)

### `monochromatic`
```clojure
(monochromatic color & [count]) => [color ...]
```
Generate monochromatic palette by varying lightness and saturation.

### `split-complementary`
```clojure
(split-complementary color & [angle]) => [color color]
```
Get split-complementary colors.

### `generate-palette`
```clojure
(generate-palette strategy base-color & [options]) => [color ...]
```
Generate a color palette using various strategies.

**Parameters:**
- `strategy` - Palette strategy (`:monochromatic`, `:analogous`, `:triadic`, etc.)
- `base-color` - Base color for palette
- `options` - Map of options (`:count`, `:angle`, etc.)

## Color Mixing Functions

### `mix`
```clojure
(mix color1 color2 & [ratio]) => color
```
Mix two colors with optional ratio.

**Parameters:**
- `color1` - First color
- `color2` - Second color  
- `ratio` - Mix ratio (0-1, default 0.5)

**Returns:** Mixed color in same format as first color

## Utility Functions

### `color-distance`
```clojure
(color-distance color1 color2) => number
```
Calculate Euclidean distance between two colors in RGB space.

### `similar?`
```clojure
(similar? color1 color2 & [threshold]) => boolean
```
Check if two colors are similar within a threshold.

### `name->hex`
```clojure
(name->hex color-name) => string
```
Convert color name to hex.

**Examples:**
```clojure
(name->hex "red")    ;=> "#ff0000"
(name->hex "navy")   ;=> "#000080"
```

### `hex->name`
```clojure
(hex->name hex) => string
```
Find closest color name for a hex color.

### `random-color`
```clojure
(random-color) => string
```
Generate a random hex color.

### `find-accessible-color`
```clojure
(find-accessible-color background target-color & [level]) => color
```
Find an accessible color by adjusting lightness.

**Parameters:**
- `background` - Background color
- `target-color` - Desired color to make accessible
- `level` - Accessibility level (`:aa`, `:aa-large`, `:aaa`)

**Returns:** Accessible version of target color, or `nil` if none found

## Utility Helper Functions

### `clamp`
```clojure
(clamp value min-val max-val) => number
```
Clamp a value between min and max.

### `round`
```clojure
(round n & [decimals]) => number
```
Round a number to specified decimal places.

### `round-int`
```clojure
(round-int n) => integer
```
Round a number to the nearest integer.
