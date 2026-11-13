(ns jon.doc-examples-test
  "Evaluates the examples in the README and doc/ and checks every `;=>`
   result, so the documentation can't drift from the code."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def ^:private doc-files
  ["README.md"
   "doc/intro.md"
   "doc/tutorial.md"
   "doc/workflow-guide.md"
   "doc/api-reference.md"])

(defn- clojure-blocks [text]
  (map second (re-seq #"(?s)```clojure\n(.*?)```" text)))

(defn- read-forms
  "Reads each top-level form in `block` with the `;=>` comment that
   follows it on its own line or the same line, if any."
  [block]
  (let [rdr (java.io.PushbackReader. (java.io.StringReader. block)
                                     (inc (count block)))
        read-line! (fn [consumed]
                     (loop [sb (StringBuilder.)]
                       (let [c (.read rdr)]
                         (cond
                           (= -1 c) (when (pos? (.length sb)) (str sb))
                           (= (int \newline) c) (do (.append consumed \newline)
                                                    (str sb))
                           :else (do (.append consumed (char c))
                                     (recur (.append sb (char c))))))))
        ;; Looks past blank lines and plain comments, including one on the
        ;; form's own line, for a ;=> line. If the next code line comes
        ;; first, unreads everything so the next form stays intact.
        result-comment
        (fn []
          (let [consumed (StringBuilder.)]
            (loop []
              (let [line (read-line! consumed)]
                (cond
                  (nil? line)
                  (do (.unread rdr (.toCharArray (str consumed))) nil)

                  (re-find #"^\s*;=>" line)
                  (second (re-find #"^\s*;=>\s*(.*)$" line))

                  (or (str/blank? line) (re-find #"^\s*;" line))
                  (recur)

                  :else
                  (do (.unread rdr (.toCharArray (str consumed))) nil))))))]
    (loop [acc []]
      (let [form (try (read {:eof ::eof :read-cond :allow} rdr)
                      (catch Exception _ ::eof))]
        (if (= ::eof form)
          acc
          (recur (conj acc {:form form :expected (result-comment)})))))))

(defn- parse-expected
  "Parses the text after `;=>`. Returns ::prose for descriptions such as
   `Color record (lighter red)`, which aren't checked."
  [text]
  (let [rdr (java.io.PushbackReader. (java.io.StringReader. text))
        value (try (edn/read {:eof ::prose} rdr)
                   (catch Exception _ ::prose))
        rest-text (str/trim (slurp rdr))]
    (cond
      (= ::prose value) ::prose
      (symbol? value) ::prose
      (or (str/blank? rest-text) (str/starts-with? rest-text ";")) value
      :else ::prose)))

(defn- decimals [n]
  (let [s (str n)]
    (if-let [i (str/index-of s ".")]
      (- (count s) i 1)
      0)))

(defn- matches?
  "Compares a documented value with the actual one. Numbers match when the
   actual value rounds to the documented one at its shown precision."
  [expected actual]
  (cond
    (and (number? expected) (number? actual))
    (<= (Math/abs (- (double expected) (double actual)))
        (* 0.5 (Math/pow 10 (- (decimals expected)))))

    (and (sequential? expected) (sequential? actual))
    (and (= (count expected) (count actual))
         (every? true? (map matches? expected actual)))

    (and (map? expected) (map? actual))
    (and (= (set (keys expected)) (set (keys actual)))
         (every? #(matches? (get expected %) (get actual %)) (keys expected)))

    :else (= expected actual)))

(defn- check-file [path]
  (let [doc-ns (create-ns (gensym "doc-example"))]
    (binding [*ns* doc-ns]
      (refer-clojure)
      (require '[jon.color-tools :as color])
      (refer 'jon.color-tools)
      (doseq [block (clojure-blocks (slurp path))
              {:keys [form expected]} (read-forms block)
              :let [result (try {:value (eval form)}
                                (catch Throwable e {:error (ex-message e)}))
                    parsed (some-> expected parse-expected)]
              :when (and expected (not= ::prose parsed))]
        (is (and (not (:error result)) (matches? parsed (:value result)))
            (str path ": " (pr-str form) "\n  documented: " expected
                 "\n  actual:     "
                 (or (:error result) (pr-str (:value result)))))))))

(deftest doc-examples-test
  (doseq [path doc-files]
    (testing path
      (check-file path))))
