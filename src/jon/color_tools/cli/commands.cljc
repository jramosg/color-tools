(ns jon.color-tools.cli.commands
  "The command table. Each command names its arguments and options, the
   library functions it covers and a function from parsed arguments to
   plain data. jon.color-tools.cli does the dispatch and the printing."
  (:require
   [jon.color-tools :as ct]
   [jon.color-tools.cli.output :as out]
   [jon.color-tools.cli.parse :as parse]))

(def ^:dynamic *warn*
  "Called with a message for stderr when input was accepted with a caveat."
  (fn [_message]))

;; A result that is also a check. A failed check makes the exit code 1.
(defrecord Verdict [value pass?])

(defn- verdict [value pass?]
  (->Verdict value pass?))

(defn verdict? [x]
  (instance? Verdict x))

(def groups
  [[:inspect "Inspect and convert"]
   [:adjust "Adjust"]
   [:mix "Mix and blend"]
   [:generate "Palettes and gradients"]])

;; =============================================================================
;; Arguments
;; =============================================================================

(defn- checked-color
  "Parse a color and warn when it had to be clamped into sRGB."
  [s]
  (let [{:keys [color in-gamut?] :as parsed} (parse/parse-color s)]
    (when (false? in-gamut?)
      (*warn* (str s " is outside sRGB; clamped to " (ct/->hex color))))
    parsed))

(def ^:private color-param
  {:key :color :label "COLOR" :parse (comp :color checked-color)})

(def ^:private parsed-color-param
  "A color argument whose command also needs the input text and format."
  {:key :color :label "COLOR" :parse checked-color})

(defn- variadic
  "Make `param` take `min-count` or more values."
  [param min-count]
  (assoc param :rest? true :min min-count))

;; =============================================================================
;; Inspect and convert
;; =============================================================================

(defn- hex-swatch [color]
  (out/shown color :hex true))

(defn- info [{{:keys [input color]} :color} _]
  (array-map
   :input input
   :hex (hex-swatch color)
   :rgb (out/shown color :rgb)
   :hsl (out/shown color :hsl)
   :hsv (out/shown color :hsv)
   :lab (out/shown color :lab)
   :oklab (out/shown color :oklab)
   :oklch (out/shown color :oklch)
   :alpha (:a color)
   :name (ct/->name color)
   :luminance (ct/luminance color)
   :brightness (ct/brightness color)
   :dark (ct/dark? color)
   :temperature (ct/temperature color)
   :vibrant (ct/vibrant? color)
   :kelvin (ct/rgb->kelvin (ct/->rgb color))
   :text-color (hex-swatch (ct/color (ct/get-contrast-text color)))))

(defn- validate [{input :color} {:keys [as]}]
  (let [{:keys [format color error]} (parse/try-color input)
        error (or error
                  (when (and as (not= as format))
                    (str "expected " (name as) ", got " (name format))))]
    (verdict (array-map
              :input input
              :valid (nil? error)
              :format format
              :hex (some-> color hex-swatch)
              :error error)
             (nil? error))))

(defn- gamut [{{:keys [input color oklab]} :color} _]
  (let [oklab (or oklab (ct/rgb->oklab (ct/->rgb color)))
        inside? (ct/in-gamut? oklab)]
    (verdict (array-map
              :input input
              :in-gamut inside?
              :rgb (ct/oklab->rgb oklab)
              :clamped (hex-swatch color))
             inside?)))

(def ^:private inspect-commands
  [{:name "info"
    :group :inspect
    :summary "Show every format and property of a color"
    :doc (str "Prints the color in each supported format, its closest name,\n"
              "WCAG luminance, brightness, warmth, approximate temperature\n"
              "and the black or white text color that reads best on it.")
    :params [(variadic parsed-color-param 1)]
    :each? true
    :examples ["info '#3498db'"
               "info navy 'hsl(204,70%,53%)' --json"]
    :api '[->hex ->rgb ->hsl ->hsv rgb->lab rgb->oklab rgb->oklch ->name
           luminance brightness dark? temperature vibrant? rgb->kelvin
           get-contrast-text]
    :run info}

   {:name "convert"
    :group :inspect
    :summary "Convert colors between formats"
    :doc (str "Reads any accepted color and prints it in the --to format.\n"
              "--to kelvin prints none for colors with no meaningful\n"
              "temperature. lab uses the D65 white point, unlike CSS lab().")
    :params [(variadic color-param 1)]
    :each? true
    :examples ["convert '#3498db' --to hsl"
               "convert 'rgb(52,152,219)' --to oklch --precision 3"
               "convert - --to hex < colors.txt"]
    :api '[color ->color ->Color map->Color color? color-from-hex
           color-from-rgb color-from-rgba color-from-hsl color-from-hsv
           ->rgba hex->rgb rgb->hex hex->rgba rgba->hex rgb->hsl hsl->rgb
           hex->hsl hsl->hex rgb->hsv hsv->rgb parse-css-rgb parse-css-rgba
           parse-css-color-components normalize-css-color-string
           normalize-hex name->hex oklab->rgb oklab->hex oklab->oklch
           oklch->oklab oklch->rgb oklch->hex clamp-rgb round-coords]
    :run (fn [{:keys [color]} _] color)}

   {:name "validate"
    :group :inspect
    :summary "Check that input is a color; exit 1 if not"
    :doc (str "Reports the detected format and the normalized hex of each\n"
              "input. With --as, the input must also be in that format:\n"
              "hex, name, rgb, rgba, hsl, hsv, oklab, oklch or kelvin.")
    :params [(variadic {:key :color :label "COLOR" :parse identity} 1)]
    :each? true
    :opts [:as]
    :examples ["validate '#3498db'"
               "validate 'rgb(52,152,219)' --as rgb"
               "validate - < colors.txt"]
    :api '[valid-hex? valid-rgb? valid-rgba? valid-hsl? valid-hsv?
           valid-css-rgb? valid-css-rgba?]
    :run validate}

   {:name "name"
    :group :inspect
    :summary "Find the closest named color"
    :params [(variadic color-param 1)]
    :each? true
    :examples ["name '#3498db'"]
    :api '[->name hex->name]
    :run (fn [{:keys [color]} _] (ct/->name color))}

   {:name "names"
    :group :inspect
    :summary "List the named colors"
    :examples ["names" "names --to rgb"]
    :api '[color-names]
    :run (fn [_ _]
           (into (sorted-map)
                 (map (fn [[k hex]] [(keyword k) (ct/color hex)]))
                 ct/color-names))}

   {:name "gamut"
    :group :inspect
    :summary "Check that a color fits in sRGB; exit 1 if not"
    :doc (str "Useful for oklab() and oklch() input, which can describe\n"
              "colors a screen cannot show. Prints the unclamped RGB\n"
              "channels and the clamped hex.")
    :params [(variadic {:key :color :label "COLOR" :parse parse/parse-color}
                       1)]
    :each? true
    :examples ["gamut 'oklch(0.7 0.4 150)'"]
    :api '[in-gamut?]
    :run gamut}])

;; =============================================================================
;; Adjust
;; =============================================================================

(def ^:private amount-param
  {:key :amount :label "AMOUNT"
   :parse #(parse/parse-fraction "AMOUNT" %)})

(def ^:private amount-doc
  "AMOUNT is a fraction from 0 to 1, or a percentage such as 20%.")

(defn- keep-alpha
  "Carry the alpha of `source` over to `result`. The library's adjustments
   work on the RGB channels and return an opaque color."
  [result source]
  (ct/with-alpha (ct/color result) (:a source)))

(defn- adjust-command
  "A command that changes one color by an amount with library function `f`."
  [group command-name f summary]
  {:name command-name
   :group group
   :summary summary
   :doc amount-doc
   :params [color-param amount-param]
   :examples [(str command-name " '#3498db' 0.2")
              (str command-name " - 20% < colors.txt")]
   :api [(symbol command-name)]
   :run (fn [{:keys [color amount]} _]
          (keep-alpha (f color amount) color))})

(def ^:private adjust-commands
  [(adjust-command :adjust "lighten" ct/lighten
                   "Lighten a color by raising its HSL lightness")
   (adjust-command :adjust "darken" ct/darken
                   "Darken a color by lowering its HSL lightness")
   (adjust-command :adjust "saturate" ct/saturate
                   "Make a color more saturated")
   (adjust-command :adjust "desaturate" ct/desaturate
                   "Make a color less saturated")

   {:name "rotate"
    :aliases ["adjust-hue"]
    :group :adjust
    :summary "Rotate the hue of a color by degrees"
    :doc "DEGREES may be negative. The hue wraps around 360."
    :params [color-param
             {:key :degrees :label "DEGREES"
              :parse #(parse/parse-number "DEGREES" %)}]
    :examples ["rotate '#3498db' 180" "rotate '#3498db' -30 --to hsl"]
    :api '[adjust-hue]
    :run (fn [{:keys [color degrees]} _]
           (keep-alpha (ct/adjust-hue color degrees) color))}

   {:name "invert"
    :group :adjust
    :summary "Invert a color"
    :params [(variadic color-param 1)]
    :each? true
    :examples ["invert '#3498db'"]
    :api '[invert]
    :run (fn [{:keys [color]} _] (keep-alpha (ct/invert color) color))}

   {:name "grayscale"
    :aliases ["greyscale"]
    :group :adjust
    :summary "Convert a color to gray of the same brightness"
    :params [(variadic color-param 1)]
    :each? true
    :examples ["grayscale '#3498db'"]
    :api '[grayscale]
    :run (fn [{:keys [color]} _] (keep-alpha (ct/grayscale color) color))}

   {:name "alpha"
    :group :adjust
    :summary "Set the alpha channel of a color"
    :doc "ALPHA is a fraction from 0 to 1, or a percentage such as 50%."
    :params [color-param
             {:key :alpha :label "ALPHA"
              :parse #(parse/parse-fraction "ALPHA" %)}]
    :examples ["alpha '#3498db' 0.5" "alpha '#3498db' 50% --to rgba --json"]
    :api '[with-alpha]
    :run (fn [{:keys [color alpha]} _] (ct/with-alpha color alpha))}])

;; =============================================================================
;; Mix and blend
;; =============================================================================

(def ^:private blend-modes
  {:multiply ct/blend-multiply
   :screen ct/blend-screen
   :overlay ct/blend-overlay
   :alpha ct/alpha-blend})

(defn- series-command
  "A command that generates --count variations of a color with `f`."
  [command-name f summary]
  {:name command-name
   :group :mix
   :summary summary
   :doc "Generates 5 colors unless --count says otherwise."
   :params [color-param]
   :opts [:count]
   :examples [(str command-name " '#3498db'")
              (str command-name " '#3498db' --count 9 --json")]
   :api [(symbol command-name)]
   :run (fn [{:keys [color]} {:keys [count]}]
          (mapv ct/color (f color count)))})

(def ^:private mix-commands
  [{:name "mix"
    :group :mix
    :summary "Mix two colors"
    :doc (str "--ratio is the share of the second color: 0 gives the first\n"
              "color, 1 the second, 0.5 an even mix.")
    :params [color-param (assoc color-param :key :other)]
    :opts [:ratio]
    :examples ["mix '#ff0000' '#0000ff'"
               "mix '#ff0000' '#0000ff' --ratio 0.25"]
    :api '[mix]
    :run (fn [{:keys [color other]} {:keys [ratio]}]
           (ct/mix color other ratio))}

   {:name "blend"
    :group :mix
    :summary "Blend an overlay onto a base color"
    :doc (str "multiply darkens, screen lightens and overlay does both,\n"
              "depending on the base. alpha composites a translucent\n"
              "overlay onto the base and keeps the resulting alpha.")
    :params [(assoc color-param :key :base :label "BASE")
             (assoc color-param :key :overlay :label "OVERLAY")]
    :opts [[:mode "multiply (default), screen, overlay or alpha"]]
    :examples ["blend '#ff0000' '#0000ff' --mode screen"
               "blend '#ffffff' 'rgba(255,0,0,0.5)' --mode alpha"]
    :api '[blend-multiply blend-screen blend-overlay alpha-blend]
    :run (fn [{:keys [base overlay]} {:keys [mode]}]
           (let [mode (parse/parse-choice "--mode" (keys blend-modes)
                                          (or mode "multiply"))]
             ((blend-modes mode) base overlay)))}

   (adjust-command :mix "tint" ct/tint "Mix a color with white")
   (adjust-command :mix "shade" ct/shade "Mix a color with black")
   (adjust-command :mix "tone" ct/tone "Mix a color with gray")
   (series-command "tints" ct/tints "Generate a series of tints toward white")
   (series-command "shades" ct/shades
                   "Generate a series of shades toward black")
   (series-command "tones" ct/tones "Generate a series of tones toward gray")])

;; =============================================================================
;; Palettes and gradients
;; =============================================================================

(def ^:private harmonies
  [:complementary :triadic :tetradic :analogous :split-complementary
   :monochromatic])

(def palette-types
  (conj harmonies :random))

(defn- palette-of [type color {:keys [count angle]}]
  (when (and (= :monochromatic type) (some-> count (< 2)))
    (throw (parse/usage-error
            "--count must be at least 2 for a monochromatic palette")))
  (mapv ct/color
        (ct/generate-palette type color (cond-> {}
                                          count (assoc :count count)
                                          angle (assoc :angle angle)))))

(defn- palette [{:keys [color]} {:keys [type] :as opts}]
  (cond
    (= :random type) (palette-of :random nil opts)

    (nil? color)
    (throw (parse/usage-error
            "missing COLOR"
            "Only --type random works without a color."))

    type (palette-of type color opts)

    :else (apply array-map
                 (mapcat (fn [harmony]
                           [harmony (palette-of harmony color opts)])
                         harmonies))))

(defn- random-colors [_ {:keys [count]}]
  (let [random #(ct/color (ct/random-color))]
    (if count
      (vec (repeatedly count random))
      (random))))

(defn- interpolate [{:keys [colors]} {:keys [at space]}]
  (let [results (mapv #(ct/color (ct/interpolate colors % space))
                      (or at [0.5]))]
    (if (next results)
      results
      (first results))))

(def ^:private generate-commands
  [{:name "palette"
    :group :generate
    :summary "Generate a color harmony from a base color"
    :doc (str "Without --type, prints every harmony. Types: complementary,\n"
              "triadic, tetradic, analogous, split-complementary,\n"
              "monochromatic and random. --count applies to analogous (3),\n"
              "monochromatic (5) and random (5); --angle to analogous and\n"
              "split-complementary. The base color is not in the result.")
    :params [(assoc color-param :optional? true)]
    :opts [:type :count :angle]
    :examples ["palette '#3498db'"
               "palette '#3498db' --type triadic"
               "palette '#3498db' --type analogous --count 5 --angle 15"
               "palette --type random --count 8 --json"]
    :api '[generate-palette complementary triadic tetradic analogous
           monochromatic split-complementary]
    :run palette}

   {:name "random"
    :group :generate
    :summary "Generate random colors"
    :doc "Prints one color, or a list with --count."
    :opts [:count]
    :examples ["random" "random --count 5 --to hsl"]
    :api '[random-color]
    :run random-colors}

   {:name "gradient"
    :group :generate
    :summary "Generate evenly spaced steps between two or more colors"
    :doc (str "Takes two or more colors as stops. hsl and hsv take the\n"
              "shortest way around the hue circle.")
    :params [(variadic (assoc color-param :key :colors) 2)]
    :opts [:steps :space]
    :examples ["gradient '#ff0000' '#0000ff' --steps 10 --space hsl"
               "gradient red yellow green --steps 7 --json"]
    :api '[gradient]
    :run (fn [{:keys [colors]} {:keys [steps space]}]
           (mapv ct/color (ct/gradient colors (or steps 5) space)))}

   {:name "interpolate"
    :group :generate
    :summary "Pick the color at a position between two or more colors"
    :doc (str "Position 0 is the first color and 1 the last. Several\n"
              "positions give a list.")
    :params [(variadic (assoc color-param :key :colors) 2)]
    :opts [:at :space]
    :examples ["interpolate '#ff0000' '#0000ff' --at 0.25"
               "interpolate red yellow green --at 0.1,0.5,0.9 --space hsl"]
    :api '[interpolate]
    :run interpolate}

   {:name "kelvin"
    :group :generate
    :summary "Convert a color temperature in kelvin to a color"
    :doc (str "Accepts 1000 to 40000, with or without a trailing k. For\n"
              "the reverse, use 'convert COLOR --to kelvin'.")
    :params [(variadic {:key :kelvin :label "KELVIN"
                        :parse #(parse/parse-kelvin "KELVIN" %)}
                       1)]
    :each? true
    :examples ["kelvin 6500" "kelvin 2700 4000 6500 --to rgb"]
    :api '[kelvin->color kelvin->rgb kelvin->hex]
    :run (fn [{:keys [kelvin]} _] (ct/kelvin->color kelvin))}])

(def commands
  (vec (concat inspect-commands adjust-commands mix-commands
               generate-commands)))
