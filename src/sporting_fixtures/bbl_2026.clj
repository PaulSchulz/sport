(ns sporting-fixtures.bbl-2026
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import
   [java.time ZoneId ZonedDateTime]
   [java.time.format DateTimeFormatter])
  (:gen-class))

(def event-name "bbl-2026")
(def event-title "BBL 2026/27")

;; A fixture line is 80 columns. Team names and scores have fixed widths.
;; The home name and home score are right justified, so they sit against "vs".
;; A score such as "117/5(10.1/11)" needs 14 columns.
;; 3 + " " + 19 + " " + 7 + " " + 14 + " vs " + 14 + " " + 7 + " " + 7.
(def default-fixture-format
  {:width 80
   :home-width 7
   :away-width 7
   :score-width 14
   :venue-width 7})

(def teams
  {:sco {:name "Perth Scorchers" :short "Scorch."}
   :six {:name "Sydney Sixers" :short "Sixers"}
   :str {:name "Adelaide Strikers" :short "Striker"}
   :hea {:name "Brisbane Heat" :short "Heat"}
   :hur {:name "Hobart Hurricanes" :short "Hurric."}
   :ren {:name "Melbourne Renegades" :short "Reneg."}
   :sta {:name "Melbourne Stars" :short "Stars"}
   :thu {:name "Sydney Thunder" :short "Thunder"}
   :tba {:name "To be announced" :short "TBA"
         :placeholder? true}})

(def known-venues
  {"MA Chidambaram Stadium" "Chepauk"
   "The Gabba" "Gabba"
   "Corroboree Group Oval Manuka" "Manuka"
   "Ninja Stadium" "Ninja"
   "MCG" "MCG"
   "ENGIE Stadium" "ENGIE"
   "Optus Stadium" "Optus"
   "Junction Oval" "Junct."
   "SCG" "SCG"
   "Adelaide Oval" "Adel O."
   "University of Tasmania Stadium" "UTAS"
   "TBA" "TBA"})

(def team-name->id
  (into {} (for [[id team] teams] [(:name team) id])))

(defmacro back []
  (ns sporting-fixtures.core))

(defn help []
  (println "lein run -m sporting-fixtures.bbl-2026")
  (println "From the top-level REPL: (bbl26)")
  (println "(back)            ; return to the top-level REPL")
  (println "(reload)          ; reload this namespace")
  (println "(print-report)      ; fixtures, results chart, and ladder")
  (println "(test-print-report) ; the same report with sample results")
  (println "(save-report)       ; write data/bbl-2026/report.txt"))

(defn reload []
  (require 'sporting-fixtures.bbl-2026 :reload-all))

(defn api->iso-utc [s]
  (str/replace s #" " "T"))

(defn localtime [utc-string]
  (let [utc-time (ZonedDateTime/parse utc-string)
        local (.withZoneSameInstant utc-time (ZoneId/systemDefault))
        fmt (DateTimeFormatter/ofPattern "EEE dd MMM hh:mm a")]
    (.format local fmt)))

(defn read-json [filename]
  (json/parse-string (slurp filename) true))

(defn team-id [team-name]
  (or (get team-name->id team-name)
      (throw (ex-info "Unknown team" {:team team-name}))))

(defn team-name [team-id]
  (get-in teams [team-id :name] (name team-id)))

(defn team-string [team-id]
  (str/upper-case (name team-id)))

(defn pad [s width]
  (format (str "%-" width "s") (or s "")))

(defn truncate [s width]
  (let [s (or s "")]
    (cond
      (<= (count s) width) s
      (<= width 1) (subs s 0 width)
      :else (str (subs s 0 (dec width)) "."))))

(defn fit [s width]
  (pad (truncate s width) width))

(defn fit-right [s width]
  (let [s (truncate s width)]
    (format (str "%" width "s") s)))

(defn team-short [team-id]
  (or (get-in teams [team-id :short])
      (team-name team-id)))

(defn venue-short [venue]
  (or (get known-venues venue) venue))

(defn normalize-fixture [f]
  {:fixture-id (:MatchNumber f)
   :round (:RoundNumber f)
   :home (team-id (:HomeTeam f))
   :away (team-id (:AwayTeam f))
   :start-time (api->iso-utc (:DateUtc f))
   :venue (:Location f)})

(defn load-fixtures []
  (->> (read-json (str "data/" event-name "/fixtures.json"))
       (map normalize-fixture)
       (sort-by (juxt :round :start-time :fixture-id))
       vec))

(defn load-results []
  (let [path (str "data/" event-name "/results.edn")]
    (if (.exists (io/file path))
      (edn/read-string (slurp path))
      {})))

(defn apply-result [fixture results]
  (if-let [{:keys [home away winner]} (get results (:fixture-id fixture))]
    (assoc fixture :home-score home :away-score away :winner winner)
    fixture))

(defn with-results [fixtures results]
  (mapv #(apply-result % results) fixtures))

;; "159/9(20)" is 120 balls. "117/5(10.1/11)" is 10 overs and 1 ball.
(defn runs-from-score [s]
  (when (string? s)
    (when-let [runs (re-find #"\d+" s)]
      (parse-long runs))))

(defn balls-from-score [s]
  (when (string? s)
    (when-let [[_ overs balls] (or (re-find #"\((\d+)\.(\d+)" s)
                                   (re-find #"\((\d+)\)" s))]
      (+ (* 6 (parse-long overs))
         (parse-long (or balls "0"))))))

(defn played? [{:keys [home-score away-score]}]
  (boolean (and (runs-from-score home-score)
                (runs-from-score away-score)
                (balls-from-score home-score)
                (balls-from-score away-score))))

(defn no-result-score? [score]
  (= "NR" score))

(defn no-result? [{:keys [home-score away-score]}]
  (or (no-result-score? home-score)
      (no-result-score? away-score)))

(defn score-text [score]
  (if (string? score) score ""))

(defn render-fixture
  ([fixture]
   (render-fixture fixture default-fixture-format))
  ([fixture fmt]
   (let [{:keys [home-width away-width score-width venue-width]} fmt
         {:keys [fixture-id start-time home away venue home-score away-score]} fixture]
     (str (format "%3d" fixture-id) " "
          (localtime start-time) " "
          (fit-right (team-short home) home-width) " "
          (fit-right (score-text home-score) score-width) " vs "
          (fit (score-text away-score) score-width) " "
          (fit (team-short away) away-width) " "
          (fit (venue-short venue) venue-width)))))

(defn fixtures-report [fixtures]
  (let [by-round (group-by :round fixtures)
        heading (str event-title " Fixtures")]
    (str heading "\n"
         (apply str (repeat (count heading) "="))
         "\n"
         (str/join
          "\n"
          (for [round (sort (keys by-round))]
            (str "\nRound " round "\n"
                 (str/join "\n"
                           (map render-fixture
                                (sort-by (juxt :start-time :fixture-id)
                                         (get by-round round))))))))))

(defn empty-row [team]
  {:team team
   :played 0 :won 0 :lost 0 :no-result 0 :points 0
   :runs-for 0 :runs-against 0
   :balls-faced 0 :balls-bowled 0})

(defn result-delta [team runs-for runs-against balls-faced balls-bowled outcome]
  (merge {:team team
          :played 1
          :runs-for runs-for
          :runs-against runs-against
          :balls-faced balls-faced
          :balls-bowled balls-bowled}
         (case outcome
           :won {:won 1 :points 2}
           :lost {:lost 1})))

(defn no-result-delta [team]
  {:team team
   :played 1
   :no-result 1
   :points 1
   :runs-for 0
   :runs-against 0
   :balls-faced 0
   :balls-bowled 0})

(defn winner-side [home away winner]
  (cond
    (#{home :home "home"} winner) :home
    (#{away :away "away"} winner) :away))

(defn match-deltas [{:keys [home away home-score away-score winner]}]
  (cond
    (no-result? {:home-score home-score :away-score away-score})
    [(no-result-delta home) (no-result-delta away)]

    (played? {:home-score home-score :away-score away-score})
    (let [home-runs (runs-from-score home-score)
          away-runs (runs-from-score away-score)
          home-balls (balls-from-score home-score)
          away-balls (balls-from-score away-score)
          home-outcome (cond
                         (> home-runs away-runs) :won
                         (< home-runs away-runs) :lost
                         (= (winner-side home away winner) :home) :won
                         (= (winner-side home away winner) :away) :lost)]
      (when home-outcome
        [(result-delta home home-runs away-runs home-balls away-balls home-outcome)
         (result-delta away away-runs home-runs away-balls home-balls
                       (case home-outcome :won :lost :lost :won))]))

    :else []))

(defn apply-delta [table {:keys [team] :as delta}]
  (if (or (nil? team) (:placeholder? (teams team)) (not (contains? table team)))
    table
    (reduce-kv (fn [ladder key value]
                 (if (= key :team)
                   ladder
                   (update-in ladder [team key] (fnil + 0) value)))
               table
               delta)))

(defn build-ladder [fixtures]
  (reduce (fn [table fixture]
            (if-let [deltas (seq (match-deltas fixture))]
              (reduce apply-delta table deltas)
              table))
          (into {} (for [[id team] teams
                         :when (not (:placeholder? team))]
                     [id (empty-row id)]))
          fixtures))

(defn with-nrr [row]
  (let [{:keys [runs-for runs-against balls-faced balls-bowled]} row]
    (assoc row :nrr
           (if (and (pos? balls-faced) (pos? balls-bowled))
             (float (* 6 (- (/ runs-for balls-faced)
                            (/ runs-against balls-bowled))))
             0.0))))

(defn ladder-rows [fixtures]
  (->> (build-ladder fixtures)
       vals
       (map with-nrr)
       (sort-by (juxt (comp - :points) (comp - :nrr) :team))))

(defn render-ladder-row [position row]
  (format " %2d  %-22s %2d %2d %2d %2d %3d %+7.3f"
          position
          (team-name (:team row))
          (:played row)
          (:won row)
          (:lost row)
          (:no-result row)
          (:points row)
          (:nrr row)))

(defn ladder-report [fixtures]
  (let [heading (str event-title " Ladder")]
    (str heading "\n"
         (apply str (repeat (count heading) "="))
         "\n"
         (format " %2s  %-22s %2s %2s %2s %2s %3s %7s\n"
                 "#" "Team" "P" "W" "L" "NR" "Pts" "NRR")
         (str/join "\n"
                   (map-indexed (fn [i row] (render-ladder-row (inc i) row))
                                (ladder-rows fixtures))))))

;; One column per match, in kickoff order. Upper case is a home result.
;; W/w win, L/l loss, n no result, o still to play.
;; A dash is a match this team is not in. A space is an unplayed match
;; this team is not in. A vertical bar separates rounds.
(defn result-mark [team fixture]
  (let [home? (= team (:home fixture))
        involved? (or home? (= team (:away fixture)))]
    (cond
      (not involved?) (if (or (played? fixture) (no-result? fixture)) "-" " ")
      (no-result? fixture) "n"
      (not (played? fixture)) "o"
      :else
      (let [for-runs (runs-from-score (if home? (:home-score fixture) (:away-score fixture)))
            against (runs-from-score (if home? (:away-score fixture) (:home-score fixture)))
            winner (winner-side (:home fixture) (:away fixture) (:winner fixture))]
        (cond
          (> for-runs against) (if home? "W" "w")
          (< for-runs against) (if home? "L" "l")
          (and (= winner :home) home?) "W"
          (and (= winner :away) home?) "L"
          (and (= winner :away) (not home?)) "w"
          (and (= winner :home) (not home?)) "l"
          :else "=")))))

(defn round-groups [fixtures]
  (->> fixtures
       (sort-by (juxt :start-time :fixture-id))
       (partition-by :round)))

(defn pack-rounds
  "Keep each chart on a round boundary, near 60 matches wide."
  [groups]
  (loop [remaining groups
         blocks []]
    (if (seq remaining)
      (let [packed (reduce (fn [acc group]
                             (let [games (apply + (map count acc))]
                               (if (and (seq acc) (> (+ games (count group)) 60))
                                 (reduced acc)
                                 (conj acc group))))
                           []
                           remaining)]
        (recur (drop (count packed) remaining)
               (conj blocks packed)))
      blocks)))

(defn block-columns [groups]
  (->> groups
       (map (fn [group]
              (map (fn [fixture] {:kind :game :fixture fixture}) group)))
       (interpose [{:kind :sep}])
       (apply concat)))

(defn column-text [columns game-text]
  (apply str
         (for [column columns]
           (if (= (:kind column) :sep)
             "|"
             (game-text (:fixture column))))))

(defn results-chart-separator [columns]
  (str "------+"
       "-"
       (apply str (for [column columns]
                    (if (= (:kind column) :sep) "+" "-")))))

(defn fixture-digit [fixture place]
  (let [i (:fixture-id fixture)]
    (case place
      :hundreds (let [hundreds (quot i 100)]
                  (if (zero? hundreds) " " (str hundreds)))
      :tens (if (< i 10) " " (str (mod (quot i 10) 10)))
      :units (str (mod i 10)))))

(defn results-chart-header [columns]
  (str "      | " (column-text columns #(fixture-digit % :hundreds)) "\n"
       "      | " (column-text columns #(fixture-digit % :tens)) "\n"
       " Team | " (column-text columns #(fixture-digit % :units)) "\n"
       (results-chart-separator columns)))

(defn results-chart-section [fixtures groups]
  (let [columns (block-columns groups)
        first-round (:round (first (first groups)))
        last-round (:round (last (last groups)))
        ladder-fixtures (filterv #(<= (:round %) last-round) fixtures)
        teams (map :team (ladder-rows ladder-fixtures))
        heading (format "%s Results  Rounds %d-%d" event-title first-round last-round)]
    (str heading "\n"
         (apply str (repeat (count heading) "=")) "\n"
         (results-chart-header columns) "\n"
         (str/join "\n"
                   (for [team teams]
                     (format "  %3s | %s"
                             (team-string team)
                             (column-text columns #(result-mark team %)))))
         "\n"
         (results-chart-separator columns))))

(defn results-chart [fixtures]
  (str "W/w home/away win   L/l home/away loss   n no result   o to play   - other match   | round\n\n"
       (str/join "\n\n"
                 (for [groups (pack-rounds (round-groups fixtures))]
                   (results-chart-section fixtures groups)))))

(defn report-text
  ([]
   (report-text (load-results)))
  ([results]
   (let [fixtures (with-results (load-fixtures) results)]
     (str (fixtures-report fixtures) "\n\n"
          (results-chart fixtures) "\n\n"
          (ladder-report fixtures) "\n"))))

(def sample-results
  "Round 1 scores for trying the report. Match 3 is a Super Over.
   Match 4 is washed out."
  {1 {:home "159/9(20)" :away "160/4(18.2)"}
   2 {:home "198/8(20)" :away "180/7(20)"}
   3 {:home "150/6(20)" :away "150/7(20)" :winner :away}
   4 {:home "NR" :away "NR"}
   5 {:home "145/8(20)" :away "141/9(20)"}
   6 {:home "151/7(20)" :away "152/5(19.2)"}
   7 {:home "189/4(20)" :away "164/8(20)"}
   8 {:home "133/9(20)" :away "134/6(19.4)"}})

(defn save-report
  "Write the fixture and ladder report to data/bbl-2026/report.txt."
  []
  (let [path (str "data/" event-name "/report.txt")]
    (spit path (report-text))
    path))

(defn print-report []
  (print (report-text)))

(defn test-print-report []
  (print (report-text sample-results)))

(defn -main []
  (print-report))
