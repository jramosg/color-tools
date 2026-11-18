;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.runtime-parity-test
  (:require #?(:clj [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            #?(:clj [clojure.java.io :as io])
            [clojure.test :refer [deftest is testing]]
            [jon.runtime-fixture :as fixture]))

(def ^:private cases
  (:cases
   (edn/read-string
    #?(:clj (slurp (io/resource "fixtures/runtime_parity.edn"))
       :cljs (js-invoke (js/require "fs") "readFileSync"
                        "test/fixtures/runtime_parity.edn" "utf8")))))

(defn- same-result? [expected actual]
  (cond
    (number? expected)
    (and (number? actual)
         (<= (Math/abs (double (- expected actual)))
             (* 1.0E-9 (max 1.0 (Math/abs (double expected))))))

    (map? expected)
    (and (map? actual)
         (= (set (keys expected)) (set (keys actual)))
         (every? (fn [[k v]] (same-result? v (get actual k))) expected))

    (sequential? expected)
    (and (sequential? actual)
         (= (count expected) (count actual))
         (every? true? (map same-result? expected actual)))

    :else (= expected actual)))

(deftest shared-expected-values-test
  (testing "committed JVM values agree on every supported runtime"
    (is (< 2000 (count cases) 4000))
    (doseq [{:keys [op args expected] :as case} cases
            :let [actual (fixture/evaluate case)]]
      (is (same-result? expected actual)
          (pr-str {:op op :args args :expected expected :actual actual})))))
