;; copyright (c) 2025 -- Jon Ramos, all rights reserved.

(ns jon.color-tools-test
  (:require [clojure.test :refer [deftest is testing]]
            [jon.color-tools :as sut])) ; system under test

(deftest test-rgb->hsv
  (testing "RGB to HSV conversion"
    (is (= [0 0 0] (sut/rgb->hsv [0 0 0]))) ; black
    (is (= [0 0 100] (sut/rgb->hsv [255 255 255]))) ; white
    (is (= [0 100 100] (sut/rgb->hsv [255 0 0]))) ; red
    (is (= [120 100 100] (sut/rgb->hsv [0 255 0]))) ; green
    (is (= [240 100 100] (sut/rgb->hsv [0 0 255]))) ; blue
    (is (= [60 100 100] (sut/rgb->hsv [255 255 0]))) ; yellow
    (is (= [180 100 100] (sut/rgb->hsv [0 255 255]))) ; cyan
    (is (= [300 100 100] (sut/rgb->hsv [255 0 255]))) ; magenta
    (is (= [0 0 50] (sut/rgb->hsv [128 128 128]))))) ; gray

(deftest test-hsv->rgb
  (testing "HSV to RGB conversion"
    (is (= [0 0 0] (sut/hsv->rgb [0 0 0]))) ; black
    (is (= [255 255 255] (sut/hsv->rgb [0 0 100]))) ; white
    (is (= [255 0 0] (sut/hsv->rgb [0 100 100]))) ; red
    (is (= [0 255 0] (sut/hsv->rgb [120 100 100]))) ; green
    (is (= [0 0 255] (sut/hsv->rgb [240 100 100]))) ; blue
    (is (= [255 255 0] (sut/hsv->rgb [60 100 100]))) ; yellow
    (is (= [0 255 255] (sut/hsv->rgb [180 100 100]))) ; cyan
    (is (= [255 0 255] (sut/hsv->rgb [300 100 100]))) ; magenta
    (is (= [128 128 128] (sut/hsv->rgb [0 0 50]))))) ; gray

(deftest test-color-validation
  (testing "Hex color validation"
    (is (sut/valid-hex? "#ff0000"))
    (is (sut/valid-hex? "#f00"))
    (is (sut/valid-hex? "ff0000"))
    (is (not (sut/valid-hex? "#gg0000")))
    (is (not (sut/valid-hex? "#ff000"))))

  (testing "RGB color validation"
    (is (sut/valid-rgb? [255 0 0]))
    (is (sut/valid-rgb? [0 128 255]))
    (is (not (sut/valid-rgb? [256 0 0])))
    (is (not (sut/valid-rgb? [-1 0 0])))
    (is (not (sut/valid-rgb? [255 0]))))

  (testing "RGBA color validation"
    (is (sut/valid-rgba? [255 0 0 1]))
    (is (sut/valid-rgba? [0 128 255 0.5]))
    (is (not (sut/valid-rgba? [256 0 0 1])))
    (is (not (sut/valid-rgba? [255 0 0 2])))
    (is (not (sut/valid-rgba? [255 0 0]))))

  (testing "HSL color validation"
    (is (sut/valid-hsl? [0 100 50]))
    (is (sut/valid-hsl? [180 50 75]))
    (is (not (sut/valid-hsl? [361 100 50])))
    (is (not (sut/valid-hsl? [0 101 50])))
    (is (not (sut/valid-hsl? [0 100 101])))))

(deftest test-color-conversion
  (testing "Hex normalization"
    (is (= "#ff0000" (sut/normalize-hex "#f00")))
    (is (= "#ff0000" (sut/normalize-hex "f00")))
    (is (= "#ff0000" (sut/normalize-hex "#ff0000"))))

  (testing "Hex to RGB conversion"
    (is (= [255 0 0] (sut/hex->rgb "#ff0000")))
    (is (= [255 0 0] (sut/hex->rgb "#f00")))
    (is (= [0 255 0] (sut/hex->rgb "#00ff00")))
    (is (= [0 0 255] (sut/hex->rgb "#0000ff"))))

  (testing "RGB to Hex conversion"
    (is (= "#ff0000" (sut/rgb->hex [255 0 0])))
    (is (= "#00ff00" (sut/rgb->hex [0 255 0])))
    (is (= "#0000ff" (sut/rgb->hex [0 0 255])))
    (is (= "#ffffff" (sut/rgb->hex [255 255 255])))
    (is (= "#000000" (sut/rgb->hex [0 0 0]))))

  (testing "Hex to RGBA conversion"
    (is (= [255 0 0 1] (sut/hex->rgba "#ff0000")))
    (is (= [255 0 0 0.5] (sut/hex->rgba "#ff0000" 0.5))))

  (testing "RGBA to Hex conversion"
    (is (= "#ff0000" (sut/rgba->hex [255 0 0 1])))
    (is (= "#ff0000" (sut/rgba->hex [255 0 0 0.5]))))

  (testing "RGB to HSL conversion"
    (is (= [0 100 50] (sut/rgb->hsl [255 0 0])))
    (is (= [120 100 50] (sut/rgb->hsl [0 255 0])))
    (is (= [240 100 50] (sut/rgb->hsl [0 0 255]))))

  (testing "HSL to RGB conversion"
    (is (= [255 0 0] (sut/hsl->rgb [0 100 50])))
    (is (= [0 255 0] (sut/hsl->rgb [120 100 50])))
    (is (= [0 0 255] (sut/hsl->rgb [240 100 50]))))

  (testing "Hex HSL round-trip conversion"
    (is (= "#ff0000" (-> "#ff0000" sut/hex->hsl sut/hsl->hex)))
    (is (= "#00ff00" (-> "#00ff00" sut/hex->hsl sut/hsl->hex)))
    (is (= "#0000ff" (-> "#0000ff" sut/hex->hsl sut/hsl->hex)))))

(deftest test-color-manipulation
  (testing "Lighten color"
    (let [red "#ff0000"
          lighter (sut/lighten red 0.2)]
      (is (string? lighter))
      (is (not= red lighter))))

  (testing "Darken color"
    (let [red "#ff0000"
          darker (sut/darken red 0.2)]
      (is (string? darker))
      (is (not= red darker))))

  (testing "Saturate color"
    (let [gray "#808080"
          saturated (sut/saturate gray 0.3)]
      (is (string? saturated))
      (is (not= gray saturated))))

  (testing "Desaturate color"
    (let [red "#ff0000"
          desaturated (sut/desaturate red 0.3)]
      (is (string? desaturated))
      (is (not= red desaturated))))

  (testing "Adjust hue"
    (let [red "#ff0000"
          shifted (sut/adjust-hue red 60)]
      (is (string? shifted))
      (is (not= red shifted))))

  (testing "Invert color"
    (is (= "#000000" (sut/invert "#ffffff")))
    (is (= "#ffffff" (sut/invert "#000000")))
    (is (= [0 255 255] (sut/invert [255 0 0]))))

  (testing "Grayscale conversion"
    (let [red "#ff0000"
          gray (sut/grayscale red)]
      (is (string? gray))
      (is (not= red gray)))))

(deftest test-color-mixing
  (testing "Mix colors"
    (let [red "#ff0000"
          blue "#0000ff"
          mixed (sut/mix red blue)]
      (is (string? mixed))
      (is (not= red mixed))
      (is (not= blue mixed)))

    (testing "Mix with custom ratio"
      (let [red "#ff0000"
            blue "#0000ff"
            mixed-25 (sut/mix red blue 0.25)
            mixed-75 (sut/mix red blue 0.75)]
        (is (not= mixed-25 mixed-75))))))

(deftest test-contrast-and-accessibility
  (testing "Luminance calculation"
    (let [white-lum (sut/luminance "#ffffff")
          black-lum (sut/luminance "#000000")]
      (is (> white-lum black-lum))
      (is (>= white-lum 0))
      (is (<= white-lum 1))))

  (testing "Contrast ratio calculation"
    (let [ratio (sut/contrast-ratio "#000000" "#ffffff")]
      (is (= 21.0 ratio)))

    (let [ratio (sut/contrast-ratio "#ffffff" "#ffffff")]
      (is (= 1.0 ratio))))

  (testing "Accessibility check"
    (is (sut/accessible? "#000000" "#ffffff"))
    (is (sut/accessible? "#000000" "#ffffff" :aa))
    (is (sut/accessible? "#000000" "#ffffff" :aaa))
    (is (not (sut/accessible? "#777777" "#888888"))))

  (testing "Find accessible color"
    (let [accessible (sut/find-accessible-color "#ffffff" "#777777")]
      (is (string? accessible))
      (is (sut/accessible? "#ffffff" accessible))))

  (testing "Get contrast text color"
    ; Light backgrounds should get black text
    (is (= "#000000" (sut/get-contrast-text "#ffffff")))  ; white bg -> black text
    (is (= "#000000" (sut/get-contrast-text "#ffff00")))  ; yellow bg -> black text
    (is (= "#000000" (sut/get-contrast-text "#00ff00")))  ; green bg -> black text
    (is (= "#000000" (sut/get-contrast-text "#00ffff")))  ; cyan bg -> black text

    ; Dark backgrounds should get white text
    (is (= "#ffffff" (sut/get-contrast-text "#000000")))  ; black bg -> white text
    (is (= "#ffffff" (sut/get-contrast-text "#0000ff")))  ; blue bg -> white text
    (is (= "#ffffff" (sut/get-contrast-text "#800080")))  ; purple bg -> white text
    (is (= "#ffffff" (sut/get-contrast-text "#008000")))  ; dark green bg -> white text
    (is (= "#ffffff" (sut/get-contrast-text "#129BA5")))  ; teal bg -> white text

    ; Gray tones
    (is (= "#000000" (sut/get-contrast-text "#c0c0c0")))  ; light gray -> black text
    (is (= "#ffffff" (sut/get-contrast-text "#404040")))  ; dark gray -> white text

    ; Test with RGB input
    (is (= "#000000" (sut/get-contrast-text [255 255 255])))  ; white RGB -> black text
    (is (= "#ffffff" (sut/get-contrast-text [0 0 0])))       ; black RGB -> white text

    ; Test that result is always accessible (using AA-large standard for text contrast)
    (let [bg-colors ["#ff0000" "#00ff00" "#0000ff" "#ffff00" "#ff00ff"
                     "#00ffff" "#800000" "#008000" "#000080" "#808000"
                     "#800080" "#008080" "#c0c0c0" "#808080" "#000000" "#ffffff"]
          test-accessibility (fn [bg]
                               (let [text-color (sut/get-contrast-text bg)]
                                 (sut/accessible? bg text-color :aa-large)))]
      (is (every? test-accessibility bg-colors)))))

(deftest test-color-harmony
  (testing "Complementary color"
    (let [red "#ff0000"
          comp (sut/complementary red)]
      (is (string? comp))
      (is (not= red comp))))

  (testing "Triadic colors"
    (let [red "#ff0000"
          triadic (sut/triadic red)]
      (is (= 2 (count triadic)))
      (is (every? string? triadic))))

  (testing "Tetradic colors"
    (let [red "#ff0000"
          tetradic (sut/tetradic red)]
      (is (= 3 (count tetradic)))
      (is (every? string? tetradic))))

  (testing "Analogous colors"
    (let [red "#ff0000"
          analogous (sut/analogous red)]
      (is (= 3 (count analogous)))
      (is (every? string? analogous)))

    (let [red "#ff0000"
          analogous-5 (sut/analogous red 5)]
      (is (= 5 (count analogous-5)))))

  (testing "Monochromatic colors"
    (let [red "#ff0000"
          mono (sut/monochromatic red)]
      (is (= 5 (count mono)))
      (is (every? string? mono)))

    (let [red "#ff0000"
          mono-3 (sut/monochromatic red 3)]
      (is (= 3 (count mono-3)))))

  (testing "Split complementary colors"
    (let [red "#ff0000"
          split (sut/split-complementary red)]
      (is (= 2 (count split)))
      (is (every? string? split)))))

(deftest test-palette-generation
  (testing "Generate different palette types"
    (let [base "#ff0000"]
      (is (seq (sut/generate-palette :monochromatic base)))
      (is (seq (sut/generate-palette :analogous base)))
      (is (seq (sut/generate-palette :triadic base)))
      (is (seq (sut/generate-palette :tetradic base)))
      (is (seq (sut/generate-palette :complementary base)))
      (is (seq (sut/generate-palette :split-complementary base)))
      (is (seq (sut/generate-palette :random base))))))

(deftest test-color-temperature
  (testing "Warm color detection"
    (is (sut/warm? "#ff0000"))  ; Red
    (is (sut/warm? "#ff8000"))  ; Orange
    (is (sut/warm? "#ffff00"))  ; Yellow
    (is (not (sut/warm? "#0000ff")))  ; Blue
    (is (not (sut/warm? "#00ff00"))))  ; Green

  (testing "Cool color detection"
    (is (sut/cool? "#0000ff"))  ; Blue
    (is (sut/cool? "#00ff00"))  ; Green
    (is (sut/cool? "#8000ff"))  ; Purple
    (is (not (sut/cool? "#ff0000")))  ; Red
    (is (not (sut/cool? "#ff8000"))))  ; Orange

  (testing "Temperature classification"
    (is (= :warm (sut/temperature "#ff0000")))
    (is (= :cool (sut/temperature "#0000ff")))))

(deftest test-color-distance
  (testing "Color distance calculation"
    (let [red "#ff0000"
          blue "#0000ff"
          red2 "#fe0000"
          distance-far (sut/color-distance red blue)
          distance-close (sut/color-distance red red2)]
      (is (> distance-far distance-close))
      (is (>= distance-far 0))))

  (testing "Color similarity"
    (let [red "#ff0000"
          red2 "#fe0000"
          blue "#0000ff"]
      (is (sut/similar? red red2 10))
      (is (not (sut/similar? red blue 50))))))

(deftest test-color-analysis
  (testing "Brightness calculation"
    (let [white-brightness (sut/brightness "#ffffff")
          black-brightness (sut/brightness "#000000")]
      (is (> white-brightness black-brightness))
      (is (>= white-brightness 0))
      (is (<= white-brightness 255))))

  (testing "Dark/light detection"
    (is (sut/dark? "#000000"))
    (is (sut/light? "#ffffff"))
    (is (not (sut/dark? "#ffffff")))
    (is (not (sut/light? "#000000"))))

  (testing "Vibrant/muted detection"
    (is (sut/vibrant? "#ff0000"))  ; Pure red is vibrant
    (is (sut/muted? "#808080"))    ; Gray is muted
    (is (not (sut/vibrant? "#808080")))
    (is (not (sut/muted? "#ff0000")))))

(deftest test-color-names
  (testing "Color name to hex conversion"
    (is (= "#ff0000" (sut/name->hex "red")))
    (is (= "#008000" (sut/name->hex "green")))
    (is (= "#0000ff" (sut/name->hex "blue")))
    (is (= "#ffffff" (sut/name->hex "white")))
    (is (= "#000000" (sut/name->hex "black")))
    (is (nil? (sut/name->hex "nonexistent"))))

  (testing "Hex to color name approximation"
    (let [red-name (sut/hex->name "#ff0000")]
      (is (string? red-name))
      (is (= "red" red-name)))))

(deftest test-random-color
  (testing "Random color generation"
    (let [random1 (sut/random-color)
          random2 (sut/random-color)]
      (is (string? random1))
      (is (string? random2))
      (is (sut/valid-hex? random1))
      (is (sut/valid-hex? random2)))))

(deftest test-comprehensive-verification
  (testing "Color conversions work correctly"
    (is (= [255 0 0] (sut/hex->rgb "#ff0000")))
    (is (= "#ff0000" (sut/rgb->hex [255 0 0])))
    (is (= [0 100 50] (sut/hex->hsl "#ff0000"))))

  (testing "Color manipulation produces valid results"
    (let [original "#ff0000"
          lighter (sut/lighten original 0.2)
          darker (sut/darken original 0.2)
          inverted (sut/invert original)]
      (is (sut/valid-hex? lighter))
      (is (sut/valid-hex? darker))
      (is (sut/valid-hex? inverted))
      (is (= "#00ffff" inverted))))

  (testing "Color harmony functions return valid colors"
    (let [base "#ff0000"
          comp (sut/complementary base)
          triadic (sut/triadic base)
          analogous (sut/analogous base 3)]
      (is (sut/valid-hex? comp))
      (is (every? sut/valid-hex? triadic))
      (is (every? sut/valid-hex? analogous))
      (is (= 2 (count triadic)))
      (is (= 3 (count analogous)))))

  (testing "Accessibility functions work correctly"
    (is (= 21.0 (sut/contrast-ratio "#000000" "#ffffff")))
    (is (sut/accessible? "#000000" "#ffffff"))
    (is (not (sut/accessible? "#777777" "#888888"))))

  (testing "Palette generation produces valid results"
    (let [mono (sut/monochromatic "#3f51b5" 3)
          randoms (repeatedly 3 sut/random-color)]
      (is (= 3 (count mono)))
      (is (every? sut/valid-hex? mono))
      (is (= 3 (count randoms)))
      (is (every? sut/valid-hex? randoms))))

  (testing "Color analysis functions work correctly"
    (is (sut/warm? "#ff0000"))
    (is (sut/cool? "#0000ff"))
    (is (= 255 (sut/brightness "#ffffff")))
    (is (sut/dark? "#000000"))
    (is (sut/light? "#ffffff")))

  (testing "Color name functions work correctly"
    (is (= "#ff0000" (sut/name->hex "red")))
    (is (= "red" (sut/hex->name "#ff0000")))))

(deftest test-color-record
  (testing "Color record creation and conversion"
    ; Test creating colors
    (let [red-color (sut/->color 255 0 0)
          blue-hex (sut/color-from-hex "#0000ff")
          green-rgb (sut/color-from-rgb [0 255 0])
          yellow-hsl (sut/color-from-hsl [60 100 50])]

      ; Test color? predicate
      (is (sut/color? red-color))
      (is (not (sut/color? "#ff0000")))
      (is (not (sut/color? [255 0 0])))

      ; Test conversions from Color records
      (is (= [255 0 0] (sut/->rgb red-color)))
      (is (= "#ff0000" (sut/->hex red-color)))
      (is (= [0 100 50] (sut/->hsl red-color)))
      (is (= [255 0 0 1.0] (sut/->rgba red-color)))

      ; Test different creation methods
      (is (= [0 0 255] (sut/->rgb blue-hex)))
      (is (= [0 255 0] (sut/->rgb green-rgb)))
      (is (= [255 255 0] (sut/->rgb yellow-hsl)))))

  (testing "color-from-hsv conversion and factory"
    ; Ensure color-from-hsv creates a Color and converts properly
    (let [black (sut/color-from-hsv [0 0 0])
          white (sut/color-from-hsv [0 0 100])
          red (sut/color-from-hsv [0 100 100])
          green (sut/color-from-hsv [120 100 100])
          blue (sut/color-from-hsv [240 100 100])]
      (is (sut/color? black))
      (is (sut/color? white))
      (is (sut/color? red))
      (is (sut/color? green))
      (is (sut/color? blue))

      (is (= [0 0 0] (sut/->rgb black)))
      (is (= [255 255 255] (sut/->rgb white)))
      (is (= [255 0 0] (sut/->rgb red)))
      (is (= [0 255 0] (sut/->rgb green)))
      (is (= [0 0 255] (sut/->rgb blue)))

      (is (= "#000000" (sut/->hex black)))
      (is (= "#ffffff" (sut/->hex white)))
      (is (= "#ff0000" (sut/->hex red)))
      (is (= "#00ff00" (sut/->hex green)))
      (is (= "#0000ff" (sut/->hex blue)))))

  (testing "Color record with existing functions"
    (let [red-color (sut/->color 255 0 0)
          blue-color (sut/->color 0 0 255)]

      ; Test luminance calculation
      (is (> (sut/luminance red-color) 0))
      (is (< (sut/luminance red-color) 1))

      ; Test contrast calculation
      (is (> (sut/contrast-ratio red-color blue-color) 1))

      ; Test color manipulation returns Color records
      (let [lighter-red (sut/lighten red-color 0.2)
            darker-red (sut/darken red-color 0.2)]
        (is (sut/color? lighter-red))
        (is (sut/color? darker-red))
        (is (not= (sut/->rgb red-color) (sut/->rgb lighter-red)))
        (is (not= (sut/->rgb red-color) (sut/->rgb darker-red))))

      ; Test get-contrast-text with Color input
      (let [contrast-text (sut/get-contrast-text red-color)]
        (is (string? contrast-text))  ; Should return hex string for consistency
        (is (or (= contrast-text "#000000") (= contrast-text "#ffffff"))))))

  (testing "Color record toString"
    (let [red-color (sut/->color 255 0 0)
          red-alpha (sut/->color 255 0 0 0.5)]
      (is (= "#ff0000" (str red-color)))
      (is (= "rgba(255,0,0,0.5)" (str red-alpha)))))

  (testing "Mixed input types work together"
    ; Test that Color records, strings, and vectors can be used interchangeably
    (let [red-color (sut/->color 255 0 0)
          red-string "#ff0000"
          red-vector [255 0 0]]

      ; All should have same luminance
      (is (= (sut/luminance red-color)
             (sut/luminance red-string)
             (sut/luminance red-vector)))

      ; All should have same contrast with white
      (is (= (sut/contrast-ratio red-color "#ffffff")
             (sut/contrast-ratio red-string "#ffffff")
             (sut/contrast-ratio red-vector "#ffffff")))))

  (testing "Color factory function"
    ; Test various input formats
    (is (sut/color? (sut/color 255 0 0)))
    (is (sut/color? (sut/color 255 0 0 0.8)))
    (is (sut/color? (sut/color "#00ff00")))
    (is (sut/color? (sut/color [0 0 255])))
    (is (sut/color? (sut/color [255 255 0 0.5])))

    ; Test that they create the right colors
    (is (= [255 0 0] (sut/->rgb (sut/color 255 0 0))))
    (is (= [0 255 0] (sut/->rgb (sut/color "#00ff00"))))
    (is (= [0 0 255] (sut/->rgb (sut/color [0 0 255]))))
    (is (= 0.5 (:a (sut/color [255 255 0 0.5])))))

  (testing "->hsv conversion consistency"
      ; Test that ->hsv produces same results as rgb->hsv
    (let [test-colors [[255 0 0]     ; red
                       [0 255 0]     ; green  
                       [0 0 255]     ; blue
                       [255 255 0]   ; yellow
                       [255 0 255]   ; magenta
                       [0 255 255]   ; cyan
                       [255 255 255] ; white
                       [0 0 0]       ; black
                       [128 128 128] ; gray
                       [255 87 51]]] ; orange
      (doseq [rgb test-colors]
        (let [color-rec (sut/color-from-rgb rgb)
              hsv-from-color (sut/->hsv color-rec)
              hsv-from-rgb (sut/rgb->hsv rgb)]
          (is (= hsv-from-rgb hsv-from-color)))))))

(deftest test-normalize-color-input
  (testing "normalize-color-input with Color record returns a Color"
    (let [c (sut/->color 10 20 30)
          out (sut/normalize-color-input c)]
      (is (sut/color? out))
      (is (= (sut/->rgb c) (sut/->rgb out)))))

  (testing "normalize-color-input with hex string returns a Color"
    (let [out (sut/normalize-color-input "#0a141e")]
      (is (sut/color? out))
      (is (= [10 20 30] (sut/->rgb out)))))

  (testing "normalize-color-input with RGB vector returns a Color"
    (let [out (sut/normalize-color-input [10 20 30])]
      (is (sut/color? out))
      (is (= [10 20 30] (sut/->rgb out)))))

  (testing "normalize-color-input throws on invalid input"
    (is (thrown? clojure.lang.ExceptionInfo
                 (sut/normalize-color-input :invalid-input)))))
