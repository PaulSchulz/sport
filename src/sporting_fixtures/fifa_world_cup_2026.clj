(ns sporting-fixtures.fifa-world-cup-2026
  (:require [clojure.data.csv   :as csv]
            [clojure.java.io    :as io]
            [clojure.java.shell :as sh] ;; Printing reports
            [clojure.string     :as str]
            [clojure.walk       :as walk]
            [cheshire.core      :as json]  ;; Pretty print JSON
            [clj-yaml.core      :as yaml]    ;; YAML output
            [fipp.edn           :as fipp :refer [pprint] :rename {pprint fipp}]
            ;; Time manipulation
            [clj-time.core      :as t]
            [clj-time.format    :as f]
            [clj-time.local     :as l]
            [clojure.pprint]

            [clojure.test :refer :all]

            [sporting-fixtures.reports :as r]
            )
  (:import
   [java.time ZonedDateTime ZoneId]
   [java.time Instant Duration]
   [java.time.format DateTimeFormatter])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn reload []
  (require ['sporting-fixtures.fifa-world-cup-2026 :as 'fifa26] :reload-all)
  )

(defn help []
  ;; Process downloaded file
  ;; Use from command line with:
  (println "Uaage: lein run -m sporting-fixtures.fifa-world-cup-2026 [command]")
  (println "  where the optional 'command' is one of:")
  (println "    help")
  (println "    report")
  (println "    report-save")
  (println "    report-print")
  (println "    fixtures")
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def event-name "fifa-world-cup-2026")
(def event-title "FIFA World Cup 2026")

;; (def data (r/data-read-event event-name))
(def data {})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tools and Utilities
;;
;; Consider changing to 'clojure-java-time'
;;   See: https://github.com/dm3/clojure.java-time
;; Time parsing an manipulation
;; Formatters can be listed with: (f/show-formatters)
(defn api->iso-utc [s]
  (clojure.string/replace s #" " "T"))

(defn localtime [utc-string]
  (let [utc-time (ZonedDateTime/parse utc-string)
        local    (.withZoneSameInstant
                  utc-time
                  (ZoneId/systemDefault))
        fmt      (DateTimeFormatter/ofPattern "EEE dd MMM pph:mm a")]
    (.format local fmt)))

(defn localtime-org [utc-string]
  (let [utc-time (ZonedDateTime/parse utc-string)
        local    (.withZoneSameInstant
                  utc-time
                  (ZoneId/systemDefault))
        fmt      (DateTimeFormatter/ofPattern "yyyy-MM-dd EEE HH:mm")]
    (.format local fmt)))

(defn ->instant [iso]
  (Instant/parse iso))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; From ChatGPT
(defn read-json [filename]
  (json/parse-string (slurp filename) true)) ; true = keyword keys

;; The following uses 'pprint', but could also use 'fipp.edn'
(defn save-data []
  (spit (str "data/" event-name "/fixtures.edn")
        (with-out-str
          (fipp (read-json (str "data/" event-name "/fixtures.json"))))))

(defn save-results []
  (spit (str "data/" event-name "/results.edn") ""))

(defn read-data []
  (read-string (slurp (str "data/" event-name "/fixtures.edn"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Teams
(defn load-teams []
  (read-string (slurp (str "data/" event-name "/teams.edn"))))

;;(def teams "")
(def teams (load-teams))

(def team-name->id
  (into {}
        (for [[id team] teams]
          [(:name team) id])))

(defn team-id
  [team-name]
  (or (get team-name->id team-name)
      (throw (ex-info "Error: Unknown team"
                      {:team team-name}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Team Reports
(defn render-team-header []
  (format "%3s  %-30s  %2s %2s %2s %2s %2s %2s %2s %2s %2s\n"
          "" "Country" "Pt" "P" "W" "L" "D" "F" "A" "D" "R"
          )
  )

(defn render-team [[team-id team]]
  (format "%3s  %-30s   -  -  -  -  -  -  -  -  -\n"
          (-> (name team-id)
              str/upper-case)
          (str (:name team) "(" (:ranking team) ")")
          ))

(defn render-team-stats [[team-id team]]
  (format "%3s  %-30s   -  -  -  -  -  -  -  -  -\n"
          (-> (name team-id)
              str/upper-case)
          (str (:name team) "(" (:ranking team) ")")
          ))

(defn team-filter-placeholder [teams]
  (into {} (filter (fn [[k v]] (not (:placeholder? v))) teams)))

(defn team-filter [teams key group]
  (into {} (filter (fn [[k v]] (= (key v) group)) teams)))

(defn team-filter-group [group teams]
  (into {} (filter (fn [[k v]] (= (:group v) group)) teams)))

(defn team-filter-confederation [teams confederation]
  (into {} (filter (fn [[k v]] (= (:confederation v) confederation)) teams)))

(defn teams-list [teams]
  (str
   "FIFA World Cup 2026 Teams\n"
   "============================\n\n"
   (->> teams
        ;; fixtures-by-date
        team-filter-placeholder
        (map render-team)
        (clojure.string/join "\n"))))

(defn groups-list [teams]
  (let [groups [:a :b :c :d :e :f :g :h :i :j :k :l]]
    (str
     "FIFA World Cup 2026 Groups\n"
     "========================== \n"
     (->> groups
          (map (fn [g]
                 (str
                  (format "Group %s\n" (str/upper-case (name g) ))
                  "\n"
                  (apply str
                         (->> teams
                              ;;                  ;;(team-filter-group group)
                              (map render-team)))
                  )
                 )
               )
          dorun
          ))))

(defn groups-list2 [teams]
  (let [groups [:a :b :c :d :e :f :g :h :i :j :k :l]]
    (str
     "FIFA World Cup 2026 Groups\n"
     "========================== \n"
     (str "  " (render-team-header))
     (apply str
            (map (fn [g]
                   (str
                    (format "Group %s\n" (str/upper-case (name g) ))
                    (apply str
                           (map (fn [[k v]] (str "  " (render-team [k v])) )
                                (team-filter-group g teams)
                                )
                           )
                    "\n"))
                 groups)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Normalize data
(defn ->kebab-keyword [k]
  (-> k
      name
      (str/replace #"([a-z])([A-Z])" "$1-$2") ; camelCase
      (str/replace "_" "-")                   ; snake_case
      str/lower-case
      keyword))

(defn normalize-keys [data]
(walk/postwalk
 (fn [x]
   (if (map? x)
     (into {}
           (for [[k v] x]
             [(->kebab-keyword k) v]))
     x))
 data))

(defn normalize-fixture [f]
;;(print  (str (:MatchNumber f)
;;             (:home-team f) "[" (team-id (:home-team f)) "]"))
;;(print " v ")
;;(println (str  (:away-team f) "[" (team-id (:away-team f)) "]"))

{:fixture-id (:match-number f)
 :home       (team-id (:home-team f))
 :away       (team-id (:away-team f))
 :start-time (api->iso-utc (:date-utc f))
 :venue      (:location f)})

(defn normalize-fixtures [raw]
(->> raw
     normalize-keys
     (map normalize-fixture)
     vec))

;; Need to define fixtures first
;; (def fixtures-by-id
;;   (into {} (map (juxt :fixture-id identity) fixtures)))

(def data
  (normalize-fixtures (read-data)))

(defn load-results []
  (read-string (slurp (str "data/" event-name "/results.edn"))))

;; (def results [])
(def results (load-results))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Games Report
(defn fixtures-by-date [fixtures]
(sort-by :start-time fixtures))

(defn unplayed? [fixture]
(nil? (:home-score fixture)))

(defn results-by-game
[results]
(into {}
      (map (juxt :game identity))
      results))

(defn team-name [team-id]
(get-in teams [team-id :name] (name team-id)))

(defn team-string [team-id]
(-> team-id
    name
    clojure.string/upper-case))

(defn involves-team?
[team {:keys [home away]}]
(or (= team home)
    (= team away)))

;; External: Uses 'results' data loaded above
(defn render-fixture
[{:keys [fixture-id start-time home away venue]}]
(let [result ((results-by-game results) fixture-id)]
  (format " %3s%1s %s  %-20s vs %-20s  %s"
          fixture-id
          (if result "-" " ")
          (localtime start-time)
          (team-name home)
          (team-name away)
          venue)))

;; External: Uses 'results' data loaded above
(defn render-fixture2
[{:keys [fixture-id start-time home away venue]}]
(let [result ((results-by-game results) fixture-id)]
  (format " %3s%1s %s  %-6s  vs  %-6s  %s"
          fixture-id
          (if result "-" " ")
          (localtime start-time)
          (team-string home)
          (team-string away)
          venue)))

(defn fixtures-report [fixtures]
(str
 "FIFA World Cup 2026 Fixtures\n"
 "============================\n\n"
 (->> fixtures
      fixtures-by-date
      (map render-fixture2)
      (clojure.string/join "\n"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; To use - (render-fixture-org data)
;; Past into org-journal file to include in agenda.
(defn render-fixture-org
[{:keys [fixture-id start-time home away venue]}]
(format "**** FIFA26 Game %s: %-3s vs %-3s - %s\n<%s>"
        fixture-id
        (team-string home)
        (team-string away)
        venue
        (localtime-org start-time)
        ))

(defn fixtures-report-org [fixtures]
  (str
   "*** FIFA World Cup 2026 Fixtures\n"
   (->> fixtures
        fixtures-by-date
        (map render-fixture-org)
        (clojure.string/join "\n"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Statistics
;; Games are stored in a list of maps -'results'
(def score-re #"(\d+)(?:\((\d+)\))?(R*)")

(defn parse-score [s]
  (when-let [[_ score pens reds] (re-matches score-re s)]
    (cond-> {:score (parse-long score)}
      pens (assoc :penalties (parse-long pens))
      (seq reds) (assoc :red (count reds)))))

(deftest parse-score-test
  (testing "Extract game results from scoreboard string"
    (let [input     "3"
          expected  {:score 3}]
      (is (= (parse-score input)
             expected)))))

;; (parse-score "3")
;; => {:score 3}

;; (parse-score "3RR")
;; => {:score 3, :red 2}

;; (parse-score "1(2)")
;; => {:score 1, :penalties 2}

;; (parse-score "1(3)R")
;; => {:score 1, :penalties 3, :red 1}

;; (parse-score "1(4)RR")
;; => {:score 1, :penalties 4, :red 2}




(defn game-outcome [scoreboard]
  (let [[teama teamb] (keys scoreboard)
        teama-score   (teama scoreboard)
        teamb-score   (teamb scoreboard)
        ]

    {teama teama-score
     teamb teamb-score}
    ))

(defn process-data [games results teams]

)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn report-games [data]
(println event-title)
(r/report-games data)
)

(defn report-teams [data]
(println event-title)
(r/report-teams data)
)

;; (defn report [data]
;; (println event-title)
;; (println (r/report-games data))
;; (println (r/report-teams data))
;; (println (r/report-games-timeline data))
;; )

(defn report-games-save [data]
(let [file (str (-> data :details :datadir) "report-games.txt")]
(spit file (r/report-games data))
))

(defn report-teams-save [data]
(let [file (str (-> data :details :datadir) "report-teams.txt")]
(spit file (r/report-teams data))
))

(defn report-save [data]
(report-games-save data)
(report-teams-save data))

(defn report-games-print [data]
(let [file (str (-> data :details :datadir) "report-games.txt")]
(println "Printing games report")
(sh/sh "/usr/bin/enscript" "-B" "-l" "--margins=0:0:0:0" file))
)

(defn report-teams-print [data]
(let [file (str (-> data :details :datadir) "report-teams.txt")]
(println "Printing teams report")
(sh/sh "/usr/bin/enscript" "-B" "-l" "--margins=0:0:0:0" file))
)

(defn report-print [data]
(println (report-games-print data))
(println (report-teams-print data))
)

(defn -main [& options]
(println "-" options "-")
(let [command (if options
                (first options)
                nil)]
(case command
  "help"         (help)
  ;; "report"       (report data)
  ;; "report-save"  (report-save data)
  ;; "report-print" (report-print data)
  ;; "fixtures"     (println (fixtures-report data))
  (help)
  )
))
