;; Functions to produce generic event reports.
;; See (help) for assumptions, usage examples and workflow explanation.
;; Include in repl with
;;   (require ['sporting-fixtures.reports :as 'r])

(ns sporting-fixtures.reports
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :as sh] ;; Printing reports
   [clojure.pprint :as pp]
   [clojure.string :as str]
   [java-time :as time] ;; Used for generating local-time.
   ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
(defn reload []
  (require ['sporting-fixtures.reports :as 'r] :reload))

;;(defn run-tests []
;;  (clojure.test/run-tests 'sporting-fixtures.reports))

(defn help []
  (println ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;")
  (println ";; This is a 'helper' module for creating generic event reports.")
  (println ";; It assumes:")
  (println ";;   - data and results have been stored and are in the default format")
  (println)
  (println ";; Load data from event (example)")
  (println "(def data (r/data-read-event \"bbl-2022\"))")
  (println)
  (println ";; To display a report of game results")
  (println "(println (r/report-games data))")
  (println)
  (println ";; To save the report to a text file")
  (println "(println (r/report-games-save data))")
  (println ";; This can then be printed from the terminal with the following:")
  (println ";;   enscript -r report-games.txt")
  (println)
  (println ";; Or, once the report has been generated, send directly to default printer with:")
  (println "(r/report-games-print data)")
  (println) ;; -- add help here --
  )
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; TODO: Move data loading and saving to a separate shared module
;; Read data from file
(defn data-read [filename]
  (read-string (slurp filename)))

(defn data-init [data]
  (let [filename (:data data)]
    ;; Read in data file, or initialise
    (if (and (some? filename) (.exists (io/file filename)))
      (data-read filename)
      (println ";; Error: Data file not found"))))

(defn data-write [data]
  (spit (:data data)
        (clojure.pprint/write data
                              :stream nil)))

;; Helper function - Read in data, using event id.
(defn data-read-event [event]
  (data-read (str "data/" event "/data.clj")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn convert-stage-group [stage group]
  (format "%-2s%s"
          (if (some? stage)
            (case stage
              :group          "G"
              :group-16       "S"
              :quarter-final  "QF"
              :semi-final     "SF"
              :third-play-off "PO"
              :final          "GF"
              "")
            "G")
          (if (some? group)
            (case group
              :group-a    "A"
              :group-b    "B"
              :group-c    "C"
              :group-d    "D"
              :group-e    "E"
              :group-f    "F"
              :group-g    "G"
              :group-h    "H"
              :group-16-1 "1"
              :group-16-2 "2"
              :group-16-3 "3"
              :group-16-4 "4"
              :group-16-5 "5"
              :group-16-6 "6"
              :group-16-7 "7"
              :group-16-8 "8"
              :qf-1       "1"
              :qf-2       "2"
              :qf-3       "3"
              :qf-4       "4"
              :sf-1       "1"
              :sf-2       "2"
              :final      " "
              :gf         " "
              "")
            "-")))

;; Convert time-stamp to localtime
(defn convert-to-localtime
  "timestamp - Timestamp string eg. 2022-11-13 08:00:00Z
  timezone - eg. Australia/Adelaide"
  [timestamp timezone]
  (let [timestamp-array (re-matches #"^(....)-(..)-(..) (..):(..):(..)(.*)"
                                    timestamp)
        year   (Integer/parseInt (timestamp-array 1))
        month  (Integer/parseInt (timestamp-array 2))
        day    (Integer/parseInt (timestamp-array 3))
        hour   (Integer/parseInt (timestamp-array 4))
        minute (Integer/parseInt (timestamp-array 5))
        second (Integer/parseInt (timestamp-array 6))
        zone   (timestamp-array 7)]
    (time/format
     (time/formatter "yyyy-MM-dd EEE hh:mma")
     (time/with-zone-same-instant
       (time/zoned-date-time  year month day hour minute second 0 zone) timezone))))

(defn format-game-result [game]
  (str
   (if (and (vector? (:teams game))
            (= (count (:teams game)) 2))
     (let [home (nth (:teams game) 0)
           away (nth (:teams game) 1)]
       (if (and (some? (:scoreboard game))
                (= (count (:scoreboard game)) 2)
                (not (= (vals (:scoreboard game)) ["" ""])))
         (let [home-scoreboard (home (:scoreboard game))
               away-scoreboard (away (:scoreboard game))]
           (format "%-3s %-12s  %-3s %-12s"
                   (str/upper-case (name home))
                   home-scoreboard
                   (str/upper-case (name away))
                   away-scoreboard))
         (format "%-3s  %-3s"
                 (str/upper-case (name home))
                 (str/upper-case (name away))))))
   (if (and (some? (:summary game))
            (string? (:summary game)))
     (:summary game)
     "")))

(defn format-game-result-football [game]
  (let [home (nth (:teams game) 0)
        away (nth (:teams game) 1)]
    (str
     (if (and (vector? (:teams game))
              (= (count (:teams game)) 2))
       (str
        (format "%-3s  %-3s"
                (str/upper-case (name home))
                (str/upper-case (name away)))

        (if (and (some? (:scoreboard game))
                 (= (count (:scoreboard game)) 2)
                 (not (= (vals (:scoreboard game)) ["" ""])))
          (let [home-scoreboard (home (:scoreboard game))
                away-scoreboard (away (:scoreboard game))]
            (format "  /  %-3s %-4s  %-3s %-4s "
                    (str/upper-case (name home))
                    home-scoreboard
                    (str/upper-case (name away))
                    away-scoreboard))
          )))
     (if (and (some? (:summary game))
              (string? (:summary game)))
       (:summary game)
       "")))
  )


(defn report-games [data]
  ;; Conditional formatting
  (if (= (:code (:details data)) :football)
    (def report-format "%3s %3s  %s  %s  %s\n")
    (def report-format "%3s %3s  %s  %s  %s\n")
    )

  (str
   "-------------------------------------------------------------------------------\n"
   (apply str
          (map (fn [game]
                 (format report-format
                         (:MatchNumber game)
                         (:RoundNumber game)
                         (convert-to-localtime (:DateUtc game) "Australia/Adelaide")
                         (convert-stage-group (:stage game) (:group game))
                         (if (= (:code (:details data)) :football)
                           (format-game-result-football game)
                           (format-game-result game)
                           )

                         ))
               (:results data)))
   "-------------------------------------------------------------------------------\n"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn report-games-save [data]
  (let [file (str (-> data :details :datadir) "report-games.txt")]
    (spit file (report-games data))))

(defn report-games-print [data]
  (let [file (str (-> data :details :datadir) "report-games.txt")]
    (println file)
    (sh/sh "/usr/bin/enscript" "-r" file)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Football Reports Note: Double array is used for decomposition of arguments
;; when used with 'map'.

(defn stats-header []
  (str
   "Team   G W L D  F  A   D R P  Result    Path\n"
   "--------------------------------------------------------------------------------\n"
   )
  )

(defn stats-key []
  (str
   (str/join "\n"
             ["Key"
              "  G   - Games"
              "  W   - Wins"
              "  L   - Losses"
              "  D   - Draws"
              "  F   - Goals For"
              "  A   - Goals Against"
              "  D   - Goal Difference"
              "  R   - Red Cards"
              "  P   - Tournament Points"])
   )
  )

(defn path-string [path]
  (str/join " "
            (map (fn [match] (if match
                              (str/upper-case (name match))
                              " - "
                              ))
                 path)
            )
  )

(defn stats-team [[team results]]
  (str
   (format "  %-5s"
           (str/upper-case (name team)))
   (format "%d %d %d %d %2d %2d %3d %d %d  "
           (nth results 0)
           (nth results 1)
           (nth results 2)
           (nth results 3)
           (nth results 4)
           (nth results 5)
           (nth results 6)
           (nth results 7)
           (nth results 8)
           )
   (format "%-8s  "
           (if (nth results 9)
             (str/upper-case (name (nth results 9)))))
   (format "%s"
           (if (nth results 10)
             (path-string (nth results 10))))
   "\n"
   )
  )

;; Used to sort teams in group list.
(defn group-sort [[id team]]
  (let [points (nth team 8)
        diff   (nth team 6)
        red    (nth team 7)]
    ;; Use negative to sort by 'most' first.
    [(- points) (- diff) red]
    )
  )

(defn stats-group [[group teams]]
  (str
   (format "%s\n" (str/upper-case (name group)))
   (apply str (map stats-team (sort-by group-sort teams)))
   "\n"
   )
  )

(defn stats-groups [data]
  (let [groups (:groups data)]
    (str
     (stats-header)
     ;; (clojure.pprint/pprint groups)
     (apply str
            (map stats-group groups)
            )
     (stats-key)
     ))
  )

(defn report-teams [data]
  (stats-groups data)
  )
