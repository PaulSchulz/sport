(ns sporting-fixtures.afl
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

(defn -help []
;; Process downloaded file
;; Use from command line with:
  (println "lein run -m sporting-fixtures.afl")
  )
;;
;; Development
;;   (ns sporting-fixtures.afl)
;;   (load "afl")
;;   (use 'sporting-fixtures.afl)
;;   (use 'clojure.core)
;;   (clojure.pprint/pprint (read-event-data filename))
;;   (clojure.pprint/pprint (rest (reduce conj [] (read-event-data filename)) ))

(def event-name     "2019-aus-afl-mens")
(def event-data-csv "data/download/afl-2019-CenAustraliaStandardTime.csv")

(def preamble
  {:title    "2016 AFL Draw & Fixtures"
   :location "Australia"
   :code      "afl"
   :date      {:from "24 Mar 2016"
               :to "28 Aug 2016"}
   :format    "afl-mens-2019"
   :version   0.1
   })

(def teams
  {:fre {:name "Fremantle"}
   :wce {:name "West Coast Eagles"}
   :haw {:name "Hawthorn"}
   :syd {:name "Sydney Swans"}
   :ric {:name "Richmond"}
   :wbd {:name "Western Bulldogs"}
   :adl {:name "Adelaide Crows"}
   :nmb {:name "North Melbourne"}
   :pta {:name "Port Adelaide"}
   :gee {:name "Geelong Cats"}
   :gws {:name "GWS Giants"}
   :col {:name "Collingwood"}
   :mel {:name "Melbourne"}
   :stk {:name "St Kilda"}
   :ess {:name "Essendon"}
   :gcs {:name "Gold Coast Suns"}
   :bri {:name "Brisbane Lions"}
   :car {:name "Carlton"}
   }
)

(def venues
  {:mcg {:name "MCG"}
   :adl {:name "Adelaide Oval"}
   :mrv {:name "Marvel Stadium"}
   :gab {:name "Gabba"}
   :syd {:name "Sydney Showground Stadium"}
   :opt {:name "Optus Stadium"}
   :scg {:name "SCG"}
   :gee {:name "GMHBA Stadium"}
   :met {:name "Metricon Stadium"}
   :can {:name "UNSW Canberra Oval"}
   :tas {:name "University of Tasmania Stadium"}
   :mar {:name "Mars Stadium"}
   :blu {:name "Blundstone Arena"}
   :jia {:name "Adelaide Arena at Jiangwan Stadium"}
   :riv {:name "Riverway Stadium"}
   :tra {:name "TIO Traeger Park"}
   :tio {:name "TIO Stadium"}
   })

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-lookup-team-id [teams]
  (reduce conj {}
          (map (fn [[k v]] [(:name v) k]) teams)
          )
  )

(def lookup-team-id (create-lookup-team-id teams))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-lookup-venue-id [venues]
  (reduce conj {}
          (map (fn [[k v]] [(:name v) k]) venues)
          )
  )

(def lookup-venue-id (create-lookup-venue-id venues))
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

;; FIXME - Change this to use headers from first line in file.
(defn parse-record [record]
  (reduce conj
          {}
          [[:round-number (if (integer? (parse-number (record 0)))
                            (parse-number (record 0))
                            (record 0))]
           [:date         (record 1)]
           (if (not= (record 2) "TBA")
             [:location
              (if (lookup-venue-id (record 2))
                (lookup-venue-id (record 2))
                (record 2))
                ]
             nil
             )
           ;; lookup-team-id returns 'nil' for 'To be determined'
           (if (or (lookup-team-id (record 3))
                   (lookup-team-id (record 4)))
             [:teams [(lookup-team-id (record 3))
                      (lookup-team-id (record 4))]]
             nil)
           (if (not= (record 5) "")
             [:result       (convert-result (record 5))]
             nil
             )
           ]))

(defn parse-fixture-data [data]
  (map parse-record data))

(defn write-event-json [data]
  (spit "data/2019-aus-afl-mens.json"
        (generate-string
         data
         {:pretty true})
        ))

(defn file-write-yaml [data]
  (spit "data/2019-aus-afl-mens.yml"
        (str
         "---\n"
        (yaml/generate-string data)
        )))

(defn create-event-data [filename]
  (conj preamble
        ;; [:teams teams]
        ;; [:venues venues]
        [:games (parse-fixture-data (read-event-data filename))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2019 AFL Mens
;;
(defn event-id-to-filename [id]
  (str "data/" id ".yml"))

(defn read-event [event]
  (yaml/parse-string (slurp event)))

(defn get-event   [] (read-event (event-id-to-filename event-name)))

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
   (format " %3s | %-16s | %-10s | %-9s"
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
                9 2
                18 3
                27 4
                36 5
                45 6
                54 7
                63 8
                72 9
                81 10
                90 11
                99 12
                105 13
                111 14
                117 15
                126 16
                135 17
                144 18
                153 19
                162 20
                171 21
                180 22
                189 23}
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
            (= i 198)
            "-----+- Finals ---------+------------+------------------\n"
            (= i 202)
            "-----+- Semifinals -----+------------+------------------\n"
            (= i 204)
            "-----+- Preliminary Final -----------+------------------\n"
            (= i 206)
            "-----+- Grand Final ----+------------+------------------\n"
            :else "")
          (cond
            (contains? rounds i)
            (format
             "-----+- Round %2d -------+------------+------------------\n"
             (rounds i))
            :else "")
          (format " %3d | %s | %-10s |  %4s %4s "
                  (inc i)
                  ;; (:round-number x)
                  ;;(localtime (:date x))
                  (:date x)
                  (str (if (nth (:teams x) 0)
                         (str/upper-case (nth (:teams x) 0))
                         "---")
                       " vs "
                       (if (nth (:teams x) 1)
                         (str/upper-case (nth (:teams x) 1))
                         "---"))
                  (format "%3s"
                          (if (nth (:result x) 0)
                            (nth (:result x) 0)
                            "-"))
                  (format "%3s"
                          (if (nth (:result x) 1)
                            (nth (:result x) 1)
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

(defn calculate-stats [result]
  (let [a-points (nth result 0)
        b-points (nth result 1)]

    (cond
      (not a-points)       nil
      (not b-points)       nil
      (> a-points b-points) [[4 1 1 0 0 a-points b-points (- a-points b-points)]
                             [0 1 0 1 0 b-points a-points (- b-points a-points)]]
      (< a-points b-points) [[0 1 0 1 0 a-points b-points (- a-points b-points)]
                             [4 1 1 0 0 b-points a-points (- b-points a-points)]]
      :else                 [[2 1 0 0 1 a-points b-points (- a-points b-points)]
                             [2 1 0 0 1 b-points a-points (- b-points a-points)]]
      )
    ))

;; Calculate the statistics from the games of an event
(defn calculate-statistics [games]
  (map-indexed
   (fn [i x]
     (let [team-a (nth (:teams x) 0)
           team-b (nth (:teams x) 1)
           stats  (calculate-stats  (:result x))
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

;; Double reduce as the statistics returned from
;; 'calculate-statistics' is a double array, returning the statistics
;; of the game for each of the teams.
(defn reduce-statistics [stats]
  (reduce (fn [val coll]
            (reduce conj val coll))
          [] stats)
  )

;; statistics - Map of team statistics
;; update     - Array with 'team id' and 'statistics update'
(defn update-statistics [statistics update]
  (let [team         (nth update 0)
        stats-old    (if (team statistics)
                       (team statistics)
                       [0 0 0 0 0 0 0 0])
        stats-update (if (nth update 1)
                       (nth update 1)
                       [0 0 0 0 0 0 0 0])
        ]
    ;; Update old statstics with new values and add to data
    (conj statistics {team (map + stats-old stats-update)}))
  )

(defn event-statistics [games]
  (reduce update-statistics {}
          (reduce-statistics
           (calculate-statistics games))))

(defn calculate-percentage [stats]
  (let [for     (nth stats 5)
        against (nth stats 6)]
  (* (/ (* 1.0 for) (* 1.0 against)) 100.0)
  ))

(defn stats-header []
  " Pos | Team | P  | Pts      % |  P  W  L  D |   For    Ag  Diff")

(defn stats-separator []
  "-----+------+----+------------+-------------+----------------------")

(defn stats-string [i team stats]
  (format " %3d | %-4s | %2d | %2d %7.2f | %2d %2d %2d %2d | %5d %5d %5d"
          i
          (str/upper-case (name team))
          (nth stats 1) ;; Played
          (nth stats 0) ;; Points
          ;; Calculate percentage
          (calculate-percentage stats)
          (nth stats 1) ;; Played
          (nth stats 2) ;; Won
          (nth stats 3) ;; Lose
          (nth stats 4) ;; Draw
          (nth stats 5) ;; Points for
          (nth stats 6) ;; Points against
          (nth stats 7) ;; Difference
          ))

(defn event-stats-table [event]
  (let [statistics (event-statistics (:games event))]
    (str
     (stats-header)
     "\n"
     (stats-separator)
     "\n"
     (str/join
      "\n"
      (map-indexed
       (fn [i [k v]]
         (str
          (if (= i 8)
            (str (stats-separator) "\n")
            "")
          (stats-string (inc i) k v)))
       ;; Sort the ladder - first value in statistics, then second value
       (sort (fn [el1 el2]
               (cond
                 (> (nth (nth el1 1) 0) (nth (nth el2 1) 0)) true
                 (< (nth (nth el1 1) 0) (nth (nth el2 1) 0)) false
                 (> (calculate-percentage (nth el1 1))
                    (calculate-percentage (nth el2 1)))      true
                 :else false))
        (map (fn [x] x) statistics))
       )
      )
     "\n"
     (stats-separator)
    ))
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
  
  ;; Create YAML data file from downloaded CSV
  ;; Only need to fo this once. Will erase any data added manually
  ;; (eg. game results)
  (if false
    (let []
      (println "Create YAML data file")
      (println "  CSV:  " event-data-csv)
      (println "  YAML: " "data/2019-aus-afl-mens.yml")
      (file-write-yaml (create-event-data event-data-csv))))
  
  (println "")
  (println "Games")
  (println (event-games-table (get-event)))

  (println "")
  (println "Statistics / Ladder")
  (println (event-stats-table (get-event)))

  (println "")
  (println "Finals")
  (println (event-finals-chart (get-event)))
  )

