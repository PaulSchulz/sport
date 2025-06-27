(ns sporting-fixtures.core
  (:gen-class)
  (:require
   [clojure.main]
   [clj-time.core   :as t]
   [clj-time.format :as f]
   [clj-yaml.core :as yaml]
   [clojure.string :as str])
  (:use [clojure.java.shell :only [sh]]
        [clojure.pprint :only [pprint]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn reload []
  (do
    (println "Reloading core")
    (use 'sporting-fixtures.core :reload-all)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn help []
  (println
   (str/join
    "\n"
    [";;;;;;;;;;;;;;;;;;;"
     ";; Macros"
     ";;;;;;;;;;;;;;;;;;;"
     "(setup)  ;; Loads setup, process, process-results and reports modules (see next)"
     "(round-report-org rnd-number)"
     ""
     ";; Event data (CSV) can be found at"
     ";;  https://fixturedownload.com/"
     ""
     ";; To generate a report from the CLI you can also do:"
     ";;   echo \"(round-report-org 16)\" | lein repl"
     ])))

(defmacro setup []
  (require ['sporting-fixtures.setup :as 's])
  (require ['sporting-fixtures.process :as 'p])
  (require ['sporting-fixtures.process-results :as 'pr])
  (require ['sporting-fixtures.reports :as 'r])
  )

(defmacro status []
  (if (resolve 'sport)
    (println "sport is:" (deref (resolve 'sport)))
    (println "sport is not defined"))
  (if (resolve 'data)
    (do
      (println "data is loaded")
      (println (format "  event: %s"
                       (:event (deref(resolve 'data))))))
    (println "data is not defined"))
  )

(defn event-id-to-filename [id]
  (str "data/" id ".yml"))

(defn get-events []
  (str/split (:out (sh "bash" "-c" "ls data/20*.yml")) #"\n"))

(defn get-events-new []
  (str/split (:out (sh "bash" "-c" "ls data/*/data.clj")) #"\n"))

(defn list-events []
  (println "Old Style")
  (println
   (str/join "\n"
             (map (fn [event] (format "* %s" event))
                  (get-events))
             ))

  (println "New Style")
  (println
   (str/join "\n")
   (map #(format "* %s" %) (get-events-new))
   )
  )

(defn read-event [event]
  (yaml/parse-string (slurp event)))

(defn display-event [event]
  (pprint (read-event event)))

(defn event-details [fmt event]
  (let [data (read-event event)]
    (format fmt
            (str (:title data) " / " (:location data))
            (:from (:date data))
            (:to   (:date data))
            (:code data)
            (:version data))))

(defn events-table []
  (let [fmt "  #  %-56s  %-12s %-12s %16s %-6s"
        width 110]
    (println
     (str/join "\n"
               [(str/join "" (repeat width "-"))
                (format fmt "Name / Location" "From" "To" "Code" "Ver")
                (str/join "" (repeat width "-"))
                (str/join "\n" (map #(event-details fmt %) (get-events)))]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2019 FIFA Womens World Cup
;; fifa-2019
(def  event-name  "2019-fra-fifa-women-worldcup")
(defn get-event   [] (read-event (event-id-to-filename event-name)))
(defn event-games [] (:games (get-event)))

;; Consider changing to 'clojure-java-time'
;;   See: https://github.com/dm3/clojure.java-time
;; Time parsing an manipulation
;; Formatters can be listed with: (f/show-formatters)
(defn localtime [datetime]
  (let [formatter (f/formatter :rfc822)
        my-formatter (f/with-zone
                       (f/formatter "EE dd hh:mm aa")
                       (t/default-time-zone))]
    (f/unparse my-formatter
               (f/parse formatter datetime))))

;; Create results table
(defn event-games-table-header []
  (str
   (format " %2s | %-15s | %-10s | %-9s    | %s"
           "Id"
           "Time"
           "Teams"
           "Score"
           "Result")
   "\n"
   "--------------------------------------------------------------------------"
   "\n"))

(defn event-games-table [event]
  (let [games (:games event)]
    (str
     (event-games-table-header)
     (str/join
      "\n"
      (map-indexed
       (fn [i x]
         (str
          (if (= i 36) "----- Round of 16 ---------------------------------\n" "")
          (if (= i 44) "----- Quarter Finals ------------------------------\n" "")
          (if (= i 48) "----- Semi Finals ---------------------------------\n" "")
          (if (= i 50) "----- Playoff -------------------------------------\n" "")
          (if (= i 51) "----- Final ---------------------------------------\n" "")
          (format " %2d | %s | %-10s |  %4s %4s | %s"
                  (:id x)
                  (localtime (:time x))
                  (str (if (nth (:teams x) 0)
                         (str/upper-case (nth (:teams x) 0))
                         "---")
                       " vs "
                       (if (nth (:teams x) 1)
                         (str/upper-case (nth (:teams x) 1))
                         "---"))
                  (format "%2s%-3s"
                          (if (nth (:goals x) 0)
                            (nth (:goals x) 0)
                            "-")
                          (if (nth (:penalties x) 0)
                            (str "(" (nth (:penalties x) 0) ")")
                            "   "))
                  (format "%2s%-3s"
                          (if (nth (:goals x) 1)
                            (nth (:goals x) 1)
                            "-")
                          (if (nth (:penalties x) 1)
                            (str "(" (nth (:penalties x) 1) ")")
                            "   "))
                  (if (:result x) (:result x) ""))))
       games))
     "\n"
     (str (str/join "" (repeat 78 "-")) "\n"))))

;; goals - vector containing number of goals for each team.
;; No need to consider penalties and points are not awarded for matches
;; where a penalty shootout is required (in knockout rounds).
(defn calculate-points [goals]
  (let [a-goals (nth goals 0)
        b-goals (nth goals 1)]

    (cond
      (not a-goals)       nil
      (not b-goals)       nil
      (> a-goals b-goals) [3 0]
      (< a-goals b-goals) [0 3]
      :else               [1 1])))

;; goals - vector containing number of goals for each team.
;; No need to consider penalties and points are not awarded for matches
;; where a penalty shootout is required (in knockout rounds).
(defn calculate-stats [goals]
  (let [a-goals (nth goals 0)
        b-goals (nth goals 1)]

    (cond
      (not a-goals)       nil
      (not b-goals)       nil
      (> a-goals b-goals) [[3 1 1 0 0 a-goals b-goals (- a-goals b-goals)]
                           [0 1 0 1 0 b-goals a-goals (- b-goals a-goals)]]
      (< a-goals b-goals) [[0 1 0 1 0 a-goals b-goals (- a-goals b-goals)]
                           [3 1 1 0 0 b-goals a-goals (- b-goals a-goals)]]
      :else               [[1 1 0 0 1 a-goals b-goals (- a-goals b-goals)]
                           [1 1 0 0 1 b-goals a-goals (- b-goals a-goals)]])))

;; goals     - vector containing number of goals
;;             scored by each team.
;; penalties - vector containing number of penalties
;;             scored by each team. Can be 'nil' if the
;;             there is a goal difference.
(defn calculate-winner [goals penalties]
  (let [a-goals     (nth goals 0)
        b-goals     (nth goals 1)
        ;; Check in case penalties is 'nil'
        sane-penalties (if penalties penalties [0 0])
        a-penalties (nth sane-penalties 0)
        b-penalties (nth sane-penalties 1)]

    (cond
      (not a-goals)               nil
      (not b-goals)               nil
      (> a-goals b-goals)         [1 0]
      (< a-goals b-goals)         [0 1]
      ;; Assert: Number of goals scors is the same for each team.
      (not a-penalties)           [0 0]
      (not b-penalties)           [0 0]
      (> a-penalties b-penalties) [1 0]
      (< a-penalties b-penalties) [0 1]
      :else                       [0 0])))

;; Calculate the statistics from the games of an event
(defn calculate-statistics [games]
  (map-indexed
   (fn [i x]
     (let [team-a (nth (:teams x) 0)
           team-b (nth (:teams x) 1)
           stats  (calculate-stats  (:goals x))]
       (conj {}
             (if team-a
               {(keyword team-a) (nth stats 0)}
               nil)
             (if team-b
               {(keyword team-b) (nth stats 1)}
               nil))))

   games))

(defn reduce-statistics [stats]
  (reduce (fn [val coll]
            (reduce conj val coll))
          [] stats))

;; statistics - Map of team statistics
;; update     - Array with 'team id' and 'statistics update'
(defn update-statistics [statistics update]
  (let [team         (nth update 0)
        stats-update (if (nth update 1)
                       (nth update 1)
                       [0 0 0 0 0 0 0 0])
        stats-old    (if (team statistics)
                       (team statistics)
                       [0 0 0 0 0 0 0 0])]

    (conj statistics {team (map + stats-old stats-update)})))

(defn event-statistics [games]
  (reduce update-statistics {}
          (reduce-statistics
           (calculate-statistics games))))

(defn stats-separator []
  "------+----+---------+------------+---------------------------\n")

(defn stats-header []
  (str
   "      |    | Games   | Goals      | Group     Knockout Result\n"
   " Team | Pt | P/W/L/D | Fr/Ag/Diff | Result    Qual 16  8  4  2\n"
   (stats-separator)))

(defn stats-string [team stats results]
  (format " %3s  | %2d | %1d %1d %1d %1d | %2d %2d %4d | %s"
          (str/upper-case (name team))
          (nth stats 0)
          (nth stats 1)
          (nth stats 2)
          (nth stats 3)
          (nth stats 4)
          (nth stats 5)
          (nth stats 6)
          (nth stats 7)
          (format "%9s   %s%s%s%s%s"
                  (if (:group-stage results)
                    (:group-stage results)
                    "")
                  (if (:group results)
                    (if (= (:group results) "qualified")
                      " *-"
                      " +-")
                    " x ")
                  (if (:qual16 results)
                    (if (= (:qual16 results) "win")
                      "-+-"
                      "-x ")
                    " . ")
                  (if (:quarter results)
                    (if (= (:quarter results) "win")
                      "-+-"
                      "-x ")
                    " . ")
                  (if (:semi results)
                    (if (= (:semi results) "win")
                      "-+-"
                      "-x ")
                    " . ")
                  (if (:final results)
                    (cond
                      (= (:final results) "champion")  "-W "
                      (= (:final results) "runnerup") "-o "
                      (= (:final results) "third")    "-3 "
                      (= (:final results) "fourth")   "-4 "
                      :else " . ")
                    " . "))))

(defn event-stats-table [event]
  (let [statistics (event-statistics (:games event))
        results    (:results event)]
    (str
     (stats-header)
     (str/join
      "\n"
      (map
       (fn [[k v]] (stats-string k v (k results)))
       (sort (fn [el1 el2]
               (if (> (nth (nth el1 1) 0)
                      (nth (nth el2 1) 0))
                 true
                 false))
             (map (fn [x] x) statistics)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Results chart

(defn results-separator []
  "------+-------------------------------------+--------+----+--+-+-\n")

(defn results-header []
  (str
   "      | Game     111111111122222222223333333|33344444|4444|45|5|5\n"
   " Team | 123456789012345678901234567890123456|78901234|5678|90|1|2\n"
   (results-separator)))

(defn results-string [event team stats results]
  (format "  %3s | %s"
          (str/upper-case (name team))
          (str/join
           ""
           (map-indexed
            (fn [i x]
              (str
               (cond
                 (= i 36) "|"
                 (= i 44) "|"
                 (= i 48) "|"
                 (= i 50) "|"
                 (= i 51) "|")
               (cond
                 (= (nth (:teams x) 0) (name team))
                 (cond
                   (= (calculate-winner (:goals x) (:penalties x)) [1 0]) "W"
                   (= (calculate-winner (:goals x) (:penalties x)) [0 1]) "L"
                   (= (calculate-winner (:goals x) (:penalties x)) [0 0]) "d"
                   :else "o")
                 (= (nth (:teams x) 1) (name team))
                 (cond
                   (= (calculate-winner (:goals x) (:penalties x)) [0 1]) "W"
                   (= (calculate-winner (:goals x) (:penalties x)) [1 0]) "L"
                   (= (calculate-winner (:goals x) (:penalties x)) [0 0]) "d"
                   :else "o")
                 (not (:goals x))      " "
                 (:last results)       (if (< i (:last results))
                                         "-"
                                         " ")
                 ;;(< i (:last results)) " "
                 :else
                 (cond
                   (<= i 35) "-"
                   (and (<= 36 i 43) (:group results)) "-"
                   (and (<= 44 i 47) (= (:qual16 results) "win")) "-"
                   (and (<= 48 i 49) (= (:quarter results) "win")) "-"
                   :else " "))))
            (:games event)))))

(defn event-results-table [event]
  (let [games      (:games event)
        statistics (event-statistics (:games event))
        results    (:results event)]
    (str
     (results-header)
     (str/join
      "\n"
      (map
       (fn [[k v]] (results-string event k v (k results)))
       (sort (fn [el1 el2]
               (if (> (nth (nth el1 1) 0)
                      (nth (nth el2 1) 0))
                 true
                 false))
             (map (fn [x] x) statistics)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Group chart

(defn team-group [event team]
  (let [groups (:groups event)]
    (str team
         (seq
          groups))))

(defn event-group-table [event]
  (let [games  (:games event)
        groups (:groups event)
        stats  (event-statistics (take 36 games))
        results (:results event)]
    (str
     (stats-header)
     (str/join
      (str
       "\n"
       (stats-separator))
      (map (fn [group group-details]
             (str/join
              "\n"
              (map
               (fn [x]
                 (let [team (keyword x)]
                   (stats-string team (team stats) (team results))))

               group)))
           (:groups (get-event))
           (:group-details (get-event)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Invert results for lookup, for automatic lookup in game results.
;; (Not yet implemented.)
(defn group-placings [event]
  (reduce conj {}
          (map
           (fn [x] [(:group-stage (x 1)) (x 0)])
           (:results event))))

(defn format-placing-team [placings team]
  (str/upper-case (name (placings team))))

(defn format-game [event game-number]
  (let [game        (nth (:games event) (dec game-number))
        teams       (:teams     game)
        goals       (:goals     game)
        penalties   (:penalties game)
        team-a      (if (nth teams 0)
                      (str/upper-case (nth teams 0))
                      "---")
        team-b      (if (nth teams 1)
                      (str/upper-case (nth teams 1))
                      "---")
        goals-a     (nth goals     0)
        goals-b     (nth goals     1)
        penalties-a (nth penalties 0)
        penalties-b (nth penalties 1)
        places      (:places    game)
        place-a     (nth places 0)
        place-b     (nth places 1)
        results     (:results   event)]
    (str
     (cond
       penalties (str " " team-a " " goals-a "(" penalties-a ")"
                      " vs "
                      "" goals-b "(" penalties-b ") " team-b)
       goals     (str "    " team-a " " goals-a ""
                      " vs "
                      "" goals-b " " team-b)
       teams     (str "      " team-a " vs " team-b)
       :else     "--- vs ---")
     " [" game-number "]")))

(defn event-finals-chart [event]
  (let [placings (group-placings event)] ;; Not yet used.
    (str/join
     "\n"
     [""
      (format "  %-27s                             %-27s"
              (format-game event 37)
              (format-game event 42))
      (format "  %-27s                             %-27s"
              (format-game event 39)
              (format-game event 38))
      ""
      (format "         %-27s                     %-27s"
              (format-game event 45)
              (format-game event 48))
      (format "                                 %-27s"
              (format-game event 52))
      (format "                %-27s       %-27s"
              (format-game event 49)
              (format-game event 50))
      (format "                                 %-27s"
              (format-game event 51))
      (format "         %-27s                     %-27s"
              (format-game event 46)
              (format-game event 47))
      ""
      (format "  %-27s                             %-27s"
              (format-game event 40)
              (format-game event 44))
      (format "  %-27s                             %-27s"
              (format-game event 41)
              (format-game event 43))])))

(defn print-event-report [event]
  (println (:title event))
  (println "From: " (:from (:date event)))
  (println "To:   " (:to   (:date event)))
  (println)
  (println "Games")
  (println (event-games-table event))
  (println)
  (println "Group Stage Results")
  (println (event-group-table event))
  (println)
  (println "Results")
  (println (event-results-table event))
  (println)
  (println "Statistics")
  (println (event-stats-table event))
  (println)
  (println "Finals")
  (println (event-finals-chart event))
  (println))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Macros
(def macros [])

(defmacro add-macro
  "Add user macro to available list"
  [name]

  (def macros (conj macros name))
  )

(defmacro list-macros
  "List the available macros"
  []
  (println macros)
  )
(add-macro "list-macros")

(defmacro round-report-org
  "Produce a report of the games in a round in 'org' format."
  [round]
  (setup)
  (def sport "afl-2025")
  (def data (s/data-read-with-lines (str "data/" sport "/data.clj")))
  (def games (:results data))
  (println (r/report-games-afl-schedule
            (conj data {:results (r/round-filter games round)})))
  )
(add-macro "round-report-org")
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main-event-report
  [& args]
  (let [event (get-event)]
    (print-event-report event)))

(defn -main
  [& args]
  (println "Sporting Fixtures and Events")
  (println "----------------------------")
  (println)
  (help)
  (println "Running Setup")
  (setup)
  ;;  (clojure.main/repl)
  )
