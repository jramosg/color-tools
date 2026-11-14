(ns jon.color-tools.cli.output
  "Renders command results as terminal text, JSON, EDN or CSS.

   Commands return plain data in which colors are Color records. `resolve`
   turns each color into the requested format, then one of the renderers
   prints the tree."
  (:refer-clojure :exclude [resolve])
  (:require
   [clojure.string :as str]
   [jon.color-tools :as ct]))

(def color-formats
  [:hex :rgb :rgba :hsl :hsv :lab :oklab :oklch :name :kelvin])

(def css-color-formats
  "Formats whose text form is a valid CSS color."
  #{:hex :rgb :rgba :hsl :oklab :oklch})

(def default-text-places
  "Decimal places for fractional numbers in text output."
  4)

;; A color pinned to one format, whatever --to says.
(defrecord Shown [color fmt swatch?])

(defn shown
  ([color fmt] (shown color fmt false))
  ([color fmt swatch?] (->Shown color fmt swatch?)))

;; A color after conversion: the value to print and the RGB of its swatch.
(defrecord Cell [fmt value rgb swatch?])

(defn- plain-map? [x]
  (and (map? x) (not (record? x))))

;; =============================================================================
;; Numbers
;; =============================================================================

#?(:cljs
   (defn- expand-exponent
     "Rewrite JavaScript's exponent notation, such as 1e-7, in plain digits."
     [s]
     (if-let [[_ sign whole frac exponent]
              (re-matches #"(-?)(\d+)\.?(\d*)e([+-]\d+)" s)]
       (let [digits (str whole frac)
             point (+ (count whole) (parse-long exponent))
             zeros #(apply str (repeat % "0"))]
         (str sign
              (cond
                (<= point 0) (str "0." (zeros (- point)) digits)
                (>= point (count digits)) (str digits
                                               (zeros (- point (count digits))))
                :else (str (subs digits 0 point) "." (subs digits point)))))
       s)))

(defn format-number
  "Print a number without an exponent or a trailing .0, the same way on
   every platform."
  [x]
  (cond
    (or (NaN? x) (infinite? x)) (str x)
    #?@(:clj [(integer? x) (str x)])
    :else #?(:clj (-> (BigDecimal/valueOf (double x))
                      .stripTrailingZeros
                      .toPlainString)
             :cljs (expand-exponent (str x)))))

(defn- rounder
  "A function that rounds fractional numbers as the options ask. Integers
   pass through."
  [{:keys [precision significant]} default-places]
  (let [options (cond
                  precision {:decimal-places precision}
                  significant {:significant-digits significant}
                  default-places {:decimal-places default-places})]
    (fn [x]
      (if (and options (number? x) (not (integer? x)))
        (first (ct/round-coords [x] options))
        x))))

;; =============================================================================
;; Colors
;; =============================================================================

(defn- css-function [fn-name separator parts]
  (str fn-name "(" (str/join separator parts) ")"))

(defn- color-value
  "The data form of `color` in `fmt`: a string for hex and names, a number
   for kelvin, a coordinate vector otherwise."
  [color fmt]
  (let [rgb (ct/->rgb color)]
    (case fmt
      :hex (ct/->hex color)
      :rgb rgb
      :rgba (ct/->rgba color)
      :hsl (ct/->hsl color)
      :hsv (ct/->hsv color)
      :lab (ct/rgb->lab rgb)
      :oklab (ct/rgb->oklab rgb)
      :oklch (ct/rgb->oklch rgb)
      :name (ct/->name color)
      :kelvin (ct/rgb->kelvin rgb))))

(defn- value-text
  "The text form of a converted color."
  [fmt value]
  (let [numbers (fn [xs] (map format-number xs))
        percents (fn [[h & more]]
                   (cons (format-number h)
                         (map #(str (format-number %) "%") more)))]
    (case fmt
      (:hex :name :css-rgba) value
      :rgb (css-function "rgb" ", " (numbers value))
      :rgba (css-function "rgba" ", " (numbers value))
      :hsl (css-function "hsl" ", " (percents value))
      :hsv (css-function "hsv" ", " (percents value))
      :lab (css-function "lab" " " (numbers value))
      :oklab (css-function "oklab" " " (numbers value))
      :oklch (css-function "oklch" " " (numbers value))
      :kelvin (if value (str (format-number value) "K") "none"))))

(defn- cell
  "Convert a color to `fmt`. With no format, opaque colors print as hex and
   translucent ones as rgba(), so the alpha is never dropped silently."
  [color fmt swatch? round-fn]
  (let [rounded (fn [fmt]
                  (let [value (color-value color fmt)]
                    (if (sequential? value)
                      (mapv round-fn value)
                      (round-fn value))))
        rgb (ct/->rgb color)]
    (cond
      fmt (->Cell fmt (rounded fmt) rgb swatch?)
      (= 1.0 (:a color)) (->Cell :hex (ct/->hex color) rgb swatch?)
      :else (->Cell :css-rgba (value-text :rgba (rounded :rgba)) rgb
                    swatch?))))

(defn resolve
  "Walk a command result. Colors become Cells in the format the options
   ask for, and fractional numbers are rounded. Text output rounds to 4
   decimal places by default; structured output keeps full precision."
  [tree {:keys [to format] :as options}]
  (let [round-fn (rounder options (when (#{:text :css} format)
                                    default-text-places))
        walk (fn walk [x]
               (cond
                 (ct/color? x) (cell x to true round-fn)
                 (instance? Shown x) (cell (:color x) (:fmt x) (:swatch? x)
                                           round-fn)
                 (plain-map? x) (apply array-map
                                       (mapcat (fn [[k v]] [k (walk v)]) x))
                 (sequential? x) (mapv walk x)
                 (number? x) (round-fn x)
                 :else x))]
    (walk tree)))

;; =============================================================================
;; Text
;; =============================================================================

(defn- ansi-256
  "The closest color in the 6x6x6 cube of the 256-color palette."
  [[r g b]]
  (let [level #(ct/round-int (* 5 (/ % 255.0)))]
    (+ 16 (* 36 (level r)) (* 6 (level g)) (level b))))

(defn- swatch
  "Two spaces on a background of `rgb`, for terminals. :ansi is :truecolor
   or :256."
  [rgb ansi]
  (str "\u001b[48;"
       (if (= :truecolor ansi)
         (str "2;" (str/join ";" rgb))
         (str "5;" (ansi-256 rgb)))
       "m  \u001b[0m "))

(defn- scalar-text [x ansi]
  (cond
    (instance? Cell x) (str (when (and ansi (:swatch? x))
                              (swatch (:rgb x) ansi))
                            (value-text (:fmt x) (:value x)))
    (nil? x) "none"
    (keyword? x) (name x)
    (number? x) (format-number x)
    :else (str x)))

(defn- inline-text [x ansi]
  (if (sequential? x)
    (str/join " " (map #(inline-text % ansi) x))
    (scalar-text x ansi)))

(defn- pad [s width]
  (apply str s (repeat (- width (count s)) " ")))

(defn- text-lines [x ansi indent]
  (cond
    (plain-map? x)
    (let [width (apply max 0 (map (comp count name key) x))]
      (mapcat (fn [[k v]]
                (if (plain-map? v)
                  (cons (str indent (name k))
                        (text-lines v ansi (str indent "  ")))
                  [(str indent (pad (name k) width) "  "
                        (inline-text v ansi))]))
              x))

    (and (sequential? x) (some plain-map? x))
    (apply concat (interpose [""] (map #(text-lines % ansi indent) x)))

    (sequential? x)
    (map #(str indent (inline-text % ansi)) x)

    :else
    [(str indent (scalar-text x ansi))]))

(defn text
  "Render a resolved result as lines of text: one value per line, maps as
   aligned key and value columns. `ansi` is :truecolor, :256 or nil."
  [tree ansi]
  (str/join (map #(str % "\n") (text-lines tree ansi ""))))

;; =============================================================================
;; JSON and EDN
;; =============================================================================

(defn- plain
  "Strip Cells down to their values. `key-fn` and `keyword-fn` convert map
   keys and keyword values."
  [tree key-fn keyword-fn]
  (let [walk (fn walk [x]
               (cond
                 (instance? Cell x) (:value x)
                 (plain-map? x) (apply array-map
                                       (mapcat (fn [[k v]] [(key-fn k) (walk v)])
                                               x))
                 (sequential? x) (mapv walk x)
                 (keyword? x) (keyword-fn x)
                 :else x))]
    (walk tree)))

(defn- json-string [s]
  (let [escape (fn [c]
                 (case c
                   \" "\\\""
                   \\ "\\\\"
                   \newline "\\n"
                   \return "\\r"
                   \tab "\\t"
                   (let [code #?(:clj (int c) :cljs (.charCodeAt c 0))]
                     (if (< code 32)
                       (let [hex #?(:clj (Integer/toHexString code)
                                    :cljs (.toString code 16))]
                         (str "\\u" (subs "0000" (count hex)) hex))
                       c))))]
    (str "\"" (apply str (map escape s)) "\"")))

(defn- json-lines
  "Pretty-print JSON. Objects and arrays of objects span lines; arrays of
   scalars stay on one."
  [x indent]
  (let [inner (str indent "  ")
        nested? #(or (map? %) (and (sequential? %) (some coll? %)))]
    (cond
      (map? x)
      (if (empty? x)
        "{}"
        (str "{\n"
             (str/join ",\n"
                       (map (fn [[k v]]
                              (str inner (json-string k) ": "
                                   (json-lines v inner)))
                            x))
             "\n" indent "}"))

      (and (sequential? x) (some nested? x))
      (str "[\n"
           (str/join ",\n" (map #(str inner (json-lines % inner)) x))
           "\n" indent "]")

      (sequential? x)
      (str "[" (str/join ", " (map #(json-lines % inner) x)) "]")

      (string? x) (json-string x)
      (nil? x) "null"
      (boolean? x) (str x)
      (and (number? x) (or (NaN? x) (infinite? x))) "null"
      (number? x) (format-number x)
      :else (json-string (str x)))))

(defn json [tree]
  (str (json-lines (plain tree name name) "") "\n"))

(defn edn [tree]
  (str (pr-str (plain tree identity identity)) "\n"))

;; =============================================================================
;; CSS
;; =============================================================================

(defn css
  "Render a map of custom property names to resolved colors as a CSS rule."
  [vars selector]
  (str selector " {\n"
       (str/join (map (fn [[k v]]
                        (str "  " k ": " (scalar-text v nil) ";\n"))
                      vars))
       "}\n"))
