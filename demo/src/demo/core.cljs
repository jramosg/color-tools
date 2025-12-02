(ns demo.core
  (:require
   [jon.color-tools :as color]
   [reagent.core :as r]
   [demo.sitetools :refer [start!]]))

(def default-color "#3498db")
;; State
(def state
  (r/atom
   {:format "hex"
    :color-picker default-color
    :blend {:base default-color :overlay "#ff0000"}
    :tst {:base default-color}
    :gradient {:start default-color :end "#0000ff" :steps 10 :space "rgb"}
    :alpha {:base "#ffffff" :overlay default-color :alpha-base 100 :alpha-overlay 50}
    :delta {:color1 "#ff0000" :color2 "#fe0101"}
    :kelvin {:temp 6500}
    :contrast {:bg default-color :fg "#ffffff"}
    :harmony {:base default-color}}))

;; Components

(defn icon [paths]
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :width "28" :height "28" :viewBox "0 0 24 24"
         :fill "none" :stroke "currentColor" :stroke-width "2"
         :stroke-linecap "round" :stroke-linejoin "round"}
   paths])

(def icons
  {:palette [icon [:g [:circle {:cx "13.5" :cy "6.5" :r ".5" :fill "currentColor"}]
                   [:circle {:cx "17.5" :cy "10.5" :r ".5" :fill "currentColor"}]
                   [:circle {:cx "8.5" :cy "7.5" :r ".5" :fill "currentColor"}]
                   [:circle {:cx "6.5" :cy "12.5" :r ".5" :fill "currentColor"}]
                   [:path {:d "M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.55 0 1-.45 1-1 0-2 2-2 2-2v-2c0-1.1.9-2 2-2h1c2.21 0 4-1.79 4-4 0-4.42-3.58-8-8-8z"}]]]
   :layers [icon [:g [:polygon {:points "12 2 2 7 12 12 22 7 12 2"}]
                  [:polyline {:points "2 17 12 22 22 17"}]
                  [:polyline {:points "2 12 12 17 22 12"}]]]
   :grid [icon [:g [:rect {:x "3" :y "3" :width "7" :height "7"}]
                [:rect {:x "14" :y "3" :width "7" :height "7"}]
                [:rect {:x "14" :y "14" :width "7" :height "7"}]
                [:rect {:x "3" :y "14" :width "7" :height "7"}]]]
   :activity [icon [:polyline {:points "22 12 18 12 15 21 9 3 6 12 2 12"}]]
   :droplet [icon [:path {:d "M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"}]]
   :eye [icon [:g [:path {:d "M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"}]
               [:circle {:cx "12" :cy "12" :r "3"}]]]
   :thermometer [icon [:path {:d "M14 14.76V3.5a2.5 2.5 0 0 0-5 0v11.26a4.5 4.5 0 1 0 5 0z"}]]
   :contrast [icon [:path {:d "M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z" :fill-rule "evenodd"}]]
   :aperture [icon [:g [:circle {:cx "12" :cy "12" :r "10"}]
                    [:line {:x1 "14.31" :y1 "8" :x2 "20.05" :y2 "17.94"}]
                    [:line {:x1 "9.69" :y1 "8" :x2 "21.17" :y2 "8"}]
                    [:line {:x1 "7.38" :y1 "12" :x2 "13.12" :y2 "2.06"}]
                    [:line {:x1 "9.69" :y1 "16" :x2 "3.95" :y2 "6.06"}]
                    [:line {:x1 "14.31" :y1 "16" :x2 "2.83" :y2 "16"}]
                    [:line {:x1 "16.62" :y1 "12" :x2 "10.88" :y2 "21.94"}]]]
   :target [icon [:g [:circle {:cx "12" :cy "12" :r "10"}]
                  [:circle {:cx "12" :cy "12" :r "6"}]
                  [:circle {:cx "12" :cy "12" :r "2"}]]]
   :clipboard [icon [:g [:path {:d "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"}]
                     [:rect {:x "8" :y "2" :width "8" :height "4" :rx "1" :ry "1"}]]]
   :box [icon [:g [:path {:d "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"}]
               [:polyline {:points "3.27 6.96 12 12.01 20.73 6.96"}]
               [:line {:x1 "12" :y1 "22.08" :x2 "12" :y2 "12"}]]]
   :check [icon [:polyline {:points "20 6 9 17 4 12"}]]
   :x [icon [:g [:line {:x1 "18" :y1 "6" :x2 "6" :y2 "18"}]
             [:line {:x1 "6" :y1 "6" :x2 "18" :y2 "18"}]]]
   :heart [icon [:path {:d "M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"}]]})

(defn copy-to-clipboard [text]
  (js/navigator.clipboard.writeText text))

(defn copyable-color [color-val]
  (let [format (:format @state)
        hex (if (string? color-val) color-val (color/->hex color-val))
        rgba (color/->rgba hex)
        display-text (if (= format "rgba")
                       (str "rgba(" (nth rgba 0) ", " (nth rgba 1) ", " (nth rgba 2) ", " (or (nth rgba 3) 1) ")")
                       (.toUpperCase hex))
        text-color (color/get-contrast-text hex)]
    [:button.copyable-color
     {:style {:background-color hex
              :color text-color}
      :on-click #(copy-to-clipboard display-text)
      :title "Click to copy"}
     [:span display-text]
     [:div.copyable-color__icon (:clipboard icons)]]))

(defn color-picker-section []
  (let [hex (:color-picker @state)
        format (:format @state)
        rgb (color/->rgb hex)
        hsl (color/->hsl hex)
        hsv (color/->hsv hex)
        text-color (color/get-contrast-text hex)]
    [:section.section
     [:h2 (:target icons) "Color Picker & Conversions"]
     [:div.card
      [:div.color-picker-grid
       [:div
        [:div.input-group
         [:label "Pick a color:"]
         [:input {:type "color"
                  :value hex
                  :on-change (fn [e]
                               (let [v (-> e .-target .-value)]
                                 (swap! state (fn [m]
                                                (-> m
                                                    (assoc :color-picker v)
                                                    (assoc-in [:blend :base] v)
                                                    (assoc-in [:tst :base] v)
                                                    (assoc-in [:gradient :start] v)
                                                    (assoc-in [:alpha :base] v)
                                                    (assoc-in [:harmony :base] v)
                                                    (assoc-in [:contrast :bg] v))))))}]]
        [:div.input-group
         [:label "Display Format:"]
         [:select {:value format
                   :on-change #(swap! state assoc :format (-> % .-target .-value))}
          [:option {:value "hex"} "HEX"]
          [:option {:value "rgba"} "RGBA"]]]]

       [:div.color-display {:style {:background-color hex :color text-color}}
        [copyable-color hex]]

       [:div.conversions
        [:div.conversion-item
         [:strong "HEX"]
         [:code (.toUpperCase hex)]]
        [:div.conversion-item
         [:strong "RGB"]
         [:code (str "rgb(" (first rgb) ", " (second rgb) ", " (last rgb) ")")]]
        [:div.conversion-item
         [:strong "HSL"]
         [:code (str "hsl(" (Math/round (:h hsl)) "°, " (Math/round (:s hsl)) "%, " (Math/round (:l hsl)) "%)")]]
        [:div.conversion-item
         [:strong "HSV"]
         [:code (str "hsv(" (Math/round (:h hsv)) "°, " (Math/round (:s hsv)) "%, " (Math/round (:v hsv)) "%)")]]]]]]))

(defn blending-section []
  (let [{:keys [base overlay]} (:blend @state)
        multiply (color/blend-multiply base overlay)
        screen (color/blend-screen base overlay)
        overlay-blend (color/blend-overlay base overlay)]
    [:section.section
     [:h2 (:layers icons) "Blending Modes"]
     [:div.card
      [:div.blend-controls
       [:div.input-group
        [:label "Base Color:"]
        [:input {:type "color" :value base
                 :on-change #(swap! state assoc-in [:blend :base] (-> % .-target .-value))}]]
       [:div.input-group
        [:label "Overlay Color:"]
        [:input {:type "color" :value overlay
                 :on-change #(swap! state assoc-in [:blend :overlay] (-> % .-target .-value))}]]]
      [:div.blend-results
       [:div.blend-item
        [:h4 "Multiply"]
        [:div.blend-swatch {:style {:background multiply}}]
        [copyable-color multiply]]
       [:div.blend-item
        [:h4 "Screen"]
        [:div.blend-swatch {:style {:background screen}}]
        [copyable-color screen]]
       [:div.blend-item
        [:h4 "Overlay"]
        [:div.blend-swatch {:style {:background overlay-blend}}]
        [copyable-color overlay-blend]]]]]))

(defn tst-section []
  (let [base (get-in @state [:tst :base])
        factors (range 0.1 1.0 0.1)
        tints (map #(color/tint base %) factors)
        shades (map #(color/shade base %) factors)
        tones (map #(color/tone base %) factors)]
    [:section.section
     [:h2 (:grid icons) "Tints, Shades & Tones"]
     [:div.card
      [:div.input-group
       [:label "Base Color:"]
       [:input {:type "color" :value base
                :on-change #(swap! state assoc-in [:tst :base] (-> % .-target .-value))}]]
      [:div.tst-grid
       [:div.tst-section
        [:h4 "Tints (Lighter)"]
        [:div.tst-swatches
         (doall (map-indexed (fn [i c]
                               ^{:key i}
                               [:div.tst-swatch {:style {:background c :color (color/get-contrast-text c)}}
                                (str (* (inc i) 10) "%")])
                             tints))]]
       [:div.tst-section
        [:h4 "Shades (Darker)"]
        [:div.tst-swatches
         (doall (map-indexed (fn [i c]
                               ^{:key i}
                               [:div.tst-swatch {:style {:background c :color (color/get-contrast-text c)}}
                                (str (* (inc i) 10) "%")])
                             shades))]]
       [:div.tst-section
        [:h4 "Tones (Less Saturated)"]
        [:div.tst-swatches
         (doall (map-indexed (fn [i c]
                               ^{:key i}
                               [:div.tst-swatch {:style {:background c :color (color/get-contrast-text c)}}
                                (str (* (inc i) 10) "%")])
                             tones))]]]]]))

(defn gradient-section []
  (let [{:keys [start end steps space]} (:gradient @state)
        _ (prn "space " (:gradient @state))
        gradient (color/gradient [start end] steps (keyword space))]
    [:section.section
     [:h2 (:activity icons) "Interpolation & Gradients"]
     [:div.card
      [:div.gradient-controls
       [:div.input-group
        [:label "Start Color:"]
        [:input {:type "color" :value start
                 :on-change #(swap! state assoc-in [:gradient :start] (-> % .-target .-value))}]]
       [:div.input-group
        [:label "End Color:"]
        [:input {:type "color" :value end
                 :on-change #(swap! state assoc-in [:gradient :end] (-> % .-target .-value))}]]
       [:div.input-group
        [:label "Steps:"]
        [:input {:type "range" :min 3 :max 20 :value steps
                 :on-change #(swap! state assoc-in [:gradient :steps] (js/parseInt (-> % .-target .-value)))}]
        [:span steps]]
       [:div.input-group
        [:label "Color Space:"]
        [:select {:value space
                  :on-change #(swap! state assoc-in [:gradient :space] (-> % .-target .-value))}
         [:option {:value "rgb"} "RGB"]
         [:option {:value "hsl"} "HSL"]
         [:option {:value "hsv"} "HSV"]]]]
      [:div.gradient-display
       (doall (map-indexed (fn [i c]
                             ^{:key i}
                             [:div.gradient-step {:style {:background c :color (color/get-contrast-text c)} :data-color c}])
                           gradient))]]]))

(defn alpha-section []
  (let [{:keys [base overlay alpha-base alpha-overlay]} (:alpha @state)
        a-base (/ alpha-base 100.0)
        a-overlay (/ alpha-overlay 100.0)
        result (color/alpha-blend (color/with-alpha base a-base) (color/with-alpha overlay a-overlay))
        [_r _g _b a] (color/->rgba result)
        res-hex (color/->hex result)
        res-alpha (or a 1.0)]
    [:section.section
     [:h2 (:droplet icons) "Alpha Blending & Compositing"]
     [:div.card
      [:div.alpha-controls
       [:div.input-group
        [:label "Base Color:"]
        [:input {:type "color" :value base
                 :on-change #(swap! state assoc-in [:alpha :base] (-> % .-target .-value))}]
        [:input {:type "range" :min 0 :max 100 :value alpha-base
                 :on-change #(swap! state assoc-in [:alpha :alpha-base] (js/parseInt (-> % .-target .-value)))}]
        [:span (.toFixed a-base 2)]]
       [:div.input-group
        [:label "Overlay Color:"]
        [:input {:type "color" :value overlay
                 :on-change #(swap! state assoc-in [:alpha :overlay] (-> % .-target .-value))}]
        [:input {:type "range" :min 0 :max 100 :value alpha-overlay
                 :on-change #(swap! state assoc-in [:alpha :alpha-overlay] (js/parseInt (-> % .-target .-value)))}]
        [:span (.toFixed a-overlay 2)]]]
      [:div.alpha-result
       [:div.alpha-preview
        [:h4 "Base Color"]
        [:div.alpha-swatch
         [:div.alpha-swatch-inner {:style {:background base :opacity a-base}}]]
        [copyable-color base]]
       [:div.alpha-preview
        [:h4 "Overlay Color"]
        [:div.alpha-swatch
         [:div.alpha-swatch-inner {:style {:background overlay :opacity a-overlay}}]]
        [copyable-color overlay]]
       [:div.alpha-preview
        [:h4 "Result"]
        [:div.alpha-swatch
         [:div.alpha-swatch-inner {:style {:background res-hex :opacity res-alpha}}]]
        [copyable-color res-hex]]]]]))

(defn delta-section []
  (let [{:keys [color1 color2]} (:delta @state)
        de (color/delta-e color1 color2)
        [interpretation _] (cond
                             (< de 1) ["Not perceptible by human eyes" "excellent"]
                             (< de 2) ["Perceptible through close observation" "good"]
                             (< de 10) ["Perceptible at a glance" "fair"]
                             :else ["Colors are significantly different" "poor"])]
    [:section.section
     [:h2 (:eye icons) "Perceptual Color Difference (Delta E)"]
     [:div.card
      [:div.delta-controls
       [:div.input-group
        [:label "Color 1:"]
        [:input {:type "color" :value color1
                 :on-change #(swap! state assoc-in [:delta :color1] (-> % .-target .-value))}]]
       [:div.input-group
        [:label "Color 2:"]
        [:input {:type "color" :value color2
                 :on-change #(swap! state assoc-in [:delta :color2] (-> % .-target .-value))}]]]
      [:div.delta-result
       [:h3 "Delta E (CIE76)"]
       [:div.delta-value (.toFixed de 2)]
       [:div.delta-interpretation interpretation]
       [:div.delta-colors
        [copyable-color color1]
        [copyable-color color2]]]]]))

(defn kelvin-section []
  (let [temp (get-in @state [:kelvin :temp])
        hex (color/kelvin->hex temp)
        description (cond
                      (< temp 2000) "Candlelight"
                      (< temp 3000) "Warm/Sunrise"
                      (< temp 4000) "Incandescent"
                      (< temp 5000) "Fluorescent"
                      (< temp 6000) "Daylight"
                      (< temp 7000) "Overcast Sky"
                      :else "Clear Blue Sky")
        presets [{:name "Candle" :kelvin 1900}
                 {:name "Sunrise" :kelvin 2700}
                 {:name "Tungsten" :kelvin 3200}
                 {:name "Fluorescent" :kelvin 4500}
                 {:name "Daylight" :kelvin 6500}
                 {:name "Overcast" :kelvin 7000}
                 {:name "Blue Sky" :kelvin 10000}]]
    [:section.section
     [:h2 (:thermometer icons) "Color Temperature (Kelvin)"]
     [:div.card
      [:div.input-group
       [:label "Temperature (K):"]
       [:input {:type "range" :min 1000 :max 15000 :step 100 :value temp
                :on-change #(swap! state assoc-in [:kelvin :temp] (js/parseInt (-> % .-target .-value)))}]
       [:span (str temp "K")]]
      [:div.kelvin-display
       [:div.kelvin-swatch-container
        [copyable-color hex]]
       [:div.kelvin-info
        [:div.kelvin-info-item [:strong "Temperature"] (str temp "K")]
        [:div.kelvin-info-item [:strong "Description"] description]]]
      [:div.kelvin-presets
       (doall (map (fn [{:keys [name kelvin]}]
                     (let [p-hex (color/kelvin->hex kelvin)]
                       ^{:key name}
                       [:div.kelvin-preset {:on-click #(swap! state assoc-in [:kelvin :temp] kelvin)}
                        [:div.kelvin-preset-swatch {:style {:background p-hex}}]
                        [:strong name]
                        [:div (str kelvin "K")]]))
                   presets))]]]))

(defn contrast-section []
  (let [{:keys [bg fg]} (:contrast @state)
        ratio (color/contrast-ratio bg fg)
        pass-aa (>= ratio 4.5)
        pass-aa-large (>= ratio 3)
        pass-aaa (>= ratio 7)]
    [:section.section
     [:h2 (:contrast icons) "Accessibility & Contrast"]
     [:div.card
      [:div.contrast-controls
       [:div.input-group
        [:label "Background:"]
        [:input {:type "color" :value bg
                 :on-change #(swap! state assoc-in [:contrast :bg] (-> % .-target .-value))}]
        [copyable-color bg]]
       [:div.input-group
        [:label "Foreground:"]
        [:input {:type "color" :value fg
                 :on-change #(swap! state assoc-in [:contrast :fg] (-> % .-target .-value))}]
        [copyable-color fg]]]
      [:div.contrast-result
       [:div.contrast-ratio-display
        [:div.contrast-ratio-value (str ratio #_(.toFixed ratio 2) ":1")]]
       [:div.contrast-wcag
        [:div.wcag-item {:class (if pass-aa "pass" "fail")}
         [:strong "WCAG AA"]
         [:div "Normal Text"]
         [:div.wcag-item__result (if pass-aa [:<> (:check icons) " Pass"] [:<> (:x icons) " Fail"])]]
        [:div.wcag-item {:class (if pass-aa-large "pass" "fail")}
         [:strong "WCAG AA"]
         [:div "Large Text"]
         [:div.wcag-item__result (if pass-aa-large [:<> (:check icons) " Pass"] [:<> (:x icons) " Fail"])]]
        [:div.wcag-item {:class (if pass-aaa "pass" "fail")}
         [:strong "WCAG AAA"]
         [:div "Normal Text"]
         [:div.wcag-item__result (if pass-aaa [:<> (:check icons) " Pass"] [:<> (:x icons) " Fail"])]]]
       [:div.contrast-preview {:style {:background bg :color fg}}
        "Sample text with this color combination"]]]]))

(defn harmony-section []
  (let [base (get-in @state [:harmony :base])
        comp (color/complementary base)
        analog (color/analogous base)
        triad (color/triadic base)
        tetrad (color/tetradic base)]
    [:section.section
     [:h2 (:aperture icons) "Color Harmony"]
     [:div.card
      [:div.input-group
       [:label "Base Color:"]
       [:input {:type "color" :value base
                :on-change #(swap! state assoc-in [:harmony :base] (-> % .-target .-value))}]]
      [:div.harmony-grid
       [:div.harmony-section
        [:h4 "Complementary"]
        [:div.harmony-swatches
         [copyable-color base]
         [copyable-color comp]]]
       [:div.harmony-section
        [:h4 "Analogous"]
        [:div.harmony-swatches
         (doall (map-indexed (fn [i c] ^{:key i} [copyable-color c]) analog))]]
       [:div.harmony-section
        [:h4 "Triadic"]
        [:div.harmony-swatches
         (doall (map-indexed (fn [i c] ^{:key i} [copyable-color c]) triad))]]
       [:div.harmony-section
        [:h4 "Tetradic (Square)"]
        [:div.harmony-swatches
         (doall (map-indexed (fn [i c] ^{:key i} [copyable-color c]) tetrad))]]]]]))

(defn app []
  [:<>
   [color-picker-section]
   [blending-section]
   [tst-section]
   [gradient-section]
   [alpha-section]
   [delta-section]
   [kelvin-section]
   [contrast-section]
   [harmony-section]])

(defn init []
  (start! {:body [#'app]
           :title-prefix "Reagent: "
           :css-infiles ["site/public/css/examples.css"
                         "site/public/css/main.css"]}))