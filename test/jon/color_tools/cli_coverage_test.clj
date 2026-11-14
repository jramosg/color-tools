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
      "cli/version must match the version in build.clj"))

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
