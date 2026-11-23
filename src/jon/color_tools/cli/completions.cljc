(ns jon.color-tools.cli.completions
  "Shell completions generated from command-specific option metadata."
  (:require [clojure.string :as str]))

(def shells [:bash :zsh :fish])

(defn- words [xs]
  (str/join " " (map name xs)))

(defn- flags [{:keys [long short]}]
  (cond-> [long] short (conj short)))

(defn- value-options [options]
  (group-by #(if (:choices %) :choices :free) (filter :arg options)))

(defn- contexts [{:keys [command-options global-options]}]
  (->> (assoc command-options "" global-options)
       (group-by (fn [[_ options]]
                   (mapv #(select-keys % [:long :short :arg :choices :doc])
                         options)))
       vals
       (map (fn [entries]
              [(sort (map first entries)) (second (first entries))]))
       (sort-by (comp first first))))

(defn- pattern [names]
  (str/join "|" (map #(if (empty? %) "''" %) names)))

(defn- command-scan [shell {:keys [commands options]}]
  (let [bash? (= :bash shell)
        index (if bash? "COMP_CWORD" "CURRENT")
        array (if bash? "COMP_WORDS" "words")]
    (str "  local command='' command_index=0 i word skip=0\n"
         "  for ((i=" (if bash? 1 2) "; i<" index "; i++)); do\n"
         "    word=\"${" array "[i]}\"\n"
         "    if (( skip )); then skip=0; continue; fi\n"
         "    case \"$word\" in\n"
         "      --*=*) continue ;;\n"
         "      " (str/join "|" (mapcat flags (filter :arg options)))
         ") skip=1 ;;\n"
         "      " (str/join "|" (map :name commands))
         ") command=\"$word\"; command_index=$i; break ;;\n"
         "    esac\n"
         "  done\n")))

(defn- bash-reply [xs]
  (str "COMPREPLY=($(compgen -W \"" (words xs) "\" -- \"$cur\"))"))

(defn- option-case [shell options]
  (let [{:keys [choices free]} (value-options options)
        reply (if (= :bash shell)
                bash-reply
                #(str "compadd -- " (words %)))]
    (str "      case \"$prev\" in\n"
         (str/join
          (for [option choices]
            (str "        " (str/join "|" (flags option)) ") "
                 (reply (:choices option)) "; return ;;\n")))
         (when (seq free)
           (str "        " (str/join "|" (mapcat flags free))
                ") return ;;\n"))
         "      esac\n"
         "      if [[ \"$cur\" == -* ]]; then\n"
         "        " (reply (map :long options)) "; return\n"
         "      fi\n")))

(defn- option-contexts [shell spec]
  (str "  case \"$command\" in\n"
       (str/join
        (for [[names options] (contexts spec)]
          (str "    " (pattern names) ")\n"
               (option-case shell options)
               "      ;;\n")))
       "  esac\n"))

(defn- bash [{:keys [commands first-arguments] :as spec}]
  (str "# bash completion for color-tools\n"
       "_color_tools() {\n"
       "  local cur=\"${COMP_WORDS[COMP_CWORD]}\"\n"
       "  local prev=\"${COMP_WORDS[COMP_CWORD-1]}\"\n"
       "  COMPREPLY=()\n"
       (command-scan :bash spec)
       (option-contexts :bash spec)
       "  if [ -z \"$command\" ]; then\n"
       "    " (bash-reply (map :name commands)) "\n"
       "  elif (( COMP_CWORD == command_index+1 )); then\n"
       "    case \"$command\" in\n"
       (str/join
        (for [[command xs] first-arguments]
          (str "      " command ") " (bash-reply xs) " ;;\n")))
       "    esac\n"
       "  fi\n"
       "}\n"
       "complete -F _color_tools color-tools\n"))

(defn- zsh-quote [s]
  (-> s
      (str/replace "'" "'\\''")
      (str/replace ":" "\\:")))

(defn- zsh [{:keys [commands first-arguments] :as spec}]
  (str "#compdef color-tools\n# zsh completion for color-tools\n"
       "_color_tools() {\n"
       "  local cur=\"${words[CURRENT]}\" prev=\"${words[CURRENT-1]}\"\n"
       "  local -a commands\n"
       "  commands=(\n"
       (str/join (for [{:keys [name summary]} commands]
                   (str "    '" name ":" (zsh-quote summary) "'\n")))
       "  )\n"
       (command-scan :zsh spec)
       (option-contexts :zsh spec)
       "  if [ -z \"$command\" ]; then\n"
       "    _describe 'command' commands\n"
       "  elif (( CURRENT == command_index+1 )); then\n"
       "    case \"$command\" in\n"
       (str/join
        (for [[command xs] first-arguments]
          (str "      " command ") compadd -- " (words xs) " ;;\n")))
       "    esac\n"
       "  fi\n"
       "}\n"
       "if [ \"${funcstack[1]}\" = _color_tools ]; then\n"
       "  _color_tools \"$@\"\n"
       "else\n"
       "  compdef _color_tools color-tools\n"
       "fi\n"))

(defn- fish-quote [s]
  (str "'" (str/replace s "'" "\\'") "'"))

(defn- fish-command-scan [{:keys [commands options]}]
  (str "function __color_tools_command\n"
       "  set -l skip 0\n"
       "  for word in (commandline -opc)[2..-1]\n"
       "    if test $skip -eq 1; set skip 0; continue; end\n"
       "    switch $word\n"
       "      case '--*=*'; continue\n"
       "      case " (str/join " " (map fish-quote
                                        (mapcat flags (filter :arg options))))
       "; set skip 1\n"
       "      case " (words (map :name commands)) "\n"
       "        printf '%s\\n' \"$word\"; return 0\n"
       "    end\n"
       "  end\n"
       "  return 1\n"
       "end\n"))

(defn- fish [{:keys [commands first-arguments] :as spec}]
  (str "# fish completion for color-tools\n"
       (fish-command-scan spec)
       "complete -c color-tools -f\n"
       (str/join
        (for [{:keys [name summary]} commands]
          (str "complete -c color-tools -n "
               (fish-quote "not __color_tools_command >/dev/null")
               " -a " name " -d " (fish-quote summary) "\n")))
       (str/join
        (for [[command xs] first-arguments]
          (str "complete -c color-tools -n "
               (fish-quote (str "contains -- (__color_tools_command) "
                                command))
               " -a " (fish-quote (words xs)) "\n")))
       (str/join
        (for [[names options] (contexts spec)
              :let [condition (if (= [""] names)
                                "not __color_tools_command >/dev/null"
                                (str "contains -- (__color_tools_command) "
                                     (words names)))]
              {:keys [long short arg choices doc]} options]
          (str "complete -c color-tools -n " (fish-quote condition)
               " -l " (subs long 2)
               (when short (str " -s " (subs short 1)))
               (when arg " -x")
               (when choices (str " -a " (fish-quote (words choices))))
               " -d " (fish-quote doc) "\n")))))

(defn script
  "Generate a completion script using global and per-command options."
  [shell spec]
  (case shell
    :bash (bash spec)
    :zsh (zsh spec)
    :fish (fish spec)))
