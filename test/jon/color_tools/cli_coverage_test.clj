(ns jon.color-tools.cli-coverage-test
  "Keeps the CLI, its documentation and the build in step with the
   library."
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [jon.color-tools]
   [jon.color-tools.cli :as cli]
   [jon.color-tools.cli.commands :as commands]))

(def ^:private public-api
  (set (keys (ns-publics 'jon.color-tools))))

(def ^:private covered
  (into (set (keys commands/helpers)) (mapcat :api) commands/commands))

(deftest every-public-function-has-a-command-test
  (testing "public functions with no command; add one or list it in helpers"
    (is (= #{} (set/difference public-api covered))))
  (testing "functions the command table names that the library lacks"
    (is (= #{} (set/difference covered public-api)))))

(deftest version-test
  (is (= (second (re-find #"\(def version \"([^\"]+)\"\)" (slurp "build.clj")))
         cli/version)
      "cli/version must match the version in build.clj")
  (is (= (second (re-find #"\"version\": \"([^\"]+)\""
                          (slurp "npm/package.json")))
         cli/version)
      "npm/package.json must have the same version as cli/version"))

(defn- command-of
  "The commands that cover library function `sym`, as markdown."
  [sym]
  (->> commands/commands
       (filter #(some #{sym} (:api %)))
       (map #(str "`" (:name %) "`"))
       (str/join ", ")))

(defn coverage-table
  "The public-API-to-command table for doc/cli.md."
  []
  (str "| Library function | Command |\n"
       "| --- | --- |\n"
       (str/join
        (for [sym (sort public-api)]
          (str "| `" sym "` | "
               (if-let [reason (commands/helpers sym)]
                 (str "None. " reason)
                 (command-of sym))
               " |\n")))))

;; =============================================================================
;; Documented examples
;; =============================================================================

(defn- shell-words
  "Split a command line the way a shell would, for the quoting the docs
   use: single quotes, double quotes and bare words."
  [line]
  (map (fn [[_ single double bare]] (or single double bare))
       (re-seq #"'([^']*)'|\"([^\"]*)\"|(\S+)" line)))

(defn- run-words [words stdin]
  (cli/run (vec words) {:stdin (constantly stdin) :tty? false :env {}}))

(defn example-output
  "What a `$ color-tools ...` line of the docs prints: stdout, then stderr."
  [line]
  (let [[prompt program & args] (shell-words line)]
    (assert (= ["$" "color-tools"] [prompt program]) line)
    (let [{:keys [out err]} (run-words args "")]
      (str out err))))

(defn- console-examples
  "Pairs of [command-line expected-output] from the console blocks of a
   markdown document."
  [markdown]
  (for [[_ block] (re-seq #"(?s)```console\n(.*?)```" markdown)
        [[command] output] (->> (str/split-lines block)
                                (partition-by #(str/starts-with? % "$ "))
                                (partition-all 2))]
    [command (str/join (map #(str % "\n") output))]))

(deftest cli-guide-test
  (let [guide (slurp "doc/cli.md")]
    (testing "every console example prints what the guide says"
      (let [examples (console-examples guide)]
        (is (< 50 (count examples)))
        (doseq [[command expected] examples]
          (is (= expected (example-output command)) command))))
    (testing "the coverage table is current"
      (is (str/includes? guide (coverage-table))
          (str "Replace the table in doc/cli.md with:\n" (coverage-table))))))

(deftest command-examples-test
  (doseq [command commands/commands
          example (:examples command)
          :when (not (str/includes? example "$"))
          :let [words (take-while (complement #{"<" ">" ">>" "|" "&&"})
                                  (shell-words example))
                {:keys [exit err]} (run-words words "#ff0000\n#0000ff\n")]]
    (testing example
      (is (contains? #{0 1} exit) err))))
