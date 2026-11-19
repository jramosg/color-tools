;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.runtime-parity-runner
  "Node entrypoint for evaluating the same inputs as the JVM."
  (:require [cljs.reader :as edn]
            [jon.runtime-parity :as parity]))

(defn- write-results! [inputs-path results-path]
  (let [fs (js/require "fs")
        {:keys [cases]}
        (edn/read-string
         (js-invoke fs "readFileSync" inputs-path "utf8"))
        results {:runtime :node :results (mapv parity/evaluate cases)}]
    (js-invoke fs "writeFileSync" results-path (pr-str results) "utf8")))

(defn -main [& args]
  (when-not (= 2 (count args))
    (throw (ex-info "Expected input and output paths" {:args args})))
  (apply write-results! args))

(set! *main-cli-fn* -main)
