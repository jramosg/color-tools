(ns jon.color-tools.cli.output-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [are deftest is testing]]
   [jon.color-tools :as ct]
   [jon.color-tools.cli.output :as out]))

(def ^:private blue (ct/color "#3498db"))
(def ^:private glass (ct/color 52 152 219 0.5))

(defn- text
  ([tree] (text tree {}))
  ([tree options]
   (out/text (out/resolve tree (merge {:format :text} options)) nil)))

(deftest format-number-test
  (are [x expected] (= expected (out/format-number x))
    219 "219"
    219.0 "219"
    0.5 "0.5"
    0.0 "0"
    -42.06 "-42.06"
    0.00001 "0.00001"
    1.5E-9 "0.0000000015"
    1.0E21 "1000000000000000000000"))

(deftest color-text-test
  (testing "each --to format"
    (are [fmt expected] (= (str expected "\n") (text blue {:to fmt}))
      nil "#3498db"
      :hex "#3498db"
      :rgb "rgb(52, 152, 219)"
      :rgba "rgba(52, 152, 219, 1)"
      :hsl "hsl(204, 70%, 53%)"
      :hsv "hsv(204, 76%, 86%)"
      :lab "lab(60.1597 -6.0866 -42.2363)"
      :oklab "oklab(0.6531 -0.0618 -0.1197)"
      :oklch "oklch(0.6531 0.1347 242.6867)"
      :name "teal"))
  (testing "kelvin, with and without a temperature"
    (is (= "6504K\n" (text (ct/color "#ffffff") {:to :kelvin})))
    (is (= "none\n" (text (ct/color "#ff0000") {:to :kelvin}))))
  (testing "translucent colors keep their alpha by default"
    (is (= "rgba(52, 152, 219, 0.5)\n" (text glass)))
    (is (= "#3498db\n" (text glass {:to :hex}))))
  (testing "rounding options"
    (is (= "oklch(0.65 0.13 242.69)\n"
           (text blue {:to :oklch :precision 2})))
    (is (= "oklch(0.653 0.135 243)\n"
           (text blue {:to :oklch :significant 3})))))

(deftest text-layout-test
  (testing "sequences print one value per line"
    (is (= "#3498db\n#ffffff\n" (text [blue (ct/color "#fff")]))))
  (testing "nested sequences share a line"
    (is (= "#3498db #ffffff\n0.5\n"
           (text [[blue (ct/color "#fff")] [0.5]]))))
  (testing "maps align keys, indent nested maps and name nil"
    (is (= (str "ratio   3.15\n"
                "level   aa\n"
                "pass    false\n"
                "kelvin  none\n"
                "scale\n"
                "  50   #ffffff\n"
                "  500  #3498db\n")
           (text (array-map :ratio 3.15 :level :aa :pass false :kelvin nil
                            :scale (array-map :50 (ct/color "#fff")
                                              :500 blue))))))
  (testing "sequences of maps are separated by a blank line"
    (is (= "a  1\n\na  2\n" (text [{:a 1} {:a 2}]))))
  (testing "pinned formats ignore --to"
    (is (= "rgb(52, 152, 219)\n"
           (text (out/shown blue :rgb) {:to :hsl})))))

(deftest swatch-test
  (let [resolved (out/resolve blue {:format :text})]
    (is (= "\u001b[48;2;52;152;219m  \u001b[0m #3498db\n"
           (out/text resolved :truecolor)))
    (is (= "\u001b[48;5;74m  \u001b[0m #3498db\n"
           (out/text resolved :256)))
    (is (not (str/includes? (out/text resolved nil) "\u001b")))))

(deftest json-test
  (testing "colors follow --to; numbers keep full precision"
    (is (= "\"#3498db\"\n" (out/json (out/resolve blue {:format :json}))))
    (is (= "[204, 70, 53]\n"
           (out/json (out/resolve blue {:format :json :to :hsl}))))
    (is (= "[0.653091517685353, 0.13470212300578396, 242.68666272850191]\n"
           (out/json (out/resolve blue {:format :json :to :oklch}))))
    (is (= "[0.65, 0.13, 242.69]\n"
           (out/json (out/resolve blue {:format :json :to :oklch
                                        :precision 2})))))
  (testing "objects span lines and keep their order"
    (is (= (str "{\n"
                "  \"ratio\": 3.15,\n"
                "  \"level\": \"aa\",\n"
                "  \"pass\": false,\n"
                "  \"kelvin\": null,\n"
                "  \"colors\": [\"#3498db\", \"rgba(52, 152, 219, 0.5)\"],\n"
                "  \"scale\": {\n"
                "    \"500\": \"#3498db\"\n"
                "  }\n"
                "}\n")
           (out/json
            (out/resolve
             (array-map :ratio 3.15 :level :aa :pass false :kelvin nil
                        :colors [blue glass] :scale {:500 blue})
             {:format :json})))))
  (testing "arrays of objects span lines; empty collections stay short"
    (is (= "[\n  {\n    \"a\": 1\n  },\n  {\n    \"a\": 2\n  }\n]\n"
           (out/json [{:a 1} {:a 2}])))
    (is (= "[]\n" (out/json [])))
    (is (= "{}\n" (out/json {}))))
  (testing "strings are escaped"
    (is (= "\"a\\\"b\\\\c\\n\\u0001\"\n" (out/json "a\"b\\c\n\u0001")))))

(deftest edn-test
  (is (= "{:mode :light, :primary \"#3498db\", :hsl [204 70 53]}\n"
         (out/edn (out/resolve (array-map :mode :light :primary blue
                                          :hsl (out/shown blue :hsl))
                               {:format :edn})))))

(deftest css-test
  (is (= ":root {\n  --ct-primary: #3498db;\n  --ct-accent: oklch(1 0 0);\n}\n"
         (out/css (out/resolve
                   (array-map "--ct-primary" blue
                              "--ct-accent"
                              (out/shown (ct/color "#fff") :oklch))
                   {:format :css :precision 2})
                  ":root"))))
