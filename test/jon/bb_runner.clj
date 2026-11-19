;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.bb-runner
  "Discover and run the shared .cljc tests on Babashka."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as test]))

(defn- test-namespaces []
  (->> (file-seq (io/file "test"))
       (map str)
       (filter #(str/ends-with? % "_test.cljc"))
       (map #(-> %
                 (subs 5)
                 (str/replace #"\.cljc$" "")
                 (str/replace #"[/\\]" ".")
                 (str/replace "_" "-")
                 symbol))
       sort))

(defn run-tests! []
  (let [namespaces (test-namespaces)]
    (when (empty? namespaces)
      (throw (ex-info "No shared .cljc test namespaces found"
                      {:babashka/exit 1})))
    (doseq [namespace namespaces] (require namespace))
    (let [{:keys [fail error]} (apply test/run-tests namespaces)]
      (when (pos? (+ fail error))
        (throw (ex-info "Babashka tests failed" {:babashka/exit 1}))))))
