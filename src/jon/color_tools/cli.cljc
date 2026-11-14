(ns jon.color-tools.cli
  "Command-line interface to jon.color-tools.

   `run` is pure: it takes the argument vector and a map describing the
   process, and returns the exit code with the text for stdout and stderr.
   jon.color-tools.main does the IO."
  (:require
   [clojure.string :as str]
   [jon.color-tools.cli.commands :as commands]
   [jon.color-tools.cli.output :as out]
   [jon.color-tools.cli.parse :as parse]))

(def version "1.3.0")

(def ^:private program "color-tools")

(def exit-ok 0)
(def exit-check-failed 1)
(def exit-usage 2)

;; =============================================================================
;; Options
;; =============================================================================

(defn- one-of
  "A parser for an option that takes one of `values`."
  [flag values]
  #(parse/parse-choice flag values %))

(def ^:private input-formats
  [:hex :name :rgb :rgba :hsl :hsv :oklab :oklch :kelvin])

(def ^:private option-specs
  [{:id :to :long "--to" :short "-t" :arg "FORMAT"
    :parse (one-of "--to" out/color-formats)
    :doc "Print colors in this format; see Formats"}
   {:id :format :long "--format" :short "-f" :arg "FORMAT"
    :parse (one-of "--format" [:text :json :edn :css])
    :doc "Output as text (default), json or edn"}
   {:id :json :long "--json" :doc "Same as --format json"}
   {:id :edn :long "--edn" :doc "Same as --format edn"}
   {:id :precision :long "--precision" :short "-p" :arg "N"
    :parse #(parse/parse-integer "--precision" 0 15 %)
    :doc "Round fractional numbers to N decimal places"}
   {:id :significant :long "--significant" :arg "N"
    :parse #(parse/parse-integer "--significant" 1 17 %)
    :doc "Round fractional numbers to N significant digits"}
   {:id :color :long "--color" :arg "WHEN"
    :parse (one-of "--color" [:auto :always :never])
    :doc "Show color swatches: auto (default), always or never"}
   {:id :no-color :long "--no-color" :doc "Same as --color never"}
   {:id :quiet :long "--quiet" :short "-q"
    :doc "Print nothing on stdout; use the exit code"}
   {:id :help :long "--help" :short "-h" :doc "Show help"}
   {:id :version :long "--version" :short "-V" :doc "Show the version"}
   {:id :ratio :long "--ratio" :short "-r" :arg "RATIO"
    :parse #(parse/parse-fraction "--ratio" %)
    :doc "Share of the second color, from 0 to 1 (default 0.5)"}
   {:id :mode :long "--mode" :short "-m" :arg "MODE"
    :doc "Mode"}
   {:id :count :long "--count" :short "-n" :arg "N"
    :parse #(parse/parse-integer "--count" 1 nil %)
    :doc "How many colors to generate"}
   {:id :as :long "--as" :arg "FORMAT"
    :parse (one-of "--as" input-formats)
    :doc "Require the input to be in this format"}])

(def ^:private global-options
  [:to :format :json :edn :precision :significant :color :no-color :quiet
   :help :version])

(def ^:private option-by-id
  (into {} (map (juxt :id identity)) option-specs))

(def ^:private option-by-flag
  (into {} (mapcat (fn [spec]
                     (for [flag [(:long spec) (:short spec)] :when flag]
                       [flag spec])))
        option-specs))

;; =============================================================================
;; Suggestions
;; =============================================================================

(defn- edit-distance [a b]
  (let [step (fn [previous ca]
               (reductions (fn [left [diagonal up cb]]
                             (min (inc left) (inc up)
                                  (cond-> diagonal (not= ca cb) inc)))
                           (inc (first previous))
                           (map vector previous (rest previous) b)))]
    (last (reduce step (range (inc (count b))) a))))

(defn- did-you-mean
  "A hint naming the candidate closest to `word`, if one is close."
  [word candidates]
  (let [[distance closest] (->> candidates
                                (map (juxt #(edit-distance word %) identity))
                                sort
                                first)]
    (when (and closest (<= distance 2))
      (str "Did you mean " closest "?"))))

;; =============================================================================
;; Commands and help
;; =============================================================================

(def ^:private command-by-name
  (into {} (mapcat (fn [command]
                     (for [n (cons (:name command) (:aliases command))]
                       [n command])))
        commands/commands))

(defn- command-options
  "The command's own option specs. An entry may be [id doc] to describe the
   option in the command's terms."
  [command]
  (map (fn [entry]
         (if (vector? entry)
           (assoc (option-by-id (first entry)) :doc (second entry))
           (option-by-id entry)))
       (:opts command)))

(defn- usage-line [command]
  (str "Usage: " program " " (:name command)
       (str/join (map (fn [{:keys [label rest? optional?]}]
                        (cond
                          rest? (str " " label "...")
                          optional? (str " [" label "]")
                          :else (str " " label)))
                      (:params command)))
       " [options]"))

(defn- columns
  "Format rows of [left right] as two aligned, indented columns."
  [rows]
  (let [width (apply max 0 (map (comp count first) rows))]
    (str/join (map (fn [[left right]]
                     (str "  " left
                          (apply str (repeat (- width (count left)) " "))
                          "  " right "\n"))
                   rows))))

(defn- wrap
  "Join `words` with `separator` into lines of at most 78 characters,
   each starting with `indent`."
  [words separator indent]
  (let [fits? (fn [line word]
                (<= (+ (count indent) (count line) (count separator)
                       (count word))
                    78))]
    (->> (reduce (fn [lines word]
                   (if (and (seq lines) (fits? (peek lines) word))
                     (conj (pop lines) (str (peek lines) separator word))
                     (conj lines word)))
                 []
                 (map str words))
         (map #(str indent % "\n"))
         str/join)))

(defn- option-rows [specs]
  (map (fn [{:keys [long short arg doc]}]
         [(str (if short (str short ", ") "    ") long
               (when arg (str " " arg)))
          doc])
       specs))

(def ^:private global-option-help
  (columns (option-rows (map option-by-id global-options))))

(defn- top-help []
  (str program " " version
       " - convert, adjust and check colors from the terminal\n\n"
       "Usage: " program " <command> [arguments] [options]\n\n"
       (str/join
        (for [[group title] commands/groups]
          (str title ":\n"
               (columns (for [command commands/commands
                              :when (= group (:group command))]
                          [(:name command) (:summary command)]))
               "\n")))
       "Options:\n" global-option-help "\n"
       "Formats (--to):\n" (wrap (map name out/color-formats) ", " "  ")
       "  Without --to, colors print as hex, or as rgba() when translucent."
       "\n\n"
       "Colors:\n"
       (str/join (map #(str "  " % "\n") parse/color-forms))
       "  Quote colors: # and () mean something to the shell.\n"
       "  Pass - in place of a color to read colors from stdin, one per"
       " line.\n\n"
       "Exit codes:\n"
       (columns [["0" "success"]
                 ["1" "a check failed, such as contrast below --level"]
                 ["2" "invalid input or usage"]])
       "\n"
       "Run '" program " help <command>' for arguments, options and"
       " examples.\n"))

(defn- command-help [command]
  (let [own (command-options command)]
    (str (usage-line command) "\n\n"
         (:summary command) ".\n"
         (when-let [doc (:doc command)] (str "\n" doc "\n"))
         (when (seq (:aliases command))
           (str "\nAlso available as: " (str/join ", " (:aliases command))
                "\n"))
         (when (seq own)
           (str "\nOptions:\n" (columns (option-rows own))))
         "\nGlobal options:\n" global-option-help
         (when-let [examples (seq (:examples command))]
           (str "\nExamples:\n"
                (str/join (map #(str "  " program " " % "\n") examples))))
         (when-let [api (seq (:api command))]
           (str "\nLibrary functions:\n" (wrap api " " "  "))))))

(defn- unknown-command [word]
  (parse/usage-error
   (str "unknown command " (pr-str word))
   (str (some-> (did-you-mean word (keys command-by-name)) (str "\n"))
        "Run '" program " help' for the list of commands.")))

(defn- find-command [word]
  (or (command-by-name word)
      (throw (unknown-command word))))

;; =============================================================================
;; Argument parsing
;; =============================================================================

(defn- option-token?
  "True for -x and --name. A lone - means stdin and -30 is a number."
  [token]
  (and (str/starts-with? token "-")
       (not= "-" token)
       (not (re-matches #"-\.?\d.*" token))))

(defn- add-option [opts {:keys [id parse multi?]} raw]
  (let [value ((or parse identity) raw)]
    (if multi?
      (update opts id (fnil into []) value)
      (assoc opts id value))))

(defn- parse-argv
  "Split argv into options and positional arguments. Options may come
   anywhere; -- ends them."
  [argv]
  (loop [tokens (seq argv)
         opts {}
         positional []]
    (let [[token & more] tokens]
      (cond
        (nil? token)
        {:opts opts :args positional}

        (= "--" token)
        {:opts opts :args (into positional more)}

        (option-token? token)
        (let [[flag inline] (if (str/starts-with? token "--")
                              (str/split token #"=" 2)
                              [token])
              spec (option-by-flag flag)]
          (cond
            (nil? spec)
            (throw (parse/usage-error
                    (str "unknown option " flag)
                    (did-you-mean flag (keys option-by-flag))))

            (and (nil? (:arg spec)) inline)
            (throw (parse/usage-error (str flag " takes no value")))

            (nil? (:arg spec))
            (recur more (assoc opts (:id spec) true) positional)

            (some? inline)
            (recur more (add-option opts spec inline) positional)

            (empty? more)
            (throw (parse/usage-error
                    (str flag " needs a value: " flag " " (:arg spec))))

            :else
            (recur (rest more) (add-option opts spec (first more))
                   positional)))

        :else
        (recur more opts (conj positional token))))))

(defn- check-options
  "Reject options the command does not take."
  [command opts]
  (let [allowed (into (set global-options)
                      (map :id)
                      (command-options command))]
    (doseq [id (keys opts)
            :when (not (allowed id))]
      (throw (parse/usage-error
              (str (:long (option-by-id id)) " is not an option of '"
                   (:name command) "'")
              (str "Run '" program " help " (:name command)
                   "' for its options."))))))

;; =============================================================================
;; Binding arguments
;; =============================================================================

(defn- stdin-entries
  "The non-blank lines of stdin, each tagged with its line number."
  [stdin]
  (->> (str/split-lines (stdin))
       (keep-indexed (fn [i line]
                       (when-not (str/blank? line)
                         {:text (str/trim line)
                          :source (str "stdin line " (inc i))})))
       vec))

(defn- expand-stdin
  "Replace a - argument with the lines of stdin. In a variadic position
   the lines join the arguments of a single run. In a fixed position the
   command runs once per line. Returns the runs with how stdin was used."
  [command args stdin]
  (let [fixed-count (count (remove :rest? (:params command)))
        entries (mapv (fn [arg] {:text arg}) args)
        dashes (keep-indexed (fn [i arg] (when (= "-" arg) i)) args)]
    (cond
      (empty? dashes)
      {:runs [entries]}

      (next dashes)
      (throw (parse/usage-error "only one argument can be - (stdin)"))

      :else
      (let [i (first dashes)
            lines (stdin-entries stdin)
            before (subvec entries 0 i)
            after (subvec entries (inc i))]
        (if (< i fixed-count)
          {:stdin :per-line
           :runs (mapv #(into (conj before %) after) lines)}
          {:stdin :inline
           :runs [(into (into before lines) after)]})))))

(defn- parse-entry
  "Parse one argument. Errors in stdin input say which line."
  [param {:keys [text source]}]
  (try
    ((:parse param) text)
    (catch #?(:clj Exception :cljs :default) e
      (if (and source (parse/usage-error? e))
        (throw (parse/usage-error (str source ": " (ex-message e))
                                  (:hint (ex-data e))))
        (throw e)))))

(defn- bind
  "Match the entries of one run to the command's parameters. Returns the
   fixed arguments as a map and the variadic ones as a vector."
  [command entries stdin-mode]
  (let [fixed (remove :rest? (:params command))
        rest-param (first (filter :rest? (:params command)))
        required (count (remove :optional? fixed))
        extra (drop (count fixed) entries)
        usage (usage-line command)]
    (when (< (count entries) required)
      (throw (parse/usage-error
              (str "missing " (:label (nth fixed (count entries))))
              usage)))
    (when (and (not rest-param) (seq extra))
      (throw (parse/usage-error
              (str "unexpected argument " (pr-str (:text (first extra))))
              usage)))
    (when (and rest-param
               (< (count extra) (:min rest-param))
               (not (and (:each? command) (= :inline stdin-mode))))
      (throw (parse/usage-error
              (if (= 1 (:min rest-param))
                (str "missing " (:label rest-param))
                (str "expected at least " (:min rest-param) " "
                     (:label rest-param) " arguments"))
              usage)))
    [(into {} (map (fn [param entry]
                     [(:key param) (parse-entry param entry)])
                   fixed entries))
     (when rest-param
       (mapv #(parse-entry rest-param %) extra))]))

(defn- run-once
  "Run the command on one set of arguments. A command marked :each? runs
   once per variadic argument; a single argument gives a single result."
  [command entries opts stdin-mode]
  (let [[fixed more] (bind command entries stdin-mode)
        rest-key (:key (first (filter :rest? (:params command))))
        handler (:run command)]
    (cond
      (and rest-key (:each? command))
      (let [results (mapv #(handler (assoc fixed rest-key %) opts) more)]
        (if (and (= 1 (count results)) (not= :inline stdin-mode))
          (first results)
          results))

      rest-key (handler (assoc fixed rest-key more) opts)
      :else (handler fixed opts))))

(defn- unwrap
  "Separate results from their verdicts. Returns [value all-passed?]."
  [result]
  (cond
    (commands/verdict? result)
    [(:value result) (:pass? result)]

    (vector? result)
    (let [unwrapped (map unwrap result)]
      [(mapv first unwrapped) (every? second unwrapped)])

    :else [result true]))

;; =============================================================================
;; Output
;; =============================================================================

(defn- output-format [opts]
  (cond
    (:json opts) :json
    (:edn opts) :edn
    :else (:format opts :text)))

(defn- ansi-mode
  "How to draw swatches: :truecolor, :256 or nil for none. NO_COLOR and
   FORCE_COLOR decide when --color is auto; otherwise swatches show only
   on a terminal."
  [opts {:keys [tty? env]}]
  (let [setting (if (:no-color opts) :never (:color opts :auto))
        forced (get env "FORCE_COLOR")
        on? (case setting
              :always true
              :never false
              (cond
                (seq (get env "NO_COLOR")) false
                (and (seq forced) (not= "0" forced)) true
                :else (and tty? (not= "dumb" (get env "TERM")))))]
    (when on?
      (if (#{"truecolor" "24bit"} (get env "COLORTERM"))
        :truecolor
        :256))))

(defn- render [command value opts process]
  (let [fmt (output-format opts)
        resolved (out/resolve value (assoc opts :format fmt))]
    (case fmt
      :json (out/json resolved)
      :edn (out/edn resolved)
      :css (if (:css? command)
             (out/css resolved (:selector opts ":root"))
             (throw (parse/usage-error
                     (str "--format css is not available for '"
                          (:name command) "'"))))
      (out/text resolved (ansi-mode opts process)))))

;; =============================================================================
;; Entry point
;; =============================================================================

(defn- run-command [command args opts process]
  (check-options command opts)
  (let [warnings (volatile! [])
        {:keys [runs stdin]} (expand-stdin command args (:stdin process))
        results (binding [commands/*warn* #(vswap! warnings conj %)]
                  (mapv #(run-once command % opts stdin) runs))
        [value passed?] (unwrap (if (= :per-line stdin)
                                  results
                                  (first results)))]
    {:exit (if passed? exit-ok exit-check-failed)
     :out (if (:quiet opts) "" (render command value opts process))
     :err (str/join (map #(str program ": warning: " % "\n") @warnings))}))

(defn- dispatch [argv process]
  (let [{:keys [opts args]} (parse-argv argv)
        [word & more] args]
    (cond
      (:version opts)
      {:exit exit-ok :out (str program " " version "\n") :err ""}

      (or (nil? word) (and (= "help" word) (empty? more)))
      {:exit exit-ok :out (top-help) :err ""}

      (= "help" word)
      {:exit exit-ok :out (command-help (find-command (first more))) :err ""}

      (:help opts)
      {:exit exit-ok :out (command-help (find-command word)) :err ""}

      :else
      (run-command (find-command word) (vec more) opts process))))

(defn run
  "Run the CLI. `argv` is the argument vector. `process` describes the
   outside world:

   - :stdin, a function that returns all of standard input as a string
   - :tty?, whether stdout is a terminal
   - :env, a map of environment variables

   Returns {:exit code :out text :err text}. Usage errors and bad input
   exit with 2 and a message on stderr, never a stack trace."
  [argv process]
  (try
    (dispatch argv process)
    (catch #?(:clj Throwable :cljs :default) e
      {:exit exit-usage
       :out ""
       :err (str program ": "
                 (if (parse/usage-error? e)
                   (str (ex-message e)
                        (some->> (:hint (ex-data e)) (str "\n")))
                   (str "unexpected error: " (or (ex-message e) (str e))))
                 "\n")})))
