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
      (is (sut/accessible? "#ffffff" accessible)))))

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