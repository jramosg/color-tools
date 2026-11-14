(ns jon.color-tools.cli.completions
  "Shell completion scripts for bash, zsh and fish, generated from the
   command and option tables so they can't fall behind the CLI."
  (:require
   [clojure.string :as str]))

(def shells [:bash :zsh :fish])

(defn- words [xs]
  (str/join " " (map name xs)))

(defn- flags [{:keys [long short]}]
  (cond-> [long] short (conj short)))

(defn- value-options
  "Options that take a value, split into those with a fixed set of
   choices and those that take free text."
  [options]
  (group-by #(if (:choices %) :choices :free) (filter :arg options)))

;; =============================================================================
;; bash
;; =============================================================================

(defn- bash [{:keys [commands options first-arguments]}]
  (let [{:keys [choices free]} (value-options options)
        reply (fn [xs]
                (str "COMPREPLY=($(compgen -W \"" (words xs)
                     "\" -- \"$cur\"))"))]
    (str
     "# bash completion for color-tools\n"
     "_color_tools() {\n"
     "  local cur prev\n"
     "  cur=\"${COMP_WORDS[COMP_CWORD]}\"\n"
     "  prev=\"${COMP_WORDS[COMP_CWORD-1]}\"\n"
     "  COMPREPLY=()\n"
     "  case \"$prev\" in\n"
     (str/join
      (for [option choices]
        (str "    " (str/join "|" (flags option)) ") "
             (reply (:choices option)) "; return ;;\n")))
     "    " (str/join "|" (mapcat flags free)) ") return ;;\n"
     "  esac\n"
     "  if [[ \"$cur\" == -* ]]; then\n"
     "    " (reply (map :long options)) "\n"
     "  elif [ \"$COMP_CWORD\" -eq 1 ]; then\n"
     "    " (reply (map :name commands)) "\n"
     "  elif [ \"$COMP_CWORD\" -eq 2 ]; then\n"
     "    case \"${COMP_WORDS[1]}\" in\n"
     (str/join
      (for [[command xs] first-arguments]
        (str "      " command ") " (reply xs) " ;;\n")))
     "    esac\n"
     "  fi\n"
     "}\n"
     "complete -F _color_tools color-tools\n")))

;; =============================================================================
;; zsh
;; =============================================================================

(defn- zsh-quote
  "Quote `s` for a zsh single-quoted string in a _describe list, where a
   colon separates the word from its description."
  [s]
  (-> s
      (str/replace "'" "'\\''")
      (str/replace ":" "\\:")))

(defn- zsh [{:keys [commands options first-arguments]}]
  (let [{:keys [choices free]} (value-options options)]
    (str
     "#compdef color-tools\n"
     "# zsh completion for color-tools\n"
     "_color_tools() {\n"
     "  local cur=\"${words[CURRENT]}\" prev=\"${words[CURRENT-1]}\"\n"
     "  local -a commands\n"
     "  commands=(\n"
     (str/join
      (for [{:keys [name summary]} commands]
        (str "    '" name ":" (zsh-quote summary) "'\n")))
     "  )\n"
     "  case \"$prev\" in\n"
     (str/join
      (for [option choices]
        (str "    " (str/join "|" (flags option)) ") compadd "
             (words (:choices option)) "; return ;;\n")))
     "    " (str/join "|" (mapcat flags free)) ") return ;;\n"
     "  esac\n"
     "  if [[ \"$cur\" == -* ]]; then\n"
     "    compadd -- " (words (map :long options)) "\n"
     "  elif (( CURRENT == 2 )); then\n"
     "    _describe 'command' commands\n"
     "  elif (( CURRENT == 3 )); then\n"
     "    case \"${words[2]}\" in\n"
     (str/join
      (for [[command xs] first-arguments]
        (str "      " command ") compadd " (words xs) " ;;\n")))
     "    esac\n"
     "  fi\n"
     "}\n"
     "if [ \"${funcstack[1]}\" = _color_tools ]; then\n"
     "  _color_tools \"$@\"\n"
     "else\n"
     "  compdef _color_tools color-tools\n"
     "fi\n")))

;; =============================================================================
;; fish
;; =============================================================================

(defn- fish-quote [s]
  (str "'" (str/replace s "'" "\\'") "'"))

(defn- fish [{:keys [commands options first-arguments]}]
  (str
   "# fish completion for color-tools\n"
   "complete -c color-tools -f\n"
   (str/join
    (for [{:keys [name summary]} commands]
      (str "complete -c color-tools -n __fish_use_subcommand -a " name
           " -d " (fish-quote summary) "\n")))
   (str/join
    (for [[command xs] first-arguments]
      (str "complete -c color-tools -n '__fish_seen_subcommand_from "
           command "' -a " (fish-quote (words xs)) "\n")))
   (str/join
    (for [{:keys [long short arg choices doc]} options]
      (str "complete -c color-tools -l " (subs long 2)
           (when short (str " -s " (subs short 1)))
           (when arg " -x")
           (when choices (str " -a " (fish-quote (words choices))))
           " -d " (fish-quote doc) "\n")))))

(defn script
  "The completion script for `shell`, one of :bash, :zsh or :fish.

   `spec` holds:
   - :commands, maps with :name and :summary
   - :options, maps with :long, :short, :arg, :choices and :doc
   - :first-arguments, a map from a command name to the words that can
     follow it, such as the properties of `is`"
  [shell spec]
  (case shell
    :bash (bash spec)
    :zsh (zsh spec)
    :fish (fish spec)))
