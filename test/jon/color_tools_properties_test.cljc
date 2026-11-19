;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.color-tools-properties-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clojure.test.check :as check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [jon.color-tools :as sut]))

(def ^:private seed 111)
(def ^:private trials 400)
(def ^:private full-precision {:full-precision? true})

(defn- check-property! [generators assertion-fn]
  (let [{:keys [pass? seed fail shrunk result] :as report}
        (check/quick-check trials (prop/for-all* generators assertion-fn)
                           :seed seed)
        failure (:result shrunk result)
        failure (or (ex-cause failure) failure)]
    (is pass?
        (pr-str (cond-> {:seed seed
                         :trial (:num-tests report)
                         :input fail
                         :smallest (:smallest shrunk)}
                  (ex-message failure) (assoc :message (ex-message failure))
                  (ex-data failure) (assoc :details (ex-data failure)))))))

(defn- check! [passed? details]
  (when-not passed?
    (throw (ex-info "Color property failed" details)))
  true)

(def ^:private byte-channel (gen/choose 0 255))
(def ^:private fractional-channel
  (gen/frequency
   [[4 (gen/fmap #(/ % 1000.0) (gen/choose 1 254999))]
    [1 (gen/elements [0.0 0.000001 0.5 10.31475 10.314751
                      127.5 254.5 254.999999 255.0])]]))

(defn- rgb-gen [channel]
  (gen/frequency
   [[6 (gen/tuple channel channel channel)]
    [2 (gen/fmap #(vector % % %) channel)]
    [2 (gen/tuple (gen/elements [0 255]) channel
                  (gen/elements [0 255]))]]))

(def ^:private byte-rgb (rgb-gen byte-channel))
(def ^:private fractional-rgb (rgb-gen fractional-channel))
(def ^:private rgb-input (gen/one-of [byte-rgb fractional-rgb]))
(def ^:private unit-interval
  (gen/frequency
   [[4 (gen/fmap #(/ % 1000) (gen/choose 0 1000))]
    [1 (gen/elements [0 1])]]))

(defn- plain-number? [x]
  (and (number? x)
       (not (NaN? x))
       (not (infinite? x))
       #?(:clj (not (ratio? x)) :cljs true)))

(defn- valid-coords? [validator coords]
  (and (validator coords) (every? plain-number? coords)))

(defn- valid-result? [kind result]
  (case kind
    :hex (sut/valid-hex? result)
    :rgb (valid-coords? sut/valid-rgb? result)
    :rgba (valid-coords? sut/valid-rgba? result)
    :hsl (valid-coords? sut/valid-hsl? result)
    :hsv (valid-coords? sut/valid-hsv? result)
    :color (and (sut/color? result)
                (valid-coords? sut/valid-rgba? (sut/->rgba result)))
    :css-rgba (and (sut/valid-css-rgba? result)
                   (valid-result? :rgba (sut/parse-css-rgba result)))))

(defn- check-results! [kind results]
  (doseq [[operation result] results]
    (check! (valid-result? kind result)
            {:operation operation :expected kind :result result})))

(defn- near-rgb? [expected actual tolerance]
  (and (vector? actual)
       (= 3 (count actual))
       (every? plain-number? actual)
       (every? #(<= (Math/abs (double %)) tolerance)
               (map - expected actual))))

(defn- lab->rgb-reference [[l a b]]
  ;; Test-only D65 Lab inverse. Use the published rounded XYZ-to-sRGB
  ;; coefficients and reference white, independently of the forward matrix.
  (let [fy (/ (+ l 16.0) 116.0)
        fx (+ fy (/ a 500.0))
        fz (- fy (/ b 200.0))
        xyz-component #(if (> % (/ 6.0 29.0))
                         (* % % %)
                         (/ (- (* 116.0 %) 16.0) (/ 24389.0 27.0)))
        x (* 0.95047 (xyz-component fx))
        y (xyz-component fy)
        z (* 1.08883 (xyz-component fz))
        linear [(+ (* 3.2404542 x) (* -1.5371385 y) (* -0.4985314 z))
                (+ (* -0.9692660 x) (* 1.8760108 y) (* 0.0415560 z))
                (+ (* 0.0556434 x) (* -0.2040259 y) (* 1.0572252 z))]
        encode #(if (<= % 0.0031308)
                  (* 12.92 %)
                  (- (* 1.055 (Math/pow % (/ 1.0 2.4))) 0.055))]
    (mapv #(* 255 (encode %)) linear)))

(deftest rgb-round-trips
  (doseq [[channels generator hex-tolerance] [[:byte byte-rgb 0]
                                              [:fractional fractional-rgb 0.5]]]
    (testing (name channels)
      (check-property!
       [generator]
       (fn [rgb]
         ;; Tolerances are absolute errors per channel on the 0–255 scale.
         ;; The rounded HSL and HSV defaults can lose up to half a degree,
         ;; half a percentage point per coordinate, and half an RGB unit.
         ;; Lab uses rounded reference coefficients. OKLCh can set a
         ;; near-neutral hue to zero. Hex rounds to the nearest byte.
         (doseq [[space converted tolerance]
                 [[:hsl (sut/hsl->rgb (sut/rgb->hsl rgb full-precision)
                                      full-precision) 1e-9]
                  [:hsv (sut/hsv->rgb (sut/rgb->hsv rgb full-precision)
                                      full-precision) 1e-9]
                  [:hsl-rounded (sut/hsl->rgb (sut/rgb->hsl rgb)) 6]
                  [:hsv-rounded (sut/hsv->rgb (sut/rgb->hsv rgb)) 6]
                  [:lab (lab->rgb-reference (sut/rgb->lab rgb)) 1e-3]
                  [:oklab (sut/oklab->rgb (sut/rgb->oklab rgb)) 1e-4]
                  [:oklch (sut/oklch->rgb (sut/rgb->oklch rgb)) 0.005]
                  [:hex (sut/hex->rgb (sut/rgb->hex rgb)) hex-tolerance]]]
           (check! (near-rgb? rgb converted tolerance)
                   {:space space :rgb rgb :round-trip converted
                    :tolerance tolerance}))
         true)))))

(deftest adjustments-and-mix-identities
  (check-property!
   [rgb-input rgb-input]
   (fn [rgb other]
     (doseq [[color target] [[rgb other]
                             [(sut/rgb->hex rgb) (sut/rgb->hex other)]
                             [(sut/color rgb full-precision)
                              (sut/color other full-precision)]]
             :let [source-rgb (sut/->rgb color)
                   target-rgb (sut/->rgb target)
                   mixed-rgb (if (vector? color) identity
                                 #(mapv (fn [x] (Math/round (double x))) %))]]
       ;; HSL adjustments still return whole channels. Mix and its
       ;; variants keep fractions for RGB vector results.
       (doseq [[operation expected actual]
               [[:lighten source-rgb (sut/lighten color 0)]
                [:darken source-rgb (sut/darken color 0)]
                [:saturate source-rgb (sut/saturate color 0)]
                [:desaturate source-rgb (sut/desaturate color 0)]
                [:adjust-hue-zero source-rgb (sut/adjust-hue color 0)]
                [:adjust-hue-full-turn source-rgb (sut/adjust-hue color 360)]
                [:mix-zero (mixed-rgb source-rgb) (sut/mix color target 0)]
                [:mix-one (mixed-rgb target-rgb) (sut/mix color target 1)]
                [:tint (mixed-rgb source-rgb) (sut/tint color 0)]
                [:shade (mixed-rgb source-rgb) (sut/shade color 0)]
                [:tone (mixed-rgb source-rgb) (sut/tone color 0)]]
               :let [hsl? (#{:lighten :darken :saturate :desaturate
                             :adjust-hue-zero :adjust-hue-full-turn}
                           operation)]]
         (check! (near-rgb? expected (sut/->rgb actual) (if hsl? 0.5 0))
                 {:operation operation :input color :target target
                  :expected expected :result actual})))
     true)))

(defn- check-format! [kind color other amount degrees steps]
  (check-results!
   kind
   {:lighten (sut/lighten color amount)
    :darken (sut/darken color amount)
    :adjust-hue (sut/adjust-hue color degrees)
    :invert (sut/invert color)
    :complementary (sut/complementary color)
    :mix (sut/mix color other amount)
    :blend-multiply (sut/blend-multiply color other)
    :blend-screen (sut/blend-screen color other)
    :blend-overlay (sut/blend-overlay color other)
    :tint (sut/tint color amount)
    :shade (sut/shade color amount)
    :tone (sut/tone color amount)})
  (let [hex-or-rgb (if (= kind :hex) :hex :rgb)
        target (sut/find-accessible-color other color :aaa)]
    (check-results! hex-or-rgb
                    {:saturate (sut/saturate color amount)
                     :desaturate (sut/desaturate color amount)
                     :grayscale (sut/grayscale color)})
    (when (some? target)
      (check-results! hex-or-rgb {:find-accessible-color target}))
    (doseq [result (sut/generate-palette :monochromatic color {:count steps})]
      (check-results! hex-or-rgb {:monochromatic result})))
  (doseq [[operation palette] {:triadic (sut/triadic color)
                               :tetradic (sut/tetradic color)
                               :analogous (sut/analogous color steps degrees)
                               :split-complementary
                               (sut/split-complementary color degrees)
                               :tints (sut/tints color steps)
                               :shades (sut/shades color steps)
                               :tones (sut/tones color steps)}
          result palette]
    (check-results! kind {operation result}))
  (doseq [space [:rgb :hsl :hsv]]
    (check-results! kind {:interpolate
                          (sut/interpolate [color other] amount space)})
    (doseq [result (sut/gradient [color other] steps space)]
      (check-results! kind {:gradient result})))
  (check-results! :color {:color (sut/color color)})
  (check-results! :rgb {:->rgb (sut/->rgb color)})
  (check-results! :rgba {:->rgba (sut/->rgba color)})
  (check-results! :hex {:->hex (sut/->hex color)
                        :get-contrast-text (sut/get-contrast-text color)})
  (check-results! :css-rgba {:->css (sut/->css color)})
  (check-results! :hsl {:->hsl (sut/->hsl color)})
  (check-results! :hsv {:->hsv (sut/->hsv color)}))

(deftest manipulation-results-have-valid-ranges-and-formats
  (check-property!
   [rgb-input rgb-input unit-interval (gen/choose -720 720) (gen/choose 1 8)]
   (fn [rgb other amount degrees steps]
     (doseq [[kind encode] [[:hex sut/rgb->hex]
                            [:rgb identity]
                            [:color #(sut/color % full-precision)]]]
       (check-format! kind (encode rgb) (encode other) amount degrees steps))
     (let [rational-rgb (mapv #(/ % 7) rgb)]
       (check-results! :rgb {:mix-rational-start (sut/mix rational-rgb other 0)
                             :mix-rational-end (sut/mix other rational-rgb 1)}))
     true)))

(deftest alpha-results-have-valid-ranges-and-formats
  (check-property!
   [rgb-input rgb-input unit-interval unit-interval]
   (fn [rgb other alpha opacity]
     (let [hex (sut/rgb->hex rgb)
           rgba (conj rgb (double alpha))
           overlay (conj other opacity)
           record (sut/color rgba full-precision)
           css (sut/->css rgba)]
       (check-results! :rgba
                       {:with-alpha (sut/with-alpha rgb alpha)
                        :alpha-blend (sut/alpha-blend rgba overlay)
                        :alpha-blend-same-rgb
                        (sut/alpha-blend rgba (conj rgb opacity))
                        :parse-css-rgba (sut/parse-css-rgba css)})
       (check-results! :color {:with-alpha (sut/with-alpha record alpha)
                               :alpha-blend (sut/alpha-blend rgb record)})
       (check-results! :hex {:alpha-blend (sut/alpha-blend hex overlay)
                             :rgba->hex (sut/rgba->hex rgba)})
       (check-results! (if (== alpha 1) :hex :css-rgba)
                       {:with-alpha (sut/with-alpha hex alpha)})
       (check-results! (if (or (== alpha 1) (== opacity 1)) :hex :css-rgba)
                       {:alpha-blend (sut/alpha-blend css overlay)}))
     true)))

(deftest conversion-results-have-valid-ranges-and-formats
  (check-property!
   [rgb-input (gen/choose 1000 40000)
    (gen/tuple (gen/choose 0 360) (gen/choose 0 100) (gen/choose 0 100))]
   (fn [rgb kelvin coords]
     (let [hex (sut/rgb->hex rgb)
           oklab (sut/rgb->oklab rgb)
           oklch (sut/rgb->oklch rgb)
           [l c h] oklch
           lab (sut/rgb->lab rgb)
           color (sut/color rgb full-precision)
           css (str "rgb(" (str/join "," rgb) ")")]
       (check-results! :rgb {:hex->rgb (sut/hex->rgb hex)
                             :clamp-rgb (sut/clamp-rgb (sut/oklab->rgb oklab))
                             :hsl->rgb (sut/hsl->rgb coords)
                             :hsv->rgb (sut/hsv->rgb coords)
                             :hsl->rgb-precise
                             (sut/hsl->rgb coords full-precision)
                             :hsv->rgb-precise
                             (sut/hsv->rgb coords full-precision)
                             :parse-css-rgb (sut/parse-css-rgb css)
                             :kelvin->rgb (sut/kelvin->rgb kelvin)})
       (check-results! :rgba {:hex->rgba (sut/hex->rgba hex)})
       (check-results! :hsl {:hex->hsl (sut/hex->hsl hex)
                             :rgb->hsl (sut/rgb->hsl rgb full-precision)})
       (check-results! :hsv {:rgb->hsv (sut/rgb->hsv rgb full-precision)})
       (check-results! :hex {:normalize-hex (sut/normalize-hex hex)
                             :hsl->hex (sut/hsl->hex coords)
                             :oklab->hex (sut/oklab->hex oklab)
                             :oklch->hex (sut/oklch->hex oklch)
                             :kelvin->hex (sut/kelvin->hex kelvin)})
       (check-results! :color
                       {:color-from-rgb (sut/color-from-rgb rgb)
                        :color-from-hex (sut/color-from-hex hex)
                        :color-from-rgba (sut/color-from-rgba (conj rgb 0.5))
                        :color-from-hsl (sut/color-from-hsl coords)
                        :color-from-hsv (sut/color-from-hsv coords)
                        :kelvin->color (sut/kelvin->color kelvin)
                        :oklab->rgb-space
                        (sut/->rgb-space (sut/->oklab-space color))})
       (check! (and (vector? lab) (= 3 (count lab))
                    (every? plain-number? lab) (<= 0 (first lab) 100))
               {:operation :rgb->lab :result lab})
       (check! (and (vector? oklab) (= 3 (count oklab))
                    (every? plain-number? oklab)
                    (<= 0 (first oklab) 1)
                    (sut/in-gamut? oklab))
               {:operation :rgb->oklab :result oklab})
       (check! (and (vector? oklch) (= 3 (count oklch))
                    (every? plain-number? oklch)
                    (<= 0 l 1) (<= 0 c) (<= 0 h 360))
               {:operation :rgb->oklch :result oklch}))
     true)))

(deftest color-comparisons-are-symmetric
  (check-property!
   [rgb-input rgb-input]
   (fn [left right]
     (doseq [[operation measure] [[:contrast-ratio sut/contrast-ratio]
                                  [:color-distance sut/color-distance]
                                  [:delta-e sut/delta-e]]
             :let [ab (measure left right)
                   ba (measure right left)]]
       (check! (and (plain-number? ab) (== ab ba)
                    (if (= operation :contrast-ratio) (<= 1 ab 21) (<= 0 ab)))
               {:operation operation :forward ab :reverse ba}))
     true)))

(def ^:private theme-pairs
  {:background :on-background :surface :on-surface :muted :on-muted
   :primary :on-primary :secondary :on-secondary :accent :on-accent})

(deftest accessible-theme-keeps-contrast-promises
  (check-property!
   [rgb-input]
   (fn [rgb]
     (doseq [mode [:light :dark]
             level [:aa-large :aa :aaa]
             :let [theme (sut/accessible-theme rgb {:mode mode :level level})]]
       (check-results! :hex
                       (merge (dissoc theme :mode :level :scale)
                              (:scale theme)))
       (doseq [[background foreground] theme-pairs
               :let [bg (theme background)
                     fg (theme foreground)]]
         (check! (or (sut/accessible? bg fg level)
                     (and (= level :aaa)
                          (#{:primary :secondary :accent} background)
                          (#{"#000000" "#ffffff"} fg)
                          (not (sut/accessible? bg "#000000" :aaa))
                          (not (sut/accessible? bg "#ffffff" :aaa))
                          (>= (sut/contrast-ratio bg fg)
                              (sut/contrast-ratio
                               bg (if (= fg "#000000") "#ffffff" "#000000")))))
                 {:mode mode :level level :pair [background foreground]
                  :colors [bg fg]}))
       (check! (sut/accessible? (:background theme) (:focus-ring theme)
                                :aa-large)
               {:mode mode :level level :pair [:background :focus-ring]
                :colors [(:background theme) (:focus-ring theme)]}))
     true)))
