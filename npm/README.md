# color-tools

Convert, adjust and check colors from the terminal.

```bash
npx color-tools contrast '#3498db' '#ffffff' --level aa
npx color-tools convert '#3498db' --to oklch
npx color-tools palette '#3498db' --type triadic
npx color-tools gradient '#ff0000' '#0000ff' --steps 10 --space hsl
npx color-tools theme '#3498db' --mode dark --format css > theme.css
```

One file, no dependencies. Text output for people, `--json` for scripts,
and exit codes for CI: `0` success, `1` a check failed, `2` invalid input.

Run `color-tools help` for the commands, or read the
[CLI guide](https://github.com/jramosg/color-tools/blob/master/doc/cli.md).

This package is the command-line build of
[jon/color-tools](https://github.com/jramosg/color-tools), a color library
for Clojure and ClojureScript.
