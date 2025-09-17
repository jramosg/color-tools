;; copyright (c) 2025 -- Jon Ramos, all rights reserved.

   (ns jon.color-tools
     (:require
      [clojure.string :as str]))

(def color-names
  {"red" "#ff0000"
   "green" "#008000"
   "blue" "#0000ff"
   "yellow" "#ffff00"
   "orange" "#ffa500"
   "purple" "#800080"
   "pink" "#ffc0cb"
   "brown" "#a52a2a"
   "black" "#000000"
   "white" "#ffffff"
   "gray" "#808080"
   "cyan" "#00ffff"
   "magenta" "#ff00ff"
   "lime" "#00ff00"
   "maroon" "#800000"
   "navy" "#000080"
   "olive" "#808000"
   "silver" "#c0c0c0"
   "teal" "#008080"})

;; =============================================================================
;; Utility Functions
;; =============================================================================

(defn clamp
  "Clamp a value between min and max"
  [value min-val max-val]
  (max min-val (min max-val value)))

(defn round
  "Round a number to specified decimal places"
  [n & [decimals]]
  (let [decimals (or decimals 0)
        factor (Math/pow 10 decimals)]
    (cond-> (/ (Math/round #?(:clj (double (* n factor))
                              :cljs (* n factor)))
               factor)
      (= decimals 0) int)))

(defn round-int [n]
  (round n 0))

(defn parse-int [s & [radix]]
  #?(:clj (Integer/parseInt s (or radix 10))
     :cljs (js/parseInt s (or radix 10))))

;; =============================================================================
;; Color Format Validation
;; =============================================================================

(defn valid-hex?
  "Check if string is a valid hex color"
  [hex]
  (and (string? hex)
       (re-matches #"^#?[0-9a-fA-F]{3}([0-9a-fA-F]{3})?$" hex)))

(defn valid-rgb?
  "Check if vector is a valid RGB color"
  [rgb]
  (and (vector? rgb)
       (= 3 (count rgb))
       (every? #(and (number? %) (<= 0 % 255)) rgb)))

(defn valid-rgba?
  "Check if vector is a valid RGBA color"
  [rgba]
  (and (vector? rgba)
       (= 4 (count rgba))
       (every? #(and (number? %) (<= 0 % 255)) (take 3 rgba))
       (let [alpha (nth rgba 3)]
         (and (number? alpha) (<= 0 alpha 1)))))

(defn valid-hsl?
  "Check if vector is a valid HSL color"
  [hsl]
  (and (vector? hsl)
       (= 3 (count hsl))
       (let [[h s l] hsl]
         (and (number? h) (<= 0 h 360)
              (number? s) (<= 0 s 100)
              (number? l) (<= 0 l 100)))))

(defn valid-hsv?
  "Check if vector is a valid HSV color"
  [hsv]
  (and (vector? hsv)
       (= 3 (count hsv))
       (let [[h s v] hsv]
         (and (number? h) (<= 0 h 360)
              (number? s) (<= 0 s 100)
              (number? v) (<= 0 v 100)))))

(defn valid-css-rgb?
  "Check if string is a valid CSS rgb() color"
  [css-str]
  (and (string? css-str)
       (boolean (re-matches #"^\s*rgb\s*\(\s*\d+\s*(?:,\s*|\s+)\s*\d+\s*(?:,\s*|\s+)\s*\d+\s*\)\s*$"
                            (str/lower-case css-str)))))

(defn valid-css-rgba?
  "Check if string is a valid CSS rgba() color"
  [css-str]
  (and (string? css-str)
       (boolean (re-matches #"^\s*rgba\s*\(\s*\d+\s*(?:,\s*|\s+)\s*\d+\s*(?:,\s*|\s+)\s*\d+\s*(?:,\s*|\s+)\s*(?:0(?:\.\d+)?|1(?:\.0+)?|\.\d+)\s*\)\s*$"
                            (str/lower-case css-str)))))

;; =============================================================================
;; Color Format Conversions
;; =============================================================================

(defn normalize-hex
  "Normalize hex color to 6 characters with #"
  [hex]
  (let [clean-hex (str/replace hex #"^#" "")]
    (str "#" (if (= 3 (count clean-hex))
               (apply str (mapcat #(repeat 2 %) clean-hex))
               clean-hex))))

(defn hex->rgb
  "Convert hex color to RGB vector"
  [hex]
  {:pre [(valid-hex? hex)]}
  (let [normalized (normalize-hex hex)
        hex-val (subs normalized 1)]
    [(parse-int (subs hex-val 0 2) 16)
     (parse-int (subs hex-val 2 4) 16)
     (parse-int (subs hex-val 4 6) 16)]))

(defn rgb->hex
  "Convert RGB vector to hex string"
  [rgb]
  {:pre [(valid-rgb? rgb)]}
  (let [[r g b] rgb
        to-hex #(let [n (-> % (clamp 0 255) float Math/round int)
                      hex-str #?(:clj (Integer/toString n 16)
                                 :cljs (.toString n 16))]
                  (if (= 1 (count hex-str))
                    (str "0" hex-str)
                    hex-str))]
    (str "#" (to-hex r) (to-hex g) (to-hex b))))

(defn hex->rgba
  "Convert hex color to RGBA vector with optional alpha"
  [hex & [alpha]]
  {:pre [(valid-hex? hex)]}
  (let [rgb (hex->rgb hex)
        a (or alpha 1)]
    (conj rgb a)))

(defn rgba->hex
  "Convert RGBA vector to hex string (ignoring alpha)"
  [rgba]
  {:pre [(valid-rgba? rgba)]}
  (rgb->hex (into [] (take 3 rgba))))

(defn rgb->hsl
  "Convert RGB to HSL"
  [rgb]
  {:pre [(valid-rgb? rgb)]}
  (let [[r g b] (map #(/ % 255) rgb)
        max-val (max r g b)
        min-val (min r g b)
        diff (- max-val min-val)
        l (/ (+ max-val min-val) 2)
        s (if (zero? diff)
            0
            (if (< l 0.5)
              (/ diff (+ max-val min-val))
              (/ diff (- 2 max-val min-val))))
        h (cond
            (zero? diff) 0
            (= max-val r) (mod (/ (- g b) diff) 6)
            (= max-val g) (+ (/ (- b r) diff) 2)
            (= max-val b) (+ (/ (- r g) diff) 4))]
    [(round (* h 60))
     (round (* s 100))
     (round (* l 100))]))

(defn hsl->rgb
  "Convert HSL to RGB"
  [hsl]
  {:pre [(valid-hsl? hsl)]}
  (let [[h s l] hsl
        h-norm (/ h 360)
        s-norm (/ s 100)
        l-norm (/ l 100)
        abs-fn #(if (< % 0) (- %) %)
        c (* (- 1 (abs-fn (- (* 2 l-norm) 1))) s-norm)
        x (* c (- 1 (abs-fn (- (mod (* h-norm 6) 2) 1))))
        m (- l-norm (/ c 2))
        [r' g' b'] (cond
                     (< h-norm (/ 1 6)) [c x 0]
                     (< h-norm (/ 2 6)) [x c 0]
                     (< h-norm (/ 3 6)) [0 c x]
                     (< h-norm (/ 4 6)) [0 x c]
                     (< h-norm (/ 5 6)) [x 0 c]
                     :else [c 0 x])]
    [(round-int (* (+ r' m) 255))
     (round-int (* (+ g' m) 255))
     (round-int (* (+ b' m) 255))]))

(defn hex->hsl
  "Convert hex to HSL"
  [hex]
  {:pre [(valid-hex? hex)]}
  (-> hex hex->rgb rgb->hsl))

(defn hsl->hex
  "Convert HSL to hex"
  [hsl]
  {:pre [(valid-hsl? hsl)]}
  (-> hsl hsl->rgb rgb->hex))

(defn rgb->hsv
  "Convert RGB to HSV"
  [rgb]
  {:pre [(valid-rgb? rgb)]}
  (let [[r g b] (map #(/ % 255) rgb)
        max-val (max r g b)
        min-val (min r g b)
        diff (- max-val min-val)
        v max-val
        s (if (zero? max-val) 0 (/ diff max-val))
        h (cond
            (zero? diff) 0
            (= max-val r) (mod (/ (- g b) diff) 6)
            (= max-val g) (+ (/ (- b r) diff) 2)
            (= max-val b) (+ (/ (- r g) diff) 4))]
    [(round (* h 60)) (round (* s 100)) (round (* v 100))]))

(defn hsv->rgb
  "Convert HSV to RGB"
  [hsv]
  (let [[h s v] hsv
        h-norm (/ h 360)
        s-norm (/ s 100)
        v-norm (/ v 100)
        c (* v-norm s-norm)
        abs-fn #(if (< % 0) (- %) %)
        x (* c (- 1 (abs-fn (- (mod (* h-norm 6) 2) 1))))
        m (- v-norm c)
        [r' g' b'] (cond
                     (< h-norm (/ 1 6)) [c x 0]
                     (< h-norm (/ 2 6)) [x c 0]
                     (< h-norm (/ 3 6)) [0 c x]
                     (< h-norm (/ 4 6)) [0 x c]
                     (< h-norm (/ 5 6)) [x 0 c]
                     :else [c 0 x])]
    [(round-int (* (+ r' m) 255))
     (round-int (* (+ g' m) 255))
     (round-int (* (+ b' m) 255))]))

(defn normalize-css-color-string
  "Normalize CSS color string by removing extra spaces and standardizing separators"
  [css-str]
  (-> css-str
      str/trim
      str/lower-case
      (str/replace #"\s*,\s*" ",")  ; normalize comma separators
      (str/replace #"\s+" " ")      ; normalize spaces
      (str/replace #"\(\s*" "(")    ; remove space after opening paren
      (str/replace #"\s*\)" ")")))  ; remove space before closing paren

(defn parse-css-color-components
  "Parse CSS rgb() or rgba() string into color component vector.
  
  Takes a CSS color string and normalizes it internally before extracting
  the color components using regex matching. Handles various formatting
  variations including different spacing and case.
  
  Args:
    css-str - A CSS color string in format:
              - 'rgb(r,g,b)' or 'rgb(r g b)' 
              - 'rgba(r,g,b,a)' or 'rgba(r g b a)'
              - Case insensitive, flexible whitespace
  
  Returns:
    - For rgb(): [r g b] where r, g, b are integers 0-255
    - For rgba(): [r g b a] where r, g, b are integers 0-255 and a is float 0.0-1.0
    - nil if the string doesn't match expected format or values are out of range
  
  Examples:
    (parse-css-color-components \"rgb(255,0,0)\") => [255 0 0]
    (parse-css-color-components \"RGB( 255 , 0 , 0 )\") => [255 0 0]
    (parse-css-color-components \"rgba(255,0,0,0.5)\") => [255 0 0 0.5]
    (parse-css-color-components \"rgb(256,0,0)\") => nil (out of range)"
  [css-str]
  (let [normalized (normalize-css-color-string css-str)
        rgba? (str/starts-with? normalized "rgba")
        [_ & matches] (if rgba?
                        (re-find #"rgba\((\d+)(?:,| )(\d+)(?:,| )(\d+)(?:,| )((?:0(?:\.\d+)?|1(?:\.0+)?|\.\d+))\)" normalized)
                        (re-find #"rgb\((\d+)(?:,| )(\d+)(?:,| )(\d+)\)" normalized))]
    (when (and matches
               (->> matches
                    (take 3)
                    (every? #(<= 0 (parse-int %) 255)))
               (or (not rgba?)
                   (<= 0 (parse-double (last matches)) 1)))
      (cond-> [(parse-int (nth matches 0))
               (parse-int (nth matches 1))
               (parse-int (nth matches 2))]
        rgba? (conj (parse-double (nth matches 3)))))))

(defn parse-css-rgb
  "Parse CSS rgb() string to RGB vector"
  [css-str]
  {:pre [(valid-css-rgb? css-str)]}
  (if-let [rgb (parse-css-color-components css-str)]
    rgb
    (throw (ex-info "Invalid CSS RGB format" {:input css-str}))))

(defn parse-css-rgba
  "Parse CSS rgba() string to RGBA vector"
  [css-str]
  {:pre [(valid-css-rgba? css-str)]}
  (if-let [rgba (parse-css-color-components css-str)]
    rgba
    (throw (ex-info "Invalid CSS RGBA format" {:input css-str}))))

;; =============================================================================
;; Color Record Type
;; =============================================================================

(defrecord Color [r g b a]
  Object
  (toString [_]
    (if (= a 1.0)
      (rgb->hex [r g b])
      (str "rgba(" r "," g "," b "," a ")"))))

(defn color?
  "Check if value is a Color record"
  [x]
  (instance? Color x))

(defn ->color
  "Create a Color record from RGB values with optional alpha"
  ([r g b] (->color r g b 1.0))
  ([r g b a]
   {:pre [(and (>= r 0) (<= r 255))
          (and (>= g 0) (<= g 255))
          (and (>= b 0) (<= b 255))
          (and (>= a 0) (<= a 1))]}
   (->Color (int r) (int g) (int b) (double a))))

(defn color-from-hex
  "Create a Color record from hex string"
  [hex-str]
  {:pre [(valid-hex? hex-str)]}
  (let [[r g b] (hex->rgb hex-str)]
    (->color r g b)))

(defn color-from-rgb
  "Create a Color record from RGB vector"
  [rgb-vec]
  {:pre [(valid-rgb? rgb-vec)]}
  (let [[r g b] rgb-vec]
    (->color r g b)))

(defn color-from-rgba
  "Create a Color record from RGBA vector"
  [rgba-vec]
  {:pre [(valid-rgba? rgba-vec)]}
  (let [[r g b a] rgba-vec]
    (->color r g b a)))

(defn color-from-hsl
  "Create a Color record from HSL vector"
  [hsl-vec]
  {:pre [(valid-hsl? hsl-vec)]}
  (let [rgb (hsl->rgb hsl-vec)]
    (color-from-rgb rgb)))

(defn color-from-hsv
  "Create a Color record from HSV vector"
  [hsv-vec]
  {:pre [(valid-hsv? hsv-vec)]}
  (let [rgb (hsv->rgb hsv-vec)]
    (color-from-rgb rgb)))

;; Helper function to normalize color inputs
(defn- normalize-color-input
  "Convert various color inputs to Color record"
  [color-input]
  (cond
    (color? color-input) color-input
    (string? color-input) (cond
                            (valid-hex? color-input) (color-from-hex color-input)
                            (valid-css-rgb? color-input) (color-from-rgb (parse-css-rgb color-input))
                            (valid-css-rgba? color-input) (color-from-rgba (parse-css-rgba color-input))
                            :else (throw (ex-info "Invalid color string format" {:input color-input})))
    (vector? color-input) (if (= 4 (count color-input))
                            (color-from-rgba color-input)
                            (color-from-rgb color-input))
    :else (throw (ex-info "Invalid color input" {:input color-input
                                                 :reason :unknown-format}))))

;; Convenient factory function
(defn color
  "Create a Color record from various input formats.
   
   Usage:
   (color 255 0 0)           ; RGB values
   (color 255 0 0 0.8)       ; RGBA values  
   (color \"#ff0000\")         ; Hex string
   (color [255 0 0])         ; RGB vector
   (color [255 0 0 0.8])     ; RGBA vector
   (color (color 255 0 0))   ; From another Color record"
  ([input] (normalize-color-input input))
  ([r g b] (->color r g b))
  ([r g b a] (->color r g b a)))

(defn ->rgb
  "Convert a color to RGB vector"
  [color]
  (let [color-record (normalize-color-input color)]
    ((juxt :r :g :b) color-record)))

(defn ->rgba
  "Convert Color to RGBA vector"
  [color]
  (let [color-record (normalize-color-input color)]
    [(:r color-record) (:g color-record) (:b color-record) (:a color-record)]))

(defn ->hex
  "Convert Color to hex string"
  [color]
  (rgb->hex (->rgb color)))

(defn ->hsl
  "Convert Color to HSL vector"
  [color]
  (rgb->hsl (->rgb color)))

(defn ->hsv
  "Convert Color to HSV vector"
  [color]
  (rgb->hsv (->rgb color)))

;; =============================================================================
;; Color Manipulation
;; =============================================================================

(defn lighten
  "Lighten a color by a percentage (0-1)"
  [color amount]
  (let [color-record (normalize-color-input color)
        hsl (->hsl color-record)
        [h s l] hsl
        new-l (clamp (+ l (* amount 100)) 0 100)
        new-hsl [h s new-l]]
    (cond
      (color? color) (color-from-hsl new-hsl)
      (string? color) (hsl->hex new-hsl)
      (vector? color) (hsl->rgb new-hsl))))

(defn darken
  "Darken a color by a percentage (0-1)"
  [color amount]
  (let [color-record (normalize-color-input color)
        hsl (->hsl color-record)
        [h s l] hsl
        new-l (clamp (- l (* amount 100)) 0 100)
        new-hsl [h s new-l]]
    (cond
      (color? color) (color-from-hsl new-hsl)
      (string? color) (hsl->hex new-hsl)
      (vector? color) (hsl->rgb new-hsl))))

(defn saturate
  "Increase saturation of a color by a percentage (0-1)"
  [color amount]
  (let [color-record (normalize-color-input color)
        [h s l] (->hsl color-record)
        new-s (clamp (+ s (* amount 100)) 0 100)]
    (if (string? color)
      (hsl->hex [h new-s l])
      (hsl->rgb [h new-s l]))))

(defn desaturate
  "Decrease saturation of a color by a percentage (0-1)"
  [color amount]
  (let [color-record (normalize-color-input color)
        [h s l] (->hsl color-record)
        new-s (clamp (- s (* amount 100)) 0 100)]
    (if (string? color)
      (hsl->hex [h new-s l])
      (hsl->rgb [h new-s l]))))

(defn adjust-hue
  "Adjust the hue of a color by degrees"
  [color degrees]
  (let [color-record (normalize-color-input color)
        [h s l] (->hsl color-record)
        new-h (mod (+ h degrees) 360)
        new-hsl [new-h s l]]
    (cond
      (color? color) (color-from-hsl new-hsl)
      (string? color) (hsl->hex new-hsl)
      (vector? color) (hsl->rgb new-hsl))))

(defn invert
  "Invert a color"
  [color]
  (let [color-record (normalize-color-input color)
        [r g b] (->rgb color-record)
        inverted [(- 255 r) (- 255 g) (- 255 b)]]
    (cond
      (color? color) (color-from-rgb inverted)
      (string? color) (rgb->hex inverted)
      (vector? color) inverted)))

(defn grayscale
  "Convert a color to grayscale"
  [color]
  (let [color-record (normalize-color-input color)
        [r g b] (->rgb color-record)
        gray (round-int (+ (* 0.299 r) (* 0.587 g) (* 0.114 b)))
        gray-rgb [gray gray gray]]
    (if (string? color)
      (rgb->hex gray-rgb)
      gray-rgb)))

;; =============================================================================
;; Color Mixing and Blending
;; =============================================================================

(defn mix
  "Mix two colors with optional ratio (0-1, default 0.5)"
  [color1 color2 & [ratio]]
  (let [ratio (or ratio 0.5)
        [r1 g1 b1] (->rgb (normalize-color-input color1))
        [r2 g2 b2] (->rgb (normalize-color-input color2))
        mixed [(round-int (+ r1 (* (- r2 r1) ratio)))
               (round-int (+ g1 (* (- g2 g1) ratio)))
               (round-int (+ b1 (* (- b2 b1) ratio)))]]
    (cond
      (color? color1) (color-from-rgb mixed)
      (string? color1) (rgb->hex mixed)
      (vector? color1) mixed)))

;; =============================================================================
;; Contrast and Accessibility
;; =============================================================================

(defn luminance
  "Calculate relative luminance of a color"
  [color]
  (let [rgb (->rgb (normalize-color-input color))
        [r g b] (map #(let [val (/ % 255)]
                        (if (<= val 0.03928)
                          (/ val 12.92)
                          (Math/pow (/ (+ val 0.055) 1.055) 2.4)))
                     rgb)]
    (+ (* 0.2126 r) (* 0.7152 g) (* 0.0722 b))))

(defn contrast-ratio
  "Calculate contrast ratio between two colors"
  [color1 color2]
  (let [lum1 (luminance color1)
        lum2 (luminance color2)
        lighter (max lum1 lum2)
        darker (min lum1 lum2)]
    (round (/ (+ lighter 0.05) (+ darker 0.05)) 2)))

(defn accessible?
  "Check if two colors meet WCAG AA accessibility standards"
  [color1 color2 & [level]]
  (let [ratio (contrast-ratio color1 color2)
        threshold (case level
                    :aaa 7
                    :aa-large 3
                    4.5)]  ; AA normal
    (>= ratio threshold)))

(defn find-accessible-color
  "Find an accessible color by adjusting lightness"
  [background target-color & [level]]
  (let [level (or level :aa)
        threshold (case level
                    :aaa 7
                    :aa-large 3
                    4.5)  ; AA normal
        target-color-hsl (->hsl target-color)
        try-color (fn [lightness]
                    (let [[h s _] target-color-hsl
                          new-color (if (string? target-color)
                                      (hsl->hex [h s lightness])
                                      (hsl->rgb [h s lightness]))]
                      (when (>= (contrast-ratio background new-color) threshold)
                        new-color)))]
    (or (try-color 0)    ; Try black first
        (try-color 100)  ; Then white
        (some try-color (range 0 101 5))))) ; Then try other lightness values

(defn get-contrast-text
  "Get the best contrast text color (black or white) for a given background color.
   Uses luminance threshold of 0.35 following accessibility best practices, with
   fallback to ensure WCAG AA compliance:
   - Light backgrounds (luminance > 0.35) get black text
   - Dark backgrounds (luminance ≤ 0.35) get white text
   - If the primary choice doesn't meet WCAG AA standards, tries the alternative
   - If neither meets standards, returns the one with better contrast ratio
   Always returns a hex color string for consistency."
  [background-color]
  (let [bg-luminance (luminance background-color)
        black-color "#000000"
        white-color "#ffffff"

        ;; Primary choice based on luminance threshold
        primary-color (if (> bg-luminance 0.35) black-color white-color)
        alternative-color (if (> bg-luminance 0.35) white-color black-color)

        ;; Check accessibility (use AA-large threshold 3.0 for text contrast choice)
        primary-accessible? (>= (contrast-ratio background-color primary-color) 3.0)
        alternative-accessible? (>= (contrast-ratio background-color alternative-color) 3.0)

        ;; Calculate contrast ratios for both options
        primary-ratio (contrast-ratio background-color primary-color)
        alternative-ratio (contrast-ratio background-color alternative-color)]

    (cond
      ;; If primary choice is accessible, use it
      primary-accessible? primary-color

      ;; If primary fails but alternative is accessible, use alternative
      alternative-accessible? alternative-color

      ;; If neither is accessible, choose the one with better contrast ratio
      :else (if (> primary-ratio alternative-ratio)
              primary-color
              alternative-color))))

;; =============================================================================
;; Color Harmony and Palettes
;; =============================================================================

(defn complementary
  "Get the complementary color"
  [color]
  (adjust-hue color 180))

(defn triadic
  "Get triadic colors (two colors that form a triangle on the color wheel)"
  [color]
  [(adjust-hue color 120)
   (adjust-hue color 240)])

(defn tetradic
  "Get tetradic colors (three colors that form a square on the color wheel)"
  [color]
  [(adjust-hue color 90)
   (adjust-hue color 180)
   (adjust-hue color 270)])

(defn analogous
  "Generate analogous colors"
  [color & [count angle]]
  (let [count (or count 3)
        angle (or angle 30)]
    (map #(adjust-hue color (* % angle)) (range 1 (inc count)))))

(defn monochromatic
  "Generate monochromatic palette by varying lightness and saturation"
  [color & [count]]
  (let [count (or count 5)
        [h] (->hsl color)
        variations (for [i (range count)
                         :let [factor (/ i (dec count))
                               new-l (+ 20 (* 60 factor))
                               new-s (+ 50 (* 50 (- 1 (* 0.5 factor))))]]
                     (if (string? color)
                       (hsl->hex [h new-s new-l])
                       (hsl->rgb [h new-s new-l])))]
    variations))

(defn split-complementary
  "Get split-complementary colors"
  [color & [angle]]
  (let [angle (or angle 30)
        [h] (->hsl color)
        comp-hue (+ h 180)]
    [(adjust-hue color (- comp-hue angle))
     (adjust-hue color (+ comp-hue angle))]))

;; =============================================================================
;; Advanced Palette Generation
;; =============================================================================

(defn random-color
  "Generate a random color"
  []
  (rgb->hex [(rand-int 256) (rand-int 256) (rand-int 256)]))

(defn generate-palette
  "Generate a color palette using various strategies"
  [strategy base-color & [options]]
  (case strategy
    :monochromatic (monochromatic base-color (:count options 5))
    :analogous (analogous base-color (:count options 3) (:angle options 30))
    :triadic (triadic base-color)
    :tetradic (tetradic base-color)
    :complementary [(complementary base-color)]
    :split-complementary (split-complementary base-color (:angle options 30))
    :random (repeatedly (:count options 5) random-color)
    [base-color]))

;; =============================================================================
;; Color Distance and Similarity
;; =============================================================================

(defn color-distance
  "Calculate Euclidean distance between two colors in RGB space"
  [color1 color2]
  (let [[r1 g1 b1] (->rgb color1)
        [r2 g2 b2] (->rgb color2)]
    (Math/sqrt (+ (Math/pow (- r2 r1) 2)
                  (Math/pow (- g2 g1) 2)
                  (Math/pow (- b2 b1) 2)))))

(defn similar?
  "Check if two colors are similar within a threshold"
  [color1 color2 & [threshold]]
  (let [threshold (or threshold 50)]
    (<= (color-distance color1 color2) threshold)))

;; =============================================================================
;; Utility Functions for Color Names
;; =============================================================================

(defn name->hex
  "Convert color name to hex"
  [color-name]
  (get color-names (str/lower-case color-name)))

(defn hex->name
  "Find closest color name for a hex color (approximate)"
  [hex]
  (let [target-rgb (hex->rgb hex)]
    (->> color-names
         (map (fn [[name hex-val]]
                [name (color-distance target-rgb (hex->rgb hex-val))]))
         (sort-by second)
         ffirst)))

(defn ->name
  "Find closest color name for a color"
  [color]
  (hex->name (->hex color)))

;; =============================================================================
;; Color Temperature and Warmth
;; =============================================================================

(defn warm?
  "Check if a color is warm (red/orange/yellow family)"
  [color]
  (let [[h] (->hsl color)]
    (or (<= 0 h 60) (<= 300 h 360))))

(defn cool?
  "Check if a color is cool (blue/green/purple family)"
  [color]
  (not (warm? color)))

(defn temperature
  "Get color temperature as a keyword"
  [color]
  (if (warm? color) :warm :cool))

;; =============================================================================
;; Color Analysis
;; =============================================================================

(defn brightness
  "Calculate perceived brightness of a color (0-255)"
  [color]
  (let [[r g b] (->rgb color)]
    (round-int (+ (* 0.299 r) (* 0.587 g) (* 0.114 b)))))

(defn dark?
  "Check if a color is dark"
  [color]
  (< (brightness color) 128))

(defn light?
  "Check if a color is light"
  [color]
  (not (dark? color)))

(defn vibrant?
  "Check if a color is vibrant (high saturation)"
  [color & [threshold]]
  (let [threshold (or threshold 60)
        [_ s] (->hsl color)]
    (> s threshold)))

(defn muted?
  "Check if a color is muted (low saturation)"
  [color & [threshold]]
  (not (vibrant? color threshold)))
