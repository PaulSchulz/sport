(ns sporting-fixtures.fifa-womens-world-cup-2023
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :refer :all] ;; Pretty print JSON
            [clj-yaml.core :as yaml]    ;; YAML output
            ;; Time manipulation
            [clj-time.core   :as t]
            [clj-time.format :as f]
            [clj-time.local  :as l]
            [clojure.pprint]
            ;;
            [sporting-fixtures.reports :as r]
            )
  (:gen-class))

(defn -help []
  ;; Process downloaded file
  ;; Use from command line with:
  (println "lein run -m sporting-fixtures.fifa-womens-world-cup-2023"))
;;

(def event-name "fifa-womens-world-cup-2023")
(def data (r/data-read-event event-name))

(defn -main [& title]
  (println title)
  (println event-name)
  (println (r/report-games data))
  )
