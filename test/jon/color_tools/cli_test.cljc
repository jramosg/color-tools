(ns jon.color-tools.cli-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [are deftest is testing]]
   [jon.color-tools :as ct]
   [jon.color-tools.cli :as cli]))

(defn- run
  "Run the CLI with `args`. A map as the first argument sets :stdin text,
   :tty? and :env."
  [& args]
  (let [[process args] (if (map? (first args))
                         [(first args) (rest args)]
                         [{} args])]
    (cli/run (vec args)
             {:stdin (constantly (:stdin process ""))
              :tty? (:tty? process false)
              :env (:env process {})})))

(defn- out [& args]
  (let [{:keys [exit out err]} (apply run args)]
    (is (= [0 ""] [exit err]) (str "args: " (pr-str args)))
    out))

(defn- lines [& args]
  (str/split-lines (apply out args)))

(deftest help-and-version-test
  (testing "no arguments, help and --help print the command list"
    (let [help (out)]
      (is (str/starts-with? help (str "color-tools " cli/version)))
      (is (str/includes? help "Usage: color-tools <command>"))
      (is (str/includes? help "Exit codes:"))
      (is (= help (out "help")))
      (is (= help (out "--help")))))
  (testing "command help, three ways"
    (let [help (out "help" "convert")]
      (is (str/starts-with? help "Usage: color-tools convert COLOR..."))
      (is (str/includes? help "Examples:"))
      (is (str/includes? help "Library functions:"))
      (is (= help (out "convert" "--help")))
      (is (= help (out "-h" "convert")))))
  (testing "help lines fit a terminal"
    (doseq [help (cons (out) (map #(out "help" %) ["convert" "validate"]))
            line (str/split-lines help)]
      (is (<= (count line) 80) line)))
  (testing "version"
    (is (= (str "color-tools " cli/version "\n") (out "--version")))
    (is (= (out "--version") (out "-V")))))

(deftest usage-error-test
  (are [args message]
       (let [{:keys [exit out err]} (apply run args)]
         (and (= 2 exit)
              (= "" out)
              (str/starts-with? err (str "color-tools: " message))))
    ["covert" "#fff"] "unknown command \"covert\"\nDid you mean convert?"
    ["help" "nope"] "unknown command \"nope\""
    ["convert"] "missing COLOR\nUsage: color-tools convert COLOR..."
    ["convert" "nope"] "invalid color \"nope\": not a color\nAccepted colors:"
    ["names" "extra"] "unexpected argument \"extra\""
    ["convert" "#fff" "--bogus"] "unknown option --bogus"
    ["convert" "#fff" "--josn"] "unknown option --josn\nDid you mean --json?"
    ["convert" "#fff" "--to"] "--to needs a value: --to FORMAT"
    ["convert" "#fff" "--to" "cmyk"] "--to must be one of hex, rgb"
    ["convert" "#fff" "--json=yes"] "--json takes no value"
    ["convert" "#fff" "--as" "hex"] "--as is not an option of 'convert'"
    ["convert" "#fff" "--precision" "x"] "--precision must be a whole number"
    ["convert" "#fff" "--format" "css"] "--format css is not available"
    ["convert" "-" "-"] "only one argument can be - (stdin)"))

(deftest option-syntax-test
  (is (= "hsl(0, 0%, 100%)\n" (out "convert" "#fff" "--to" "hsl")))
  (is (= "hsl(0, 0%, 100%)\n" (out "--to=hsl" "convert" "#fff")))
  (is (= "hsl(0, 0%, 100%)\n" (out "convert" "-t" "HSL" "#fff")))
  (testing "-- ends the options"
    (is (= 2 (:exit (run "convert" "--" "--to"))))))

(deftest output-format-test
  (is (= "#ffffff\n" (out "convert" "fff")))
  (is (= "\"#ffffff\"\n" (out "convert" "fff" "--json")))
  (is (= "\"#ffffff\"\n" (out "convert" "fff" "--format" "json")))
  (is (= "[255 255 255]\n" (out "convert" "fff" "--edn" "--to" "rgb")))
  (testing "several inputs give a list"
    (is (= "[\"#ffffff\", \"#000080\"]\n"
           (out "convert" "fff" "navy" "--json"))))
  (testing "rounding"
    (is (= "oklch(0.6531 0.1347 242.6867)\n"
           (out "convert" "#3498db" "--to" "oklch")))
    (is (= "oklch(0.65 0.13 242.69)\n"
           (out "convert" "#3498db" "--to" "oklch" "-p" "2")))
    (is (= "[0.653, 0.135, 243]\n"
           (out "convert" "#3498db" "--to" "oklch" "--significant" "3"
                "--json"))))
  (testing "--quiet prints nothing"
    (is (= "" (out "convert" "fff" "--quiet")))))

(deftest swatch-test
  (let [swatch? #(str/includes? (:out %) "\u001b[48;")]
    (testing "off unless stdout is a terminal"
      (is (not (swatch? (run "convert" "fff"))))
      (is (swatch? (run {:tty? true} "convert" "fff"))))
    (testing "--color and --no-color override"
      (is (swatch? (run "convert" "fff" "--color" "always")))
      (is (not (swatch? (run {:tty? true} "convert" "fff" "--no-color"))))
      (is (not (swatch? (run {:tty? true} "convert" "fff" "--color"
                             "never")))))
    (testing "environment"
      (is (not (swatch? (run {:tty? true :env {"NO_COLOR" "1"}}
                             "convert" "fff"))))
      (is (not (swatch? (run {:tty? true :env {"TERM" "dumb"}}
                             "convert" "fff"))))
      (is (swatch? (run {:env {"FORCE_COLOR" "1"}} "convert" "fff")))
      (is (swatch? (run {:env {"NO_COLOR" "1"}}
                        "convert" "fff" "--color" "always"))))
    (testing "24-bit color only when the terminal says so"
      (is (str/includes?
           (:out (run {:tty? true :env {"COLORTERM" "truecolor"}}
                      "convert" "#3498db"))
           "\u001b[48;2;52;152;219m"))
      (is (str/includes? (:out (run {:tty? true} "convert" "#3498db"))
                         "\u001b[48;5;74m")))
    (testing "never in structured output"
      (is (not (swatch? (run "convert" "fff" "--json" "--color"
                             "always")))))))

(deftest stdin-test
  (testing "- reads one color per line and always gives a list"
    (is (= "#ffffff\n#000080\n"
           (out {:stdin "#fff\n\n  navy  \n"} "convert" "-")))
    (is (= "[\"#ffffff\"]\n" (out {:stdin "#fff\n"} "convert" "-" "--json")))
    (is (= "[]\n" (out {:stdin ""} "convert" "-" "--json")))
    (is (= "" (out {:stdin ""} "convert" "-"))))
  (testing "stdin colors join the ones on the command line"
    (is (= "#000000\n#ffffff\n#ff0000\n"
           (out {:stdin "#fff\n"} "convert" "black" "-" "red"))))
  (testing "errors name the line"
    (let [{:keys [exit err]} (run {:stdin "#fff\n\nnope\n"} "convert" "-")]
      (is (= 2 exit))
      (is (str/starts-with?
           err "color-tools: stdin line 3: invalid color \"nope\"")))))

(deftest info-test
  (is (= ["input        #3498db"
          "hex          #3498db"
          "rgb          rgb(52, 152, 219)"
          "hsl          hsl(204, 70%, 53%)"
          "hsv          hsv(204, 76%, 86%)"
          "lab          lab(60.1597 -6.0866 -42.2363)"
          "oklab        oklab(0.6531 -0.0618 -0.1197)"
          "oklch        oklch(0.6531 0.1347 242.6867)"
          "alpha        1"
          "name         teal"
          "luminance    0.283"
          "brightness   130"
          "dark         false"
          "temperature  cool"
          "vibrant      true"
          "kelvin       none"
          "text-color   #ffffff"]
         (lines "info" "#3498db")))
  (testing "structured output keeps coordinates as numbers"
    (let [json (out "info" "rgba(255,255,255,0.5)" "--json")]
      (is (str/includes? json "\"rgb\": [255, 255, 255],"))
      (is (str/includes? json "\"alpha\": 0.5,"))
      (is (str/includes? json "\"kelvin\": 6504,"))
      (is (str/includes? json "\"text-color\": \"#000000\""))))
  (testing "several colors print as blocks"
    (is (= 35 (count (lines "info" "red" "blue"))))))

(deftest convert-test
  (are [input fmt expected] (= [expected] (lines "convert" input "--to" fmt))
    "#3498db" "hex" "#3498db"
    "#3498db" "rgb" "rgb(52, 152, 219)"
    "#3498db" "rgba" "rgba(52, 152, 219, 1)"
    "#3498db" "hsl" "hsl(204, 70%, 53%)"
    "#3498db" "hsv" "hsv(204, 76%, 86%)"
    "#3498db" "name" "teal"
    "white" "kelvin" "6504K"
    "red" "kelvin" "none"
    "hsl(204, 70%, 53%)" "hex" "#3398db"
    "hsv(204, 76%, 86%)" "hex" "#3599db"
    "oklch(0.6531 0.1347 242.6867)" "hex" "#3498db"
    "6500k" "hex" "#fffefa")
  (testing "translucent colors keep their alpha unless told otherwise"
    (is (= ["rgba(52, 152, 219, 0.5)"]
           (lines "convert" "rgba(52,152,219,0.5)")))
    (is (= ["#3498db"] (lines "convert" "rgba(52,152,219,0.5)" "-t" "hex"))))
  (testing "colors outside sRGB are clamped with a warning"
    (is (= {:exit 0
            :out "#00d600\n"
            :err (str "color-tools: warning: oklch(0.7 0.4 150) is outside"
                      " sRGB; clamped to #00d600\n")}
           (run "convert" "oklch(0.7 0.4 150)")))))

(deftest validate-test
  (is (= ["input   #FFF"
          "valid   true"
          "format  hex"
          "hex     #ffffff"
          "error   none"]
         (lines "validate" "#FFF")))
  (testing "invalid input exits 1 and says why"
    (let [{:keys [exit out err]} (run "validate" "rgb(300,0,0)")]
      (is (= [1 ""] [exit err]))
      (is (str/includes? out "valid   false"))
      (is (str/includes? out "error   rgb channels must be from 0 to 255"))))
  (testing "one bad input fails the batch"
    (is (= 1 (:exit (run {:stdin "#fff\nnope\n"} "validate" "-"))))
    (is (= 0 (:exit (run {:stdin "#fff\nnavy\n"} "validate" "-")))))
  (testing "--as requires a format"
    (is (= 0 (:exit (run "validate" "#fff" "--as" "hex"))))
    (let [{:keys [exit out]} (run "validate" "navy" "--as" "hex")]
      (is (= 1 exit))
      (is (str/includes? out "error   expected hex, got name")))))

(deftest name-test
  (is (= "teal\n" (out "name" "#3498db")))
  (is (= "red\nnavy\n" (out "name" "#fe0000" "#000081")))
  (is (= ["black    #000000" "blue     #0000ff"] (take 2 (lines "names"))))
  (is (= 19 (count (lines "names"))))
  (is (str/includes? (out "names" "--to" "rgb" "--json")
                     "\"navy\": [0, 0, 128]")))

(deftest gamut-test
  (is (= ["input     #3498db"
          "in-gamut  true"
          "rgb       52 152 219"
          "clamped   #3498db"]
         (lines "gamut" "#3498db")))
  (let [{:keys [exit out err]} (run "gamut" "oklch(0.7 0.4 150)")]
    (is (= [1 ""] [exit err]))
    (is (= ["input     oklch(0.7 0.4 150)"
            "in-gamut  false"
            "rgb       -154.0082 213.8568 -83.3699"
            "clamped   #00d600"]
           (str/split-lines out)))))

(deftest adjust-test
  (testing "results match the library"
    (are [args expected] (= [expected] (apply lines args))
      ["lighten" "#3498db" "0.2"] (ct/lighten "#3498db" 0.2)
      ["darken" "#3498db" "20%"] (ct/darken "#3498db" 0.2)
      ["saturate" "#3498db" "0.2"] (ct/saturate "#3498db" 0.2)
      ["desaturate" "#3498db" "0.2"] (ct/desaturate "#3498db" 0.2)
      ["rotate" "#3498db" "-30"] (ct/adjust-hue "#3498db" -30)
      ["adjust-hue" "#3498db" "180"] (ct/adjust-hue "#3498db" 180)
      ["invert" "#3498db"] (ct/invert "#3498db")
      ["grayscale" "#3498db"] (ct/grayscale "#3498db")
      ["greyscale" "#3498db"] (ct/grayscale "#3498db")))
  (testing "known values"
    (is (= ["#8ac4ea"] (lines "lighten" "#3498db" "0.2")))
    (is (= ["#db7633"] (lines "rotate" "#3498db" "180")))
    (is (= ["#828282" "#4c4c4c"] (lines "grayscale" "#3498db" "red"))))
  (testing "alpha"
    (is (= ["rgba(52, 152, 219, 0.5)"] (lines "alpha" "#3498db" "0.5")))
    (is (= "[52, 152, 219, 0.5]\n"
           (out "alpha" "#3498db" "50%" "--to" "rgba" "--json")))
    (is (= ["#3498db"] (lines "alpha" "rgba(52,152,219,0.5)" "1"))))
  (testing "adjustments keep the alpha of the input"
    (is (= ["rgba(138, 196, 234, 0.5)"]
           (lines "lighten" "rgba(52,152,219,0.5)" "0.2")))
    (is (= ["rgba(203, 103, 36, 0.5)"]
           (lines "invert" "rgba(52,152,219,0.5)"))))
  (testing "a fixed argument of - runs the command once per line"
    (is (= "[\"#ff6666\", \"#6666ff\"]\n"
           (out {:stdin "#f00\n#00f\n"} "lighten" "-" "0.2" "--json"))))
  (testing "argument errors"
    (are [args message]
         (let [{:keys [exit err]} (apply run args)]
           (and (= 2 exit)
                (str/starts-with? err (str "color-tools: " message))))
      ["lighten" "#fff"] "missing AMOUNT\nUsage: color-tools lighten COLOR"
      ["lighten" "#fff" "2"] "AMOUNT must be from 0 to 1, or 0% to 100%"
      ["lighten" "#fff" "0.1" "0.2"] "unexpected argument \"0.2\""
      ["rotate" "#fff" "far"] "DEGREES must be a number, got \"far\""
      ["alpha" "#fff" "1.5"] "ALPHA must be from 0 to 1")))

(deftest mix-test
  (testing "mix"
    (is (= ["#800080"] (lines "mix" "#ff0000" "#0000ff")))
    (is (= ["#bf0040"] (lines "mix" "#ff0000" "#0000ff" "--ratio" "0.25")))
    (is (= ["#ff0000"] (lines "mix" "#ff0000" "#0000ff" "-r" "0%"))))
  (testing "blend modes match the library"
    (are [mode expected]
         (= [expected] (lines "blend" "#ff8040" "#4080ff" "--mode" mode))
      "multiply" (ct/blend-multiply "#ff8040" "#4080ff")
      "screen" (ct/blend-screen "#ff8040" "#4080ff")
      "overlay" (ct/blend-overlay "#ff8040" "#4080ff"))
    (is (= (lines "blend" "#ff8040" "#4080ff" "--mode" "multiply")
           (lines "blend" "#ff8040" "#4080ff"))
        "multiply is the default"))
  (testing "alpha compositing keeps the resulting alpha"
    (is (= ["#ff8080"]
           (lines "blend" "#ffffff" "rgba(255,0,0,0.5)" "-m" "alpha")))
    (is (= ["rgba(170, 0, 85, 0.75)"]
           (lines "blend" "rgba(0,0,255,0.5)" "rgba(255,0,0,0.5)" "-m"
                  "alpha"))))
  (testing "unknown mode"
    (let [{:keys [exit err]} (run "blend" "#fff" "#000" "--mode" "nope")]
      (is (= 2 exit))
      (is (str/includes? err "--mode must be one of multiply, screen"))))
  (testing "tint, shade and tone"
    (is (= ["#9acced"] (lines "tint" "#3498db" "0.5")))
    (is (= ["#1a4c6e"] (lines "shade" "#3498db" "0.5")))
    (is (= ["#5a8cae"] (lines "tone" "#3498db" "50%"))))
  (testing "series"
    (is (= (ct/tints "#3498db") (lines "tints" "#3498db")))
    (is (= (ct/shades "#3498db" 3) (lines "shades" "#3498db" "-n" "3")))
    (is (= "[\"#5a8cae\", \"#808080\"]\n"
           (out "tones" "#3498db" "--count" "2" "--json")))
    (is (= ["#ff8080 #ffffff" "#8080ff #ffffff"]
           (lines {:stdin "#f00\n#00f\n"} "tints" "-" "-n" "2")))
    (is (= 2 (:exit (run "tints" "#fff" "--count" "0"))))))

(deftest palette-test
  (testing "each type matches the library"
    (are [type expected]
         (= expected (lines "palette" "#3498db" "--type" type))
      "complementary" [(ct/complementary "#3498db")]
      "triadic" (ct/triadic "#3498db")
      "tetradic" (ct/tetradic "#3498db")
      "analogous" (ct/analogous "#3498db")
      "split-complementary" (ct/split-complementary "#3498db")
      "monochromatic" (ct/monochromatic "#3498db")))
  (testing "--count and --angle"
    (is (= (ct/analogous "#3498db" 5 15)
           (lines "palette" "#3498db" "--type" "analogous" "--count" "5"
                  "--angle" "15")))
    (is (= (ct/split-complementary "#3498db" 45)
           (lines "palette" "#3498db" "--type" "split-complementary"
                  "--angle" "45")))
    (is (= (ct/monochromatic "#3498db" 3)
           (lines "palette" "#3498db" "--type" "monochromatic" "-n" "3"))))
  (testing "without --type, every harmony"
    (is (= ["complementary        #db7633"
            "triadic              #db3398 #98db33"
            "tetradic             #ca33db #db7633 #44db33"
            "analogous            #3344db #7633db #ca33db"
            "split-complementary  #33a9db #6633db"
            "monochromatic        #003d66 #066aad #1096ef #5db4ee #a6d4f2"]
           (lines "palette" "#3498db")))
    (is (str/includes? (out "palette" "#3498db" "--json")
                       "\"triadic\": [\"#db3398\", \"#98db33\"],")))
  (testing "random needs no color"
    (let [colors (lines "palette" "--type" "random" "--count" "8")]
      (is (= 8 (count colors)))
      (is (every? ct/valid-hex? colors))))
  (testing "errors"
    (are [args message]
         (let [{:keys [exit err]} (apply run args)]
           (and (= 2 exit)
                (str/starts-with? err (str "color-tools: " message))))
      ["palette"] "missing COLOR\nOnly --type random works without a color."
      ["palette" "#fff" "--type" "nope"] "--type must be one of complementary"
      ["palette" "#fff" "--type" "monochromatic" "-n" "1"]
      "--count must be at least 2 for a monochromatic palette")))

(deftest random-test
  (is (ct/valid-hex? (str/trim (out "random"))))
  (is (= 5 (count (lines "random" "--count" "5"))))
  (is (re-matches #"hsl\(\d+, \d+%, \d+%\)\n" (out "random" "--to" "hsl"))))

(deftest gradient-test
  (is (= (ct/gradient ["#ff0000" "#0000ff"] 5)
         (lines "gradient" "#ff0000" "#0000ff")))
  (is (= ["#ff0000" "#ff0080" "#ff00ff" "#8000ff" "#0000ff"]
         (lines "gradient" "#ff0000" "#0000ff" "--steps" "5" "--space"
                "hsl")))
  (is (= (ct/gradient ["#ff0000" "#0000ff"] 4 :hsv)
         (lines "gradient" "#ff0000" "#0000ff" "--steps" "4" "--space"
                "hsv")))
  (testing "more than two stops, from the command line or stdin"
    (is (= "[\"#ff0000\", \"#ffff00\", \"#008000\"]\n"
           (out "gradient" "red" "yellow" "green" "--steps" "3" "--json")))
    (is (= ["#ff0000" "#ffff00" "#008000"]
           (lines {:stdin "red\nyellow\ngreen\n"} "gradient" "-" "--steps"
                  "3"))))
  (testing "errors"
    (are [args message]
         (let [{:keys [exit err]} (apply run args)]
           (and (= 2 exit)
                (str/starts-with? err (str "color-tools: " message))))
      ["gradient" "red"]
      (str "expected at least 2 COLOR arguments\n"
           "Usage: color-tools gradient COLOR COLOR...")
      ["gradient" "red" "blue" "--steps" "1"]
      "--steps must be a whole number of at least 2"
      ["gradient" "red" "blue" "--space" "lab"]
      "--space must be one of rgb, hsl, hsv")))

(deftest interpolate-test
  (is (= ["#800080"] (lines "interpolate" "#ff0000" "#0000ff")))
  (is (= ["#bf0040"] (lines "interpolate" "#ff0000" "#0000ff" "--at" "0.25")))
  (is (= ["#ff0000"] (lines "interpolate" "#ff0000" "#0000ff" "--at" "0")))
  (is (= ["#0000ff"] (lines "interpolate" "#ff0000" "#0000ff" "--at" "100%")))
  (testing "several positions give a list"
    (is (= ["#ff3300" "#ffff00" "#008000"]
           (lines "interpolate" "red" "yellow" "green" "--at" "0.1,0.5"
                  "--at" "1" "--space" "hsl"))))
  (is (= 2 (:exit (run "interpolate" "red" "blue" "--at" "2")))))

(deftest kelvin-test
  (is (= [(ct/kelvin->hex 6500)] (lines "kelvin" "6500")))
  (is (= ["rgb(255, 167, 87)" "rgb(255, 206, 166)"]
         (lines "kelvin" "2700" "4000k" "--to" "rgb")))
  (let [{:keys [exit err]} (run "kelvin" "500")]
    (is (= 2 exit))
    (is (str/includes? err "KELVIN must be from 1000 to 40000 kelvin"))))
