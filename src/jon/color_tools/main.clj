(ns jon.color-tools.main
  "Process entry point for the color-tools CLI on the JVM and Babashka."
  (:require
   [jon.color-tools.cli :as cli]))

(defn- terminal?
  "True when stdout is a terminal. JDK 22 and later return a console even
   when output is redirected, so ask it; older JDKs lack the method and
   return no console at all in that case."
  []
  (when-let [console (System/console)]
    (try
      (.isTerminal console)
      (catch Exception _ true))))

(defn -main [& args]
  (let [{:keys [exit out err]}
        (cli/run (vec args) {:stdin #(slurp *in*)
                             :tty? (boolean (terminal?))
                             :env (into {} (System/getenv))})]
    (print out)
    (flush)
    (binding [*out* *err*]
      (print err)
      (flush))
    (System/exit exit)))
