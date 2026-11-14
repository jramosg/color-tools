(ns jon.color-tools.node
  "Process entry point for the color-tools CLI on Node.js.

   Node's objects are read by string key, with unchecked-get and
   js-invoke, so the advanced compiler does not rename the properties."
  (:require
   [jon.color-tools.cli :as cli]))

(def ^:private process (js* "process"))

(defn- read-stdin []
  (js-invoke (js/require "fs") "readFileSync" 0 "utf8"))

(defn- environment []
  (let [env (unchecked-get process "env")]
    (into {}
          (map (fn [k] [k (unchecked-get env k)]))
          (js/Object.keys env))))

(defn- ignore-closed-pipe!
  "Exit quietly when the reader of `stream` goes away, as in
   `color-tools names | head -1`. Node otherwise crashes with EPIPE."
  [stream]
  (js-invoke stream "on" "error"
             (fn [error]
               (when-not (= "EPIPE" (unchecked-get error "code"))
                 (throw error)))))

(defn -main [& args]
  (let [stdout (unchecked-get process "stdout")
        _ (ignore-closed-pipe! stdout)
        {:keys [exit out err]}
        (cli/run (vec args) {:stdin read-stdin
                             :tty? (boolean (unchecked-get stdout "isTTY"))
                             :env (environment)})]
    (js-invoke stdout "write" out)
    (js-invoke (unchecked-get process "stderr") "write" err)
    (unchecked-set process "exitCode" exit)))

(set! *main-cli-fn* -main)
