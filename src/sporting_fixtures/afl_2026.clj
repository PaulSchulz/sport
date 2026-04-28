(ns sporting-fixtures.afl-2026
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clojure.string   :as str]
            [clojure.walk     :as walk]
            [cheshire.core    :as json]
            [clj-yaml.core    :as yaml]    ;; YAML output
            [fipp.edn         :as fipp :refer [pprint] :rename {pprint fipp}]
            ;; Time manipulation
            [clj-time.core    :as t]
            [clj-time.format  :as f]
            [clj-time.local   :as l]
            ;; [clojure.pprint 1  :as pprint]
            [clojure.test :refer :all]
            )
  (:import
   [java.time ZonedDateTime ZoneId]
   [java.time Instant Duration]
   [java.time.format DateTimeFormatter])
  (:gen-class)
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def event-name   "afl-2026")
(def event-data   "data/")
(def filename     (str event-data event-name ".yml"))

(defn reload []
  (do
    (println "Reloading afl-2026")
    (use 'sporting-fixtures.afl-2026 :reload-all)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn help []
  (println
   (str/join
    "\n" [
          ";; --- Well done, you got here. ---"
          ";; To display a game fixture report"
          "(println (fixtures-report data))"
          ""
          ";; To save this report to a file"
          "(save-fixtures-report)"
          ""
          ";; To display a ladder report"
          "(println (ladder-report data results teams))"
          ""
          ";; To save this report to a file"
          "(save-ladder-report)"
          ""
          ";; To display the next week's fixtures"
          "(println (report-next-week-games (next-week-games data)))"
          ""
          ]
    ))
  ;; Process downloaded file
  ;; Use from command line with:
  (println
   (str/join
    "\n" [
          ";; From the command line"
          "lein run -m sporting-fixtures.afl-2026"
          ""
          ";; Printing"
          "lein run -m sporting-fixtures.afl-2026 | enscript -B -f Courier9 | lp"
          ]
    )))


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

(defn read-data []
  (read-string (slurp (str "data/" event-name "/fixtures.edn"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Teams
(def teams
  (read-string (slurp (str "data/" event-name "/teams.edn"))))

(def team-name->id
  (into {}
        (for [[id team] teams]
          [(:name team) id])))

(defn team-id
  [team-name]
  (or (get team-name->id team-name)
      (throw (ex-info "Unknown team"
                      {:team team-name}))))

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

(def results
  (read-string (slurp (str "data/" event-name "/results.edn"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Layer
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Extract runs from scoreboard string
(defn runs-from-score [s]
  (when s
    (Long/parseLong
     (re-find #"\d+" s))))

(deftest ^:test runs-from-score-test
  (is (= 117 (runs-from-score "117/5(10.1/11)")))
  (is (= 0   (runs-from-score "0/0(0)"))))

;; Extract balls from scoreboard string
(defn balls-from-score [s]
  (when-let [[_ overs balls] (or (re-find #"\((\d+)\.(\d+)" s)
                                 (re-find #"\((\d+)\)" s))]
    (+ (* 6 (Long/parseLong overs))
       (Long/parseLong (or balls "0")))))

(deftest ^:test balls-from-score-test
  (is (= 61 (balls-from-score "117/5(10.1/11)")))
  (is (= 0   (balls-from-score "0/0(0)"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Report Layer
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Results chart
(defn ruler [n]
  (str
   (apply str (map #(quot % 10) (range 1 (inc n))))
   ;; "\n"
   ;; (apply str (map #(mod % 10) (range 1 (inc n))))
   ))

(deftest ^:test ruler-test
  (testing "Debugging ruler"
    (let [input 20
          result "00000000011111111112"]
      (is (= (ruler input)
             result)))))

(defn results-by-game
  [results]
  (into {}
        (map (juxt :game identity))
        results))

(deftest ^:test results-by-game-test
  (testing "Index results by game number in a map"
    (let [input [{:game 1,
                  :scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"},
                  :summary "SCO won by 5 wickets (5 balls left)"}
                 {:game 2,
                  :scoreboard {:ren "212/5(20)", :hea "198/8(20)"},
                  :summary "REN won by 14 runs"}]
          result  {1 {:game 1,
                      :scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"},
                      :summary "SCO won by 5 wickets (5 balls left)"},
                   2 {:game 2,
                    :scoreboard {:ren "212/5(20)", :hea "198/8(20)"},
                    :summary "REN won by 14 runs"}}]
      (is (= (results-by-game input)
             result)))))

;; Pull out game statistics from scoreboard
;; Needs to be given home and away teams as result data is stored in an unordered
;; map.
;; This could probably be merged/used in 'delta-from-game' (first step)
(defn stats-from-game [home away {:keys [scoreboard]}]
  (cond
    (nil? scoreboard) {:home home :away away}
    :else
    (let [teams (keys scoreboard)]
      (when-not (some #{:tbd} teams)
        (let [[[home home-score]
               [away away-score]] (seq scoreboard)
              home-runs (runs-from-score home-score)
              away-runs (runs-from-score away-score)
              home-balls (balls-from-score home-score)
              away-balls (balls-from-score away-score)
              winner (cond (> home-runs away-runs) home
                           (< home-runs away-runs) away
                         :else nil)
              ]
          {:home home
           :away away
           :home-runs home-runs
           :away-runs away-runs
           :home-balls home-balls
           :away-balls away-balls
           :winner winner}))
      )))

(deftest ^:test stats-from-game-test
  (testing "Pull out game statistics from scoreboard"
    (let [home :six
          away :sco
          result {:scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"}}
          output {:home :six, :away :sco,
                  :home-runs 113, :away-runs 117,
                  :home-balls 66, :away-balls 61
                  :winner :sco}]
      (is (= (stats-from-game home away result)
             output)))))

;; Use fixture details to lookup home and away teams
(defn get-stats-from-fixture-result [fixture result]
  (let [home (:home fixture)
        away (:away fixture)]
    (stats-from-game home away result)))

(deftest ^:test get-stats-from-fixture-result-test
  (testing "get statistics from fixture"
    (let [fixture {:home :six :away :sco}
          results {:scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"}}
          result {:home :six, :away :sco,
                  :home-runs 113, :away-runs 117,
                  :home-balls 66, :away-balls 61
                  :winner :sco}
          ]
      (is (= (get-stats-from-fixture-result fixture results)
             result)))))

(defn lookup-fixture-result [fixture results]
  (let [fixture-id (:fixture-id fixture)
        results-by-id (results-by-game results)
        result (get results-by-id fixture-id)]

    result
    ))

(deftest ^:test lookup-fixture-result-test
  (testing "Lookup result from a fixture"
    (let [fixture {:fixture-id 1,
                   :home :sco, :away :six,
                   :start-time "2025-12-14T08:15:00Z", :venue "Perth Stadium"}
          results [{:game 1,
                    :scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"},
                    :summary "SCO won by 5 wickets (5 balls left)"}]
          result (first results)
          ]
      (is (= (lookup-fixture-result fixture results)
             result)))))

;; Width of game table cell
(def game-width 1)

;; Create results report header
(defn results-header [games]
  (let [label-width game-width
        total-width (* game-width (count games))]
    (str
     (format "%-6s|" "")
     (apply str
            (map-indexed
             (fn [i _]
               ;; (format (str "%" game-width "d") (inc i))
               (if (= 0 (quot (inc i) 10))
                 " "
                 (format "%1d" (quot (inc i) 10) )))
             games))
     "\n"
     (format "%-6s|" "")
     (apply str
            (map-indexed
             (fn [i _]
               (format (str "%" game-width "d") (mod (inc i) 10)))
             games))
     "\n"
     (format "%-6s|" "Team")
     (apply str
            (repeat total-width "-"))
     "\n")))

(deftest ^:test results-header-test
  (testing "Result report header format"
    (let [input [{:game 1,
                  :scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"},
                  :summary "SCO won by 5 wickets (5 balls left)"}
                 {:game 2,
                  :scoreboard {:ren "212/5(20)", :hea "198/8(20)"},
                  :summary "REN won by 14 runs"}]
          result "      |  1  2\nTeam  |--\n"]
      (is (= (results-header input)
             result)))))

;; Generate result string to display in cell
;; Update
(defn result-cell [team stats]
  (cond
    (or (= team (:home stats)) (= team (:away stats)))
    (if (not (nil? (:winner stats)))
      (if (= team (:winner stats)) "X" "+" )
      "=")

    :else "-"
    )
  )

(deftest ^:test result-cell-test
  (testing "Generate result string to display in cell 1"
    (let [team :sco
          stats (stats-from-game :six :sco {:scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"}})
          output "--*"]
      (is (= (result-cell team stats)
             output))))
  (testing "Generate result string to display in cell 2"
    (let [team :six
          stats (stats-from-game :six :sco {:scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"}})
          output "--+"]
      (is (= (result-cell team stats)
             output)))))

(defn team-results-row [team fixtures results]
  (let [results-by-id (results-by-game results)]
    (str
     (format "%-6s|" (str/upper-case (name team)))
     (apply str
            (map #(let [fixture %
                        result (lookup-fixture-result fixture results)
                        stats (get-stats-from-fixture-result fixture result)]
                    (result-cell team stats)
                    )
                 fixtures)))))

(deftest ^:test team-results-row-test
  (testing "Row of game results"
    (let [team    :sco
          fixtures [{:fixture-id 1,
                     :home :sco, :away :six,
                     :start-time "2025-12-14T08:15:00Z", :venue "Perth Stadium"}
                    {:fixture-id 2,
                     :home :ren, :away :hea,
                     :start-time "2025-12-15T08:15:00Z", :venue "GMHBA Stadium"}]
          results [{:game 1,
                    :scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"},
                    :summary "SCO won by 5 wickets (5 balls left)"}]
          output  "SCO   |--*---"
          ]
      (is (= (team-results-row team fixtures results)
             output)))))

(defn results-report [fixtures results teams]
  (let [fixtures (sort-by :start-time fixtures)]
    (str
     (results-header fixtures)
     (apply str
            (for [[team {:keys [placeholder?]}] teams
                  :when (not placeholder?)]
              (str (team-results-row team fixtures results) "\n")
              ;;(println team)
              )))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Games Report
(defn fixtures-by-date [fixtures]
  (sort-by :start-time fixtures))

(defn unplayed? [fixture]
  (nil? (:home-score fixture)))

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

(defn fixtures-report [fixtures]
  (str
   "AFL 2026 Fixtures\n"
   "=================\n\n"
   (->> fixtures
        fixtures-by-date
        (map render-fixture)
        (clojure.string/join "\n"))))
                                        ;
(defn save-fixtures-report []
  (spit (str "data/" event-name "/fixtures.txt")
        (fixtures-report data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Next Round Report
(defn next-week-games [fixtures]
(let [now (Instant/now)
week-from-now (.plus now (Duration/ofDays 7))]
(->> fixtures
(filter unplayed?)
(filter #(let [start (->instant (:start-time %))]
           (and (.isAfter start now)
                (.isBefore start week-from-now))))
(sort-by #(->instant (:start-time %))))))

(defn render-game-org
  [{:keys [fixture-id start-time home away venue]}]
  (let [result ((results-by-game results) fixture-id)]
    (format "**** AFL: Game %2s - %-3s v %-3s\n<%s>"
            fixture-id
            ;; (if result "-" " ")
            (team-string home)
            (team-string away)
            (localtime-org start-time)
            ;;            venue)))
            )))

(defn report-next-week-games [fixtures]
  (str
   "\n"
   "*** Round\n"
   (->> fixtures
        fixtures-by-date
        (map render-game-org)
        (clojure.string/join "\n"))
   "\n"
   ))
                                        ;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Find next game to be played
(defn next-fixture [fixtures]
  (let [now (Instant/now)]
    (->> fixtures
         (filter unplayed?)
         (filter #(-> (:start-time %)
                      ->instant
                      (.isAfter now)))
         (sort-by #(->instant (:start-time %)))
         first)))

(deftest ^:test next-fixture-test
(let [fixtures [{:fixture-id 1
                 :start-time "2099-01-01T00:00:00Z"
                 :home :six
                 :away :sco}
                {:fixture-id 2
                 :start-time "2099-01-02T00:00:00Z"
                 :home :heat
                 :away :stars}]]
(is (= 1 (:fixture-id (next-fixture fixtures))))))

(defn render-next-fixture [{:keys [start-time home away venue]}]
  (format "Next game:\n\n%s\n%s vs %s\n%s"
          (localtime start-time)
          (team-name home)
          (team-name away)
          venue))

(defn next-game-report [games]
  (if-let [game (next-fixture games)]
    (render-next-fixture game)
    "No upcoming games."))

;; fixture for next team
(defn next-fixture-for-team [fixtures team]
  (let [now (java.time.Instant/now)]
    (->> fixtures
         (filter unplayed?)
         (filter #(involves-team? team %))
         (filter #(-> (:start-time %)
                      ->instant
                      (.isAfter now)))
         (sort-by #(->instant (:start-time %)))
         first)))

(defn render-next-fixture-for-team [fixture team]
  (let [{:keys [start-time home away venue]} fixture
        opponent (if (= team home) away home)]
    (format "Next match for %s:\n\n%s\n%s vs %s\n%s"
            (team-name team)
            (localtime start-time)
            (team-name home)
            (team-name away)
            venue)))

(defn next-match-for-team-report [fixtures team]
  (if-let [fixture (next-fixture-for-team fixtures team)]
    (render-next-fixture-for-team fixture team)
    (format "No upcoming matches for %s."
            (team-name team))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Results / Ladder Report

;; Initialise Ladder data
(defn empty-ladder [teams]
  (into {}
        (for [[team {:keys [placeholder?]}] teams
              :when (not placeholder?)]
          [team {:team team
                 :played 0 :won 0 :lost 0 :tied 0 :points 0
                 :runs-for 0 :runs-against 0
                 :balls-faced 0 :balls-bowled 0
                 :nrr 0.0}])))

(deftest ^:test empty-ladder-test
  (let [teams {:sco {} :str {}}
        expected2 {:sco {:team :sco,
                         :played 0,
                         :won 0,
                         :lost 0,
                         :tied 0,
                         :points 0,
                         :runs-for 0,
                         :runs-against 0,
                         :balls-faced 0,
                         :balls-bowled 0,
                         :nrr 0.0,
                         },
                   :str {:team :str,
                         :played 0,
                         :won 0,
                         :lost 0,
                         :tied 0,
                         :points 0,
                         :runs-for 0,
                         :runs-against 0
                         :balls-faced 0,
                         :balls-bowled 0,
                         :nrr 0.0,
                         }}
        ]
    (is (= (empty-ladder teams) expected2))))

(deftest ^:test tbd-not-in-ladder-test
  (is (not (contains?
            (empty-ladder teams)
            :tbd))))

;; Create ladder-dalta record from rules
(defn ladder-delta [{:keys [home away home-runs away-runs home-balls away-balls]}]
  (let [home-win? (> home-runs away-runs)
        away-win? (> away-runs home-runs)
        home-base {:team home
                   :played 1
                   :runs-for home-runs
                   :runs-against away-runs
                   :balls-faced home-balls
                   :balls-bowled away-balls}
        away-base {:team away
                   :played 1
                   :runs-for away-runs
                   :runs-against home-runs
                   :balls-faced away-balls
                   :balls-bowled home-balls}]
    [(merge home-base
            (cond
              home-win? {:won 1 :points 2}
              away-win? {:lost 1}
              :else     {:tied 1 :points 1}))
     (merge away-base
            (cond
              away-win? {:won 1 :points 2}
              home-win? {:lost 1}
              :else     {:tied 1 :points 1}))]))

(deftest ladder-delta-test
  (testing "Calculate ladder delta from game result rules"
    (let [input      {:home :sco
                      :away :str
                      :home-runs 120
                      :away-runs 121
                      :home-balls 60
                      :away-balls 55}
          expected   [{:team :sco, :played 1,
                       :runs-for 120, :runs-against 121,
                       :balls-faced 60 :balls-bowled 55,
                       :lost 1}
                      {:team :str, :played 1,
                       :runs-for 121, :runs-against 120,
                       :balls-faced 55, :balls-bowled 60,
                       :won 1, :points 2}]]
      (is (= (ladder-delta input)
             expected)))))

;; Creates data deltas
(comment
  (defn delta-from-game [{:keys [scoreboard]}]
    (let [[[home home-score]
                           [away away-score]] (seq scoreboard)
          home-runs (runs-from-score home-score)
          away-runs (runs-from-score away-score)]
      (ladder-delta
                       {:home home
      :away away
      :home-runs home-runs
      :away-runs away-runs})))
  )

;; Create ladder delta from scoreboard
(defn delta-from-game [{:keys [scoreboard]}]
  (let [teams (keys scoreboard)]
    (when-not (some #{:tbd} teams)
      (let [[[home home-score]
             [away away-score]] (seq scoreboard)
            home-runs (runs-from-score home-score)
            away-runs (runs-from-score away-score)
            home-balls (balls-from-score home-score)
            away-balls (balls-from-score away-score)
            ]
        (ladder-delta
         {:home home
          :away away
          :home-runs home-runs
          :away-runs away-runs
          :home-balls home-balls
          :away-balls away-balls})))
    ))

(deftest delta-from-game-test
  (testing "Extract ladder delta details from game result"
    (let [input     {:scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"}}
          expected  [{:team :six, :played 1,
                      :runs-for 113, :runs-against 117,
                      :balls-faced 66, :balls-bowled 61,
                      :lost 1}
                     {:team :sco, :played 1,
                      :runs-for 117, :runs-against 113,
                      :balls-faced 61, :balls-bowled 66,
                      :won 1, :points 2}]]
      (is (= (delta-from-game input)
             expected)))))

;; Apply a single delta
(defn apply-delta [ladder {:keys [team] :as delta}]
  (if (contains? ladder team)
    (reduce-kv
     (fn [l k v]
       (if (= k :team)
         l
         (update-in l [team k] (fnil + 0) v)))
     ladder
     delta)
    ladder))  ;; silently ignore placeholder / unknown teams

(deftest apply-delta-test
  (testing "Apply ladder delta to ladder record"
    (let [ladder   {:str {:team :str,
                          :played 0,
                          :runs-for 0,
                          :runs-against 0,
                          :balls-faced 0,
                          :balls-bowled 0,
                          :won 0,
                          :lost 0,
                          :points 0}}
          delta    {:team :str,
                    :played 1,
                    :runs-for 121,
                    :runs-against 120,
                    :balls-faced 66,
                    :balls-bowled 61,
                    :won 1,
                    :points 2}
          expected {:str {:team :str,
                          :played 1,
                          :runs-for 121,
                          :runs-against 120,
                          :balls-faced 66,
                          :balls-bowled 61,
                          :won 1,
                          :lost 0,
                          :points 2}} ]
      (is (= (apply-delta ladder delta)
             expected)))))

;; Apply all deltas in an array to the ladder data
(defn apply-deltas
  [ladder deltas]
  (reduce apply-delta ladder deltas))

(deftest apply-deltas-test
  (testing "Apply multiple deltas to ladder data"
    (let [ladder   (empty-ladder {:six {} :sco {}})
          deltas   (delta-from-game {:scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"}})
          expected {:six {:team :six,
                          :played 1,
                          :won 0,
                          :lost 1,
                          :tied 0,
                          :points 0,
                          :runs-for 113,
                          :runs-against 117,
                          :balls-faced 66,
                          :balls-bowled 61,
                          :nrr 0.0},
                    :sco {:team :sco,
                          :played 1,
                          :won 1,
                          :lost 0,
                          :tied 0,
                          :points 2,
                          :runs-for 117,
                          :runs-against 113,
                          :balls-faced 61,
                          :balls-bowled 66,
                          :nrr 0.0}}]
      (is (= (apply-deltas ladder deltas)
             expected)))))

;; Build ladder information from results
(defn build-ladder [fixtures results teams]
  (let [ladder (empty-ladder teams)
        deltas (mapcat delta-from-game results)]
    (reduce apply-delta ladder deltas)))

(deftest ^:test build-ladder-test
  (testing "Build ladder information from results"
    (let [fixtures []
          teams    {:six {} :sco {}}
          results  [{:scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"}}]
          expected {:six {:team :six,
                          :played 1,
                          :won 0,
                          :lost 1,
                          :tied 0,
                          :points 0,
                          :runs-for 113,
                          :runs-against 117,
                          :balls-faced 66,
                          :balls-bowled 61,
                          :nrr 0.0},
                    :sco {:team :sco,
                          :played 1,
                          :won 1,
                          :lost 0,
                          :tied 0,
                          :points 2,
                          :runs-for 117,
                          :runs-against 113,
                          :balls-faced 61,
                          :balls-bowled 66,
                          :nrr 0.0}}]
      (is (= (build-ladder fixtures results teams)
             expected)))))

;; Sort ladder for reporting
(defn ladder-rows [ladder]
  [ladder]
  (->> ladder
       vals
       (sort-by (juxt
                 (comp - :points)
                 (comp - :nrr)))))

(deftest ^:test ladder-rows-sort-test
  (let [ladder {:a {:team :a :points 2 :nrr 0.1}
                :b {:team :b :points 2 :nrr 0.5}
                :c {:team :c :points 4 :nrr -0.2}}]
    (is (= [:c :b :a]
           (map :team (ladder-rows ladder))))))

(deftest ^:test ladder-rows-test
  (testing "Sorting ladder rows"
    (let [ladder {:hea {:team :hea
                        :played 8
                        :won 6
                        :lost 2
                        :points 12
                        :nrr 0.87}
                  :six {:team :six
                        :played 8
                        :won 5
                        :lost 3
                        :points 10
                        :nrr 0.42}
                  :hur {:team :hur
                        :played 9
                        :won 6
                        :lost 3
                        :points 12
                        :nrr 0.8}}
          result [:hea :hur :six]]
      (is (= (map :team (ladder-rows ladder))
             result)))))

;; Calculate the 'Net run rate'
(defn calculate-nrr [{:keys [runs-for runs-against balls-faced balls-bowled] :as row}]
  (if (and (pos? balls-faced) (pos? balls-bowled))
    (let [for-rate     (/ runs-for balls-faced)
          against-rate (/ runs-against balls-bowled)]
      (assoc row :nrr (float (* 6 (- for-rate against-rate)))))
    (assoc row :nrr 0.0)))

(defn with-nrr [ladder]
  (into {}
        (for [[team row] ladder]
          [team (calculate-nrr row)])))

(defn render-ladder-row [{:keys [team played won lost tied points nrr]}]
  (format "%-20s %2d %2d %2d %2d %3d %+7.3f"
          (:name (teams team))
          played won lost tied points nrr))

(defn ladder-report [fixtures results teams]
  (let [ladder (build-ladder fixtures results teams)]
    (str
     "Team                     P  W  L  T Pts     NRR\n"
     "-----------------------------------------------\n"
     (->> ladder
          with-nrr
          ladder-rows
          (map-indexed
           (fn [i row]
             (format "%2s %s"
                     (inc i)
                     (render-ladder-row row)
                     )))
          (clojure.string/join "\n")))))

(defn save-ladder-report []
  (spit ("data/" event-name "/ladder.txt")
        (ladder-report data results teams)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Working with results
(comment
  ;; Fixture record
  {:fixture-id 1
   :home :sco
   :away :six
   :start-time "2025-12-14T08:15:00Z"
   :venue "Perth Stadium"
   :scoreboard {:six "113/5(11)", :sco "117/5(10.1/11)"},
   :summary "SCO won by 5 wickets (5 balls left)"
   :outcome {:six {:runs 113, :points 0},
             :sco {:runs 117, :points 2},
             :outcome :sco}}

  ;; Ladder Record
  {:bri
   {:team   :bri
    :played  0
    :won     0
    :lost    0
    :tied    0
    :points  0
    :for     0
    :against 0
    :nrr     0.0}
   }
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Create results table
(defn event-games-table-header []
  (str
   (format " %2s | %-16s | %-10s | %-26s | %s"
           "Id"
           "Time"
           "Teams"
           "Score"
           "Result")
   "\n"
   (str/join (repeat 78 "-"))
   "\n"
   )
  )

;; Assumes 'result' contains points
(defn calculate-game-statistics [game]
  (let [home   (:home   game)
        away   (:away   game)
        result (:result game)]

    (if (and home away result)
      (let [home-points (home result)
            away-points (away result)]
        (if (and home-points away-points)
          (cond
            (> home-points away-points) {home {:w 1 :l 0 :d 0 :pf home-points :pa away-points}
                                         away {:w 0 :l 1 :d 0 :pf away-points :pa home-points}}
            (< home-points away-points) {home {:w 0 :l 1 :d 0 :pf home-points :pa away-points}
                                         away {:w 1 :l 0 :d 0 :pf away-points :pa home-points}}
            :else {home {:w 0 :l 0 :d 1 :pf home-points :pa away-points}
                   away {:w 0 :l 0 :d 1 :pf away-points :pa home-points}}
            )
          nil))
      nil)))

;;
(defn event-games-table [event]
  (let [games (:games event)
        teams (:teams event)
        ;;        team-by-id    (into {} (for [[k v] teams] [k (:name v)]))
        ;;        team-by-name  (clojure.set/map-invert team-by-id)]
        ]
    (str
     (event-games-table-header)
     (str/join
      "\n"
      (map-indexed
       (fn [i x]
         (let [id   (inc i)
               home (:home x)
               away (:away x)
               ]
           (str
            (if (= id 1)  (str "----- Rounds ----------" (str/join (repeat 55 "-")) "\n") "")
            (if (= id 57) (str "----- Finals ----------" (str/join (repeat 55 "-")) "\n") "")
            ;; (format " %2d | %s | %-10s | %-12s  %-12s | %s %s"
            (format " %2d | %s | %-10s | %-20s  %-20s | %s %s"
                    id
                    ;;(:id x)
                    ;;(localtime (:time x))
                    (:time x)
                    (str (if home
                           (str/upper-case (name home)) "---")
                         " vs "
                         (if away
                           (str/upper-case (name away)) "---"))

                    (if (and (:score x)
                             (not= (:score x) {}))
                      (home (:score x))
                      "_-___/__._")

                    (if (and (:score x)
                             (not= (:score x) {}))
                      (away (:score x))
                      "_-___/__._")

                    (if (and (:result x)
                             (not= (:result x) {}))
                      (home (:result x))
                      "-")
                    (if (and (:result x)
                             (not= (:result x) {}))
                      (away (:result x))
                      "-")
                    ;;(conj x {:home home-id} {:away away-id})
                                        ; (calculate-game-statistics x)

                    ))))
       games)
      )
     "\n"
     (str (str/join "" (repeat 78 "-")) "\n")
     )))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn event-games-table-statistics [event]
  (let [games (:games event)
        teams (:teams event)]
    (str
     (str/join
      "\n"
      (map-indexed
       (fn [i x]
         (let [id (inc i)
               home (:home x)
               away (:away x)]
           (str
            (format "%2d | %-10s | %s"
                    id
                    (str (if home
                           (str/upper-case (name home)) "---")
                         " vs "
                         (if away
                           (str/upper-case (name away)) "---"))
                    (calculate-game-statistics x)
                    ))))
       games)
      ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; statistics - Map of team statistics
;; update     - Array with 'team id' and 'statistics update'
;; Calculate the statistics from the games of an event

;; Return statistical data for each game.
;; Format of a vecter with one entry per game, with a map of home and
;; away team points.
(defn calculate-statistics [games]
  (map calculate-game-statistics games))

;;
(defn reduce-statistics [stats]
  (reduce (fn [val coll] (reduce conj val coll))
          [] stats)
  )

;; merge
;; merge-with
;;(reduce into [] stats)

;;(defn create-statistics-updates [games]
;;  (apply merge-with + games)
;;  )

;; TODO - Need to fix this to use new statistics format (map)
;; Apply update to statistics
;;   statistics - is a mapped by team name
;;   update - eg. (teamid
(defn update-statistics [statistics update]
  (let [team         (nth update 0)
        stats-update (if (nth update 1)
                       (nth update 1)
                       {})
        stats-old    (if (team statistics)
                       (team statistics)
                       {})]

    (conj statistics {team (map + stats-old stats-update)}))
  )

(defn event-statistics [games]
  (reduce update-statistics {}
          (reduce-statistics (calculate-statistics games))))

(defn stats-separator []
  "------+----+---------+------------+---------------------------\n"
  )

(defn stats-header []
  (str
   "      |    | Games   | Goals      | Group     Knockout Result\n"
   " Team | Pt | P/W/L/D | Fr/Ag/Diff | Result    Qual 16  8  4  2\n"
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
          (format "%9s   %s%s%s%s%s"
                  (if (:group-stage results)
                    (:group-stage results)
                    "")
                  (if (:group results)
                    (if (= (:group results) "qualified")
                      " *-"
                      " +-"
                      )
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
                      (= (:final results) "runnerup" ) "-o "
                      (= (:final results) "third" )    "-3 "
                      (= (:final results) "fourth" )   "-4 "
                      :else " . "
                      )
                    " . ")
                  )
          ))

(defn event-stats-table [event]
  (let [statistics (event-statistics (:games event))
        results    (:results event)]
    (str
     (stats-header)
     (str/join
      "\n"
      ;;      (map
      ;;       (fn [[k v]] (stats-string k v (k results)))
      ;;       (sort (fn [el1 el2]
      ;;               (if (> (nth (nth el1 1) 0)
      ;;                      (nth (nth el2 1) 0))
      ;;                 true
      ;;                 false))
      ;;        (map (fn [x] x) statistics))
      ;;       )

      )
     ))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (defn results-separator []
    "------+---------------------------------------------------------+-----\n"
    )

  (defn results-header []
    (str
     "      | Game     11111111112222222222333333333344444444445555555|55566\n"
     " Team | 12345678901234567890123456789012345678901234567890123456|78901\n"
     )
    )

  (defn results-string [event team]
    (let [games (:games event)
          teams (:teams event)
          team-by-id    (into {} (for [[k v] teams] [k (:name v)]))
          team-by-name  (clojure.set/map-invert team-by-id)
          ]
      (format "  %3s | %s"
              (str/upper-case (name team))
              (str/join
               ""
               (map-indexed
                (fn [index game]
                  (str
                   (cond
                     (= (+ index 1) 57) "|"
                     :else        ""
                     )
                   (let [home (:home game)
                         away (:away game)
                         ;;statistics (calculate-game-statistics
                         ;;            (conj game {:home home-id} {:away away-id}))
                         statistics (calculate-game-statistics game)
                         ]
                     (if statistics
                       (cond
                         (or (= team home) (= team away))
                         (cond
                           (= (:w (team statistics)) 1) "W"
                           (= (:l (team statistics)) 1) "L"
                           (= (:d (team statistics)) 1) "d"
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
  )
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The following is required to map the team names to keywords.
;; Need to carry through all elements of the event data structure.
(defn remap-event [event]
  (let [games (:games event)
        teams (:teams event)
        team-by-id    (into {} (for [[k v] teams] [k (:name v)]))
        team-by-name  (clojure.set/map-invert team-by-id)
        ]
    {:teams teams
     :games
     (map (fn [game]
            (let [home-id (team-by-name (:home game))
                  away-id (team-by-name (:away game))]
              (conj game {:home home-id} {:away away-id})))
          games)
     }
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-event-report [event]
  (println (:title event))
  (println "From: " (:from (:date event)))
  (println "To:   " (:to   (:date event)))
  (println)
  (println "Games")
  (println (event-games-table (remap-event event)))
  (println)
  ;; Used for debugging
  ;;  (println "Games Statistics")
  ;;  (println (event-games-table-statistics (remap-event event)))
  ;;  (println)
  (println "Results")
  ;; (println (event-results-table (remap-event event)))
  (println)
                                        ;  (println "Statistics")
                                        ;  (println (event-stats-table event))
                                        ;  (println)
                                        ;  (println "Finals")
                                        ;  (println (event-finals-chart event))
  (println)
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main []
  ;; (help)
  ;;  (let [event (read-event-data filename)
  ;;        games (:games event)
  ;;        teams (:teams event)]
  ;;  (println (create-event-report event)))
  (-> (fixtures-report data) println)
  (println)
  ;;  (-> (ladder-report data results teams) println)
  ;;  (println)
  ;;  (-> (next-game-report data) println)
  ;;  (println)
  ;;  (-> (next-match-for-team-report data :adl) println)
  )
