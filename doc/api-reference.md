# API Reference

This reference covers the public API in `jon.color-tools`. Most functions
accept hex strings, RGB vectors, RGBA vectors, CSS `rgb()`/`rgba()` strings, or
`Color` records unless the parameter says otherwise.

## Color Inputs

- Hex strings: `"#ff0000"`, `"#f00"`, `"ff0000"`
- RGB vectors: `[255 0 0]`
- RGBA vectors: `[255 0 0 0.5]`
- CSS RGB strings: `"rgb(255, 0, 0)"`, `"rgb(255 0 0)"`
- CSS RGBA strings: `"rgba(255, 0, 0, 0.5)"`, `"rgba(255 0 0 0.5)"`
- `Color` records created with `color`

Manipulation functions generally preserve the input shape. A hex string returns
a hex string, an RGB vector returns a vector, and a `Color` record returns a
`Color` record.

## Validation

### `color-names`

```clojure
color-names => map
```

Map of supported CSS color names to hex values. It is used by `name->hex`,
`hex->name`, and `->name`.

### `valid-hex?`

```clojure
(valid-hex? hex) => boolean
```

Returns true when `hex` is a valid 3- or 6-digit hex color, with or without a
leading `#`.

### `valid-rgb?`

```clojure
(valid-rgb? rgb) => boolean
```

Returns true when `rgb` is `[r g b]` and every channel is between 0 and 255.

### `valid-rgba?`

```clojure
(valid-rgba? rgba) => boolean
```

Returns true when `rgba` is `[r g b a]`, RGB channels are between 0 and 255,
and alpha is between 0 and 1.

### `valid-hsl?`

```clojure
(valid-hsl? hsl) => boolean
```

Returns true when `hsl` is `[h s l]`, hue is between 0 and 360, and saturation
and lightness are between 0 and 100.

### `valid-hsv?`

```clojure
(valid-hsv? hsv) => boolean
```

Returns true when `hsv` is `[h s v]`, hue is between 0 and 360, and saturation
and value are between 0 and 100.

### `valid-css-rgb?`

```clojure
(valid-css-rgb? css-str) => boolean
```

Returns true for supported CSS `rgb()` strings. Comma-separated,
space-separated, case-insensitive, and extra-whitespace forms are accepted.

### `valid-css-rgba?`

```clojure
(valid-css-rgba? css-str) => boolean
```

Returns true for supported CSS `rgba()` strings, including decimal alpha
values such as `.5`.

## Format Conversion

### `normalize-hex`

```clojure
(normalize-hex hex) => string
```

Expands and normalizes a hex color to `#rrggbb`.

```clojure
(normalize-hex "#f53")
;=> "#ff5533"
```

### `hex->rgb`

```clojure
(hex->rgb hex) => [r g b]
```

Converts a valid hex color to an RGB vector.

### `rgb->hex`

```clojure
(rgb->hex rgb) => string
```

Converts an RGB vector to a `#rrggbb` string.

### `clamp-rgb` (Unreleased)

```clojure
(clamp-rgb rgb) => [r g b]
```

Clamps each channel to the `0` to `255` range and keeps fractions. Use it on
computed channels, such as `oklab->rgb` output, before `->hex` or `color`,
which reject values outside that range.

```clojure
(clamp-rgb [300.0 -5.0 254.6])
;=> [255.0 0.0 254.6]
```

Clamping an out-of-gamut color changes its hue and lightness. It does not
find the closest in-gamut color.

### `hex->rgba`

```clojure
(hex->rgba hex) => [r g b a]
(hex->rgba hex alpha) => [r g b a]
```

Converts a hex color to RGBA. Alpha defaults to `1.0`.

### `rgba->hex`

```clojure
(rgba->hex rgba) => string
```

Converts an RGBA vector to hex, ignoring alpha.

### `rgb->hsl`

```clojure
(rgb->hsl rgb) => [h s l]
```

Converts RGB to HSL.

### `hsl->rgb`

```clojure
(hsl->rgb hsl) => [r g b]
```

Converts HSL to RGB.

### `hex->hsl`

```clojure
(hex->hsl hex) => [h s l]
```

Converts hex to HSL.

### `hsl->hex`

```clojure
(hsl->hex hsl) => string
```

Converts HSL to hex.

### `rgb->hsv`

```clojure
(rgb->hsv rgb) => [h s v]
```

Converts RGB to HSV.

### `hsv->rgb`

```clojure
(hsv->rgb hsv) => [r g b]
```

Converts HSV to RGB.

### `normalize-css-color-string`

```clojure
(normalize-css-color-string css-str) => string
```

Normalizes supported CSS color strings by trimming whitespace and standardizing
separators.

### `parse-css-color-components`

```clojure
(parse-css-color-components css-str) => [r g b] | [r g b a] | nil
```

Parses a CSS `rgb()` or `rgba()` string into numeric components. Returns `nil`
when the string is unsupported or contains out-of-range values.

### `parse-css-rgb`

```clojure
(parse-css-rgb css-str) => [r g b]
```

Parses a valid CSS `rgb()` string.

### `parse-css-rgba`

```clojure
(parse-css-rgba css-str) => [r g b a]
```

Parses a valid CSS `rgba()` string.

## Color Records and Universal Conversion

### `color`

```clojure
(color input) => Color
(color r g b) => Color
(color r g b a) => Color
```

Creates a `Color` record from any supported input, RGB channels, or RGBA
channels.

```clojure
(color "#ff0000")
(color [255 0 0])
(color 255 0 0 0.5)
```

### `color?`

```clojure
(color? x) => boolean
```

Returns true when `x` is a `Color` record.

### `->color`

```clojure
(->color r g b) => Color
(->color r g b a) => Color
```

Low-level constructor for `Color` records. Prefer `color` when accepting user
input because it validates and normalizes supported formats.

### `color-from-hex`

```clojure
(color-from-hex hex) => Color
```

Creates a `Color` record from a valid hex color.

### `color-from-rgb`

```clojure
(color-from-rgb rgb) => Color
```

Creates a `Color` record from an RGB vector.

### `color-from-rgba`

```clojure
(color-from-rgba rgba) => Color
```

Creates a `Color` record from an RGBA vector.

### `color-from-hsl`

```clojure
(color-from-hsl hsl) => Color
```

Creates a `Color` record from an HSL vector.

### `color-from-hsv`

```clojure
(color-from-hsv hsv) => Color
```

Creates a `Color` record from an HSV vector.

### `->rgb`

```clojure
(->rgb color) => [r g b]
```

Converts any supported color input to RGB.

### `->rgba`

```clojure
(->rgba color) => [r g b a]
```

Converts any supported color input to RGBA.

### `->hex`

```clojure
(->hex color) => string
```

Converts any supported color input to hex.

### `->hsl`

```clojure
(->hsl color) => [h s l]
```

Converts any supported color input to HSL.

### `->hsv`

```clojure
(->hsv color) => [h s v]
```

Converts any supported color input to HSV.

## Color Manipulation

### `lighten`

```clojure
(lighten color amount) => color
```

Increases lightness by `amount`, where `0.2` means 20 percentage points.

### `darken`

```clojure
(darken color amount) => color
```

Decreases lightness by `amount`.

### `saturate`

```clojure
(saturate color amount) => color
```

Increases saturation by `amount`.

### `desaturate`

```clojure
(desaturate color amount) => color
```

Decreases saturation by `amount`.

### `adjust-hue`

```clojure
(adjust-hue color degrees) => color
```

Rotates hue by `degrees`.

### `invert`

```clojure
(invert color) => color
```

Inverts RGB channels.

### `grayscale`

```clojure
(grayscale color) => color
```

Converts a color to grayscale using luminance weights.

## Contrast and Accessibility

### `luminance`

```clojure
(luminance color) => number
```

Calculates WCAG relative luminance from 0 to 1.

### `contrast-ratio`

```clojure
(contrast-ratio color1 color2) => number
```

Calculates the WCAG contrast ratio, from 1 to 21, rounded to 2 decimal
places for display. Use `accessible?` for pass/fail checks: it compares the
unrounded ratio, so a pair at 4.4999 fails AA even though `contrast-ratio`
returns `4.5`.

```clojure
(contrast-ratio [0 111 251] [255 255 255]) ;=> 4.5
(accessible? [0 111 251] [255 255 255])    ;=> false
```

### `accessible?`

```clojure
(accessible? color1 color2) => boolean
(accessible? color1 color2 level) => boolean
```

Checks whether two colors meet a WCAG threshold. `level` can be `:aa`,
`:aa-large`, or `:aaa`; it defaults to `:aa`. The thresholds are 4.5, 3 and
7. The check compares the unrounded contrast ratio.

### `find-accessible-color`

```clojure
(find-accessible-color background target-color) => color | nil
(find-accessible-color background target-color level) => color | nil
```

Attempts to adjust `target-color` until it meets the requested contrast level
against `background`. `level` defaults to `:aa`.

### `get-contrast-text`

```clojure
(get-contrast-text background-color) => string
```

Returns either black or white, choosing the readable text color for a
background.

## Palette Generation

### `complementary`

```clojure
(complementary color) => color
```

Returns the color opposite the input on the color wheel.

### `triadic`

```clojure
(triadic color) => [color color]
```

Returns the two other colors in a triadic harmony.

### `tetradic`

```clojure
(tetradic color) => [color color color]
```

Returns three colors at 90, 180, and 270 degrees from the input.

### `analogous`

```clojure
(analogous color) => [color ...]
(analogous color count) => [color ...]
(analogous color count angle) => [color ...]
```

Generates adjacent colors. `count` defaults to `3`; `angle` defaults to `30`.

### `monochromatic`

```clojure
(monochromatic color) => [color ...]
(monochromatic color count) => [color ...]
```

Generates one-hue variations. `count` defaults to `5`.

### `split-complementary`

```clojure
(split-complementary color) => [color color]
(split-complementary color angle) => [color color]
```

Returns the two colors on either side of the complement. `angle` defaults to
`30`.

### `random-color`

```clojure
(random-color) => string
```

Generates a random hex color.

### `generate-palette`

```clojure
(generate-palette strategy base-color) => [color ...]
(generate-palette strategy base-color options) => [color ...]
```

Generates palettes with `:monochromatic`, `:analogous`, `:triadic`,
`:tetradic`, `:complementary`, `:split-complementary`, or `:random`.

Supported options include `:count` and `:angle`, depending on strategy.

## Accessible Design Tokens

### `accessible-theme`

```clojure
(accessible-theme base-color) => map
(accessible-theme base-color options) => map
```

Generates WCAG-aware UI color tokens from one brand color.

Options:

- `:mode` - `:light` or `:dark`; defaults to `:light`
- `:level` - `:aa`, `:aa-large`, or `:aaa`; defaults to `:aa`

Returned tokens:

- `:background`, `:on-background`
- `:surface`, `:on-surface`
- `:muted`, `:on-muted`
- `:primary`, `:on-primary`
- `:secondary`, `:on-secondary`
- `:accent`, `:on-accent`
- `:border`, `:focus-ring`
- `:scale` with `:50` through `:900`
- `:mode` and `:level`

```clojure
(def theme (accessible-theme "#3498db" {:mode :light :level :aa}))

(select-keys theme [:background :primary :on-primary :focus-ring])
;=> {:background "#f7fbfe"
;    :primary "#3498db"
;    :on-primary "#000000"
;    :focus-ring "#8433db"}
```

### `theme->css-vars`

```clojure
(theme->css-vars theme) => map
(theme->css-vars theme options) => map
```

Exports an `accessible-theme` map as CSS custom properties. `:prefix` defaults
to `"--ct-"`.

```clojure
(theme->css-vars theme {:prefix "--brand-"})
;=> {"--brand-primary" "#3498db"
;    "--brand-on-primary" "#000000"
;    "--brand-scale-500" "#3498db"
;    ...}
```

## Mixing, Blending, and Alpha

### `mix`

```clojure
(mix color1 color2) => color
(mix color1 color2 ratio) => color
```

Linearly mixes two colors. `ratio` defaults to `0.5`; `0` returns `color1`
and `1` returns `color2`.

### `blend-multiply`

```clojure
(blend-multiply base-color overlay-color) => color
```

Applies multiply blending.

### `blend-screen`

```clojure
(blend-screen base-color overlay-color) => color
```

Applies screen blending.

### `blend-overlay`

```clojure
(blend-overlay base-color overlay-color) => color
```

Applies overlay blending.

### `tint`

```clojure
(tint color amount) => color
```

Mixes a color with white.

### `shade`

```clojure
(shade color amount) => color
```

Mixes a color with black.

### `tone`

```clojure
(tone color amount) => color
```

Mixes a color with gray.

### `tints`

```clojure
(tints color) => [color ...]
(tints color count) => [color ...]
```

Generates tint steps. `count` defaults to `5`.

### `shades`

```clojure
(shades color) => [color ...]
(shades color count) => [color ...]
```

Generates shade steps. `count` defaults to `5`.

### `tones`

```clojure
(tones color) => [color ...]
(tones color count) => [color ...]
```

Generates tone steps. `count` defaults to `5`.

### `alpha-blend`

```clojure
(alpha-blend base-color overlay-color) => color
```

Composites `overlay-color` over `base-color` using source-over alpha blending.

### `with-alpha`

```clojure
(with-alpha color alpha) => color
```

Sets the alpha channel for a color.

## Interpolation and Gradients

### `interpolate`

```clojure
(interpolate colors position) => color
(interpolate colors position color-space) => color
```

Interpolates through two or more colors at `position` from 0 to 1. Color space
defaults to `:rgb`; `:hsl` and `:hsv` are also supported.

### `gradient`

```clojure
(gradient colors steps) => [color ...]
(gradient colors steps color-space) => [color ...]
```

Generates a gradient with `steps` colors. Color space defaults to `:rgb`.

## Color Difference and Similarity

### `color-distance`

```clojure
(color-distance color1 color2) => number
```

Calculates Euclidean RGB distance.

### `similar?`

```clojure
(similar? color1 color2) => boolean
(similar? color1 color2 threshold) => boolean
```

Checks RGB distance against `threshold`, which defaults to `50`.

### `rgb->lab`

```clojure
(rgb->lab rgb) => [l a b]
```

Converts RGB to CIE L*a*b*.

### `rgb->oklab` (Unreleased)

```clojure
(rgb->oklab rgb) => [l a b]
```

Pass an sRGB vector `[r g b]` with each channel from `0` to `255`.
`rgb->oklab` removes sRGB gamma encoding, then converts the linear RGB
channels to Oklab using
[Björn Ottosson's reference formula](https://bottosson.github.io/posts/oklab/).

Interpret the result as follows:

- `l`: lightness on a 0 to 1 scale for sRGB, from black to white.
- `a`: green (negative) to red (positive).
- `b`: blue (negative) to yellow (positive).

`rgb->oklab` preserves the computed floating-point values. Use
[`round-coords`](#round-coords) to round coordinates for display. The `l`
scale differs from the 0 to 100 lightness scale of `rgb->lab`.

```clojure
(require '[jon.color-tools :as color])

(color/round-coords (color/rgb->oklab [255 0 0]) {:decimal-places 4})
;=> [0.628 0.2249 0.1258]

(color/round-coords (color/rgb->oklab [128 128 128]) {:decimal-places 4})
;=> [0.5999 0.0 0.0]

;; Normalize hex strings, CSS strings, or Color records with ->rgb.
(-> "#336699"
    color/->rgb
    color/rgb->oklab
    (color/round-coords {:decimal-places 4}))
;=> [0.4993 -0.033 -0.093]
```

The function checks `valid-rgb?` with an assertion precondition. With
assertions enabled, invalid RGB inputs cause an `AssertionError`.
Use `valid-rgb?` at user-input boundaries to handle validation before calling
the converter. The function does not clamp channels.

Treat the result as Oklab coordinates: `color` and `->hex` interpret
three-element vectors as RGB.

### `oklab->rgb` (Unreleased)

```clojure
(oklab->rgb oklab) => [r g b]
```

Converts Oklab coordinates `[l a b]` back to an sRGB vector on the `0` to
`255` scale. It is the inverse of `rgb->oklab`: the matrices are the exact
inverses of the ones `rgb->oklab` uses, so a round trip returns the original
channels to within `1e-9`.

The result keeps full precision and is not clamped. Colors outside the sRGB
gamut produce channels below `0` or above `255`; check them with
[`in-gamut?`](#in-gamut-unreleased).

```clojure
(color/round-coords (color/oklab->rgb [0.628 0.2249 0.1258])
                    {:decimal-places 2})
;=> [255.02 -0.01 0.23]

(color/round-coords (color/oklab->rgb [0.9 0.3 0.3]) {:decimal-places 2})
;=> [422.12 -66.4 -134.52]  ; outside sRGB
```

`->hex` and `color` reject channels outside `0` to `255`, including float
noise such as `-1e-12`. Use [`oklab->hex`](#oklab-hex-unreleased) for hex
output, or [`clamp-rgb`](#clamp-rgb-unreleased) before `color`. `color`
truncates fractional channels, so round them first.

The function checks that the input is a vector of three finite numbers with
an assertion precondition.

### `oklab->hex` (Unreleased)

```clojure
(oklab->hex oklab) => string
```

Converts Oklab coordinates to a `#rrggbb` string. Clamps channels outside
the sRGB gamut with `clamp-rgb`, then rounds each one to 8 bits.

```clojure
(color/oklab->hex [0.4993 -0.033 -0.093])
;=> "#336699"

(color/oklab->hex [0.9 0.3 0.3])
;=> "#ff0000"  ; clamped
```

Clamping moves an out-of-gamut color's hue and lightness. Check
`in-gamut?` first when that matters.

### `in-gamut?` (Unreleased)

```clojure
(in-gamut? oklab) => boolean
```

Checks whether Oklab coordinates fall inside the sRGB gamut, so that
`oklab->rgb` returns every channel between `0` and `255`. It allows `1e-4` on
the `0` to `255` scale for float error: Ottosson's 10-digit constants put sRGB
white at `l = 0.99999999`, so `[1 0 0]` lands just above `255`.

```clojure
(color/in-gamut? (color/rgb->oklab [51 102 153]))
;=> true

(color/in-gamut? [0.9 0.3 0.3])
;=> false
```

### `oklab->oklch` (Unreleased)

```clojure
(oklab->oklch oklab) => [l c h]
```

Converts Oklab coordinates to OKLCh, the polar form that CSS `oklch()` uses:

- `l`: lightness, unchanged from Oklab.
- `c`: chroma, `0` and up. sRGB colors stay below about `0.33`.
- `h`: hue in degrees, from `0` up to `360`.

Colors with chroma below `1e-6` get hue `0`; CSS writes that hue as `none`.
Grays converted from sRGB carry about `2e-8` of chroma noise, so their
computed hue would be meaningless. The result is unrounded.

```clojure
(color/round-coords (color/oklab->oklch [0.628 0.2249 0.1258])
                    {:decimal-places 4})
;=> [0.628 0.2577 29.2209]
```

### `oklch->oklab` (Unreleased)

```clojure
(oklch->oklab oklch) => [l a b]
```

Converts OKLCh back to Oklab. Hue wraps, so `-30` and `330` give the same
color. Negative chroma fails the assertion precondition.

```clojure
(color/round-coords (color/oklch->oklab [0.628 0.2577 29.23])
                    {:decimal-places 4})
;=> [0.628 0.2249 0.1258]
```

### `rgb->oklch`, `oklch->rgb` and `oklch->hex` (Unreleased)

```clojure
(rgb->oklch rgb) => [l c h]
(oklch->rgb oklch) => [r g b]
(oklch->hex oklch) => string
```

Shortcuts that go through Oklab. `rgb->oklch` takes the same sRGB input as
`rgb->oklab`. `oklch->rgb` returns unrounded, unclamped channels like
`oklab->rgb`, and `oklch->hex` clamps like `oklab->hex`. To check the gamut,
convert to Oklab first: `(in-gamut? (oklch->oklab lch))`.

```clojure
(color/round-coords (color/rgb->oklch [255 0 0]) {:decimal-places 4})
;=> [0.628 0.2577 29.2339]

(color/oklch->hex [0.628 0.2577 29.23])
;=> "#ff0000"
```

### `delta-e`

```clojure
(delta-e color1 color2) => number
```

Calculates CIE76 Delta E perceptual difference.

### `perceptually-similar?`

```clojure
(perceptually-similar? color1 color2) => boolean
(perceptually-similar? color1 color2 threshold) => boolean
```

Checks Delta E against `threshold`, which defaults to `2.3`.

## Names and Temperature

### `name->hex`

```clojure
(name->hex color-name) => string | nil
```

Looks up a named CSS color.

### `hex->name`

```clojure
(hex->name hex) => string
```

Finds the closest named color for a hex value.

### `->name`

```clojure
(->name color) => string
```

Finds the closest named color for any supported color input.

### `warm?`

```clojure
(warm? color) => boolean
```

Returns true for colors in the red, orange, or yellow hue ranges.

### `cool?`

```clojure
(cool? color) => boolean
```

Returns true for colors outside the warm hue ranges.

### `temperature`

```clojure
(temperature color) => :warm | :cool
```

Classifies a color as `:warm` or `:cool`.

### `brightness`

```clojure
(brightness color) => number
```

Calculates perceived brightness from 0 to 255.

### `dark?`

```clojure
(dark? color) => boolean
```

Returns true when brightness is below 128.

### `light?`

```clojure
(light? color) => boolean
```

Returns true when brightness is 128 or greater.

### `vibrant?`

```clojure
(vibrant? color) => boolean
(vibrant? color threshold) => boolean
```

Checks whether saturation is above `threshold`, which defaults to `60`.

### `muted?`

```clojure
(muted? color) => boolean
(muted? color threshold) => boolean
```

Checks whether saturation is not above `threshold`, which defaults to `60`.

### `kelvin->rgb`

```clojure
(kelvin->rgb kelvin) => [r g b]
```

Converts a color temperature from 1000K to 40000K into RGB with Tanner
Helland's approximation of the black-body curve. Throws `ex-info` outside that
range.

### `kelvin->hex`

```clojure
(kelvin->hex kelvin) => string
```

Converts a color temperature into hex.

### `kelvin->color`

```clojure
(kelvin->color kelvin) => Color
```

Converts a color temperature into a `Color` record.

### `rgb->kelvin`

```clojure
(rgb->kelvin rgb) => number | nil
```

Approximates the correlated color temperature of an sRGB color. It converts to
CIE 1931 xy chromaticity and applies McCamy's formula, which is accurate from
about 2000K to 12500K.

Returns `nil` for black, for colors more than `0.05` from the black-body curve
in CIE 1960 uv (Duv), and for results outside 1000K to 40000K. Saturated
primaries such as pure red have no meaningful temperature.

```clojure
(rgb->kelvin [255 255 255])
;=> 6504  ; sRGB white is D65

(rgb->kelvin (kelvin->rgb 3000))
;=> 2879

(rgb->kelvin [255 0 0])
;=> nil
```

## Numeric Helpers

### `parse-int`

```clojure
(parse-int s) => integer
(parse-int s radix) => integer
```

Parses a string as an integer. `radix` defaults to `10`.

### `clamp`

```clojure
(clamp value min-val max-val) => number
```

Clamps `value` into the inclusive range.

### `round`

```clojure
(round n) => number
(round n decimals) => number
```

Rounds `n` to `decimals` places. `decimals` defaults to `0`.

### `round-int`

```clojure
(round-int n) => integer
```

Rounds `n` to the nearest integer.

### `round-coords`

```clojure
(round-coords coords {:decimal-places n}) => [number ...]
(round-coords coords {:significant-digits n}) => [number ...]
```

Rounds each number in a coordinate vector or list for display and returns a
vector of doubles. Conversions such as `rgb->oklab` return full precision;
round only the values you show or export.

Pass exactly one option:

- `:decimal-places`: an integer from `0` to `15`. Digits after the decimal
  point.
- `:significant-digits`: an integer from `1` to `17`. Digits counted from
  the first nonzero digit, as in Color.js.

```clojure
(round-coords (rgb->oklab [255 0 0]) {:decimal-places 4})
;=> [0.628 0.2249 0.1258]

(round-coords [123.456 0.001234] {:decimal-places 2})
;=> [123.46 0.0]

(round-coords [123.456 0.001234] {:significant-digits 3})
;=> [123.0 0.00123]
```

The function always returns doubles, even at `0` decimal places; use
`round-int` for integers. Ties round toward positive infinity, as
`Math/round` does: `1.25` becomes `1.3` and `-1.25` becomes `-1.2` at one
place. Rounding works on the stored binary value, so `1.005` rounds to `1.0`
at two places. The JVM and JavaScript give the same results.

`NaN` and infinite values pass through unchanged. Values too large to have a
fractional digit at the requested precision come back unchanged. Invalid
options and non-numeric coordinates throw `ex-info`.
ClojureScript has no separate integer type, so `{:decimal-places 2.0}` is
accepted there and rejected on the JVM.
