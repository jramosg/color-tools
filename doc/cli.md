# Command-Line Interface

Use `color-tools` to convert colors, adjust them, generate palettes and
gradients, check WCAG contrast, or export theme CSS. Run commands on
Node.js, Babashka or the JVM.

Choose terminal text, JSON or EDN output. In scripts and CI, use exit codes
to check contrast, validate input or test color properties.

```console
$ color-tools contrast '#3498db' '#ffffff'
ratio     3.15
aa        false
aa-large  true
aaa       false
$ color-tools convert '#3498db' --to oklch
oklch(0.6531 0.1347 242.6867)
$ color-tools palette '#3498db' --type triadic
#db3498
#98db34
```

## Installation

Use the runtime you have installed. The commands and color calculations
match across runtimes; full-precision numeric output can differ in its final
floating-point bits.

For unreleased CLI changes, use [a checkout](#from-a-checkout). Registry
installation requires a package release that includes the CLI.

### Node.js

```bash
npm install --global @jramosg/color-tools
color-tools contrast '#3498db' '#ffffff'

# or, without installing
npx @jramosg/color-tools contrast '#3498db' '#ffffff'
```

You need Node.js 18 or newer. The package installs the `color-tools` command
from one compiled file and has no runtime package dependencies.

### Babashka

```bash
bbin install io.github.jramosg/color-tools
```

Install [bbin](https://github.com/babashka/bbin) and add its bin directory to
your `PATH` before installing the command.

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

### From a checkout

```bash
git clone https://github.com/jramosg/color-tools.git
color-tools/bin/color-tools help
```

`bin/color-tools` uses Babashka when it is installed and the Clojure CLI
otherwise. You can symlink it into a directory on your `PATH`.

## Getting help

```bash
color-tools help            # command list, global options and exit codes
color-tools help contrast   # arguments, options and examples of a command
color-tools contrast --help # the same
color-tools --version
```

### Shell completion

Load the completion script for your shell to complete command names,
options and values such as the formats of `--to`. You can use command
aliases and place global options before the command.

```bash
# bash: add to ~/.bashrc
source <(color-tools completions bash)

# zsh: add to ~/.zshrc, after compinit
source <(color-tools completions zsh)

# fish
color-tools completions fish > ~/.config/fish/completions/color-tools.fish
```

A mistyped command or option suggests the closest match:

```console
$ color-tools covert '#3498db'
color-tools: unknown command "covert"
Did you mean convert?
Run 'color-tools help' for the list of commands.
```

## Writing colors

Use these notations wherever a command takes a color:

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
color. Use single quotes for literal colors:

```bash
color-tools info '#3498db'
color-tools info 'rgb(52, 152, 219)'
color-tools info "$BRAND_COLOR"     # double quotes around a variable
color-tools info 3498db             # hex without the # needs no quotes
```

Colors retain fractional sRGB channels and alpha. The CLI opts into the
library's full-precision RGB, HSL and HSV conversions and record construction.
Oklab and OKLCh inputs keep their computed channels instead of passing through
hex. Hex output and terminal swatches round to 8-bit channels.

Mixing, tinting and compositing keep fractional results. Interpolation and
gradients use the library's final tie rounding for hex output, and keep
fractional channels for coordinate output. HSL adjustments and other 8-bit
operations return whole channels; their intermediate calculations follow
the library's current precision rules.

Oklab and OKLCh can describe colors outside sRGB. Converting between these
two spaces preserves the original coordinates, including colors a screen
cannot show. An sRGB output clamps those colors and warns on stderr;
`color-tools gamut` checks the unclamped coordinates.

```console
$ color-tools convert 'oklab(0.7 0.4 0.4)' --to oklch
oklch(0.7 0.5657 45)
```

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
hsl(204.0719, 69.8745%, 53.1373%)
$ color-tools lighten '#3498db' 0.2 --to rgb
rgb(139, 196, 234)
$ color-tools palette '#3498db' --type triadic --to oklch
oklch(0.6119 0.2203 349.0988)
oklch(0.8169 0.2003 129.6267)
```

Without `--to`, colors print as hex. A translucent color prints as `rgba()`
to retain alpha. Use `--to hex8` for hex with alpha, or `--to hex` to omit it.

Coordinate output (`rgb`, `hsl`, `hsv`, `lab`, `oklab`, `oklch`)
contains three coordinates and omits alpha. Use `rgba` or `hex8` when the
output must carry opacity. Native Oklab/OKLCh conversion retains alpha
while printing its three coordinates.

Conversions and one-color adjustments carry input alpha. Mixing, color
series, palettes, accessibility search and theme generation use RGB results
and return opaque colors. Interpolation and multi-step gradients are opaque;
a one-step gradient returns its first stop, including alpha. `blend --mode
alpha` computes the composited opacity.

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

Use `--json` or `--edn` (also `--format json` or `--format edn`) for
structured output. Hex colors and names are strings. Coordinate formats use
JSON arrays or EDN vectors. Kelvin output is a number, or `null`/`nil` when
the color has no meaningful temperature.

```console
$ color-tools convert '#3498db' --to hsl --json
[204.07185628742516, 69.8744769874477, 53.13725490196079]
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

For commands such as `convert` and `info`, one color argument produces one
result; multiple arguments or stdin produce a list of results. Generators
such as `gradient` produce their own lists. Object keys follow the order in
text output.

### Rounding

Text output rounds fractional numbers to 4 decimal places. JSON and EDN keep
full precision, so scripts decide how to round. `--precision N` (or `-p`)
and `--significant N` set the rounding for either.

RGB and RGBA formatting uses the library's CSS exporter. `--precision N`
rounds RGB to N decimal places and alpha to at least 3 places, so
`--precision 0` preserves an opacity such as 0.5. `--significant N` rounds
every RGB/RGBA coordinate, including whole channels, and clamps rounded RGB
to 0 through 255. Rounding affects only the output of that call.

```console
$ color-tools convert '#3498db' --to oklch
oklch(0.6531 0.1347 242.6867)
$ color-tools convert '#3498db' --to oklch --precision 2
oklch(0.65 0.13 242.69)
$ color-tools convert '#3498db' --to oklch --json
[0.653091517685353, 0.13470212300578396, 242.68666272850191]
```

### Color swatches

On a terminal, you see a swatch beside each color. With automatic detection,
pipes and files contain text without swatches. JSON, EDN and CSS output omit
swatches regardless of `--color`.

- `--color always` and `--color never` (or `--no-color`) override the
  detection.
- `NO_COLOR=1` turns swatches off and `FORCE_COLOR=1` turns them on, when
  `--color` isn't given.
- Swatches use 24-bit color when `COLORTERM` is `truecolor` or `24bit`, and
  the closest RGB match in the fixed
  [xterm color cube and grayscale ramp](https://invisible-island.net/xterm/xterm.faq.html#color_by_number)
  otherwise. The first 16 colors depend on your terminal theme and are
  excluded from matching.

## Reading colors from stdin

Warnings describe accepted input that needed adjustment, such as a clipped
color. They go to stderr for both positional colors and `--on` backgrounds,
including under `--quiet`; stdout stays suitable for scripts.

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

Before reading stdin, the CLI checks command options. You get a usage error
(exit 2) for invalid modes, missing required options and conflicting settings,
including with empty input or `--quiet`.

| Code | Meaning |
| --- | --- |
| `0` | Success. For checks: the check passed. |
| `1` | A check failed: contrast below `--level`, `validate` on bad input, `similar`, `is` and `gamut` answering no, or `accessible` finding no color. |
| `2` | Invalid input or usage. The message is on stderr and stdout is empty. |

Results go to stdout. Errors and warnings go to stderr, prefixed with
`color-tools:`. A failed check still prints its result, so you can see why
it failed; add `--quiet` (or `-q`) when you only want the exit code.

Quiet mode skips output conversion and formatting. It still checks all
inputs and output options, computes verdicts, and reports errors and warnings.

```bash
if color-tools is dark "$BACKGROUND" --quiet; then
  TEXT='#ffffff'
fi
```

## Commands

### Inspect and convert

Use `info` to inspect a color's formats, alpha, luminance and other
properties. You can omit the command name: `color-tools '#3498db'`.

```console
$ color-tools info '#3498db'
input        #3498db
hex          #3498db
rgb          rgb(52, 152, 219)
hsl          hsl(204.0719, 69.8745%, 53.1373%)
hsv          hsv(204.0719, 76.2557%, 85.8824%)
lab          lab(60.157 -6.096 -42.2295)
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
text-color   #000000
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
6503K
```

`--to kelvin` prints `none` for colors with no meaningful temperature, such
as pure red.

Use `validate` to check input and inspect its detected format. Add `--as`
to require a specific format.

Validation uses CLI notation rules, including HSV, Kelvin and flexible
separators. Success confirms the CLI can read the color; it does not
guarantee that a browser accepts the original text as CSS.

Coordinates whose conversion overflows are invalid input. Validation reports
them with exit 1 and continues through the rest of a batch; conversion
commands instead exit 2 with an input error.

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
#8bc4ea
$ color-tools darken '#3498db' 20%
#196090
$ color-tools saturate '#3498db' 0.2
#1c9df3
$ color-tools desaturate '#3498db' 0.2
#4c93c3
$ color-tools rotate '#3498db' 180
#db7734
$ color-tools rotate '#3498db' -30
#34dbcb
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
rgba(139, 196, 234, 0.5)
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
complementary        #db7734
triadic              #db3498 #98db34
tetradic             #cb34db #db7734 #45db34
analogous            #3445db #7734db #cb34db
split-complementary  #db3445 #dbcb34
monochromatic        #0f3a57 #1f6594 #328fcd #73b0d8 #b1d1e7
$ color-tools palette '#3498db' --type analogous --count 5 --angle 15
#346edb
#3445db
#4d34db
#7734db
#a134db
$ color-tools palette '#3498db' --type monochromatic --count 3
#0f3a57
#328fcd
#b1d1e7
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

`--steps 1` returns the first gradient stop. Monochromatic palettes follow
the base saturation, so gray input gives grays; `--count 1` returns the
darkest step.

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

Add `--level` to exit 1 when the pair fails that level:

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
even though the ratio displays as 4.5. For translucent text, the CLI measures
the color after compositing it onto the background.

`accessible` finds the nearest whole HSL lightness that meets `--level`
(default `aa`) on a background, keeping hue and saturation. If your color
passes, you get it unchanged.

`text-color` picks white when it meets `--level` (default `aa`), black when
only black does, and the one with more contrast when neither does. Use
`aa-large` for large text. The `text-color` field of `info` follows the same
rule and also accepts `--level`.
`luminance` prints the WCAG relative luminance.

```console
$ color-tools accessible '#3498db' --on '#ffffff'
#207ab6
$ color-tools text-color '#3498db'
#000000
$ color-tools text-color '#3498db' --level aa-large
#ffffff
$ color-tools luminance '#3498db'
0.283
```

`theme` generates UI tokens from one brand color: backgrounds, surfaces,
brand colors, a text color for each, and a 50 to 900 scale. `--mode` is
`light`, `dark` or `both`.

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
secondary      #3437db
on-secondary   #ffffff
accent         #db7734
on-accent      #000000
border         #d9dde0
focus-ring     #8534db
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

The generated text colors meet `--level` on backgrounds and surfaces.
Your brand color stays unchanged. Some mid-tone brand colors cannot support
`aaa` contrast (7:1), even with black or white text. For those colors, you get
the text color with more contrast and a warning on stderr naming the pair:

```console
$ color-tools theme '#3498db' --level aaa --quiet
color-tools: warning: on-primary has 6.66:1 on primary #3498db; no text color reaches aaa (7:1) on it
color-tools: warning: on-accent has 6.7:1 on accent #db7734; no text color reaches aaa (7:1) on it
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
  --brand-secondary: #3437db;
  --brand-on-secondary: #ffffff;
  --brand-accent: #db7734;
  --brand-on-accent: #000000;
  --brand-border: #2e383f;
  --brand-focus-ring: #8534db;
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
  prefix that starts with `--` keeps its spelling. The default is
  `--ct-`.
- `--selector` sets the selector of the rule. The default is `:root`.
- `--to` takes the formats CSS understands: `hex`, `hex8`, `rgb`, `rgba`,
  `hsl`, `oklab` and `oklch`.
- `--vars` prints the same properties as text, JSON or EDN.

`--mode both` generates the light and the dark theme together. As CSS, the
dark rule goes in a `prefers-color-scheme` media query, so the page follows
the reader's system setting:

```bash
color-tools theme '#3498db' --mode both --format css > theme.css
```

```css
:root {
  --ct-background: #f7fbfe;
  /* ... */
}

@media (prefers-color-scheme: dark) {
  :root {
    --ct-background: #06121a;
    /* ... */
  }
}
```

If your site toggles themes with a class or an attribute, name it with
`--dark-selector` and the dark rule uses that instead of the media query:

```bash
color-tools theme '#3498db' --mode both --format css \
  --dark-selector '[data-theme=dark]' > theme.css
```

As text, JSON or EDN, `--mode both` gives a map with `light` and `dark`.

### Analyze and compare

Use `distance` to compare colors. `--metric rgb` measures straight-line
distance between RGB channels; `--metric delta-e` measures CIE76 distance in
CIE L*a*b*. Smaller values mean a closer match under the chosen metric.

```console
$ color-tools distance '#3498db' '#2980b9'
43.0465
$ color-tools distance '#3498db' '#2980b9' --metric delta-e
10.3267
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
distance   10.3267
threshold  2.3
similar    false
```

`is` tests one property and answers with the exit code: `warm`, `cool`,
`light`, `dark`, `vibrant` or `muted`.

For `vibrant` and `muted`, set a saturation threshold from 0 to 100 with
`--threshold`. The default is 60. Other properties do not take a threshold.

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
          npx @jramosg/color-tools contrast '#1f2937' '#ffffff' --level aaa
          npx @jramosg/color-tools contrast '#3498db' '#0b1620' --level aa
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

Use this table to find the command for a library operation. Internal number
helpers and the colorspace protocol have no separate commands; their entries
explain where the CLI uses them. The coverage test requires an entry for each
public var, including new additions to the library.

<!-- coverage:start -->
| Library function | Command |
| --- | --- |
| `->Color` | `convert` |
| `->Oklab` | `convert` |
| `->color` | `convert` |
| `->css` | `convert` |
| `->hex` | `info`, `convert` |
| `->hsl` | `info`, `convert` |
| `->hsv` | `info`, `convert` |
| `->name` | `info`, `convert`, `name` |
| `->oklab-space` | `convert` |
| `->rgb` | `info`, `convert` |
| `->rgb-space` | `convert` |
| `->rgba` | `convert` |
| `Colorspace` | None. Protocol behind ->oklab-space and ->rgb-space; convert --to |
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
| `map->Oklab` | `convert` |
| `mix` | `mix` |
| `monochromatic` | `palette` |
| `muted?` | `is` |
| `name->hex` | `convert` |
| `normalize-css-color-string` | `convert` |
| `normalize-hex` | `convert` |
| `oklab` | `convert` |
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
| `round-int` | None. Number helper; hex and terminal swatches use 8-bit channels |
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
