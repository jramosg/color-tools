;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.runtime-parity-test
  (:require [clojure.test :refer [deftest is testing]]
            [jon.runtime-parity :as parity]))

(def ^:private case-input {:op :rgb->hsl :args [[0 24 1]]})

(deftest runtime-difference-report-test
  (testing "a platform regression reports its input and both results"
    (is (= [(assoc case-input :index 0
                   :jvm [123 100 5] :node [122 100 5])]
           (parity/differences [case-input]
                               [[123 100 5]] [[122 100 5]]))))
  (testing "strings and booleans must match exactly"
    (doseq [[jvm node] [["#d6fde1" "#d5fde1"] [true false] [false "false"]]]
      (is (= 1 (count (parity/differences [case-input] [jvm] [node])))))))

(deftest runtime-number-tolerance-test
  (testing "floating point noise is tolerated at small and large scales"
    (doseq [[jvm node] [[0.5 (+ 0.5 5.0E-10)] [1.0E8 (+ 1.0E8 0.05)]]]
      (is (empty? (parity/differences [case-input] [jvm] [node])))))
  (testing "larger numeric changes fail, including inside theme data"
    (is (seq (parity/differences [case-input] [0.5] [0.500001])))
    (is (seq (parity/differences [case-input]
                                 [{:scale [0.5]}] [{:scale [0.500001]}]))))
  (testing "nonfinite results cannot pass through an infinite tolerance"
    (doseq [[jvm node] [[0.0 ##Inf] [##-Inf 1.0]
                        [##NaN 0.0] [1.0 ##NaN]]]
      (is (seq (parity/differences [case-input] [jvm] [node])))))
  (testing "map order and whole-number representations do not matter"
    (is (empty? (parity/differences [case-input]
                                    [{:a 1.0 :b [0.5]}]
                                    [{:b [0.5] :a 1}])))))

(deftest incomplete-runtime-run-test
  (testing "empty or truncated runs cannot pass by comparing a prefix"
    (doseq [[cases jvm node] [[[] [] []]
                              [[case-input] [] []]
                              [[case-input] [1] []]
                              [[case-input] [1] [1 2]]]]
      (is (thrown? #?(:clj clojure.lang.ExceptionInfo :cljs js/Error)
                   (parity/differences cases jvm node))))))
