(ns sporting-fixtures.aflw-test
  (:use clojure.test)
  (:use [sporting-fixtures.aflw :as aflw])
  )

(deftest test-keyword-team
  (is (= (apply keyword-team ["West Coast Eagles"]) :wce) "West Coast Eagles")
  (is (= (apply keyword-team ["Western Bulldogs"])  :wbd) "Western Bulldogs")
  (is (= (apply keyword-team ["Adelaide Crows"])    :ade) "Adelaide Crows")
  )

(deftest test-calculate-points
  (is (= (apply calculate-points [[1 2]])  8)  "[1 2 8]")
  (is (= (apply calculate-points [[10 1]]) 61) "[1 2 61]")
  )

;; calculate-stats 
(def data-in-1 [:adl :mlb {:adl [1 2] :mlb [3 4]}])
(def data-out-1 {:adl
                 {:points 0,
                  :played 1,
                  :won 0,
                  :lost 1,
                  :drawn 0,
                  :points-for 8,
                  :points-against 22,
                  :points-diff -14},
                 :mlb
                 {:points 4,
                  :played 1,
                  :won 1,
                  :lost 0,
                  :drawn 0,
                  :points-for 22,
                  :points-against 8,
                  :points-diff 14}})

(def data-in-2 [:adl :mlb {:adl [4 2] :mlb [3 4]}])
(def data-out-2 {:adl
                 {:points 4,
                  :played 1,
                  :won 1,
                  :lost 0,
                  :drawn 0,
                  :points-for 26,
                  :points-against 22,
                  :points-diff 4},
                 :mlb
                 {:points 0,
                  :played 1,
                  :won 0,
                  :lost 1,
                  :drawn 0,
                  :points-for 22,
                  :points-against 26,
                  :points-diff -4}})
(def data-in-3 [:adl :mlb {:adl [4 2] :mlb [4 2]}])
(def data-out-3 {:adl
                 {:points 2,
                  :played 1,
                  :won 0,
                  :lost 0,
                  :drawn 1,
                  :points-for 26,
                  :points-against 26,
                  :points-diff 0},
                 :mlb
                 {:points 2,
                  :played 1,
                  :won 0,
                  :lost 0,
                  :drawn 1,
                  :points-for 26,
                  :points-against 26,
                  :points-diff 0}})


(deftest test-calculate-stats
  (is (= (apply calculate-stats data-in-1) data-out-1) "Home win")
  (is (= (apply calculate-stats data-in-2) data-out-2) "Away win")
  (is (= (apply calculate-stats data-in-3) data-out-3) "Draw")
)

;; calculate-statistics
(def data-in-games-1
  [{:home "Adelaide Crows"
    :away "Melbourne"
    :score {:ade [1 2] :mel [3 4]}}
   ])
(def data-out-games-1
  [{:ade {:points 0,
         :played 1,
         :won 0,
         :lost 1,
         :drawn 0,
         :points-for 8,
         :points-against 22,
         :points-diff -14},
   :mel {:points 4,
         :played 1,
         :won 1,
         :lost 0,
         :drawn 0,
         :points-for 22,
         :points-against 8,
         :points-diff 14}
   }])
(def data-out-stats-1
  [[:ade {:points 0,
          :played 1,
          :won 0,
          :lost 1,
          :drawn 0,
          :points-for 8,
          :points-against 22,
          :points-diff -14}]
   [:mel {:points 4,
          :played 1,
          :won 1,
          :lost 0,
          :drawn 0,
          :points-for 22,
          :points-against 8,
          :points-diff 14}]])

(deftest test-calculate-statistics
  (is (= (keyword-team (:home (nth data-in-games-1 0))) :ade))
  (is (= (keyword-team (:away (nth data-in-games-1 0))) :mel))
  (is (= (:score (nth data-in-games-1 0))) {:ade [1 2] :mel [3 4]})

  (is (= (conj {} {:ade {:a 1}} {:mel {:b 2}}) {:ade {:a 1} :mel {:b 2}}))
          
  (is (= (apply calculate-statistics [data-in-games-1]) data-out-games-1) "Single game")
  ;; (clojure.pprint/pprint (aflw/calculate-statistics data-in-games-1))
  ;; (clojure.pprint/pprint data-out-games-1)
  )

;; split-statistics
(def data-in-games-2
  [{:home "Adelaide Crows"
    :away "Melbourne"
    :score {:ade [1 2] :mel [3 4]}}
   {:home "Adelaide Crows"
    :away "Port Adelaide"
    :score {:ade [1 2] :por [3 4]}}
   {:home "Melbourne"
    :away "Port Adelaide"
    :score {:mel [1 2] :por [3 4]}}
   ])
(def data-out-stats-2
  [[:ade {:points 0,
          :played 1,
          :won 0,
          :lost 1,
          :drawn 0,
          :points-for 8,
          :points-against 22,
          :points-diff -14}]
   [:mel {:points 4,
          :played 1,
          :won 1,
          :lost 0,
          :drawn 0,
          :points-for 22,
          :points-against 8,
          :points-diff 14}]
   [:ade {:points 0,
          :played 1,
          :won 0,
          :lost 1,
          :drawn 0,
          :points-for 8,
          :points-against 22,
          :points-diff -14}]
   [:por {:points 4,
          :played 1,
          :won 1,
          :lost 0,
          :drawn 0,
          :points-for 22,
          :points-against 8,
          :points-diff 14}]
   [:mel {:points 0,
          :played 1,
          :won 0,
          :lost 1,
          :drawn 0,
          :points-for 8,
          :points-against 22,
          :points-diff -14}]
   [:por {:points 4,
          :played 1,
          :won 1,
          :lost 0,
          :drawn 0,
          :points-for 22,
          :points-against 8,
          :points-diff 14}]]
  )

(deftest test-split-statistics
;;  (clojure.pprint/pprint data-in-games-2)
;;  (clojure.pprint/pprint (calculate-statistics data-in-games-2))
;;  (clojure.pprint/pprint (-> data-in-games-1
;;                             calculate-statistics
;;                             split-statistics))
;;  (clojure.pprint/pprint data-out-stats-1)
  
  (is (= (-> data-in-games-1
             calculate-statistics
             split-statistics)
         data-out-stats-1) "Single match statistics")
  
  (is (= (-> data-in-games-2
             calculate-statistics
             split-statistics)
         data-out-stats-2) "Three match statistics")
  )

;; update-statistics
(def data-stats-out-2
  {:ade
   {:points 0,
    :played 2,
    :won 0,
    :lost 2,
    :drawn 0,
    :points-for 16,
    :points-against 44,
    :points-diff -28},
   :mel
   {:points 4,
    :played 2,
    :won 1,
    :lost 1,
    :drawn 0,
    :points-for 30,
    :points-against 30,
    :points-diff 0},
   :por
   {:points 8,
    :played 2,
    :won 2,
    :lost 0,
    :drawn 0,
    :points-for 44,
    :points-against 16,
    :points-diff 28}
   }
  )

(deftest test-update-statistics

  ;;  (clojure.pprint/pprint      (-> data-in-games-2
  ;;                                calculate-statistics
  ;;                                split-statistics))
  
  ;; (clojure.pprint/pprint
  ;;  (reduce (fn [stats update]
  ;;            (let [team         (nth update 0)
  ;;                  stats-update (nth update 1)]
  ;;              (conj stats {team (merge-with + (team stats) stats-update)})))
  ;;          {}
  ;;          (-> data-in-games-2
  ;;              calculate-statistics
  ;;              split-statistics))
  ;;  )

  ;; (clojure.pprint/pprint (update-statistics {}
  ;;                                         (-> data-in-games-2
  ;;                                             calculate-statistics
  ;;                                            split-statistics)))
  
  (is (= (update-statistics {}
                            (-> data-in-games-2
                                calculate-statistics
                                split-statistics))
         data-stats-out-2))
  )

(deftest test-event-statistics
  (is (= (event-statistics data-in-games-2) data-stats-out-2))
  )

(deftest test-calculate-percentage
  (is (= (calculate-percentage {:points-for 20, :points-against 10}) 200.0))
  )

;; stats-string
(def data-stats-in-1
   {:points 8,
    :played 2,
    :won 2,
    :lost 0,
    :drawn 0,
    :points-for 44,
    :points-against 16,
    :points-diff 28}
  )
(def data-stats-out-1 "   1 | ADE  |  2 |  8  275.00 |  2  2  0  0 |    44    16    28")
(deftest test-stats-string
  (is (= (apply stats-string [1 :ade data-stats-in-1]) data-stats-out-1)) 
  )

;; event-stats-table
(def data-stats-table-out-2
  (str " Pos | Team | P  | Pts      % |  P  W  L  D |   For    Ag  Diff\n"
       "-----+------+----+------------+-------------+----------------------\n"
       "   1 | POR  |  2 |  8  275.00 |  2  2  0  0 |    44    16    28\n"
       "   2 | MEL  |  2 |  4  100.00 |  2  1  1  0 |    30    30     0\n"
       "   3 | ADE  |  2 |  0   36.36 |  2  0  2  0 |    16    44   -28\n"
       "-----+------+----+------------+-------------+----------------------"
       ))

(deftest test-event-stats-table
  ;; (println (event-stats-table {:games data-in-games-2}))
  (is (= (event-stats-table {:games data-in-games-2}) data-stats-table-out-2) "Sorted statistics")
  )

(deftest test-event-games-statistics-table
   (println (event-games-statistics-table {:games data-in-games-2}))
  )
