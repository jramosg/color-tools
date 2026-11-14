(ns jon.color-tools.cli.parse
  "Turns command-line strings into colors and numbers, and rejects bad
   input with a message the user can act on."
  (:require
   [clojure.string :as str]
   [jon.color-tools :as ct]))

(defn usage-error
  "An exception for input the user can fix. The CLI prints its message and
   hint instead of a stack trace."
  ([message] (usage-error message nil))
  ([message hint]
   (ex-info message {:type ::usage :hint hint})))

(defn usage-error? [e]
  (= ::usage (:type (ex-data e))))

(def color-forms
  "The accepted color notations, as lines of help text."
  ["'#3498db', 3498db, '#fff', '#3498db80' or a name such as navy"
   "'rgb(52,152,219)', 'rgba(52,152,219,0.5)' or 52,152,219"
   "'hsl(204,70%,53%)' or 'hsv(204,76%,86%)'"
   "'oklab(0.65 -0.06 -0.11)' or 'oklch(0.65 0.13 243)'"
   "a temperature such as 6500k"])

(def color-hint
  (str "Accepted colors:\n"
       (str/join (map #(str "  " % "\n") color-forms))
       "Quote colors: # and () mean something to the shell."))

;; =============================================================================
;; Numbers
;; =============================================================================

(def ^:private number-pattern
  "[+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?")

(def ^:private number-re (re-pattern number-pattern))

(def ^:private component-re
  (re-pattern (str "(" number-pattern ")(%|deg|°)?")))

(defn- finite-number [s]
  (let [n (when (re-matches number-re s) (parse-double s))]
    (when (and n (not (infinite? n)))
      n)))

(defn parse-number
  "Parse `s` as a finite number, of at least `low` when given. `label`
   names the value in the error."
  ([label s] (parse-number label nil s))
  ([label low s]
   (let [n (finite-number (str/trim s))]
     (cond
       (nil? n)
       (throw (usage-error (str label " must be a number, got " (pr-str s))))

       (and low (< n low))
       (throw (usage-error
               (str label " must be at least " low ", got " (pr-str s))))

       :else n))))

(defn parse-choice
  "Parse one of `choices`, a sequence of keywords, ignoring case."
  [label choices s]
  (let [choice (keyword (str/lower-case (str/trim s)))]
    (if (some #{choice} choices)
      choice
      (throw (usage-error
              (str label " must be one of " (str/join ", " (map name choices))
                   "; got " (pr-str s)))))))

(defn parse-fraction
  "Parse a fraction from 0 to 1. Also accepts a percentage such as 20%."
  [label s]
  (let [text (str/trim s)
        percent? (str/ends-with? text "%")
        n (finite-number (cond-> text
                           percent? (subs 0 (dec (count text)))))
        value (cond-> n (and n percent?) (/ 100.0))]
    (if (and value (<= 0 value 1))
      value
      (throw (usage-error
              (str label " must be from 0 to 1, or 0% to 100%, got "
                   (pr-str s)))))))

(defn parse-integer
  "Parse an integer from `low` to `high`. `high` may be nil for no limit."
  [label low high s]
  (let [n (parse-long (str/trim s))]
    (if (and n (<= low n) (or (nil? high) (<= n high)))
      n
      (throw (usage-error
              (str label " must be a whole number"
                   (if high
                     (str " from " low " to " high)
                     (str " of at least " low))
                   ", got " (pr-str s)))))))

(defn- kelvin-range? [n]
  (<= 1000 n 40000))

(defn parse-kelvin
  "Parse a color temperature from 1000 to 40000, with or without a k."
  [label s]
  (let [n (finite-number (str/replace (str/trim s) #"(?i)\s*k$" ""))]
    (if (some-> n kelvin-range?)
      n
      (throw (usage-error
              (str label " must be from 1000 to 40000 kelvin, got "
                   (pr-str s)))))))

;; =============================================================================
;; Colors
;; =============================================================================

(defn- components
  "Split the inside of rgb(...) and friends into numbers with their units.
   Returns nil unless every part is a number."
  [body]
  (let [parts (remove str/blank? (str/split (str/trim body) #"[\s,/]+"))
        parsed (map #(when-let [[_ n unit] (re-matches component-re %)]
                       {:n (parse-double n) :unit unit})
                    parts)]
    (when (and (seq parsed) (every? some? parsed))
      (vec parsed))))

(defn- scaled
  "The number in a component. A percentage counts against `full`."
  [{:keys [n unit]} full]
  (if (= "%" unit)
    (* n (/ full 100.0))
    n))

(defn- alpha-of [component]
  (some-> component (scaled 1)))

(defn- valid-alpha? [alpha]
  (or (nil? alpha) (<= 0 alpha 1)))

(defn- apply-alpha [color alpha]
  (cond-> color
    alpha (ct/with-alpha alpha)))

(defn- rgb-color [[r g b a]]
  (let [rgb (mapv #(ct/round-int (scaled % 255)) [r g b])
        alpha (alpha-of a)]
    (cond
      (not (ct/valid-rgb? rgb))
      {:error "rgb channels must be from 0 to 255"}

      (not (valid-alpha? alpha))
      {:error "alpha must be from 0 to 1"}

      :else
      {:format (if alpha :rgba :rgb)
       :color (apply-alpha (ct/color-from-rgb rgb) alpha)})))

(defn- hue-color
  "Build a color from hsl or hsv components. Hue wraps around 360."
  [fmt valid? from-vec [h s x a]]
  (let [coords [(mod (:n h) 360) (:n s) (:n x)]
        alpha (alpha-of a)]
    (cond
      (not (valid? coords))
      {:error (str (name fmt) " saturation and "
                   (if (= :hsl fmt) "lightness" "value")
                   " must be from 0 to 100")}

      (not (valid-alpha? alpha))
      {:error "alpha must be from 0 to 1"}

      :else
      {:format fmt
       :color (apply-alpha (from-vec coords) alpha)})))

(defn- oklab-color
  "Build a color from Oklab coordinates. Colors outside sRGB are clamped;
   :in-gamut? records whether that happened."
  [fmt oklab alpha]
  (if (valid-alpha? alpha)
    {:format fmt
     :oklab oklab
     :in-gamut? (ct/in-gamut? oklab)
     :color (apply-alpha (ct/color (ct/oklab->hex oklab)) alpha)}
    {:error "alpha must be from 0 to 1"}))

(defn- function-color
  "Parse functional notation such as hsl(204, 70%, 53%)."
  [s]
  (when-let [[_ fn-name body] (re-matches #"(?i)([a-z]+)\s*\((.*)\)" s)]
    (let [comps (components body)
          n (count comps)
          fn-name (str/lower-case fn-name)]
      (cond
        (not (#{"rgb" "rgba" "hsl" "hsla" "hsv" "hsb" "oklab" "oklch"}
              fn-name))
        {:error (str "unknown color function " fn-name "()")}

        (not (<= 3 n 4))
        {:error (str fn-name "() takes 3 numbers and an optional alpha")}

        :else
        (case fn-name
          ("rgb" "rgba") (rgb-color comps)
          ("hsl" "hsla") (hue-color :hsl ct/valid-hsl? ct/color-from-hsl
                                    comps)
          ("hsv" "hsb") (hue-color :hsv ct/valid-hsv? ct/color-from-hsv
                                   comps)
          "oklab" (let [[l a b alpha] comps]
                    (oklab-color :oklab
                                 [(scaled l 1) (scaled a 0.4) (scaled b 0.4)]
                                 (alpha-of alpha)))
          "oklch" (let [[l c h alpha] comps
                        chroma (scaled c 0.4)]
                    (if (neg? chroma)
                      {:error "oklch chroma must not be negative"}
                      (oklab-color :oklch
                                   (ct/oklch->oklab
                                    [(scaled l 1) chroma (:n h)])
                                   (alpha-of alpha)))))))))

(defn- library-color
  "Colors the library parses itself: hex, names and plain CSS rgb()/rgba()."
  [s]
  (cond
    (ct/valid-hex? s)
    {:format :hex :color (ct/color-from-hex s)}

    (ct/name->hex s)
    {:format :name :color (ct/color-from-hex (ct/name->hex s))}

    (ct/valid-css-rgb? s)
    (when-let [rgb (ct/parse-css-color-components s)]
      {:format :rgb :color (ct/color-from-rgb rgb)})

    (ct/valid-css-rgba? s)
    (when-let [rgba (ct/parse-css-color-components s)]
      {:format :rgba :color (ct/color-from-rgba rgba)})))

(defn- alpha-hex-color
  "CSS hex with an alpha channel: #rgba or #rrggbbaa. The # is required,
   so that 4 and 8 digit numbers are not read as colors."
  [s]
  (when-let [[_ digits] (re-matches #"^#([0-9a-fA-F]{8}|[0-9a-fA-F]{4})$" s)]
    (let [short? (= 4 (count digits))
          rgb-length (if short? 3 6)
          alpha (subs digits rgb-length)
          alpha-byte (ct/parse-int (cond->> alpha short? (str alpha)) 16)]
      {:format :hex
       :color (ct/with-alpha (ct/color-from-hex (subs digits 0 rgb-length))
                (/ alpha-byte 255.0))})))

(defn- kelvin-color [s]
  (when-let [[_ n] (re-matches #"(?i)(\d+(?:\.\d+)?)\s*k" s)]
    (let [kelvin (parse-double n)]
      (if (kelvin-range? kelvin)
        {:format :kelvin :color (ct/kelvin->color kelvin)}
        {:error "kelvin must be from 1000 to 40000"}))))

(defn- bare-color
  "Bare channels such as 52,152,219 or 52,152,219,0.5."
  [s]
  (let [comps (components s)]
    (when (<= 3 (count comps) 4)
      (rgb-color comps))))

(defn try-color
  "Parse a color string. Returns a map with :input, :format and :color (a
   Color record), or with :input and :error when it isn't a color. Oklab
   and OKLCh input also carries :oklab and :in-gamut?."
  [input]
  (let [s (str/trim input)]
    (assoc (or (library-color s)
               (alpha-hex-color s)
               (kelvin-color s)
               (function-color s)
               (bare-color s)
               {:error "not a color"})
           :input input)))

(defn parse-color
  "Like try-color, but throws a usage error for input that isn't a color."
  [input]
  (let [{:keys [error] :as parsed} (try-color input)]
    (if error
      (throw (usage-error (str "invalid color " (pr-str input) ": " error)
                          color-hint))
      parsed)))
