;; Functions to setup data and check data..
;; See (help) for assumptions, usage examples and workflow explanation.
;; Include in repl with
;;   (require ['sporting-fixtures.setup :as 's])

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
  (println "(def sport \"aleague-2022\")")
  (println "(def data (s/data-read-event sport))")
  (println)
  (println ";; Check data structure")
  (println "(s/data-check data)")
  (println)
  (println ";; Write/save data to file")
  (println "(s/data-write data)")
  (println)
  (println ";; Process ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;")
  (println ";; 0. Get fixtures, and setup files")
  (println ";;    In data directory: ./get-fixtures SPORTID")
  (println "(def sport \"fifa-womens-world-cup-2023\")")
  (println "(def data (s/data-read-event sport))")
  (println)
  (println ";; 1. Merge fixture data. (:fixtures)")
  (println "(def data (conj data {:fixtures (s/fixtures-init data)}))")
  (println)
  (println ";; 2. Setup results. (:results)")
  (println "(def data (conj data {:results (s/results-init data)}))")
  (println)
  (println ";; 3. Extract and store team data. (:teams)")
  (println "(def data (conj data {:teams (s/teams-init data)}))")
  (println)
  (println ";; 4. Extract and store venues. (:venues)")
  (println "(def data (conj data {:venues (s/venues-init data)}))")
  (println)
  (println ";; 5. Setup transform data")
  (println ";;    Create 'custom.clj' to map team names to short form (for reports)")
  (println "(s/create-custom data)")
  (println)
  (println ";; 6. Edit 'custom.clj', then reload custom functions and map data.")
  (println "(s/reload)")
  (println "(def data (conj data {:results (s/results-transform data)}))")
  (println)
  (println ";; 7. Save data")
  (println "(s/data-write data)")
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
      (let [event    (:event    data)
            filename (:data     data)
            details  (:details  data)
            fixtures (:fixtures data)
            teams    (:teams    data)
            venues   (:venues   data)
            results  (:results  data)]

        (if event
          (let [] (print ";; Event: ")
               (if (string? event)
                 (print event)
                 (print "-not-a-string-"))
               (println)))

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
          (println ";; *** No fixtures"))

        (if results
          (let []
            (println ";; Results: " (count results)))
          (println ";; *** No results"))

        (if teams
          (let []
            (println ";; Teams:   " (count teams)))
          (println ";; *** No teams"))

        (if venues
          (let []
            (println ";; Venues:  " (count venues)))
          (println ";; *** No venues"))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
;; Customisation functions
(defn custom-read [filename]
  (eval (data-read filename)))

;; Helper function - read in custom code using event id.
(defn custom-read-event [event]
  (custom-read (str "data/" event "/custom.clj")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn fixtures-init [data]
  (json/read-str (slurp (:fixtures (:details data)))
                 :key-fn keyword))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setup Results
;; Assumes that fixtures have been loaded into data
(defn results-init [data]
  (let [games (:fixtures data)]
    (reduce conj []
            (map (fn [game]
                   (conj game
                         {:teams      []
                          :scoreboard {}
                          :score      {}
                          :result     {}
                          :summary    ""
                          :comment    ""}))
                 games))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Venues
(defn get-venues [games]
  (sort (vec (set (map (fn [x] (:Location x)) games)))))

;; Assumes that 'results' has been populated
(defn venues-init [data]
  (let [games (:results data)]
    (reduce conj {}
            (map (fn [venue] [venue {:id venue
                                     :name venue}])
                 (get-venues games)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Get list of team names, from home teams.
(defn get-home-teams [games]
  (sort (vec (set (map (fn [x] (:HomeTeam x)) games)))))

(defn teams-init [data]
  (let [games (:results data)]
    (reduce conj {}
            (map (fn [team] [team {:id team
                                   :name team}])
                 (get-home-teams games)))))

(defn map-team-name-init [teams]
  (reduce conj {}
          (map (fn [[k v]] [(:name v) (:id v)])
               (seq teams)))) ()

(defn map-team-keyword-init [teams]
  (let [map-teams-name (map-team-name-init teams)]
    (clojure.set/map-invert map-teams-name)))

(defn create-custom [data]
  (let [teams (:teams data)
        filename (:custom (:details data))]
    (spit
     filename
     (str ";; Custom functions for manipulating data\n"
          ";; Needs to return 'unknown' (usually :home or :away) so that\n"
          ";; score and scoreboard maps can be built without duplicate keys\n"
          "(defn map-team-id [name unknown]\n"
          "  (case name\n"
          (apply str
                 (sort
                  (map (fn [team]
                         (str
                          "    "
                          "\"" (:name team) "\""
                          " "
                          ":" (clojure.string/replace (clojure.string/lower-case (:id team))
                                                      " " "-")
                          "\n"))
                       (vals teams))))
          "    unknown))\n"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; map keywords to standard values
;; :MatchNumber -> :game (int)
;; :RoundNumber -> :round (int)
;; :Group -> not-used
;; :HomeTeam :AwayTeam -> :teams []
;; . -> :score {}
;; . -> :result {}
(defn result-transform [event result]
  (let [map-fn (custom-read-event event)
        home (map-fn (result :HomeTeam) :home)
        away (map-fn (result :AwayTeam) :away)]

    (conj result {:round      (result :RoundNumber)
                  :game       (result :MatchNumber)
                  :teams      [home away]
                  :scoreboard {home "" away ""}
                  :score      {home "" away ""}
                  :result     {home {} away {}}})))

(defn results-transform [data]
  (let [results (:results data)]
    (map (partial result-transform (:event data)) results)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Functions to create tournament matches
(defn knockout-matches
  "Create a set of knockout tournament matches"
  [players template]
  (for [i (range 0 (/ players 2))]
    (let [m (+ i 1)]
      (template m))))

;; Example
;; => (s/knockout-matches 16 (fn [x] {:id x}))
;; ;;  ({:id 1} {:id 2} {:id 3} {:id 4} {:id 5} {:id 6} {:id 7} {:id 8})
