;; Functions to setup data and check data..
;; See (help) for assumptions, usage examples and workflow explanation.

(ns sporting-fixtures.setup
  ;; (:gen-class)
  (:use [clojure.data.xml])
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.test :as t])) ;; May not be needed?

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
(defn reload []
  (require ['sporting-fixtures.setup :as 's] :reload))

(defn run-tests []
  (clojure.test/run-tests 'sporting-fixtures.setup))

(defn help []
  (println ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;")
  (println ";; This is a 'helper' module for setting up event data.")
  (println ";; It assumes:")
  (println ";;   - fixtures and data are stored under a 'sportid' subdirectory.")
  (println ";;   - fixtures are stored im 'json' format as downloaded")
  (println ";;   - data is stored as clojure data structure")
  (println ";;   - data contains a skeleton clojure structure '{}'")
  (println)
  (println ";; Load data from event")
  (println "(def data (s/data-read-event \"2022-fifa-world-cup\"))")
  (println)
  (println ";; Check data structure")
  (println "(s/data-check data)")
  (println)
  (println ";; Display data details")
  (println "(clojure.pprint/pprint (:details data))")
  (println ";; Display data section")
  (println "(clojure.pprint/pprint (keys data))")
  (println "")
  (println ";; Write data to file")
  (println "(c/data-write data)")
  (println "")
  (println ";; Update :venues (pre-populate or reset)")
  (println "(def data (conj data {:venues (c/venues-init (:fixtures data))}))")
  (println "")
  (println ";; Update :venues (pre-populate or reset)")
  (println "(def data (conj data {:venues (c/venues-init (:fixtures data))}))")
  (println)
  (println ";; Update :results (pre-populate or reset)")
  (println "(def data (conj data {:results (c/results-init (:fixtures data))}))")
  (println))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Format Example
;; {:data "data/bbl-2022/data.clj",
;;  :details
;;  {:title "",
;;   :location "",
;;   :code nil,
;;   :date "",
;;   :url "",
;;   :fixtures "data/bbl-2022/fixtures.json",
;;   :data "data/bbl-2022/data.clj"}}

;; Verify data format and status
(defn data-check [data]
  (if (= data nil)
    (println ";; No data defined")
    (if (= data {})
      (println ";; Empty data structure")
      (let [filename (:data     data)
            details  (:details  data)
            fixtures (:fixtures data)
            teams    (:teams    data)
            venues   (:venues   data)
            results  (:results  data)]

        (if filename
          (let [] (print ";; Data file defined: ")
               (if (string? filename)
                 (print filename)
                 (print "-not-a-string-"))
               (println)))

        (if details
          (let []
            (println ";; File details")
            (clojure.pprint/pprint details)))

        (if fixtures
          (let []
            (println ";; Fixtures:" (count fixtures)))
          (println ";; *** Not fixtures"))

        (if teams
          (let []
            (println ";; Teams:   " (count teams)))
          (println ";; *** Not teams"))

        (if venues
          (let []
            (println ";; Venues:  " (count venues)))
          (println ";; *** Not venues"))

        (if results
          (let []
            (println ";; Results: " (count results)))
          (println ";; *** Not results"))))))

;; Read data from file and create if it doesn't exist
(defn data-read [filename]
  (read-string (slurp filename)))

(defn data-init [data]
  (let [filename (:data data)]
    ;; Read in data file, or initialise
    (if (and (some? filename) (.exists (io/file filename)))
      (data-read filename)
      (println ";; Error: Data file not found"))))

;; (def data (data-init))

(defn data-write [data]
  (spit (:data data)
        (clojure.pprint/write data
                              :stream nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Read in data, using event id.
(defn data-read-event [event]
  (data-read (str "data/" event "/data.clj")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn fixtures-init [data]
  (json/read-str (slurp (:fixtures data))
                 :key-fn keyword))
;; (def fixtures (fixtures-init))

(defn teams-init [] {})
;; (def teams (teams-init))

(defn venues-init [] {})
;; (def venues (venues-init))

(defn results-init [] {})
;; (def results (results-init))

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

;; (def map-team-name (map-team-name-init (:teams data)))
;; (def map-team-keyword (map-team-keyword-init (:teams data)))

(def map-team-name {})
(def map-team-keyword {})

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

;;(def games        (-> data :games))
;;(def teams        (-> data :teams))
;;(def venues       (-> data :venues))

;; (defn results-by-round
;;   [Games]
;;   (Map-indexed (fn [i a]
;;                  (sort-by sort-kEy-game
;;                           >
;;                           (map-id (reduce tally-up-round {} (subvec games 0 a)))))
;;                (range 1 10)))

;; (def ladder-by-round [])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; (defn write-data-out []
;;   (let [data-new {:data    (:data data)
;;                   :details details
;;                   :games   games
;;                   :teams   times
;;                   :venues  venues
;;                   :results results}]
;;     (write-data data-new (:data data))))
