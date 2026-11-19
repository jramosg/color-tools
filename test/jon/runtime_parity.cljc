;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.runtime-parity
  "Shared evaluation and comparison for JVM/Node differential checks."
  (:require [jon.color-tools :as ct]))

(def ^:private operations
  {:rgb->hex ct/rgb->hex
   :rgb->hsl ct/rgb->hsl
   :rgb->hsv ct/rgb->hsv
   :hsl->rgb ct/hsl->rgb
   :hsv->rgb ct/hsv->rgb
   :rgb->lab ct/rgb->lab
   :rgb->oklab ct/rgb->oklab
   :rgb->oklch ct/rgb->oklch
   :lighten ct/lighten
   :darken ct/darken
   :saturate ct/saturate
   :desaturate ct/desaturate
   :adjust-hue ct/adjust-hue
   :invert ct/invert
   :grayscale ct/grayscale
   :generate-palette ct/generate-palette
   :tints ct/tints
   :shades ct/shades
   :tones ct/tones
   :gradient ct/gradient
   :interpolate ct/interpolate
   :mix ct/mix
   :blend-multiply ct/blend-multiply
   :blend-screen ct/blend-screen
   :blend-overlay ct/blend-overlay
   :alpha-blend ct/alpha-blend
   :accessible-theme ct/accessible-theme
   :contrast-ratio ct/contrast-ratio
   :accessible? ct/accessible?
   :find-accessible-color ct/find-accessible-color
   :get-contrast-text ct/get-contrast-text
   :luminance ct/luminance
   :warm? ct/warm?
   :vibrant? ct/vibrant?})

(defn- plain [value]
  (cond
    (map? value) (update-vals (into {} value) plain)
    (sequential? value) (mapv plain value)
    :else value))

(defn evaluate
  "Execute a case on the current runtime and realize its result as data."
  [{:keys [op args] :as case}]
  (if-let [f (operations op)]
    (plain (apply f args))
    (throw (ex-info "Unknown runtime parity operation" {:case case}))))

(defn- same-result? [jvm node]
  (cond
    (number? jvm)
    (and (number? node)
         (not-any? #(or (NaN? %) (infinite? %)) [jvm node])
         (<= (Math/abs (double (- jvm node)))
             (* 1.0E-9 (max 1.0 (Math/abs (double jvm))
                            (Math/abs (double node))))))

    (map? jvm)
    (and (map? node)
         (= (set (keys jvm)) (set (keys node)))
         (every? (fn [[k v]] (same-result? v (get node k))) jvm))

    (sequential? jvm)
    (and (sequential? node)
         (= (count jvm) (count node))
         (every? true? (map same-result? jvm node)))

    :else (= jvm node)))

(defn differences
  "Compare every case, rejecting empty or incomplete runs. A mismatch
   includes its input and both runtime results. Numbers allow 1e-9
   relative/absolute error; strings and booleans compare exactly."
  [cases jvm node]
  (when-not (seq cases)
    (throw (ex-info "Runtime parity requires nonempty cases" {})))
  (when-not (= (count cases) (count jvm) (count node))
    (throw (ex-info "Runtime parity result counts differ"
                    {:cases (count cases)
                     :jvm (count jvm) :node (count node)})))
  (into []
        (keep-indexed
         (fn [index [case jvm node]]
           (when-not (same-result? jvm node)
             (assoc case :index index :jvm jvm :node node))))
        (map vector cases jvm node)))
