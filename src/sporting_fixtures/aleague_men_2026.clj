(ns sporting-fixtures.aleague-men-2026
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import
   [java.time ZoneId ZonedDateTime]
   [java.time.format DateTimeFormatter])
  (:gen-class))

(def event-name "aleague-men-2026")
(def event-title "A-League Men 2026")

;; A fixture line is 80 columns. Team names and scores have fixed widths.
;; The home name and home score are right justified, so they sit against "vs".
;; 3 + " " + 19 + " " + 12 + " " + 5 + " vs " + 5 + " " + 12 + " " + 15.
(def default-fixture-format
  {:width 80
   :home-width 12
   :away-width 12
   :score-width 5
   :venue-width 15})

(def known-teams
  {"Adelaide United" {:id :adl :short "Adel United" :colour "red"}
   "Auckland" {:id :akl :short "Auckland" :colour "navy"}
   "Brisbane Roar" {:id :bri :short "Brisbane" :colour "orange"}
   "Central Coast Mariners" {:id :ccm :short "Mariners" :colour "yellow"}
   "Macarthur" {:id :mac :short "Macarthur" :colour "black"}
   "Melbourne City" {:id :mbc :short "Melb City" :colour "sky-blue"}
   "Melbourne Victory" {:id :mbv :short "Melb Victory" :colour "navy"}
   "Newcastle Jets" {:id :new :short "Newcastle" :colour "gold"}
   "Perth Glory" {:id :per :short "Perth Glory" :colour "purple"}
   "Sydney" {:id :syd :short "Sydney" :colour "sky-blue"}
   "Wellington Phoenix" {:id :wel :short "Wellington" :colour "yellow"}
   "Western Sydney Wanderers" {:id :wsw :short "Wanderers" :colour "red"}})

(def known-venues
  {"AAMI Park" "AAMI Park"
   "Allianz Stadium" "Allianz Stadium"
   "Campbelltown Sports Stadium" "Campbelltown"
   "CommBank Stadium" "CommBank Stad."
   "Coopers Stadium" "Coopers Stadium"
   "Go Media Stadium" "Go Media Stad."
   "HBF Park" "HBF Park"
   "Hnry Stadium" "Hnry Stadium"
   "Kayo Stadium" "Kayo Stadium"
   "McDonald Jones Stadium" "McDonald Jones"
   "One New Zealand Stadium" "One NZ Stadium"
   "Suncorp Stadium" "Suncorp Stadium"
   "polytec Stadium" "polytec Stadium"})

(defn help []
  (println "lein run -m sporting-fixtures.aleague-men-2026")
  (println "From the top-level REPL: (alm26)")
  (println "(extract-teams)   ; write data/aleague-men-2026/teams.edn")
  (println "(extract-venues)  ; write data/aleague-men-2026/venues.edn")
  (println "(print-report)    ; print the fixture and ladder report")
  (println "(save-report)     ; write data/aleague-men-2026/report.txt"))

(defn reload []
  (require 'sporting-fixtures.aleague-men-2026 :reload-all))

(defn api->iso-utc [s]
  (str/replace s #" " "T"))

(defn localtime [utc-string]
  (let [utc-time (ZonedDateTime/parse utc-string)
        local (.withZoneSameInstant utc-time (ZoneId/systemDefault))
        fmt (DateTimeFormatter/ofPattern "EEE dd MMM hh:mm a")]
    (.format local fmt)))

(defn read-json [filename]
  (json/parse-string (slurp filename) true))

(defn data-file [name]
  (str "data/" event-name "/" name))

(defn read-edn [filename]
  (when (.exists (io/file filename))
    (edn/read-string (slurp filename))))

(defn load-teams []
  (or (read-edn (data-file "teams.edn"))
      (into {} (for [[name {:keys [id short colour]}] known-teams]
                 [id {:name name :short short :colour colour}]))))

(defn load-venues []
  (or (read-edn (data-file "venues.edn")) {}))

(defn team-name->id [teams]
  (into {} (for [[id team] teams] [(:name team) id])))

(defn team-id [teams team-name]
  (or (get (team-name->id teams) team-name)
      (throw (ex-info "Unknown team" {:team team-name}))))

(defn team-name [teams team-id]
  (get-in teams [team-id :name] (name team-id)))

(defn team-short [teams team-id]
  (or (get-in teams [team-id :short])
      (team-name teams team-id)))

(defn team-string [team-id]
  (str/upper-case (name team-id)))

(defn ladder-teams []
  (load-teams))

(defn normalize-fixture [teams f]
  {:fixture-id (:MatchNumber f)
   :round (:RoundNumber f)
   :home (team-id teams (:HomeTeam f))
   :away (team-id teams (:AwayTeam f))
   :start-time (api->iso-utc (:DateUtc f))
   :venue (:Location f)
   :home-score (:HomeTeamScore f)
   :away-score (:AwayTeamScore f)})

(defn load-fixtures []
  (let [teams (load-teams)]
    (->> (read-json (data-file "fixtures.json"))
         (map #(normalize-fixture teams %))
         (sort-by (juxt :round :fixture-id))
         vec)))

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

(defn shorten-name [s width]
  (truncate s width))

(defn fresh-team-id [name used]
  (let [initials (->> (re-seq #"[A-Za-z]+" name)
                      (map #(subs % 0 1))
                      (apply str)
                      str/lower-case)
        base (if (seq initials) (keyword initials) :team)]
    (loop [n 1
           candidate base]
      (if (contains? used candidate)
        (recur (inc n) (keyword (str (name base) n)))
        candidate))))

(defn extract-teams
  "Write teams.edn from the fixture list. Existing short names and colours are kept."
  []
  (let [width (:home-width default-fixture-format)
        existing (or (read-edn (data-file "teams.edn")) {})
        by-name (into {} (for [[id team] existing] [(:name team) (assoc team :id id)]))
        names (->> (read-json (data-file "fixtures.json"))
                   (mapcat (juxt :HomeTeam :AwayTeam))
                   distinct
                   sort)
        teams (reduce (fn [acc name]
                        (let [prior (get by-name name)
                              known (get known-teams name)
                              id (or (:id prior) (:id known) (fresh-team-id name (set (keys acc))))]
                          (assoc acc id
                                 {:name name
                                  :short (or (:short prior)
                                             (:short known)
                                             (shorten-name name width))
                                  :colour (or (:colour prior) (:colour known))})))
                      {}
                      names)]
    (spit (data-file "teams.edn")
          (str "{\n"
               (str/join "\n"
                         (for [[id team] (sort-by (comp :name second) teams)]
                           (format " %s {:name %s\n      :short %s\n      :colour %s}"
                                   id
                                   (pr-str (:name team))
                                   (pr-str (:short team))
                                   (pr-str (:colour team)))))
               "\n}\n"))
    teams))

(defn extract-venues
  "Write venues.edn from the fixture list. Existing short names are kept."
  []
  (let [width (:venue-width default-fixture-format)
        existing (or (read-edn (data-file "venues.edn")) {})
        names (->> (read-json (data-file "fixtures.json"))
                   (map :Location)
                   distinct
                   sort)
        venues (into {}
                     (for [name names]
                       [name {:name name
                              :short (or (get-in existing [name :short])
                                         (get known-venues name)
                                         (shorten-name name width))}]))]
    (spit (data-file "venues.edn")
          (str "{\n"
               (str/join "\n"
                         (for [[name venue] (sort venues)]
                           (format " %s {:name %s\n  :short %s}"
                                   (pr-str name)
                                   (pr-str (:name venue))
                                   (pr-str (:short venue)))))
               "\n}\n"))
    venues))

(defn load-results []
  (let [path (str "data/" event-name "/results.edn")]
    (if (.exists (io/file path))
      (edn/read-string (slurp path))
      {})))

(defn apply-result [fixture results]
  (if-let [{:keys [home away]} (get results (:fixture-id fixture))]
    (assoc fixture :home-score home :away-score away)
    fixture))

(defn with-results [fixtures results]
  (mapv #(apply-result % results) fixtures))

;; A score is nil, a goal count, or a string with trailing red cards: "1R", "0RR".
(def score-re #"(\d+)(R*)")

(defn parse-score [score]
  (cond
    (nil? score) nil
    (number? score) {:goals score :reds 0}
    (string? score)
    (when-let [[_ goals reds] (re-matches score-re score)]
      {:goals (parse-long goals)
       :reds (count reds)})))

(defn goals [score]
  (:goals (parse-score score)))

(defn played? [{:keys [home-score away-score]}]
  (boolean (and (some? (goals home-score))
                (some? (goals away-score)))))

(defn score-text [score]
  (when-let [{:keys [goals reds]} (parse-score score)]
    (str goals (apply str (repeat reds "R")))))

(defn score-box [score]
  (or (score-text score) ""))

(defn venue-short [venues venue]
  (or (get-in venues [venue :short]) venue))

(defn render-fixture
  ([fixture]
   (render-fixture fixture (load-teams) (load-venues) default-fixture-format))
  ([fixture teams venues fmt]
   (let [{:keys [home-width away-width score-width venue-width]} fmt
         {:keys [fixture-id start-time home away venue home-score away-score]} fixture]
     (str (format "%3d" fixture-id) " "
          (localtime start-time) " "
          (fit-right (team-short teams home) home-width) " "
          (fit-right (score-box home-score) score-width) " vs "
          (fit (score-box away-score) score-width) " "
          (fit (team-short teams away) away-width) " "
          (fit (venue-short venues venue) venue-width)))))

(defn fixtures-report
  ([fixtures]
   (fixtures-report fixtures (load-teams) (load-venues) default-fixture-format))
  ([fixtures teams venues fmt]
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
                            (map #(render-fixture % teams venues fmt)
                                 (sort-by (juxt :start-time :fixture-id)
                                          (get by-round round)))))))))))

(defn empty-row []
  {:played 0 :won 0 :drawn 0 :lost 0 :goals-for 0 :goals-against 0 :points 0})

(defn record-result [table team goals-for goals-against]
  (let [result (cond
                 (> goals-for goals-against) {:won 1 :points 3}
                 (< goals-for goals-against) {:lost 1}
                 :else {:drawn 1 :points 1})]
    (assoc table team
           (merge-with +
                       (get table team (empty-row))
                       (merge result
                              {:played 1
                               :goals-for goals-for
                               :goals-against goals-against})))))

(defn build-ladder [fixtures]
  (reduce (fn [table {:keys [home away home-score away-score] :as fixture}]
            (if-not (played? fixture)
              table
              (-> table
                  (record-result home (goals home-score) (goals away-score))
                  (record-result away (goals away-score) (goals home-score)))))
          (into {} (map (fn [id] [id (empty-row)]) (keys (ladder-teams))))
          fixtures))

(defn ladder-sort [[_ a] [_ b]]
  (let [diff #(- (:goals-for %) (:goals-against %))]
    (compare [(:points b) (diff b) (:goals-for b)]
             [(:points a) (diff a) (:goals-for a)])))

(defn render-ladder-row [position [team stats]]
  (format " %2d  %-4s  %2d %2d %2d %2d  %3d %3d %4d  %2d"
          position
          (team-string team)
          (:played stats)
          (:won stats)
          (:drawn stats)
          (:lost stats)
          (:goals-for stats)
          (:goals-against stats)
          (- (:goals-for stats) (:goals-against stats))
          (:points stats)))

(defn ladder-report [fixtures]
  (let [heading (str event-title " Ladder")]
    (str heading "\n"
         (apply str (repeat (count heading) "="))
         "\n"
         (format " %2s  %-4s  %2s %2s %2s %2s  %3s %3s %4s  %2s\n"
                 "#" "Team" "P" "W" "D" "L" "GF" "GA" "GD" "Pt")
         (str/join "\n"
                   (map-indexed (fn [i row] (render-ladder-row (inc i) row))
                                (sort ladder-sort (build-ladder fixtures)))))))

(defn report-text []
  (let [fixtures (with-results (load-fixtures) (load-results))]
    (str (fixtures-report fixtures) "\n\n" (ladder-report fixtures) "\n")))

(defn save-report
  "Write the fixture and ladder report to data/aleague-men-2026/report.txt."
  []
  (let [path (data-file "report.txt")]
    (spit path (report-text))
    path))

(defn print-report []
  (print (report-text)))

(defn -main []
  (print-report))
