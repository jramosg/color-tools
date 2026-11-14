# Command-Line Interface

`color-tools` puts the whole library in your terminal: convert formats,
adjust and mix colors, build palettes, gradients and accessible themes, and
check WCAG contrast. You don't need to write Clojure to use it.

It prints plain text for people, JSON or EDN for scripts, CSS for themes,
and sets an exit code that CI jobs can act on.

```console
$ color-tools contrast '#3498db' '#ffffff'
ratio     3.15
aa        false
aa-large  true
aaa       false
$ color-tools convert '#3498db' --to oklch
oklch(0.6531 0.1347 242.6867)
$ color-tools palette '#3498db' --type triadic
#db3398
#98db33
```

## Installation

The CLI ships with releases after 1.3.0. Pick whichever runtime you already
have; the commands and output are the same on all of them.

### Node.js

```bash
npm install --global color-tools
# or, without installing
npx color-tools contrast '#3498db' '#ffffff'
```

One file with no dependencies. It starts in about 30 ms.

### Babashka

```bash
bbin install io.github.jramosg/color-tools
```

[bbin](https://github.com/babashka/bbin) puts a `color-tools` command on
your `PATH`. It starts in about 50 ms.

### Clojure CLI

```bash
clojure -Sdeps '{:deps {com.github.jramosg/color-tools {:mvn/version "RELEASE"}}}' \
  -M -m jon.color-tools.main contrast '#3498db' '#ffffff'
```

Or add an alias to your `deps.edn` and run `clojure -M:color-tools`:

```clojure
{:aliases
 {:color-tools
  {:extra-deps {com.github.jramosg/color-tools {:mvn/version "RELEASE"}}
   :main-opts ["-m" "jon.color-tools.main"]}}}
```

The JVM takes about a second to start.

### From a checkout

```bash
git clone https://github.com/jramosg/color-tools.git
color-tools/bin/color-tools help
```

`bin/color-tools` uses Babashka when it is installed and the Clojure CLI
otherwise. You can symlink it into a directory on your `PATH`.

## Getting help

```bash
color-tools help            # every command, option and exit code
color-tools help contrast   # arguments, options and examples of a command
color-tools contrast --help # the same
color-tools --version
```

A mistyped command or option suggests the closest match:

```console
$ color-tools covert '#3498db'
color-tools: unknown command "covert"
Did you mean convert?
Run 'color-tools help' for the list of commands.
```

## Writing colors

Every command accepts the same notations:

| Notation | Examples |
| --- | --- |
| Hex | `'#3498db'`, `3498db`, `'#fff'` |
| Hex with alpha | `'#3498db80'`, `'#f008'` |
| Name | `navy`, `teal`; see `color-tools names` |
| RGB | `'rgb(52,152,219)'`, `'rgb(52 152 219)'`, `52,152,219` |
| RGB with alpha | `'rgba(52,152,219,0.5)'`, `'rgb(52 152 219 / 50%)'` |
| HSL | `'hsl(204,70%,53%)'`, `'hsl(204deg 70% 53% / 0.5)'` |
| HSV | `'hsv(204,76%,86%)'` |
| Oklab | `'oklab(0.65 -0.06 -0.11)'` |
| OKLCh | `'oklch(0.65 0.13 243)'` |
| Temperature | `6500k` |

Quote colors. In most shells `#` starts a comment and parentheses start a
subshell, so `color-tools info #3498db` runs `color-tools info` with no
color at all. Single quotes are the safe choice:

```bash
color-tools info '#3498db'
color-tools info 'rgb(52, 152, 219)'
color-tools info "$BRAND_COLOR"     # double quotes around a variable
color-tools info 3498db             # hex without the # needs no quotes
```

Every color is held as 8-bit sRGB with an alpha, as in the library's `Color`
record. Converting `oklch()` input back to `oklch` therefore gives the
coordinates of the nearest 8-bit color, not the digits you typed.

Oklab and OKLCh can describe colors a screen cannot show. Those are clamped
into sRGB with a warning on stderr; `color-tools gamut` tells you beforehand.

```console
$ color-tools convert 'oklch(0.7 0.4 150)'
#00d600
color-tools: warning: oklch(0.7 0.4 150) is outside sRGB; clamped to #00d600
```

## Output

### Color formats: `--to`

Any command that prints colors takes `--to` (or `-t`) with `hex`, `hex8`,
`rgb`, `rgba`, `hsl`, `hsv`, `lab`, `oklab`, `oklch`, `name` or `kelvin`.

```console
$ color-tools convert '#3498db' --to hsl
hsl(204, 70%, 53%)
$ color-tools lighten '#3498db' 0.2 --to rgb
rgb(138, 196, 234)
$ color-tools palette '#3498db' --type triadic --to oklch
oklch(0.6112 0.2212 349.0374)
oklch(0.8168 0.2008 129.584)
```

Without `--to`, colors print as hex. A translucent color prints as `rgba()`
so its alpha isn't lost. `--to hex8` keeps it in hex form and `--to hex`
drops it on purpose.

```console
$ color-tools alpha '#3498db' 0.5
rgba(52, 152, 219, 0.5)
$ color-tools alpha '#3498db' 0.5 --to hex8
#3498db80
$ color-tools alpha '#3498db' 0.5 --to hex
#3498db
```

`lab` uses the D65 white point, like the library's `rgb->lab`. CSS `lab()`
uses D50, so don't paste these values into a stylesheet. `oklab` and `oklch`
match CSS.

### Structured output: `--json` and `--edn`

`--json` and `--edn` (or `--format json`, `--format edn`) print data instead
of text. Hex colors and names are strings; other formats are arrays of
numbers.

```console
$ color-tools convert '#3498db' --to hsl --json
[204, 70, 53]
$ color-tools contrast '#3498db' '#ffffff' --json
{
  "ratio": 3.15,
  "aa": false,
  "aa-large": true,
  "aaa": false
}
$ color-tools tints '#3498db' --count 3 --edn
["#78bae7" "#bbddf3" "#ffffff"]
$ color-tools similar '#3498db' '#3499dc' --edn
{:metric :rgb
 :distance 1.4142135623730951
 :threshold 50
 :similar true}
```

The shape is stable: a command given one color prints one value, and a
command given several colors, or colors from stdin, prints an array. Object
keys keep the order shown in the text output.

### Rounding

Text output rounds fractional numbers to 4 decimal places. JSON and EDN keep
full precision, so scripts decide how to round. `--precision N` (or `-p`)
and `--significant N` set the rounding for either.

```console
$ color-tools convert '#3498db' --to oklch
oklch(0.6531 0.1347 242.6867)
$ color-tools convert '#3498db' --to oklch --precision 2
oklch(0.65 0.13 242.69)
$ color-tools convert '#3498db' --to oklch --json
[0.653091517685353, 0.13470212300578396, 242.68666272850191]
```

### Color swatches

On a terminal, each color gets a small swatch in front of it. Swatches are
left out when the output goes to a pipe or a file, and in JSON, EDN and CSS,
so redirected output is always clean.

- `--color always` and `--color never` (or `--no-color`) override the
  detection.
- `NO_COLOR=1` turns swatches off and `FORCE_COLOR=1` turns them on, when
  `--color` isn't given.
- Swatches use 24-bit color when `COLORTERM` is `truecolor` or `24bit`, and
  the closest of 256 colors otherwise.

## Reading colors from stdin

Pass `-` where a color goes and the command reads colors from stdin, one per
line. Blank lines are skipped.

```bash
color-tools convert - --to hsl < colors.txt
cat colors.txt | color-tools lighten - 0.1
color-tools contrast - '#ffffff' --level aa < palette.txt
pbpaste | color-tools info -
```

With `-` the result is always a list, even for one line. A line that isn't a
color stops the run with exit code 2 and names the line:

```
color-tools: stdin line 3: invalid color "nope": not a color
```

Commands that take a list of colors, such as `gradient`, read all of stdin
as that list.

## Exit codes

| Code | Meaning |
| --- | --- |
| `0` | Success. For checks: the check passed. |
| `1` | A check failed: contrast below `--level`, `validate` on bad input, `similar`, `is` and `gamut` answering no, or `accessible` finding no color. |
| `2` | Invalid input or usage. The message is on stderr and stdout is empty. |

Results go to stdout. Errors and warnings go to stderr, prefixed with
`color-tools:`. A failed check still prints its result, so you can see why
it failed; add `--quiet` (or `-q`) when you only want the exit code:

```bash
if color-tools is dark "$BACKGROUND" --quiet; then
  TEXT='#ffffff'
fi
```

## Commands

### Inspect and convert

`info` shows everything about a color. A color on its own is shorthand for
it: `color-tools '#3498db'`.

```console
$ color-tools info '#3498db'
input        #3498db
hex          #3498db
rgb          rgb(52, 152, 219)
hsl          hsl(204, 70%, 53%)
hsv          hsv(204, 76%, 86%)
lab          lab(60.1597 -6.0866 -42.2363)
oklab        oklab(0.6531 -0.0618 -0.1197)
oklch        oklch(0.6531 0.1347 242.6867)
alpha        1
name         teal
luminance    0.283
brightness   130
dark         false
temperature  cool
vibrant      true
kelvin       none
text-color   #ffffff
```

`convert` reads any notation and prints the `--to` format.

```console
$ color-tools convert 'hsl(204, 70%, 53%)' --to hex
#3398db
$ color-tools convert navy --to rgb
rgb(0, 0, 128)
$ color-tools convert 6500k
#fffefa
$ color-tools convert '#ffffff' --to kelvin
6504K
```

`--to kelvin` prints `none` for colors with no meaningful temperature, such
as pure red.

`validate` answers "is this a color?" with an exit code, and reports the
detected format. `--as` also requires a format.

```console
$ color-tools validate '#3498db'
input   #3498db
valid   true
format  hex
hex     #3498db
error   none
$ color-tools validate 'rgb(300, 0, 0)'
input   rgb(300, 0, 0)
valid   false
format  none
hex     none
error   rgb channels must be from 0 to 255
$ color-tools validate navy --as hex
input   navy
valid   false
format  name
hex     #000080
error   expected hex, got name
```

`name` finds the closest named color and `names` lists them.

```console
$ color-tools name '#fe0000'
red
```

`gamut` checks that a color fits in sRGB.

```console
$ color-tools gamut 'oklch(0.7 0.4 150)'
input     oklch(0.7 0.4 150)
in-gamut  false
rgb       -154.0082 213.8568 -83.3699
clamped   #00d600
```

### Adjust

Amounts are fractions from 0 to 1, or percentages such as `20%`.

```console
$ color-tools lighten '#3498db' 0.2
#8ac4ea
$ color-tools darken '#3498db' 20%
#19608f
$ color-tools saturate '#3498db' 0.2
#1b9df3
$ color-tools desaturate '#3498db' 0.2
#4b93c3
$ color-tools rotate '#3498db' 180
#db7633
$ color-tools rotate '#3498db' -30
#33dbca
$ color-tools invert '#3498db'
#cb6724
$ color-tools grayscale '#3498db'
#828282
$ color-tools alpha '#3498db' 0.5
rgba(52, 152, 219, 0.5)
```

`rotate` is also available as `adjust-hue`, and `grayscale` as `greyscale`.
These commands keep the alpha of the color they are given:

```console
$ color-tools lighten 'rgba(52, 152, 219, 0.5)' 0.2
rgba(138, 196, 234, 0.5)
```

### Mix and blend

```console
$ color-tools mix '#ff0000' '#0000ff'
#800080
$ color-tools mix '#ff0000' '#0000ff' --ratio 0.25
#bf0040
$ color-tools blend '#ff8040' '#4080ff' --mode multiply
#404040
$ color-tools blend '#ff8040' '#4080ff' --mode screen
#ffc0ff
$ color-tools blend '#ff8040' '#4080ff' --mode overlay
#ff8080
$ color-tools blend '#ffffff' 'rgba(255, 0, 0, 0.5)' --mode alpha
#ff8080
```

`--ratio` is the share of the second color. `--mode alpha` composites a
translucent overlay onto the base.

`tint`, `shade` and `tone` mix a color with white, black and gray. Their
plurals generate a series.

```console
$ color-tools tint '#3498db' 0.5
#9acced
$ color-tools shade '#3498db' 0.5
#1a4c6e
$ color-tools tone '#3498db' 0.5
#5a8cae
$ color-tools tints '#3498db' --count 3
#78bae7
#bbddf3
#ffffff
$ color-tools shades '#3498db' --count 3
#236592
#113349
#000000
```

### Palettes and gradients

`palette` generates a harmony. `--type` is `complementary`, `triadic`,
`tetradic`, `analogous`, `split-complementary`, `monochromatic` or `random`.
Without `--type` it prints them all.

```console
$ color-tools palette '#3498db'
complementary        #db7633
triadic              #db3398 #98db33
tetradic             #ca33db #db7633 #44db33
analogous            #3344db #7633db #ca33db
split-complementary  #33a9db #6633db
monochromatic        #003d66 #066aad #1096ef #5db4ee #a6d4f2
$ color-tools palette '#3498db' --type analogous --count 5 --angle 15
#336edb
#3344db
#4c33db
#7633db
#a033db
$ color-tools palette '#3498db' --type monochromatic --count 3
#003d66
#1096ef
#a6d4f2
```

`--count` applies to `analogous`, `monochromatic` and `random`; `--angle` to
`analogous` and `split-complementary`. The base color is not part of the
result. `random` needs no base color:

```bash
color-tools palette --type random --count 8
color-tools random
color-tools random --count 5 --to hsl
```

`gradient` generates evenly spaced steps across two or more stops, and
`interpolate` picks single positions. `--space` is `rgb`, `hsl` or `hsv`;
the last two take the shortest way around the hue circle.

```console
$ color-tools gradient '#ff0000' '#0000ff' --steps 5
#ff0000
#bf0040
#800080
#4000bf
#0000ff
$ color-tools gradient '#ff0000' '#0000ff' --steps 5 --space hsl
#ff0000
#ff0080
#ff00ff
#8000ff
#0000ff
$ color-tools gradient red yellow green --steps 5 --json
["#ff0000", "#ff8000", "#ffff00", "#80c000", "#008000"]
$ color-tools interpolate '#ff0000' '#0000ff' --at 0.25
#bf0040
$ color-tools interpolate red yellow green --at 0.1,0.5,0.9
#ff3300
#ffff00
#339900
```

`kelvin` turns a color temperature from 1000 to 40000 into a color. For the
reverse, use `convert --to kelvin`.

```console
$ color-tools kelvin 2700 4000 6500
#ffa757
#ffcea6
#fffefa
```

### Accessibility and themes

`contrast` prints the WCAG contrast ratio of text on a background and
whether it passes each level: `aa` (4.5:1), `aa-large` (3:1) and `aaa`
(7:1).

```console
$ color-tools contrast '#3498db' '#ffffff'
ratio     3.15
aa        false
aa-large  true
aaa       false
```

With `--level` it becomes a check that exits 1 on failure:

```console
$ color-tools contrast '#3498db' '#ffffff' --level aa
ratio     3.15
aa        false
aa-large  true
aaa       false
level     aa
pass      false
```

The check compares the unrounded ratio, so a pair at 4.4999:1 fails `aa`
even though the ratio displays as 4.5. Translucent text is composited onto
the background before measuring.

`accessible` adjusts the lightness of a color until it meets `--level`
(default `aa`) on a background. `text-color` picks black or white.
`luminance` prints the WCAG relative luminance.

```console
$ color-tools accessible '#3498db' --on '#ffffff'
#000000
$ color-tools text-color '#3498db'
#ffffff
$ color-tools luminance '#3498db'
0.283
```

`theme` generates UI tokens from one brand color: backgrounds, surfaces,
brand colors, a text color that meets `--level` on each, and a 50 to 900
scale. `--mode` is `light` or `dark`.

```console
$ color-tools theme '#3498db'
mode           light
level          aa
background     #f7fbfe
on-background  #111827
surface        #ebf5fb
on-surface     #111827
muted          #b2bbc3
on-muted       #374151
primary        #3498db
on-primary     #000000
secondary      #3336db
on-secondary   #ffffff
accent         #db7633
on-accent      #000000
border         #d9dde0
focus-ring     #8433db
scale
  50   #f5fafd
  100  #ebf5fb
  200  #cce5f6
  300  #aed6f1
  400  #7bbce8
  500  #3498db
  600  #2c81ba
  700  #246a99
  800  #1d5478
  900  #153d58
```

`--format css` prints custom properties, ready for a stylesheet:

```console
$ color-tools theme '#3498db' --mode dark --format css --prefix brand --selector .dark
.dark {
  --brand-background: #06121a;
  --brand-on-background: #f9fafb;
  --brand-surface: #0b2130;
  --brand-on-surface: #f9fafb;
  --brand-muted: #394f5f;
  --brand-on-muted: #e5e7eb;
  --brand-primary: #3498db;
  --brand-on-primary: #000000;
  --brand-secondary: #3336db;
  --brand-on-secondary: #ffffff;
  --brand-accent: #db7633;
  --brand-on-accent: #000000;
  --brand-border: #2e383f;
  --brand-focus-ring: #8433db;
  --brand-scale-50: #f5fafd;
  --brand-scale-100: #ebf5fb;
  --brand-scale-200: #cce5f6;
  --brand-scale-300: #aed6f1;
  --brand-scale-400: #7bbce8;
  --brand-scale-500: #3498db;
  --brand-scale-600: #2c81ba;
  --brand-scale-700: #246a99;
  --brand-scale-800: #1d5478;
  --brand-scale-900: #153d58;
}
```

- `--prefix brand` names the properties `--brand-primary` and so on. A
  prefix that starts with `--` is used exactly as given. The default is
  `--ct-`.
- `--selector` sets the selector of the rule. The default is `:root`.
- `--to` takes the formats CSS understands: `hex`, `hex8`, `rgb`, `rgba`,
  `hsl`, `oklab` and `oklch`.
- `--vars` prints the same properties as text, JSON or EDN.

A light and a dark theme in one file:

```bash
color-tools theme '#3498db' --format css > theme.css
color-tools theme '#3498db' --mode dark --format css \
  --selector '[data-theme=dark]' >> theme.css
```

### Analyze and compare

`distance` measures how different two colors are. `--metric rgb` is the
straight-line distance between RGB channels; `--metric delta-e` is the CIE76
difference, which is closer to what the eye sees. Under 1 is invisible,
2 to 10 is noticeable and over 10 is clearly different.

```console
$ color-tools distance '#3498db' '#2980b9'
43.0465
$ color-tools distance '#3498db' '#2980b9' --metric delta-e
10.3273
```

`similar` is the same measurement as a check. The default `--threshold` is
50 for `rgb` and 2.3 for `delta-e`.

```console
$ color-tools similar '#3498db' '#3499dc'
metric     rgb
distance   1.4142
threshold  50
similar    true
$ color-tools similar '#3498db' '#2980b9' --metric delta-e
metric     delta-e
distance   10.3273
threshold  2.3
similar    false
```

`is` tests one property and answers with the exit code: `warm`, `cool`,
`light`, `dark`, `vibrant` or `muted`.

```console
$ color-tools is dark '#2c3e50'
true
$ color-tools is vibrant '#3498db' --threshold 80
false
```

`brightness` prints the perceived brightness from 0 to 255, and
`temperature` says `warm` or `cool`.

```console
$ color-tools brightness '#3498db'
130
$ color-tools temperature '#3498db' '#e74c3c'
cool
warm
```

## Recipes

### Fail a CI job on low contrast

GitHub Actions, with the brand colors in the workflow:

```yaml
jobs:
  contrast:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/setup-node@v4
      - name: Check text contrast
        run: |
          npx color-tools contrast '#1f2937' '#ffffff' --level aaa
          npx color-tools contrast '#3498db' '#0b1620' --level aa
```

Check every color of a palette file against one background. The job fails if
any line fails:

```bash
color-tools contrast - '#ffffff' --level aa < palette.txt
```

### Use results in a shell script

```bash
brand='#3498db'
text=$(color-tools text-color "$brand")
hover=$(color-tools darken "$brand" 10%)
echo "button { background: $brand; color: $text; }"
echo "button:hover { background: $hover; }"
```

### Pick fields with jq

```bash
color-tools info '#3498db' --json | jq -r '.oklch | @csv'
color-tools contrast '#3498db' '#fff' --json | jq '.ratio'
color-tools theme '#3498db' --json | jq -r '.scale["700"]'
```

### Validate colors in a design-token file

```bash
jq -r '.. | strings | select(startswith("#"))' tokens.json \
  | color-tools validate - --quiet \
  || echo 'tokens.json has an invalid color'
```

### Generate a gradient for CSS

```bash
stops=$(color-tools gradient '#ff0000' '#0000ff' --steps 5 --space hsl \
  | paste -sd, -)
echo "background: linear-gradient(to right, $stops);"
```

## Library coverage

Every public function of `jon.color-tools` is reachable from the CLI. This
table is checked by a test, so a function added to the library must get a
command or a reason before the build passes.

<!-- coverage:start -->
| Library function | Command |
| --- | --- |
| `->Color` | `convert` |
| `->color` | `convert` |
| `->hex` | `info`, `convert` |
| `->hsl` | `info`, `convert` |
| `->hsv` | `info`, `convert` |
| `->name` | `info`, `convert`, `name` |
| `->rgb` | `info`, `convert` |
| `->rgba` | `convert` |
| `accessible-theme` | `theme` |
| `accessible?` | `contrast` |
| `adjust-hue` | `rotate` |
| `alpha-blend` | `blend` |
| `analogous` | `palette` |
| `blend-multiply` | `blend` |
| `blend-overlay` | `blend` |
| `blend-screen` | `blend` |
| `brightness` | `info`, `brightness` |
| `clamp` | None. Number helper; commands validate ranges instead of clamping |
| `clamp-rgb` | `convert` |
| `color` | `convert` |
| `color-distance` | `distance` |
| `color-from-hex` | `convert` |
| `color-from-hsl` | `convert` |
| `color-from-hsv` | `convert` |
| `color-from-rgb` | `convert` |
| `color-from-rgba` | `convert` |
| `color-names` | `names` |
| `color?` | `convert` |
| `complementary` | `palette` |
| `contrast-ratio` | `contrast` |
| `cool?` | `is` |
| `dark?` | `info`, `is` |
| `darken` | `darken` |
| `delta-e` | `distance` |
| `desaturate` | `desaturate` |
| `find-accessible-color` | `accessible` |
| `generate-palette` | `palette` |
| `get-contrast-text` | `info`, `text-color` |
| `gradient` | `gradient` |
| `grayscale` | `grayscale` |
| `hex->hsl` | `convert` |
| `hex->name` | `name` |
| `hex->rgb` | `convert` |
| `hex->rgba` | `convert` |
| `hsl->hex` | `convert` |
| `hsl->rgb` | `convert` |
| `hsv->rgb` | `convert` |
| `in-gamut?` | `gamut` |
| `interpolate` | `interpolate` |
| `invert` | `invert` |
| `kelvin->color` | `kelvin` |
| `kelvin->hex` | `kelvin` |
| `kelvin->rgb` | `kelvin` |
| `light?` | `is` |
| `lighten` | `lighten` |
| `luminance` | `info`, `luminance` |
| `map->Color` | `convert` |
| `mix` | `mix` |
| `monochromatic` | `palette` |
| `muted?` | `is` |
| `name->hex` | `convert` |
| `normalize-css-color-string` | `convert` |
| `normalize-hex` | `convert` |
| `oklab->hex` | `convert` |
| `oklab->oklch` | `convert` |
| `oklab->rgb` | `convert` |
| `oklch->hex` | `convert` |
| `oklch->oklab` | `convert` |
| `oklch->rgb` | `convert` |
| `parse-css-color-components` | `convert` |
| `parse-css-rgb` | `convert` |
| `parse-css-rgba` | `convert` |
| `parse-int` | None. String helper used inside the library's own parsers |
| `perceptually-similar?` | `similar` |
| `random-color` | `random` |
| `rgb->hex` | `convert` |
| `rgb->hsl` | `convert` |
| `rgb->hsv` | `convert` |
| `rgb->kelvin` | `info`, `convert` |
| `rgb->lab` | `info`, `convert` |
| `rgb->oklab` | `info`, `convert` |
| `rgb->oklch` | `info`, `convert` |
| `rgba->hex` | `convert` |
| `round` | None. Number helper; --precision and --significant round output |
| `round-coords` | `convert` |
| `round-int` | None. Number helper; channels are whole numbers in every output |
| `saturate` | `saturate` |
| `shade` | `shade` |
| `shades` | `shades` |
| `similar?` | `similar` |
| `split-complementary` | `palette` |
| `temperature` | `info`, `temperature` |
| `tetradic` | `palette` |
| `theme->css-vars` | `theme` |
| `tint` | `tint` |
| `tints` | `tints` |
| `tone` | `tone` |
| `tones` | `tones` |
| `triadic` | `palette` |
| `valid-css-rgb?` | `validate` |
| `valid-css-rgba?` | `validate` |
| `valid-hex?` | `validate` |
| `valid-hsl?` | `validate` |
| `valid-hsv?` | `validate` |
| `valid-rgb?` | `validate` |
| `valid-rgba?` | `validate` |
| `vibrant?` | `info`, `is` |
| `warm?` | `is` |
| `with-alpha` | `alpha` |
<!-- coverage:end -->
