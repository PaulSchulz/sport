;; Functions to process and manipulate the data
;; See (help) for assumptions, usage examples and workflow explanation.
;;
;; Include in repl with
;;   (require ['sporting-fixtures.process :as 'p])

(ns sporting-fixtures.process
  ;; (:gen-class)
  (:require
   [clojure.test :as t])) ;; May not be needed?

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
(defn reload []
  (require ['sporting-fixtures.process :as 'p] :reload-all))

(defn run-tests []
  (clojure.test/run-tests 'sporting-fixtures.process))

(defn help []
  (println ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;")
  (println ";; This is a helper module for processing data")
  (println ";; into the stored data.")
  (println ";; It assumes:")
  (println ";;   - data is loaded (data)")
  (println "")
  (println ";; Load Data")
  (println "(def event \"afl-2023\"")
  (println "(p/data-read event)")
  (println "")
  (println ";; Check Data")
  (println "(p/data-check data)")
  (println "")
  (println ";; Write Data")
  (println "(p/data-write data)")
  (println))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn match-number? [match match-number]
  (if (= (:MatchNumber match) match-number)
    true
    false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Read data from for sporting evant
(def data-dir "data/")

(defn data-read [event]
  ;; TODO Add check for directory and data file.
  ;; TODO Add check that data is committed to git (git status)
  (read-string (slurp (str data-dir event "/data.clj"))))

(defn data-write [data event]
  ;; TODO Add check for directory and data file.
  ;; TODO Create and populate directory if not available.
  ;; TODO Add check that data is committed to git (git status)
  (spit (str data-dir event "/data.clj")
        (clojure.pprint/write data
                              :stream nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn afl-valid-scoreboard? [string]
  (if (re-matches #"(\d+)\.(\d+)\((\d+)\)" string)
    true
    false))

;; Verify data format and status
(defn afl-match-check [match]
  (let [match-number (:MatchNumber match)
        teams (:teams match)
        home  (first match)
        away  (second match)
        scoreboard (:scoreboard match)]
    (if (not= (count scoreboard) 0)
      (map (fn [[k v]]
             (if (afl-valid-scoreboard? v)
               true
               (println "*** Scoreboard string error: match-number:" match-number "team:" k)))
           scoreboard)
      ;; (println "*** No score recorded: match-number:" match-number)
      )))

(defn afl-check-results [results]
  (map (fn [match] (afl-match-check match))
       results))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
