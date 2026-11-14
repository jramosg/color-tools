(ns jon.color-tools.cli-process-test
  "Runs the CLI as a subprocess to check what the pure tests cannot: real
   stdin, stdout kept apart from stderr, and the exit code of the process."
  (:require
   [clojure.java.shell :as shell]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [jon.color-tools.cli :as cli]))

(defn- color-tools
  "Run the CLI on this JVM's classpath. `stdin` is the text to pipe in."
  [stdin & args]
  (let [java (str (System/getProperty "java.home") "/bin/java")
        command (concat [java "-cp" (System/getProperty "java.class.path")
                         "clojure.main" "-m" "jon.color-tools.main"]
                        args
                        [:in (or stdin "")])]
    (apply shell/sh command)))

(deftest process-test
  (testing "success: result on stdout, nothing on stderr, exit 0"
    (is (= {:exit 0 :out "hsl(204, 70%, 53%)\n" :err ""}
           (color-tools nil "convert" "#3498db" "--to" "hsl"))))
  (testing "no swatches when stdout is not a terminal"
    (is (= {:exit 0 :out "#db3398\n#98db33\n" :err ""}
           (color-tools nil "palette" "#3498db" "--type" "triadic"))))
  (testing "colors piped in on stdin"
    (is (= {:exit 0 :out "[\"#ff6666\", \"#6666ff\"]\n" :err ""}
           (color-tools "#f00\n#00f\n" "lighten" "-" "0.2" "--json"))))
  (testing "a failed check exits 1 and still prints the result"
    (let [{:keys [exit out err]}
          (color-tools nil "contrast" "#3498db" "#fff" "--level" "aa")]
      (is (= [1 ""] [exit err]))
      (is (str/includes? out "ratio     3.15"))))
  (testing "bad input exits 2 with the message on stderr only"
    (let [{:keys [exit out err]} (color-tools nil "convert" "nope")]
      (is (= [2 ""] [exit out]))
      (is (str/starts-with? err "color-tools: invalid color \"nope\""))
      (is (not (str/includes? err "Exception")))))
  (testing "warnings go to stderr and leave stdout clean"
    (is (= {:exit 0
            :out "#00d600\n"
            :err (str "color-tools: warning: oklch(0.7 0.4 150) is outside"
                      " sRGB; clamped to #00d600\n")}
           (color-tools nil "convert" "oklch(0.7 0.4 150)"))))
  (testing "--version"
    (is (= {:exit 0 :out (str "color-tools " cli/version "\n") :err ""}
           (color-tools nil "--version")))))
