;; Copyright (c) 2025 Jon Ramos
;; SPDX-License-Identifier: EPL-1.0

(ns jon.runtime-parity-check
  "Run JVM and compiled Node cases, then compare their generated outputs."
  (:require [cljs.build.api :as cljs]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [jon.runtime-cases :as inputs]
            [jon.runtime-parity :as parity]))

(defn- write-edn! [path value]
  (io/make-parents path)
  (spit path (str (pr-str value) "\n")))

(defn- read-results! [path runtime]
  (let [data (edn/read-string (slurp path))]
    (when-not (= runtime (:runtime data))
      (throw (ex-info "Unexpected parity result runtime"
                      {:path path :expected runtime
                       :actual (:runtime data)})))
    (:results data)))

(defn- run-node! [program inputs-path results-path]
  (let [{:keys [exit out err]}
        (shell/sh "node" program inputs-path results-path)]
    (when (seq out) (print out))
    (when (seq err) (binding [*out* *err*] (print err)))
    (when-not (zero? exit)
      (throw (ex-info "Node parity runner failed" {:exit exit})))))

(defn- check! [output-dir]
  (let [path #(str (io/file output-dir %))
        cases (inputs/cases)
        inputs-path (path "cases.edn")
        jvm-path (path "jvm.edn")
        node-path (path "node.edn")
        program (path "node.js")]
    (doseq [name ["cases.edn" "jvm.edn" "node.edn" "differences.edn"]]
      (io/delete-file (path name) true))
    (write-edn! inputs-path {:seed inputs/seed :cases cases})
    (println "Running" (count cases) "cases on the JVM")
    (write-edn! jvm-path {:runtime :jvm
                          :results (mapv parity/evaluate cases)})
    (println "Compiling the ClojureScript runner for Node")
    (cljs/build "test/jon/runtime_parity_runner.cljs"
                {:main 'jon.runtime-parity-runner
                 :target :nodejs
                 :optimizations :advanced
                 :output-to program
                 :output-dir (path "cljs")})
    (println "Running the same cases on Node")
    (run-node! program inputs-path node-path)
    (let [differences (parity/differences
                       cases (read-results! jvm-path :jvm)
                       (read-results! node-path :node))]
      (write-edn! (path "differences.edn") differences)
      (when (seq differences)
        (binding [*out* *err*]
          (doseq [difference (take 10 differences)] (prn difference)))
        (throw (ex-info "JVM and Node results differ"
                        {:differences (count differences)
                         :report (path "differences.edn")})))
      (println "JVM and Node agree on" (count cases) "cases"))))

(defn -main [& args]
  (try
    (when (< 1 (count args))
      (throw (ex-info "Expected at most one output directory" {})))
    (check! (or (first args) "target/runtime-parity"))
    (catch Exception e
      (binding [*out* *err*]
        (println (ex-message e))
        (when (seq (ex-data e)) (prn (ex-data e))))
      (System/exit 1))
    (finally (shutdown-agents))))
