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
  (println "(def sport \"afl-2025\")")
  (println "(def data (r/data-read-event sport))")
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
              :group-16       "GS"
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
            (format "  /  %-3s %-6s  %-3s %-6s "
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

;; AFL Single line game Report
(defn format-game-result-afl [game]
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
           (format "%-3s %-10s  %-3s %-10s"
                   (str/upper-case (name home))
                   home-scoreboard
                   (str/upper-case (name away))
                   away-scoreboard))
         (format "%-3s  %-3s"
                 (str/upper-case (name home))
                 (str/upper-case (name away))))))

   (if (and (some? (:summary game))
            (string? (:summary game)))
     (format "  %s" (:summary game))
     "")))

;; Genral sport result
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
     (format "  %s" (:summary game))
     "")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn report-games [data]
  (let [code (:code (:details data))
        title (:title (:details data))]

    ;; General event details
    ;; Number/Round/TIme/Venue/Stage - Result
    (cond
      (= code :football) (def report-format "%3s %3s  %s  %s  %s  %s\n")
      (= code :afl)      (def report-format "%3s %3s  %s  %s  %s  %s\n")
      :default           (def report-format "%3s %3s  %s  %s  %s  %s\n")
      )

    (str
     title " : Games\n"
     "----------------------------------------------------------------------------------------\n"
     (apply str
            (map (fn [game]
                 (format report-format
                         (:MatchNumber game)
                         (:RoundNumber game)
                         (convert-to-localtime (:DateUtc game) "Australia/Adelaide")
                         (str/upper-case (name (((data :venues) (:Location game)) :id)))
                         (convert-stage-group (:stage game) (:group game))
                         (cond
                           (= code :football) (format-game-result-football game)
                           (= code :afl)      (format-game-result-afl game)
                           :default           (format-game-result game)
                           )
                                                                   )
                 )
               (:results data)))
     "----------------------------------------------------------------------------------------\n")))

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
   "Team   G W L D  Gf Ga  Gd R Pts  Result    Path\n"
   "------------------------------------------------------------------------------------\n"
   )
  )


(def stats-key-string
  ["Key"
   "  G   - Games"
   "  W   - Wins"
   "  L   - Losses"
   "  D   - Draws"
   "  Gf  - Goals For"
   "  Ga  - Goals Against"
   "  Gd  - Goal Difference"
   "  R   - Red Cards"
   "  Pts - Tournament Points"]
  )

(def results-key-string
  ["Path Key"
   "  * - Won over ..."
   "  . - Lost to ..."
   "  + - Drew with ..."
   "  _ - Game scheduled with ..."])

(defn stats-key []
  (str
   (str/join "\n"
             (map (fn [a b]
                    (format "%-30s %-30s" a b))
                  stats-key-string
                  (concat results-key-string (repeat ""))
                  )
             )
   ))

(defn path-string [path]
  (str/join " "
            (remove nil?
                    (map (fn [match] (if match
                                      (str/upper-case (name match))
                                      nil
                                      ))
                         path)))
  )

(defn stats-team [[team results]]
  (str
   (format "  %-5s"
           (str/upper-case (name team)))
   (format "%d %d %d %d  %2d %2d %3d %d %3d  "
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
        goals-for (nth team 4)
        diff   (nth team 6)
        red    (nth team 7)]
    ;; Use negative to sort by 'most' first.
    [(- points) (- goals-for) (- diff) red]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; AFL Teams, No :groups in data
(defn afl-parse-scoreboard [s]
  (let [match (re-matches #"(\d+)\.(\d+)\((\d+)\)" s)]
    (if match
      [(Integer/parseInt (nth match 1))
       (Integer/parseInt (nth match 2))
       (Integer/parseInt (nth match 3))]
      [])))

(defn afl-check-score? [s]
  (let [[a b c] s]
    (= c (+ (* 6 a) b))))

(defn afl-check-score [scoreboard]
  (cond
    (= (afl-check-score? scoreboard) false)
     (println (format "WARNING: Total points does not match score. %s" scoreboard))
    :default {}
    ))

;; Win/Lose/Draw Statistics
(defn afl-game-win-lose-draw [game]
  (let [home (nth (game :teams) 0)
        away (nth (game :teams) 1)
        home-scoreboard ((game :scoreboard) home)
        away-scoreboard ((game :scoreboard) away)
        home-score (afl-parse-scoreboard home-scoreboard)
        away-score (afl-parse-scoreboard away-scoreboard)
        ]
    (cond
      (= home-score []) {home [0 0 0] away [0 0 0]}
      (= away-score []) {home [0 0 0] away [0 0 0]}
      (> (nth home-score 2) (nth away-score 2)) {home [1 0 0] away [0 1 0]}
      (< (nth home-score 2) (nth away-score 2)) {home [0 1 0] away [1 0 0]}
      :else {home [0 0 1] away [0 0 1]}
      )
    ))

;; Championship points
(defn afl-game-result [game]
  (let [home (nth (game :teams) 0)
        away (nth (game :teams) 1)
        home-scoreboard ((game :scoreboard) home)
        away-scoreboard ((game :scoreboard) away)
        home-score (afl-parse-scoreboard home-scoreboard)
        away-score (afl-parse-scoreboard away-scoreboard)
        ]
    (cond
      (= home-score []) {home 0 away 0}
      (= away-score []) {home 0 away 0}
      (> (nth home-score 2) (nth away-score 2)) {home 4 away 0}
      (< (nth home-score 2) (nth away-score 2)) {home 0 away 4}
      :else {home 2 away 2}
      )
    ))

(defn afl-game-stats [game]
  (let [teams (:teams game)
        home (nth teams 0)
        away (nth teams 1)
        scoreboard (:scoreboard game)
        home-scoreboard (scoreboard home)
        away-scoreboard (scoreboard away)
        home-score (afl-parse-scoreboard home-scoreboard)
        away-score (afl-parse-scoreboard away-scoreboard)
        result (afl-game-result game)
        home-result (result home)
        away-result (result away)
        ]
    {:teams (:teams game)
     :home home
     :away away
     :score {home home-score
             away away-score}
     :home-score home-score
     :away-score away-score
     :result result
     :home-result home-result
     :away-result away-result
     home {:played 1
           :score home-score
           :against away-score
           :result home-result}
     away {:played 1
           :score away-score
           :against home-score
           :result away-result}
     }
    )
  )

;; Test
(comment
  (pprint (r/afl-game-stats (nth (data :results) 1)))

  (def game
    {:RoundNumber 0,
     :game 2,
     :teams [:syd :haw],
     :round 0,
     :summary "",
     :result {:syd {}, :haw {}},
     :score {:syd 76, :haw 96},
     :HomeTeam "Sydney Swans",
     :DateUtc "2025-03-07 08:40:00Z",
     :scoreboard {:syd "11.10(76)", :haw "14.12(96)"},
     :AwayTeam "Hawthorn",
     :Location "SCG",
     :MatchNumber 2})
  )

(defn afl-team-stats [data team]
  (reduce
   (fn [acc m] (merge-with + acc m))
   (map team
      (map afl-game-stats (:results data)))
   ))

(defn afl-format-stats-team [team]
  (str
     (format "  %-5s"
             (str/upper-case (key team)))
     ;;  (format "%d %d %d %d  %2d %2d %3d %d %3d  "
     ;;          (nth results 0)
     ;;          (nth results 1)
     ;;          (nth results 2)
     ;;          (nth results 3)
     ;;          (nth results 4)
     ;;          (nth results 5)
     ;;          (nth results 6)
     ;;          (nth results 7)
     ;;          (nth results 8)
     ;;          )
     ;;  (format "%-8s  "
     ;;          (if (nth results 9)
     ;;            (str/upper-case (name (nth results 9)))))
     ;;  (format "%s"
     ;;          (if (nth results 10)
     ;;            (path-string (nth results 10))))
     "\n"
     ))

(defn afl-format-stats-minor [data]
  (str
   (format "%s\n" "Minor rounds")
   (apply str (map afl-format-stats-team (:teams data)))
   "\n"
   )
  )

(defn report-afl-teams [data]
  (afl-format-stats-minor data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Football Game Results
(defn report-games-timeline-header []
  (str
   "Game Reports\n"
   "------------------------------------------------------------------------------------------\n"
   )
  )

(defn report-games-timeline [data]
  (str
   (report-games-timeline-header)
   (apply str
          (map (fn [game]
                 (str
                  (format "%-3s %3s  %s  %s  %s  %s\n"
                          (:MatchNumber game)
                          (:RoundNumber game)
                          (convert-to-localtime (:DateUtc game) "Australia/Adelaide")
                          (str/upper-case (name (:location_id game)))
                          (convert-stage-group (:stage game) (:group game))
                          (if (= (:code (:details data)) :football)
                            (format-game-result-football game)
                            (format-game-result game)
                            )
                          )
                  "-----------------------------------------------------------------------------------\n"
                  (format "         %3s %s v %3s %s"
                          (str/upper-case (name (-> game :teams first)))
                          ((-> game :teams first) (-> game :scoreboard))
                          (str/upper-case (name (-> game :teams second)))
                          ((-> game :teams second) (-> game :scoreboard))
                          )
                  (if (-> game :summary)
                    (format "         %s" (-> game :summary))
                    "")
                  "\n"
                  (if (and (-> game :timeline)
                           (not (empty? (-> game :timeline))))
                    (str
                     "         Timeline\n"
                     (apply str
                            (map ;; (fn [timeline] (format "         %s\n" timeline))
                             (fn [[e1 e2 e3 e4]] (format "           %8s  %3s %-4s %s\n"
                                                        e1
                                                        (str/upper-case (name e2))
                                                        (case e3
                                                          :goal         "G"
                                                          :goal-penalty "G(P)"
                                                          :goal-own     "G(O)"
                                                          :miss         "M"
                                                          :red-card     "RC"
                                                          e3)
                                                        e4))
                             (-> game :timeline))))
                    "")
                  "\n"
                  "\n"
                  )
                 )
               (:results data)))
   ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Finals
(defn match-string [match]
  (str
   (let [team-a (-> match :teams first)
         team-b (-> match :teams second)
         scoreboard (:scoreboard match)]
     (str
      (str/upper-case (name team-a))
      (if (team-a scoreboard)
        (str " "
             (team-a scoreboard)
             )
        )
      )
     )
   " v "
   (-> match :teams second name str/upper-case)
   ))

(defn report-finals [data]
(let [finals (:finals data)
report
[(-> finals :group-16-1 :teams first)
 " v "
 (-> finals :group-16-1 :teams second)
 ]]
report)
)
