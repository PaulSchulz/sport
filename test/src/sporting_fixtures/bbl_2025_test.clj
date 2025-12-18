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
