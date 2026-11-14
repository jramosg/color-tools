(ns jon.color-tools.cli.commands
  "The command table. Each command names its arguments and options, the
   library functions it covers and a function from parsed arguments to
   plain data. jon.color-tools.cli does the dispatch and the printing."
  (:require
   [clojure.string :as str]
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
   [:generate "Palettes and gradients"]
   [:accessibility "Accessibility and themes"]
   [:compare "Analyze and compare"]])

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

(def color-param
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
    :doc (str "Position 0 is the first color and 1 the last. Repeat --at\n"
              "or separate positions with commas to get a list.")
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

;; =============================================================================
;; Accessibility and themes
;; =============================================================================

(def ^:private levels [:aa :aa-large :aaa])

(defn- contrast
  "WCAG contrast of text on a background. Translucent text is composited
   onto the background first, since that is the color a reader sees."
  [{:keys [color background]} {:keys [level]}]
  (when (< (:a background) 1)
    (*warn* "the alpha of the background is ignored"))
  (let [text (if (< (:a color) 1)
               (ct/alpha-blend (ct/with-alpha background 1) color)
               color)
        result (concat [:ratio (ct/contrast-ratio text background)]
                       (mapcat (fn [l] [l (ct/accessible? text background l)])
                               levels))]
    (if level
      (let [pass? (ct/accessible? text background level)]
        (verdict (apply array-map (concat result [:level level :pass pass?]))
                 pass?))
      (apply array-map result))))

(defn- accessible [{:keys [color]} {:keys [on level]}]
  (when-not on
    (throw (parse/usage-error "--on BACKGROUND is required")))
  (let [level (or level :aa)
        found (ct/find-accessible-color on color level)]
    (when-not found
      (*warn* (str "no lightness of " (ct/->hex color) " reaches "
                   (name level) " on " (ct/->hex on))))
    (verdict (some-> found ct/color) (some? found))))

(def ^:private theme-tokens
  [:background :on-background :surface :on-surface :muted :on-muted
   :primary :on-primary :secondary :on-secondary :accent :on-accent
   :border :focus-ring])

(def ^:private scale-steps
  [:50 :100 :200 :300 :400 :500 :600 :700 :800 :900])

(defn- css-prefix
  "The custom property prefix for --prefix. A bare word such as brand
   becomes --brand-; a value that starts with -- is used as given."
  [prefix]
  (cond
    (not (re-matches #"[A-Za-z0-9_-]+" prefix))
    (throw (parse/usage-error
            (str "--prefix may contain only letters, digits, - and _; got "
                 (pr-str prefix))))

    (str/starts-with? prefix "--") prefix
    :else (str "--" prefix (when-not (str/ends-with? prefix "-") "-"))))

(defn- theme-vars
  "The theme as custom properties, in token order. Each name comes from
   theme->css-vars, one token at a time, so the order is ours and the
   naming is the library's."
  [theme prefix]
  (let [options (cond-> {} prefix (assoc :prefix prefix))
        css-var (fn [token-theme]
                  (let [[k v] (first (ct/theme->css-vars token-theme options))]
                    [k (ct/color v)]))]
    (apply array-map
           (concat
            (mapcat #(css-var (select-keys theme [%])) theme-tokens)
            (mapcat #(css-var {:scale (select-keys (:scale theme) [%])})
                    scale-steps)))))

(defn- theme [{:keys [color]} {:keys [mode level prefix vars format]}]
  (let [mode (parse/parse-choice "--mode" [:light :dark] (or mode "light"))
        prefix (some-> prefix css-prefix)
        theme (ct/accessible-theme color {:mode mode :level (or level :aa)})
        colors (fn [m ks] (mapcat (fn [k] [k (ct/color (m k))]) ks))]
    (if (or vars (= :css format))
      (theme-vars theme prefix)
      (apply array-map
             (concat [:mode mode :level (:level theme)]
                     (colors theme theme-tokens)
                     [:scale (apply array-map
                                    (colors (:scale theme) scale-steps))])))))

(def ^:private accessibility-commands
  [{:name "contrast"
    :group :accessibility
    :summary "WCAG contrast ratio of text on a background"
    :doc (str "Prints the ratio and whether it passes each WCAG level.\n"
              "With --level, exits 1 when the pair fails that level, for\n"
              "use in scripts and CI. Translucent text is composited onto\n"
              "the background first.")
    :params [(assoc color-param :label "TEXT")
             (assoc color-param :key :background :label "BACKGROUND")]
    :opts [:level]
    :examples ["contrast '#3498db' '#ffffff'"
               "contrast '#3498db' '#ffffff' --level aa"
               "contrast - '#ffffff' --level aa --json < palette.txt"]
    :api '[contrast-ratio accessible?]
    :run contrast}

   {:name "luminance"
    :group :accessibility
    :summary "WCAG relative luminance, from 0 to 1"
    :params [(variadic color-param 1)]
    :each? true
    :examples ["luminance '#3498db'"]
    :api '[luminance]
    :run (fn [{:keys [color]} _] (ct/luminance color))}

   {:name "accessible"
    :group :accessibility
    :summary "Adjust a color until it is readable on a background"
    :doc (str "Changes the lightness of COLOR until it meets --level\n"
              "(default aa) on the --on background. Exits 1 and prints\n"
              "none when no lightness is enough.")
    :params [color-param]
    :opts [[:on "The background color (required)"] :level]
    :examples ["accessible '#3498db' --on '#ffffff'"
               "accessible '#3498db' --on '#ffffff' --level aaa"]
    :api '[find-accessible-color]
    :run accessible}

   {:name "text-color"
    :aliases ["contrast-text"]
    :group :accessibility
    :summary "Black or white, whichever reads best on a background"
    :params [(variadic (assoc color-param :label "BACKGROUND") 1)]
    :each? true
    :examples ["text-color '#3498db'"]
    :api '[get-contrast-text]
    :run (fn [{:keys [color]} _] (ct/color (ct/get-contrast-text color)))}

   {:name "theme"
    :group :accessibility
    :summary "Generate accessible UI tokens from one brand color"
    :doc (str "Prints backgrounds, surfaces, brand colors, the text color\n"
              "that meets --level on each, and a 50 to 900 scale.\n"
              "--format css prints custom properties ready to paste; with\n"
              "it, --to takes hex, rgb, rgba, hsl, oklab or oklch.\n"
              "--prefix brand gives --brand-primary; a prefix that starts\n"
              "with -- is used as given.")
    :css? true
    :params [color-param]
    :opts [[:mode "light (default) or dark"] :level :prefix :selector :vars
           [:format "text (default), json, edn or css"]]
    :examples ["theme '#3498db'"
               "theme '#3498db' --mode dark --level aaa --json"
               "theme '#3498db' --format css > theme.css"
               "theme '#3498db' --format css --prefix brand --to oklch"
               "theme '#3498db' --mode dark --format css --selector .dark"]
    :api '[accessible-theme theme->css-vars]
    :run theme}])

;; =============================================================================
;; Analyze and compare
;; =============================================================================

(def ^:private metrics
  {:rgb {:distance ct/color-distance :similar? ct/similar? :threshold 50}
   :delta-e {:distance ct/delta-e
             :similar? ct/perceptually-similar?
             :threshold 2.3}})

(def ^:private metric-doc
  (str "--metric rgb is the straight-line distance between the RGB\n"
       "channels, from 0 to about 441.67. --metric delta-e is the CIE76\n"
       "difference in LAB, closer to what the eye sees: under 1 is\n"
       "invisible, 2 to 10 noticeable, over 10 clearly different."))

(defn- similar [{:keys [color other]} {:keys [metric threshold]}]
  (let [metric (or metric :rgb)
        {:keys [distance similar?] :as m} (metrics metric)
        threshold (or threshold (:threshold m))
        result (similar? color other threshold)]
    (verdict (array-map :metric metric
                        :distance (distance color other)
                        :threshold threshold
                        :similar result)
             result)))

(def ^:private predicates
  {:warm ct/warm?
   :cool ct/cool?
   :light ct/light?
   :dark ct/dark?
   :vibrant ct/vibrant?
   :muted ct/muted?})

(defn- is [{:keys [predicate color]} {:keys [threshold]}]
  (when (and threshold (not (#{:vibrant :muted} predicate)))
    (throw (parse/usage-error
            "--threshold applies only to vibrant and muted")))
  (let [f (predicates predicate)
        result (if threshold (f color threshold) (f color))]
    (verdict result result)))

(def ^:private compare-commands
  [{:name "distance"
    :group :compare
    :summary "How different two colors are, as a number"
    :doc metric-doc
    :params [color-param (assoc color-param :key :other)]
    :opts [:metric]
    :examples ["distance '#3498db' '#2980b9'"
               "distance '#3498db' '#2980b9' --metric delta-e"]
    :api '[color-distance delta-e]
    :run (fn [{:keys [color other]} {:keys [metric]}]
           ((:distance (metrics (or metric :rgb))) color other))}

   {:name "similar"
    :group :compare
    :summary "Check that two colors are close; exit 1 if not"
    :doc (str metric-doc "\n"
              "The default --threshold is 50 for rgb and 2.3 for delta-e,\n"
              "the just noticeable difference.")
    :params [color-param (assoc color-param :key :other)]
    :opts [:metric [:threshold "Largest distance that counts as similar"]]
    :examples ["similar '#3498db' '#3499dc'"
               "similar '#3498db' '#2980b9' --metric delta-e --threshold 5"]
    :api '[similar? perceptually-similar?]
    :run similar}

   {:name "is"
    :group :compare
    :summary "Test a property of a color; exit 1 if false"
    :doc (str "PROPERTY is warm, cool, light, dark, vibrant or muted.\n"
              "Warm hues run from magenta through red to yellow. Dark\n"
              "means a brightness under 128. Vibrant means an HSL\n"
              "saturation above --threshold (default 60); muted is the\n"
              "opposite.")
    :params [{:key :predicate :label "PROPERTY"
              :parse #(parse/parse-choice "PROPERTY" (keys predicates) %)}
             (variadic color-param 1)]
    :each? true
    :opts [[:threshold "Saturation, from 0 to 100, that counts as vibrant"]]
    :examples ["is dark '#2c3e50'"
               "is vibrant '#3498db' --threshold 80"
               "is light \"$BACKGROUND\" --quiet && echo 'use dark text'"]
    :api '[warm? cool? light? dark? vibrant? muted?]
    :run is}

   {:name "brightness"
    :group :compare
    :summary "Perceived brightness, from 0 to 255"
    :params [(variadic color-param 1)]
    :each? true
    :examples ["brightness '#3498db'"]
    :api '[brightness]
    :run (fn [{:keys [color]} _] (ct/brightness color))}

   {:name "temperature"
    :group :compare
    :summary "Whether a color is warm or cool"
    :doc "For a temperature in kelvin, use 'convert COLOR --to kelvin'."
    :params [(variadic color-param 1)]
    :each? true
    :examples ["temperature '#3498db'"]
    :api '[temperature]
    :run (fn [{:keys [color]} _] (ct/temperature color))}])

(def commands
  (vec (concat inspect-commands adjust-commands mix-commands
               generate-commands accessibility-commands compare-commands)))
