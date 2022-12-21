;; To load, use:
;;  (require ['sporting-fixtures.2022-fifa-world-cup :as 'c])

(ns sporting-fixtures.2022-fifa-world-cup
  ;; (:gen-class)
  (:use [clojure.data.xml])
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [clojure.test :as t]
   [java-time :as time]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
(defn reload []
  (require ['sporting-fixtures.2022-fifa-world-cup :as 'c] :reload))

(defn run-tests []
  (clojure.test/run-tests 'sporting-fixtures.2022-fifa-world-cup))

(defn help []
  (println ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;")
  (println ";; This is a 'standalone' module for the FIFA World Cup 2022")
  (println "")
  (println ";; Module loads data from file into 'c/data'")
  (println ";; Suggestion: manipulate c/data onto data")
  (println ";;   (def data c/data)")
  (println "")
  (println ";; Display data")
  (println ";;   (clojure.pprint/pprint data)")
  (println "")
  (println ";; Write data to file")
  (println ";;   (c/data-write data)")
  (println "")
  (println ";; Update :venues (pre-populate or reset)")
  (println "(def data (conj data {:venues (c/venues-init (:fixtures data))}))")
  (println "")
  (println ";; Update :venues (pre-populate or reset)")
  (println "(def data (conj data {:venues (c/venues-init (:fixtures data))}))")
  (println)
  (println ";; Update :results (pre-populate or reset)")
  (println "(def data (conj data {:results (c/results-init (:fixtures data))}))")
  (println)
  (println ";; Reports")
  (println "(c/reload) (println (c/report-games c/data))  ;; Display Scheduled Games.")
  (println "(c/reload) (c/report-games-save c/data)       ;; Save Games Report"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Championship Details
(defn details-init []
  {:title    "FIFA World Cup"
   :location "Qatar"
   :code     :football
   :date     "2022"
   :url      "https://en.wikipedia.org/wiki/FIFA_World_Cup"
   :datadir  "data/2022-fifa-world-cup/"
   :fixtures "data/2022-fifa-world-cup/fixtures.json"
   :data     "data/2022-fifa-world-cup/data.clj"})
(def details (details-init))

(defn fixtures-init []
  (json/read-str (slurp (details :fixtures))
                 :key-fn keyword))
(def fixtures (fixtures-init))

(defn teams-init [] {})
(def teams (teams-init))

(defn venues-init [] {})
(def venues (venues-init))

(defn results-init [] {})
(def results (results-init))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Read data from file and create if it doesn't exist
(defn data-read [filename]
  (read-string (slurp filename)))

(defn data-init []
  (let [filename (:data details)]
    (if (and (some? filename) (.exists (io/file filename)))
      (data-read filename)
      {:data     filename
       :details  (details-init)
       :fixtures (fixtures-init)
       :teams    (teams-init)
       :venues   (venues-init)
       :results  (results-init)})))
(def data (data-init))

(defn data-write [data]
  (spit (:data data)
        (clojure.pprint/write data
                              :stream nil)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Process games
;; This is not needed if ":key-fn" is used when json file is read in.
(defn convert-strings-to-keywords
  "Convert a hash-map "
  [hash]
  (reduce conj {}
          (map (fn [[k v]] [(keyword k) v])
               (seq hash))))

(defn convert-array-of-hash
  "Convert all the array elements, where they are hash-maps with strings as
  keys, to an array with hash-maps indexed by keywords."
  [array]
  (map convert-strings-to-keywords array))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Get list of team names, from home teams.
(defn get-home-teams [games]
  (sort (vec (set (map (fn [x] (:HomeTeam x)) games)))))

(defn teams-init [games]
  (reduce conj {}
          (map (fn [team] [team {:id team
                                 :name team}])
               (get-home-teams games))))

(defn map-team-name-init [teams]
  (reduce conj {}
          (map (fn [[k v]] [(:name v) (:id v)])
               (seq teams)))) ()

(defn map-team-keyword-init [teams]
  (let [map-teams-name (map-team-name-init teams)]
    (clojure.set/map-invert map-teams-name)))

(def map-team-name (map-team-name-init (:teams data)))
(def map-team-keyword (map-team-keyword-init (:teams data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Venues
(defn get-venues [games]
  (sort (vec (set (map (fn [x] (:Location x)) games)))))

(defn venues-init [games]
  (reduce conj {}
          (map (fn [venue] [venue {:id venue
                                   :name venue}])
               (get-venues games))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Results
(defn results-init [games]
  (reduce conj []
          (map (fn [game]
                 (conj game
                       {:teams      []
                        :scoreboard {}
                        :score      {}
                        :result     {}
                        :summary    {}}))
               games)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; map keywords to standard values
;; :MatchNumber -> :game (int)
;; :RoundNumber -> :round (int)
;; :Group -> not-used
;; :HomeTeam :AwayTeam -> :teams []
;; . -> :score {}
;; . -> :result {}
(defn fixtures-transform-data [fixture]
  (let [home (map-team-name (fixture :HomeTeam))
        away (map-team-name (fixture :AwayTeam))]
    (conj fixture {:round (fixture :RoundNumber)
                   :game  (fixture :MatchNumber)
                   :teams [home away]
                   :score {home ""
                           away ""}
                   :result {home {}
                            away {}}})))

(defn filter-fixtures [fixture]
  {:game   (:game fixture)
   :round  (:round fixture)
   :teams  (:teams fixture)
   :score  (:score fixture)
   :result (:result fixture)})

(defn create-data-initial [fixtures]
  (map filter-fixtures fixtures))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Load Data

;;(def games        (-> data :rounds :data))
;;(def games-fields (-> data :rounds :fields))
;;(def teams        (-> data :teams  :data))
(def games        (-> data :games))
(def teams        (-> data :teams))
(def venues       (-> data :venues))

;; (defn results-by-round
;;   [Games]
;;   (Map-indexed (fn [i a]
;;                  (sort-by sort-kEy-game
;;                           >
;;                           (map-id (reduce tally-up-round {} (subvec games 0 a)))))
;;                (range 1 10)))

;; (def ladder-by-round [])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game Results

;; Calculate ladder points based on game score
;; AFL Rules
;; The 'win/lose/draw' stats are from the home team point-of-view.
;; These stats are reversed for away team.
(defn game-points [home-score away-score]
  (if (or (= home-score nil)
          (= away-score nil))
    {:home nil :away nil :won nil :lost nil :draw nil}
    (if (> home-score away-score)
      {:home 4 :away 0 :won 1 :lost 0 :draw 0}
      (if (< home-score away-score)
        {:home 0 :away 4 :won 0 :lost 1 :draw 0}
        {:home 2 :away 2 :won 0 :lost 0 :draw 1}))))

;; Tests
(t/deftest game-points-test
  (t/is (= (game-points 2 1)     {:home 4 :away 0 :won 1 :lost 0 :draw 0}))
  (t/is (= (game-points 1 2)     {:home 0 :away 4 :won 0 :lost 1 :draw 0}))
  (t/is (= (game-points 1 1)     {:home 2 :away 2 :won 0 :lost 0 :draw 1}))
  (t/is (= (game-points 1 nil)   {:home nil :away nil :won nil :lost nil :draw nil}))
  (t/is (= (game-points nil 1)   {:home nil :away nil :won nil :lost nil :draw nil}))
  (t/is (= (game-points nil nil) {:home nil :away nil :won nil :lost nil :draw nil})))

(defn game-diff [score1 score2]
  (if (or (= score1 nil)
          (= score2 nil))
    nil
    (- score1 score2)))

;; Tests
(t/deftest game-diff-test
  (t/is (= (game-diff 1 1) 0))
  (t/is (= (game-diff 2 1) 1))
  (t/is (= (game-diff 1 2) -1))
  (t/is (= (game-diff nil 0) nil))
  (t/is (= (game-diff 0 nil) nil)))

;; Game Data format
(defn game-result
  "Calculate statistics from game results."
  [game]
  (let [home (keyword (nth (:teams game) 0))
        away (keyword (nth (:teams game) 1))
        home-score (home (:score game))
        away-score (away (:score game))
        home-diff (game-diff home-score away-score)
        away-diff (game-diff away-score home-score)
        points (game-points home-score away-score)
        home-points (:home points)
        away-points (:away points)]
    (if (nil? (:score game))
      {home nil away nil}
      {home {:played  1
             :won     (:won  points)
             :lost    (:lost points)
             :draw    (:draw points)
             :points  home-points
             :for     home-score
             :against away-score
             :diff    home-diff}
       away {:played  1
             :won     (:lost points)
             :lost    (:won  points)
             :draw    (:draw points)
             :points  away-points
             :for     away-score
             :against home-score
             :diff    away-diff}})))

;; Tests
(t/deftest game-result-test
  (t/is (= (game-result {:teams [:adl :mel] :score {:adl 100 :mel 80}})
           {:adl {:played 1, :won 1 :lost 0 :draw 0 :points 4, :for 100, :against  80, :diff 20},
            :mel {:played 1, :won 0 :lost 1 :draw 0 :points 0, :for  80, :against 100, :diff -20}}) "game-result-1 - Game Result")
  (t/is (= (game-result {:teams [:adl :mel] :score {:adl nil :mel 80}})
           {:adl {:played 1 :won nil :lost nil :draw nil :points nil, :for nil, :against 80, :diff nil},
            :mel {:played 1 :won nil :lost nil :draw nil :points nil, :for 80, :against nil, :diff nil}}) "game-result-2 - Incomplete game result"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tallying / Combining match statistics

;; For the given key, add the value in result map to the tally map value and
;; return a key,value pair.
(defn tally-up-value [key tally result]
  {key (apply + (remove nil? [(key tally) (key result)]))})

(t/deftest tally-up-value-test
  (t/is (= (tally-up-value :played {:played 1} {:played 2})
           {:played 3})
        "tally-up-value"))

;; For the tally in a team, add new results and return all the data
(defn tally-up-team [tally result]
  (reduce conj
          [(tally-up-value :played  tally result)
           (tally-up-value :won     tally result)
           (tally-up-value :lost    tally result)
           (tally-up-value :draw    tally result)
           (tally-up-value :points  tally result)
           (tally-up-value :for     tally result)
           (tally-up-value :against tally result)
           (tally-up-value :diff    tally result)
           {:fulltime true}]))

;; Test - These stats are not really correct
(t/deftest tally-up-team-test
  (t/is (= (tally-up-team {:played 1 :won 1 :lost 0 :draw 0 :points 4}
                          {:played 1 :lost 1 :against 10 :points 0})
           {:played 2
            :won 1
            :lost 1
            :draw 0
            :points 4
            :for 0
            :against 10
            :diff 0
            :fulltime true})  "tally-up-team"))

;;
(defn tally-up-match [tally game]
  (let [home (keyword (nth (:teams game) 0))
        away (keyword (nth (:teams game) 1))
        result (game-result game)]
    (if (or (nil? (home result))
            (nil? (away result)))
      (conj
       tally
       {home (conj {} (home tally) {:fulltime nil})
        away (conj {} (away tally) {:fulltime nil})})
      (conj
       tally
       {home (tally-up-team (home tally) (home result))
        away (tally-up-team (away tally) (away result))}))))

;;
(t/deftest tally-up-match-test
  (t/is (= (tally-up-match {} {:teams [:adl :mel] :score {:adl 100 :mel 80}})
           {:adl
            {:played 1,
             :won 1
             :lost 0,
             :draw 0,
             :points 4,
             :for 100,
             :against 80,
             :diff 20,
             :fulltime true},
            :mel
            {:played 1,
             :won 0,
             :lost 1,
             :draw 0,
             :points 0,
             :for 80,
             :against 100,
             :diff -20,
             :fulltime true}})
        "tally-up-match"))

;; Tally up the results of a round of games.
;; Fancy tally-up-match. Applied to each game.

;; TODO Could probably simplify this process by doing the tallying to game data
;; which has already been expanded into game result data. Would probably need to
;; add a :team value into the data, and also saving it as an array. (Probably
;; best done in 'game-result')

(defn tally-up-round [tally round]
  (reduce tally-up-match tally round))

(t/deftest tally-up-round-test
  (t/is (= (tally-up-round {}
                           [{:teams [:adl :gws] :score {:adl 100 :gws 80}}
                            {:teams [:pa  :ess] :score {:pa   75 :ess 75}}])

           {:adl
            {:played   1,
             :won      1,
             :lost     0,
             :draw     0,
             :points   4,
             :for     100,
             :against  80,
             :diff     20,
             :fulltime true},
            :gws
            {:played    1,
             :won       0,
             :lost      1,
             :draw      0,
             :points    0,
             :for      80,
             :against 100,
             :diff    -20,
             :fulltime true},
            :pa
            {:played   1,
             :won      0,
             :lost     0,
             :draw     1,
             :points   2,
             :for     75,
             :against 75,
             :diff     0,
             :fulltime true},
            :ess
            {:played   1,
             :won      0,
             :lost     0,
             :draw     1,
             :points   2,
             :for     75,
             :against 75,
             :diff     0,
             :fulltime true}})
        "tally-up-round"))

;; Extact the data point to sort on.
;; Assumes two value array eg. {:adl {:points 10}}
(defn tally-sort-key [x key]
  (-> x vals first key))

;; Test
(t/deftest tally-sort-key-test
  (t/is (= (tally-sort-key {:adl {:points 4}} :points)
           4)
        "tally-sort-key"))

;; Explicit Sort Functions
(defn team-points [x]
  (tally-sort-key x :points))

;; Test
(t/deftest team-points-test
  (t/is (= (team-points {:adl {:played 1 :points 4}})
           4)
        "tally-points"))

;; Who is winning?
(defn sort-key [stats]
  (+ (* 1000 (:points (first (vals stats)))) (:diff (first (vals stats)))))

(defn sort-key-game [stats]
  (+ (* 1000 (:points (first (vals stats)))) (:diff (first (vals stats)))))

;; Test
(t/deftest sort-key-game-test []
  (t/is (= (sort-key-game {:adl {:points 26 :diff 37}})
           26037)
        "sort-key-game"))

;; Array based function
(defn map-pos
  "Given an array of hashmaps, inserts the array index into the maps using the
  key :pos. Useful for sorting and display."
  [array]
  (map-indexed (fn [i a]
                 (conj a {:pos i}))
               array))

;; Test
(t/deftest map-pos-test
  (t/is (= (map-pos [{:name :a} {:name :b}])
           [{:name :a :pos 0} {:name :b :pos 1}])
        "map-pos"))

;; Hash based function
(defn map-pos2
  "Given an array of maps, insert the array index into the element maps as :pos. This is
  useful for sorting a map."
  [data-map]
  (apply conj (map-indexed
               (fn [i a]
                 (let [k (first (keys a))
                       v (first (vals a))]
                   {k (conj v {:pos i})}))
               data-map)))

;; Test
(t/deftest map-pos2-test
  (t/is (= (map-pos2 [{:a {:name :a}} {:b {:name :b}}])
           {:a {:name :a :pos 0} :b {:name :b :pos 1}})
        "map-pos2"))

(defn map-id
  "Inserts the map key into the element map. Useful for display."
  [objects]
  (apply conj
         (map (fn [a] {(first a) (conj {:id (first a)} (second a))})
              objects)))
;; Test
(t/deftest map-id-test
  (if (= (map-id {:a {} :b {}})
         {:a {:id :a} :b {:id :b}})
    "map-id"))

;; Filter functions
;; Returns all the games up to and including 'round'
(defn filter-round [games round]
  (filter (fn [a] (= (:round a) round)) games))

(defn filter-rounds [games round]
  (filter (fn [a] (<= (:round a) round)) games))

(defn filter-game [games game]
  (filter (fn [a] (= (:game a) game)) games))

(defn filter-team [games team]
  (filter (fn [a] (or
                   (= (keyword (first (:teams a))) team)
                   (= (keyword (second (:teams a))) team))) games))

;; Test
(t/deftest filter-rounds-test
  (let [games [{:game 1,
                :teams [:mel, :wb],
                :round 1,
                :result {},
                :score {:mel 97, :wb 71},
                :venue "MCG"},
               {:game 2,
                :teams [:cal, :ric],
                :round 1,
                :result {},
                :venue "MCG"},
               {:game 9,
                :teams [:adl. :pa]
                :round 2,
                :result {},
                :venue "Adelaide Oval"}]]
    (t/is (= (filter-rounds games 1)
             [{:game 1,
               :teams [:mel :wb],
               :round 1,
               :result {},
               :score {:mel 97, :wb 71},
               :venue "MCG"}
              {:game 2, :teams [:cal :ric], :round 1, :result {}, :venue "MCG"}])
          "filter-rounds")
    ;; Debug
    ;; (clojure.pprint/pprint (filter-rounds games 1))
    ))

;; Array summing results up to that round
(defn results-by-round
  [games]
  (map-indexed (fn [i a]
                 (sort-by sort-key-game
                          >
                          (map-id (reduce tally-up-round {} (filter-rounds games a)))))
               (range 1 23)))

;; Test
(defn results-by-round-test []
  (let [games [{:game 1,
                :teams [:mel, :wb],
                :round 1,
                :result {},
                :score {:mel 97, :wb 71},
                :venue "MCG"},
               {:game 2,
                :teams [:cal, :ric],
                :round 1,
                :result {},
                :venue "MCG"},
               {:game 9,
                :teams [:adl. :pa]
                :round 2,
                :result {},
                :venue "Adelaide Oval"}]]
    (if (= (filter-rounds games 1)
           [{:game 1,
             :teams [:mel :wb],
             :round 1,
             :result {},
             :score {:mel 97, :wb 71},
             :venue "MCG"}
            {:game 2, :teams [:cal :ric], :round 1, :result {}, :venue "MCG"}])
      (println "Ok")
      (println "Not Ok"))
    (results-by-round games)))

(def ladder-by-round
  (map (fn [a] (keys (sort-by sort-key-game > a)))
       (results-by-round games)))

(def ladder-by-round2
  (map (fn [a] (map-pos2 (sort-by sort-key-game > a)))
       (results-by-round games)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; (defn write-data-out []
;;   (let [data-new {:data    (:data data)
;;                   :details details
;;                   :games   games
;;                   :teams   times
;;                   :venues  venues
;;                   :results results}]
;;     (write-data data-new (:data data))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game Report

(defn convert-stage-group [stage group]
  (format "%-2s%s"
          (case stage
            :group          "G"
            :group-16       "S"
            :quarter-final  "QF"
            :semi-final     "SF"
            :third-play-off "PO"
            :final          "F"
            "")
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
            "")))

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
                (= (count (:scoreboard game)) 2))
         (let [home-scoreboard (home (:scoreboard game))
               away-scoreboard (away (:scoreboard game))]
           (format "%s %3s v %-3s %s  "
                   (s/upper-case (name home))
                   home-scoreboard
                   away-scoreboard
                   (s/upper-case (name away))))
         (format "%s v %s  "
                 (s/upper-case (name home))
                 (s/upper-case (name away))))))
   (if (and (some? (:summary game))
            (string? (:summary game)))
     (:summary game)
     "")))

(defn report-games [data]
  (str
   "-------------------------------------------------------------------------------\n"
   (apply str
          (map (fn [game]
                 (format "%3s  %s  %s  %-3s  %s\n"
                         (:MatchNumber game)
                         (:RoundNumber game)
                         (convert-to-localtime (:DateUtc game) "Australia/Adelaide")
                         (convert-stage-group (:stage game) (:group game))
                         (format-game-result game)))
               (:results data)))
   "-------------------------------------------------------------------------------\n"))

(defn report-games-save [data]
  (spit (str (-> data :details :datadir) "schedule-2022-fifa-world-cup.txt")
        (report-games data)))
