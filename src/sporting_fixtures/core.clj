(ns sporting-fixtures.core
  (:gen-class)
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [clj-time.core   :as t]
            [clj-time.format :as f]
            [clj-time.local  :as l]
            )
  (:use [clojure.java.shell :only [sh]]
        [clojure.pprint :only [pprint]])
  )

(defn help []
  (println
   (str/join
    "\n"
    [";;;;;;;;;;;;;;;;;;;"
     ";; Useful Commands"
     ";;;;;;;;;;;;;;;;;;;"
     "(list-events)                - Show current event files"
     "(read-event \"<filename>\")    - Load event details" 
     "(display-event \"<filename>\") - Display event details"
     "(events-table)               - List events"
     ""
     ";; Examples"
     "  (display-event (event-id-to-filename \"2019-fra-fifa-women-worldcup\"))"
     ""
     ";; FIFA 2019 Womens World Cup"
     "  (event-games nil)"
     "  (println (event-games-table (get-event)))"
     "  (println (event-stats-table (get-event)))"
     "  (println (event-group-table (get-event)))"
     ]
    )))

(defn event-id-to-filename [id]
  (str "data/" id ".yml"))


(defn get-events []
  (str/split (:out (sh "bash" "-c" "ls data/20*.yml")) #"\n"))

(defn list-events []
  (map #(println (format "* %s" %)) (get-events)))

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
            (:version data)
            )))

(defn events-table []
  (let [fmt "  #  %-56s  %-12s %-12s %16s %-6s"
        width 110]
    (println
     (str/join "\n"
               [(str/join "" (repeat width "-"))
                (format fmt "Name / Location" "From" "To" "Code" "Ver")
                (str/join "" (repeat width "-"))
                (str/join "\n" (map #(event-details fmt %) (get-events)))
                ]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2019 FIFA Womens World Cup
;; fifa-2019
(defn index-of [v i] (.indexOf v i))

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
                       (t/default-time-zone))
        ]
    (f/unparse my-formatter
               (f/parse formatter datetime))
               
    ))

;; Create results table
(defn event-games-table-header []
  (str
   (format " %2s | %-15s | %-10s |  %-9s | %s"
           "Id"
           "Time"
           "Teams"
           "Score"
           "Result")
   "\n"
   "--------------------------------------------------------------------------"
   "\n"
   )
  )

(defn event-games-table [event]
  (let [games (:games event)]
    (str
     (event-games-table-header)
     (str/join
      "\n"
      (map-indexed
       (fn [i x]
         (str
          (if (= i 36) "----- Round of 16 -------------------------------\n" "")
          (if (= i 44) "----- Quarter Finals ----------------------------\n" "")
          (if (= i 48) "----- Semi Finals -------------------------------\n" "")
          (if (= i 50) "----- Playoff -----------------------------------\n" "")
          (if (= i 51) "----- Final -------------------------------------\n" "")
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
                  (str
                   (if (nth (:goals x) 0)
                     (nth (:goals x) 0)
                     "-")
                   (if (nth (:penalties x) 0)
                     (str "/" (nth (:penalties x) 0))
                     "  "))
                  (str
                   (if (nth (:goals x) 1)
                     (nth (:goals x) 1)
                     "-")
                   (if (nth (:penalties x) 1)
                     (str "/" (nth (:penalties x) 1))
                     "  "))
                  (if (:result x) (:result x) "")
                  )))
       games)
      )
     "\n"
     (str (str/join "" (repeat 78 "-")) "\n")
     )
    ))

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
      :else               [1 1]
      )
    ))

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
                           [1 1 0 0 1 b-goals a-goals (- b-goals a-goals)]]
      )
    ))

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
        b-penalties (nth sane-penalties 1)
        ]

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
      :else                       [0 0]
      )))
  
(defn calculate-statistics [games]
  (map
   (fn [x]
     (let [team-a (nth (:teams x) 0)
           team-b (nth (:teams x) 1)
;;           points (calculate-points (:goals x))
           stats  (calculate-stats  (:goals x))
;;           winner (calculate-winner (:goals x) (:penalties x))
           ]
       (conj {}
             (if team-a
               {(keyword team-a) (nth stats 0)}
               nil)
             (if team-b
               {(keyword team-b) (nth stats 1)}
               nil)
             )
      ))
   games)
  )

(defn reduce-statistics [stats]
  (reduce (fn [val coll]
            (reduce conj val coll))
          [] stats)
  )

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

    (conj statistics {team (map + stats-old stats-update)}))
  )

(defn event-statistics [games]
  (reduce update-statistics {}
          (reduce-statistics
           (calculate-statistics games))))

(defn stats-separator []
  "------+----+---------+------------+--------------------------\n"
  )

(defn stats-header []
  (str
   "      |    | Games   | Goals      |\n"      
   " Team | Pt | P/W/L/D | Fr/Ag/Diff | Results\n"
   (stats-separator)
   )
  )

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
          (format "%9s%s"
                  (if (:group-stage results)
                    (:group-stage results)
                    "")
                  (if (:qual16 results)
                    (if (= (:qual16 results) "direct")
                      "*"
                      "+"
                    )
                    " ")
                  )
          ))

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
        (map (fn [x] x) statistics))
       )
      )
    ))
  )

(defn team-group [event team]
  (let [groups (:groups event)]        
    (str team
         (seq
          groups
          )

         )
    )
  )
  
(defn event-group-table [event]
  (let [games  (:games event)
        groups (:groups event)
        stats  (event-statistics games)
        results (:results event)]
    (str
     (stats-header)
     (str/join
      (str
       "\n"
       (stats-separator)
       )
       (map (fn [group group-details]
              (str/join
               "\n"
               (map
                (fn [x]
                  (let [team (keyword x)]
                    (stats-string team (team stats) (team results))
                    )
                  )
                group)))
            (:groups (get-event))
            (:group-details (get-event))
          ))
     )
    )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main
  [& args]
  (let [event (get-event)]
    (println (:title event))
    (println "From: " (:from (:date event)))
    (println "To:   " (:to   (:date event)))
    (println)
    (println "Group Stage")
    (println (event-group-table event))
    (println)
    (println "Statistics")
    (println (event-stats-table event))
    (println)
    (println "Games")
    (println (event-games-table event))
    ))
