;; Functions to process the results of a round
;; See (help) for assumptions, usage examples and workflow explanation.
;;
;; Include in repl with
;;   (require ['sporting-fixtures.process-results :as 'pr])

(ns sporting-fixtures.process-results
  ;; (:gen-class)
  (:require
   [clojure.test :as t])) ;; May not be needed?

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
(defn reload []
  (require ['sporting-fixtures.process-results :as 'pr] :reload))

(defn run-tests []
  (clojure.test/run-tests 'sporting-fixtures.process-results))

(defn help []
  (println ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;")
  (println ";; This is a helper module for processing the results of a round")
  (println ";; into the stored data.")
  (println ";; It assumes:")
  (println ";;   - data is loaded (data)")
  (println ";;   - round update data is available")
  (println "")
  (println ";; Load round results")
  (println "(def actions (pr/read-actions \"./data/afl-2023/update-matches.clj\")")
  (println ";; or (eg.)")
  (println "(def actions [[:update-match-result 28")
  (println "               {:bri \"18.8(116)\", :col \"11.17(83)\"}")
  (println "               \"BRI won by 33 points\"]]")
  (println)
  (println "(def data (pr/process-actions data actions))"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn match-number? [match match-number]
  (if (= (:MatchNumber match) match-number)
    true
    false))

(defn action-match-update-result
  "Update the result of a match, for correct match number."
  [match match-number scoreboard summary]
  ;; Check match-number
  (if (match-number? match match-number)
    (conj match {:scoreboard scoreboard
                 :summary summary})
    match)
  ;;
  )

(defn read-actions [filename]
  (read-string (slurp filename))
  ;;
  )

(defn update-data-with-action [data action]
  (let [command (first action)]
    (println "action: " action)
    (println)

    (case (first action)
      :update-match-result
      (conj data
            {:results
             (let [match-number (nth action 1)
                   scoreboard (nth action 2)
                   summary (nth action 3)]

               (println ":update-match-result")
               (map (fn [m] (action-match-update-result
                             m
                             match-number
                             scoreboard
                             summary))
                    (:results data)))})
      data)))

;; TODO Save data to file
(defn select-match-result [data match-number]
  (let [results (:results data)]
    (filter results)))

(defn update-match-result
  "Update match data with the result"
  [data match-number scoreboard summary]

  (println "----")
  (println ";; scoreboard: " scoreboard)
  (println ";; summary:    " summary)
  (println "----")

  (let [match (nth (:results data) (- match-number 1))
        update (first (read-actions ""))
        action (first update)]

    (case action
      :update-match-result
      (let [scoreboard (nth update 2)
            summary (nth update 3)]
        (println "UPDATE")
        ;; (action-match-update-result match match-number scoreboard summary)
        (if (match-number? match match-number)
          (println "MatchNumber Correct!")
          (println "MatchNumber Wrong!"))
        (clojure.pprint/pprint (conj match {:scoreboard scoreboard
                                            :summary summary})))
      (let []
        (println "Unknown action: " (first update))
        match)))

  (println "----")
  ;; (apply)
  ;;
  )

(defn process-actions [data actions]
  ;; Apply actions to data and return the updated data.
  (reduce (fn [d a] (update-data-with-action d a))
          data
          actions))
;;;;;;;;
(defn show-match [data match-number]
  (clojure.pprint/pprint (nth (:results data) (- match-number 1))))
