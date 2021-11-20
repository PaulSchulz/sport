(ns sporting-fixtures.utils
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
            )
  (:gen-class)
  )

;; Read/download data from website
;;
;; See: https://fixturedownload.com/
;; URL's
;; https://fixturedownload.com/download/csv/aleague-2019
(def url
  {:afl-2019     "https://fixturedownload.com/download/csv/afl-2019"
   :aleague-2019 "https://fixturedownload.com/download/csv/aleague-2019"
   :afl-2020     "https://fixturedownload.com/download/csv/afl-2020"
   })

(defn copy [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn -main []
  (println "Utilities"))
