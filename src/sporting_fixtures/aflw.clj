(ns sporting-fixtures.aflw
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :refer :all] ;; Pretty print JSON
            [clj-yaml.core :as yaml]    ;; YAML output
            ;; Time manipulation
            [clj-time.core   :as t]
            [clj-time.format :as f]
            [clj-time.local  :as l]
            [clojure.pprint]
            )
  (:gen-class)
  )

;; To use this file
;; => (require 'sporting-fixtures.aflw)
;; => (ns sporting-fixtures.aflw)

;; To reload namespace
;; => (use 'sporting-fixtures.aflw :reload)

(defn -help []
;; Process downloaded file
;; Use from command line with:
  (println "lein run -m sporting-fixtures.aflw")
  )
;;
;; Development
;;   (ns sporting-fixtures.afl)
;;   (load "afl")
;;   (use 'sporting-fixtures.afl)
;;   (use 'clojure.core)
;;   (clojure.pprint/pprint (read-event-data filename))
;;   (clojure.pprint/pprint (rest (reduce conj [] (read-event-data filename)) ))

(def event-name     "2020-aus-aflw")
(def event-data-csv "data/download/afl-2019-CenAustraliaStandardTime.csv")

(def preamble
  {:title    "2016 AFL Draw & Fixtures"
   :location "Australia"
   :code      "afl"
   :date      {:from "24 Mar 2016"
               :to "28 Aug 2016"}
   :format    "afl-womens-2019"
   :version   0.1
   })

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn keyword-team [team]
  "Convert team name (string) to short keyword."
  (case team
    "West Coast Eagles" :wce
    "Western Bulldogs"  :wbd
    (keyword (clojure.string/lower-case
              (subs
               (clojure.string/replace team #" " "") ; Remove whitespace
               0 3)))
    )
  )

;; Change home and away team names to keywords.
(defn modify-teamnames [games]
  (map (fn [x]
         (merge x {:home (keyword-team (:home x))
                   :away (keyword-team (:away x))}))
       games
  ))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn convert-result [result]
  (let [string-split  (str/split result #" ")]
    [(Integer/parseInt (string-split 0))
     (Integer/parseInt (string-split 2))]
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn read-event-data [filename]
  ;;  (println (slurp filename))
  (reduce conj [] (rest
   (with-open [reader (io/reader filename)]
     (doall
      (csv/read-csv reader)))))
  )

(defn parse-number
  "Reads an integer from a string. Returns string if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    (read-string s)
    s))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Read event data from yaml file
;;
(defn event-id-to-filename [id]
  (str "data/" id ".yml"))

(defn read-event [event]
  (yaml/parse-string (slurp event)))

;; Read in event and parse data.
(defn get-event []
  (let [event (read-event (event-id-to-filename event-name))] 
    (merge event {:games (modify-teamnames (:games event))})
    )
  )

;;(defn event-games [] (:games (get-event)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Consider changing to 'clojure-java-time'
;;   See: https://github.com/dm3/clojure.java-time
;; Time parsing an manipulation
;; Formatters can be listed with: (f/show-formatters)
;;
;; Not currently used
(defn localtime [datetime]
  (let [formatter (f/formatter :rfc822)
        my-formatter (f/with-zone
                       (f/formatter "dd/MM/yyyy hh:mm")
                       (t/default-time-zone))
        ]
    (f/unparse my-formatter
               (f/parse formatter datetime))             
    ))

(defn event-games-table-separator []
  "-----+------------------+------------+------------------"
  )

;; Create results table
(defn event-games-table-header []
  (str
   (format " %3s | %-16s | %-10s | %s"
           "Id"
           "Time"
           "Teams"
           "Score"
           )
   "\n"
   (event-games-table-separator)
   "\n"
   )
  )

(defn event-games-table [event]
  (let [games (:games event)
        rounds {0 1
                7 2
                14 3
                21 4
                28 5
                35 6
                42 7
                49 8}
        ]
    (str
     (event-games-table-header)
     (str/join
      "\n"
      (map-indexed
       (fn [i x]
         (str
          (cond
            (= i 0)
            "-----+- Minor Rounds ---+------------+------------------\n"
            (= i 56)
            "-----+- Finals ---------+------------+------------------\n"
            :else "")
          (cond
            (contains? rounds i)
            (format
             "-----+- Round %2d -------+------------+------------------\n"
             (rounds i))
            :else "")
          (format " %3d | %s | %-10s |  %4s %4s"
                  (inc i)
                  ;; (:round-number x)
                  ;;(localtime (:date x))
                  (:time x)
                  (str (if (:home x)
                         (str/upper-case (name (:home x)))
                         "---")
                       " vs "
                       (if (:away x)
                         (str/upper-case (name (:away x)))
                         "---"))
                  (format "%3s"
                          (if ((:home x) (:result x))
                            ((:home x) (:result x))
                            "-"))
                  (format "%3s"
                          (if ((:away x) (:result x))
                            ((:away x) (:result x))
                            "-"))
                  )))
       games)
      )
     "\n"
     (event-games-table-separator)
     )
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statistics
;; map [:points :played :won :lost :drawn :for :against :diff]
(defn calculate-points [score]
  (if (and score (seq? score) (nth score 0) (nth score 1))
    (+ (* 6 (nth score 0)) (nth score 1))
    nil))

(defn calculate-stats [game]
  (let [home        (:home  game)
        away        (:away  game)
        score       (:score game)
        home-points (calculate-points (home score))
        away-points (calculate-points (away score))]
    (cond
      (not home-points)       nil
      (not away-points)       nil
      (> home-points away-points)
      {home {:points 4
             :played 1
             :won    1
             :lost   0
             :drawn  0
             :points-for     home-points
             :points-against away-points
             :points-diff    (- home-points away-points)}
       away {:points 0
             :played 1
             :won    0
             :lost   1
             :drawn  0
             :points-for     away-points
             :points-against home-points
             :points-diff    (- away-points home-points)}}
      (< home-points away-points)
      {home {:points 0
             :played 1
             :won    0
             :lost   1
             :drawn  0
             :points-for     home-points
             :points-against away-points
             :points-diff    (- home-points away-points)}
       away {:points 4
             :played 1
             :won    1
             :lost   0
             :drawn  0
             :points-for     away-points
             :points-against home-points
             :points-diff    (- away-points home-points)}}
      :else
      {home {:points 2
             :played 1
             :won    0
             :lost   0
             :drawn  1
             :points-for     home-points
             :points-against away-points
             :points-diff    (- home-points away-points)}
       away {:points 2
             :played 1
             :won    0
             :lost   0
             :drawn  1
             :points-for     away-points
             :points-against home-points
             :points-diff    (- away-points home-points)}}
      )))

(defn event-games-statistics-table [event]
  (str/join
   "\n"
   (map-indexed
    (fn [i x]
      (let [home (:home x)
            away (:away x)
            score (:score x)]
        (format " %3d | %-10s | %s"
                (inc i)
                (str (if (:home x)
                       (str/upper-case (name (:home x)))
                       "---")
                     " vs "
                     (if (:away x)
                       (str/upper-case (name (:away x)))
                       "---"))
                ;;(calculate-stats home away score)
                (if (or (= (home score) "-") (= (away score) "-"))
                  "-"
                  (str home " " away " " score " "
                       (calculate-stats x))
                  )
                )))
    (:games event)
    ))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Calculate the statistics from the games of an event
(defn calculate-statistics [games]
  (map-indexed
   (fn [i x]
     (let [home  (:home x)
           away  (:away x)
           score (:score x)
           stats (calculate-stats x)
           ]
       (conj {}
             (if home
               {home (home stats)}
               nil)
             (if away
               {away (away stats)}
               nil)
             )
      ))
   games)
  )

;; Split the double statistics record per game into two records, one
;; for each team.
(defn split-statistics [stats]
  (reduce (fn [val coll]
            (reduce conj val coll))
          [] stats)
  )

;; statistics - Map of team statistics
;; updates     - Array with 'team id' and 'statistics update' (map)
(defn update-statistics [statistics updates]
   (reduce (fn [stats update]
             (let [team   (nth update 0)
                   update (nth update 1)]
               (conj stats {team (merge-with + (team stats) update)})))
           statistics
           updates)
  )

(defn event-statistics [games]
  (update-statistics {}
                     (-> games
                         calculate-statistics
                         split-statistics))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TODO: Check for nil
(defn calculate-percentage [stats]
  (let [for     (:points-for stats)
        against (:points-against stats)]
  (* (/ (* 1.0 for) (* 1.0 against)) 100.0)
  ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn stats-header []
  " Pos | Team | P  | Pts      % |  P  W  L  D |   For    Ag  Diff")

(defn stats-separator []
  "-----+------+----+------------+-------------+----------------------")

(defn stats-string [i team stats]
  (format " %3d | %-4s | %2d | %2d %7.2f | %2d %2d %2d %2d | %5d %5d %5d"
          i
          (str/upper-case (name team))
          (:played stats)
          (:points stats)
          ;; Calculate percentage
          0.0 ; (calculate-percentage stats)
          (:played stats)
          (:won    stats)
          (:lost   stats)
          (:drawn  stats)
          (:points-for     stats)
          (:points-against stats)
          (:points-diff    stats)
          ))

(defn event-table-sort-fn [team-a team-b]
  (let [points-a (:points (nth team-a 1))
        points-b (:points (nth team-b 1))
        percent-a 0.0
        percent-b 0.0]
    (cond
      (> points-a points-b) true
      (< points-a points-b) false
      (> percent-a percent-b) true
      :else false)
    )
  )

(defn event-stats-table [statistics]
  (let []
    (str
     (stats-header)
     "\n"
     (stats-separator)
     "\n"
     (str/join
      "\n"
      (map-indexed
       (fn [i [k v]]
         ; (str
         ; (if (= i 8)
         ;   (str (stats-separator) "\n")
         ;   "")
          (stats-string (inc i) k v)
         )
       statistics
       )
      )
     "\n"
     (stats-separator)
    ))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Results chart

(defn results-separator []
  "------+--------+-------+-------+-------+-------+-------+-------+-------+-----\n"
  )

(defn results-header []
  (str
   "      | Game   |  11111|1111122|2222222|2333333|3333444|4444444|5555555|55566\n"
   " Team | 1234567|8901234|5678901|2345678|9012345|6789012|3456789|0123456|78901\n"
   )
  )

(defn results-string [event team]
  (let [games (:games event)
        teams (:teams event)
        ]
    (format "  %3s | %s"
            (str/upper-case (name team))
            (str/join
             ""
             (map-indexed
              (fn [index game]
                (str
                 (let [home (:home game)
                       away (:away game)
                       ;;statistics (calculate-game-statistics
                       ;;            (conj game {:home home-id} {:away away-id}))
                       statistics (calculate-stats game)
                       ]
                    (if statistics
                      (cond
                        (or (= team home) (= team away))
                        (cond
                          (= (:won   (team statistics)) 1) "W"
                          (= (:lost  (team statistics)) 1) "L"
                          (= (:drawn (team statistics)) 1) "d"
                          :else "o")
                        :else  "-"            
                        )
                      (cond
                        (or (= team home) (= team away))
                        "o"
                        :else  " "            
                        )
                   )
                    )
                 (cond
                   (= (mod index 7) 6) "|"
                   :else        ""
                   )
                 )
                )
              games)
             )
            )
    )
  )

(defn event-results-table [event]
  (let [games      (:games event)
        teams      (:teams event)
        ;; statistics (event-statistics games))
        results    (:results event)]
    (str
     (results-header)
     (results-separator)
     (str/join
      "\n"
      (map
        (fn [[key value]] (results-string event key))
;;        (sort (fn [el1 el2]
;;                (if (> (nth (nth el1 1) 0)
;;                       (nth (nth el2 1) 0))
;;                  true
;;                  false))
;;              (map (fn [x] x) statistics))
        teams
        )
      )
     "\n"
      (results-separator)
    )
  )
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Finals

(defn format-game [event game-number]
  (let [game   (nth (:games event) (dec game-number))
        teams  (:teams  game)
        result (:result game)
        team-a (if (nth teams 0)
                 (str/upper-case (nth teams 0))
                 "---")
        team-b   (if (nth teams 1)
                   (str/upper-case (nth teams 1))
                   "---")
        points-a (nth result 0)
        points-b (nth result 1)
          ]
    (str
     (cond
       result (str team-a "(" points-a ") vs " team-b "(" points-b ") [" game-number "]")
       teams  (str "    " team-a " vs " team-b " [" game-number "]")
       :else  (str "    --- vs --- [" game-number "]")
     ))
    ))

(defn event-finals-chart [event]
  (let [games (:games event)]
    (str
     "Knockout                    Semifinal                 Preliminary                 Grand Final\n"
     "--------                    ---------                 -----------                 -----------\n"
     "\n"
     (format "%-26s                            %-26s  %-26s\n"
             (format-game event 199)
             (format-game event 205)
             (format-game event 207)
             )
     "\n"
     (format "%-26s  %-26s\n"
             (format-game event 200)
             (format-game event 203)
             )
     "\n"
     (format "%-26s                            %-26s\n"
             (format-game event 201)
             (format-game event 206)
             )
     "\n"
     (format "%-26s  %-26s\n"
             (format-game event 202)
             (format-game event 204)
             )
     "\n"

            )))
    

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main []
  (println "Event: " event-name)
  (println)
  
  (println "Games")
  (println (event-games-table (get-event)))
  (println)

  (println "Game Statistics")
  (println (event-games-statistics-table (get-event)))
  (println)

  ;; Testing
  ; (clojure.pprint/pprint  (clojure.pprint/pprint (event-statistics (-> (get-event) :games))))
  
  (println "Statistics / Ladder")
  (println (event-stats-table
                  (sort event-table-sort-fn (filter (fn [x] (not= (first x) :tob)) (event-statistics (:games (get-event)))))))

  (println)

  ;; Testing
  ;(clojure.pprint/pprint (-> (get-event) :teams))
  
  (println "Conference A")
  (println (event-stats-table
            (sort event-table-sort-fn
                  (filter (fn [x] (= (:conference ((first x) (:teams (get-event))))
                                     "a")) 
                          (filter (fn [x] (not= (first x) :tob))
                                  (event-statistics (:games (get-event))))))))
  (println)

  (println "Conference B")
  (println (event-stats-table
            (sort event-table-sort-fn
                  (filter (fn [x] (= (:conference ((first x) (:teams (get-event))))
                                     "b")) 
                          (filter (fn [x] (not= (first x) :tob))
                                  (event-statistics (:games (get-event))))))))
  (println)

  (println "Games")
  (println (event-results-table (get-event)))
  (println)
  
;  (println "Finals")
;  (println (event-finals-chart (get-event)))
  )

