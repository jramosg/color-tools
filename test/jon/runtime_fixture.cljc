;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.runtime-fixture
  "Operations shared by fixture generation and the runtime parity test."
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

(defn evaluate [{:keys [op args]}]
  (plain (apply (operations op) args)))
