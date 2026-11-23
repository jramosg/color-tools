# color-tools

Convert, adjust and check colors from the terminal.

```bash
npm install --global @jramosg/color-tools

color-tools contrast '#3498db' '#ffffff' --level aa
color-tools convert '#3498db' --to oklch
color-tools palette '#3498db' --type triadic
color-tools gradient '#ff0000' '#0000ff' --steps 10 --space hsl
color-tools theme '#3498db' --mode both --format css > theme.css
```

You can run it without a global installation: `npx @jramosg/color-tools help`.

Use Node.js 18 or newer. The package contains one compiled file and has no
runtime package dependencies. Add `--json` for structured output in scripts.
Exit codes are `0` for success, `1` for a failed check and `2` for invalid
input or usage.

Run `color-tools help` for the commands, or read the
[CLI guide](https://github.com/jramosg/color-tools/blob/master/doc/cli.md).

This package is the command-line build of
[jon/color-tools](https://github.com/jramosg/color-tools), a color library
for Clojure and ClojureScript.
