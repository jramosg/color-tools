(ns jon.color-tools.cli.parse-test
  (:require
   [clojure.test :refer [are deftest is testing]]
   [jon.color-tools :as ct]
   [jon.color-tools.cli.parse :as parse]))

(defn- parsed [input]
  (let [{:keys [format color]} (parse/try-color input)]
    [format (some-> color ct/->rgba)]))

(deftest try-color-test
  (testing "formats the library parses"
    (are [input expected] (= expected (parsed input))
      "#3498db" [:hex [52 152 219 1.0]]
      "3498DB" [:hex [52 152 219 1.0]]
      "#fff" [:hex [255 255 255 1.0]]
      "  #fff  " [:hex [255 255 255 1.0]]
      "navy" [:name [0 0 128 1.0]]
      "Navy" [:name [0 0 128 1.0]]
      "rgb(52,152,219)" [:rgb [52 152 219 1.0]]
      "RGB( 52 152 219 )" [:rgb [52 152 219 1.0]]
      "rgba(52, 152, 219, 0.5)" [:rgba [52 152 219 0.5]]))
  (testing "hex with an alpha channel"
    (are [input expected] (= expected (parsed input))
      "#3498dbff" [:hex [52 152 219 1.0]]
      "#3498DB00" [:hex [52 152 219 0.0]]
      "#f00f" [:hex [255 0 0 1.0]])
    (is (= [:hex [52 152 219]]
           (update (parsed "#3498db80") 1 pop)))
    (is (< 0.5019 (peek (second (parsed "#3498db80"))) 0.502))
    (is (< 0.5333 (peek (second (parsed "#f008"))) 0.5334))
    (testing "needs the #, so plain numbers are not colors"
      (is (= "not a color" (:error (parse/try-color "3498db80"))))
      (is (= "not a color" (:error (parse/try-color "6500"))))))
  (testing "CSS forms the library does not parse"
    (are [input expected] (= expected (parsed input))
      "rgb(52 152 219 / 50%)" [:rgba [52 152 219 0.5]]
      "rgba(52,152,219)" [:rgb [52 152 219 1.0]]
      "rgb(100%, 0%, 0%)" [:rgb [255 0 0 1.0]]
      "52,152,219" [:rgb [52 152 219 1.0]]
      "52 152 219 0.5" [:rgba [52 152 219 0.5]]
      "hsl(204, 70%, 53%)" [:hsl [51 152 219 1.0]]
      "hsl(204deg 70% 53% / 0.25)" [:hsl [51 152 219 0.25]]
      "hsl(-30, 50%, 50%)" [:hsl [191 64 128 1.0]]
      "hsv(204, 76%, 86%)" [:hsv [53 153 219 1.0]]
      "hsb(204, 76, 86)" [:hsv [53 153 219 1.0]]))
  (testing "Oklab and OKLCh"
    (let [{:keys [format in-gamut? color]}
          (parse/try-color "oklab(0.65 -0.06 -0.11)")]
      (is (= :oklab format))
      (is (true? in-gamut?))
      (is (= [59 151 212] (ct/->rgb color))))
    (let [{:keys [format in-gamut? color]}
          (parse/try-color "oklch(70% 0.4 150)")]
      (is (= :oklch format))
      (is (false? in-gamut?))
      (is (= [0 214 0] (ct/->rgb color)) "clamped to sRGB")))
  (testing "color temperature"
    (is (= [:kelvin [255 254 250 1.0]] (parsed "6500k")))
    (is (= [:kelvin [255 254 250 1.0]] (parsed "6500 K"))))
  (testing "rejections say why"
    (are [input error] (= error (:error (parse/try-color input)))
      "" "not a color"
      "foo" "not a color"
      "#12345" "not a color"
      "rgb(256,0,0)" "rgb channels must be from 0 to 255"
      "rgba(0,0,0,2)" "alpha must be from 0 to 1"
      "hsl(204,170%,53%)" "hsl saturation and lightness must be from 0 to 100"
      "hsv(204,76%,186%)" "hsv saturation and value must be from 0 to 100"
      "oklch(0.7 -0.1 150)" "oklch chroma must not be negative"
      "cmyk(0,0,0,1)" "unknown color function cmyk()"
      "rgb(1,2)" "rgb() takes 3 numbers and an optional alpha"
      "500k" "kelvin must be from 1000 to 40000")))

(deftest parse-color-test
  (is (= :hex (:format (parse/parse-color "#3498db"))))
  (let [e (try (parse/parse-color "nope")
               (catch #?(:clj Exception :cljs :default) e e))]
    (is (parse/usage-error? e))
    (is (= "invalid color \"nope\": not a color" (ex-message e)))
    (is (= parse/color-hint (:hint (ex-data e))))))

(defn- usage-error? [f]
  (try (f) false
       (catch #?(:clj Exception :cljs :default) e
         (parse/usage-error? e))))

(deftest number-parsers-test
  (testing "fractions"
    (is (= 0.2 (parse/parse-fraction "AMOUNT" "0.2")))
    (is (= 0.2 (parse/parse-fraction "AMOUNT" "20%")))
    (is (= 1.0 (parse/parse-fraction "AMOUNT" "1")))
    (is (usage-error? #(parse/parse-fraction "AMOUNT" "1.5")))
    (is (usage-error? #(parse/parse-fraction "AMOUNT" "-0.1")))
    (is (usage-error? #(parse/parse-fraction "AMOUNT" "half"))))
  (testing "numbers"
    (is (= -30.0 (parse/parse-number "DEGREES" "-30")))
    (is (usage-error? #(parse/parse-number "DEGREES" "NaN")))
    (is (usage-error? #(parse/parse-number "DEGREES" "1e999")))
    (is (= 0.0 (parse/parse-number "N" 0 "0")))
    (is (usage-error? #(parse/parse-number "N" 0 "-1"))))
  (testing "integers"
    (is (= 5 (parse/parse-integer "COUNT" 1 nil "5")))
    (is (usage-error? #(parse/parse-integer "COUNT" 1 nil "0")))
    (is (usage-error? #(parse/parse-integer "COUNT" 1 10 "11")))
    (is (usage-error? #(parse/parse-integer "COUNT" 1 nil "2.5"))))
  (testing "kelvin"
    (is (= 6500.0 (parse/parse-kelvin "KELVIN" "6500")))
    (is (= 2700.0 (parse/parse-kelvin "KELVIN" "2700K")))
    (is (usage-error? #(parse/parse-kelvin "KELVIN" "999")))))
