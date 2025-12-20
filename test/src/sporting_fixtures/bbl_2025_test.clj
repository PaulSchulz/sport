(ns sporting-fixtures.bbl-2025-test
  (:require
   [clojure.test :refer :all]
   [sporting-fixtures.bbl-2025 :as bbl-2025]))

(deftest team-id-test
  (testing "team names resolve via teams.edn"
    (is (= :hea
           (bbl-2025/team-id "Brisbane Heat")))))

(deftest unknown-team-test
  (is (thrown?
       clojure.lang.ExceptionInfo
       (bbl-2025/team-id "Brisbane Heat XI"))))

(deftest kebab-keyword-test
  (testing "converts various key styles to kebab-case keywords"
    (is (= :home-team (bbl-2025/->kebab-keyword "homeTeam")))
    (is (= :home-team (bbl-2025/->kebab-keyword "home_team")))
    (is (= :home-team (bbl-2025/->kebab-keyword :homeTeam)))
    (is (= :start-time (bbl-2025/->kebab-keyword "start_time")))))

(deftest normalize-keys-test
  (testing "normalizes nested maps"
    (let [input {:matchId 42
                 :homeTeam "Heat"
                 :venue {:venueId 7
                         :venueName "Gabba"}}
          expected {:match-id 42
                    :home-team "Heat"
                    :venue {:venue-id 7
                            :venue-name "Gabba"}}]
      (is (= expected
             (bbl-2025/normalize-keys input))))))

(deftest api->iso-utc-test
  (testing "convert API time to iso format"
    (let [input    "2026-01-01 05:00:00Z"
          expected "2026-01-01T05:00:00Z"]
      (is (= expected
             (bbl-2025/api->iso-utc input))))))

(deftest normalize-fixture-test
  (testing "produces a flat, domain fixture"
    (let [raw {:match-number 42
               :home-team "Adelaide Strikers"
               :away-team "Sydney Sixers"
               :location "Gabba"
               :date-utc "2026-01-01 05:00:00Z"}
          expected {:fixture-id 42
                    :home :str
                    :away :six
                    :venue "Gabba"
                    :start-time "2026-01-01T05:00:00Z"}]
      (is (= expected
             (bbl-2025/normalize-fixture raw))))))

(deftest runs-from-score-test
  (testing "Test parsing scoreboard string for runs"
    (let [input    "158/9(20)"
          expected 158]
      (is (= expected
             (bbl-2025/runs-from-score input)))))
  (testing "Test parsing scoreboard string for runs(2)"
    (let [input    "117/5(10.1/11)"
          expected 117]
      (is (= expected
             (bbl-2025/runs-from-score input)))))
  )

(comment
  (deftest result-from-scoreboard-test
    (testing "Calculate winner from scoreboard"
      (let [input   {:six "159/9(20)", :str "160/7(19.2)"}
            expected {:home :six :away :str :home-runs 159 :away-runs 160 :outcome :away-win}]
        (is (= expected
               (bbl-2025/result-from-scoreboard input)))))
    )
  )

(comment
  (deftest merge-result-test
    (testing "Merge game result into data"
      (let [fixture {:fixture-id 1
                     :home :sco
                     :away :six
                     :start-time "2025-12-14T08:15:00Z"
                     :venue "Perth Stadium"}

            result {:game 1
                    :scoreboard {:six "113/5(11)" :sco "117/5(10.1/11)"}
                    :summary "SCO won by 5 wickets (5 balls left)"}

            expected {:fixture-id 1,
                      :home :sco,
                      :away :six,
                      :start-time "2025-12-14T08:15:00Z",
                      :venue "Perth Stadium",
                      :scoreboard {:six "113/5(11)" :sco "117/5(10.1/11)"},
                      :summary "SCO won by 5 wickets (5 balls left)",
                      :outcome {:six {:runs 113, :points 0},
                                :sco {:runs 117, :points 2},
                                :outcome :sco}}
            ]
        (is (= (bbl-2025/merge-result fixture result)
               expected))))
    )
  )
