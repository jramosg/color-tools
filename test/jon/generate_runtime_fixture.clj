;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.generate-runtime-fixture
  "Regenerate committed expected values on the JVM, never during tests."
  (:require [clojure.pprint :as pprint]
            [jon.color-tools :as ct]
            [jon.runtime-fixture :as fixture]))

(def ^:private seed 141113)

(defn- input [op & args]
  {:op op :args (vec args)})

(defn- colors []
  (let [rng (java.util.Random. seed)]
    (into [[0 0 0] [255 255 255] [127 127 127] [128 128 128]
           [0 24 1] [0 48 2] [0 39 72] [254 255 0] [254 0 255]
           [199 122 222] [153 136 238] [52 152 219] [6 244 74]
           [111 29 187] [114 193 216] [19 171 157]]
          (repeatedly 48
                      (fn [] (vec (repeatedly 3 #(.nextInt rng 256))))))))

(defn- color-cases [rgb other-rgb]
  (let [hex (ct/rgb->hex rgb)
        other (ct/rgb->hex other-rgb)]
    (concat
     (for [op [:rgb->hex :rgb->hsl :rgb->hsv :rgb->lab :rgb->oklab
               :rgb->oklch]]
       (input op rgb))
     (for [op [:rgb->hsl :rgb->hsv]]
       (input op rgb {:full-precision? true}))
     (for [op [:lighten :darken :saturate :desaturate]]
       (input op hex 0.1))
     [(input :adjust-hue hex 65)
      (input :invert hex)
      (input :grayscale hex)]
     (for [type [:complementary :triadic :tetradic :analogous
                 :split-complementary :monochromatic]]
       (input :generate-palette type hex {:count 5 :angle 25}))
     (for [op [:tints :shades :tones]] (input op hex 6))
     (for [space [:rgb :hsl :hsv]]
       (input :gradient [hex other] 7 space))
     (for [space [:rgb :hsl :hsv]]
       (input :interpolate [hex other] (/ 2.0 3) space))
     (for [op [:mix :blend-multiply :blend-screen :blend-overlay
               :contrast-ratio]]
       (input op hex other))
     (for [level [:aa :aa-large :aaa]]
       (input :accessible? hex other level))
     [(input :find-accessible-color other hex :aa)
      (input :get-contrast-text hex)
      (input :luminance rgb)
      (input :warm? rgb)
      (input :vibrant? rgb)
      (input :alpha-blend (conj rgb 0.4) (conj other-rgb 0.5))])))

(defn- regression-cases []
  [(input :rgb->hsl [0 24 1])
   (input :rgb->hsv [0 24 1])
   (input :hsl->rgb [0 60 75])
   (input :hsl->rgb [0 75 40])
   (input :hsv->rgb [0 22 90])
   (input :hsv->rgb [0 28 90])
   (input :tints "#06f44a" 6)
   (input :tones "#6f1dbb" 6)
   (input :gradient ["#72c1d8" "#13ab9d"] 4 :hsv)
   (input :accessible? "#006ffb" "#ffffff" :aa)
   (input :luminance [10.1 10.1 10.1])])

(defn -main [& _]
  (let [rgb (colors)
        pairs (map vector rgb (concat (rest rgb) [(first rgb)]))
        themes (for [color (take 6 rgb)
                     mode [:light :dark]
                     level [:aa :aa-large :aaa]]
                 (input :accessible-theme (ct/rgb->hex color)
                        {:mode mode :level level}))
        cases (concat (regression-cases)
                      (mapcat #(apply color-cases %) pairs)
                      themes)
        data {:seed seed
              :cases (mapv #(assoc % :expected (fixture/evaluate %)) cases)}]
    (spit "test/fixtures/runtime_parity.edn"
          (binding [pprint/*print-right-margin* 78]
            (with-out-str (pprint/pprint data))))
    (println "Wrote" (count (:cases data)) "runtime parity cases")))
