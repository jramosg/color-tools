#!/usr/bin/env bash
# Smoke-test a build of the color-tools CLI as a real process.
#
#   test/cli-smoke.sh bin/color-tools
#   test/cli-smoke.sh node npm/color-tools.js
#
# The Clojure tests cover behavior in depth. This checks that a given
# runtime starts the CLI, reads stdin, and returns the right exit codes.
set -uo pipefail

if [ "$#" -eq 0 ]; then
  echo "usage: $0 COMMAND [ARG...]" >&2
  exit 2
fi

failures=0

# check NAME EXPECTED_EXIT EXPECTED_STDOUT ARG...
check() {
  local name="$1" expected_exit="$2" expected_out="$3"
  shift 3
  local out exit_code
  out="$("${cli[@]}" "$@" 2>/dev/null)"
  exit_code=$?
  if [ "$exit_code" -ne "$expected_exit" ] || [ "$out" != "$expected_out" ]; then
    echo "FAIL $name"
    echo "  expected exit $expected_exit: $expected_out"
    echo "  actual   exit $exit_code: $out"
    failures=$((failures + 1))
  else
    echo "ok   $name"
  fi
}

cli=("$@")

check 'convert' 0 'hsl(204.0719, 69.8745%, 53.1373%)' convert '#3498db' --to hsl
check 'json' 0 '[52, 152, 219]' convert '#3498db' --to rgb --json
check 'oklch rounding' 0 'oklch(0.65 0.13 242.69)' \
  convert '#3498db' --to oklch --precision 2
check 'palette' 0 '#db3498
#98db34' palette '#3498db' --type triadic
check 'AA text by default' 0 '#000000' text-color '#3498db'
check 'large text level' 0 '#ffffff' text-color '#3498db' --level aa-large
check 'nearest accessible lightness' 0 '#207ab6' \
  accessible '#3498db' --on '#ffffff'
check 'passed check exits 0' 0 'true' is dark '#2c3e50'
check 'failed check exits 1' 1 'false' is light '#2c3e50'
check 'bad input exits 2' 2 '' convert nope
check 'unknown command exits 2' 2 '' frobnicate
check 'fractional RGB input' 0 '[0.25, 1.5, 2.75]' \
  convert 'rgb(0.25,1.5,2.75)' --to rgb --json
check 'precise HSL input' 0 '[127.5, 127.5, 127.5]' \
  convert 'hsl(0,0%,50%)' --to rgb --json
check 'mix keeps fractions' 0 '[127.5, 0, 127.5]' \
  mix red blue --to rgb --json
check 'alpha-safe rounding' 0 'rgba(13, 1, 255, 0.123)' \
  convert 'rgba(12.6,0.6,254.8,0.1234567)' --to rgba --precision 0
check 'rounded RGB stays in range' 0 'rgba(255, 0, 100, 0.5)' \
  convert 'rgba(255,0,96,0.5)' --to rgba --significant 1
check 'zero hue adjustment' 0 '#123456' rotate '#123456' 0
check 'neutral Lab white' 0 '[100, 0, 0]' convert white --to lab --json
check 'one-step gradient' 0 '#ff0000' gradient red blue --steps 1
check 'one-step gray palette' 0 '#333333' \
  palette '#808080' --type monochromatic --count 1

out="$(printf '#f00\n#00f\n' | "${cli[@]}" lighten - 0.2 --json 2>/dev/null)"
if [ "$out" = '["#ff6666", "#6666ff"]' ]; then
  echo 'ok   stdin'
else
  echo "FAIL stdin: $out"
  failures=$((failures + 1))
fi

# A reader that stops early must not crash the writer. The crash shows
# up on stderr, after head has already printed its line.
err="$({ "${cli[@]}" gradient red blue --steps 50000 | head -1 >/dev/null; } 2>&1)"
if [ -z "$err" ]; then
  echo 'ok   closed pipe'
else
  echo "FAIL closed pipe: $(echo "$err" | head -3)"
  failures=$((failures + 1))
fi

err="$("${cli[@]}" convert nope 2>&1 >/dev/null)"
case "$err" in
  'color-tools: invalid color "nope": not a color'*) echo 'ok   stderr' ;;
  *)
    echo "FAIL stderr: $err"
    failures=$((failures + 1))
    ;;
esac

if [ "$failures" -ne 0 ]; then
  echo "$failures check(s) failed"
  exit 1
fi
