(ns sporting-fixtures.utils
  (:require [clj-http.client :as client]
            [hickory.core]
            [hickory.select :as s]
            [clojure.data.csv :as csv]
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

;; Utilities
;; Download CSV file from website
;; https://fixturedownload.com/download/csv/afl-2020
;; Format "https://fixturedownload.com/download/csv/" comp

;; Download index page
(defn get-index-page []
  (-> (client/get "https://fixturedownload.com")
      :body hickory.core/parse hickory.core/as-hickory))

;; Extract competitions list
(defn get-comp-list []
  (map (fn [path] (clojure.string/replace path #"^/results/" ""))
       (map :href
            (map :attrs
                 (-> (s/select (s/child (s/class "fixture")
                                        s/first-child
                                        s/first-child
                                        (s/nth-child 2)
                                        (s/attr :href)
                                        )
                               (get-index-page)))))))

(defn download-competition-index []
  (spit "data/download/00-index-competitions.txt"
        (str/join "\n" (get-comp-list))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Down load competition fixture list
(defn form-filename [competition]
  (str competition "-UTC.csv"))

(defn form-path [competition]
  "Produce data path from competition details."
  (str "data/download/"
       (form-filename competition)
       )
  )

(defn download-fixtures-enable [competition]
  ;; WIP - Individial fixtures files are created on demand. Therefore
  ;; before the download can be done, the creation of the data file
  ;; needs to be triggered.
  ;;
  ;; Work in progress. Need to do a POST request and have timezome set
  ;; to UTC via 'timezone' cookie.
  (let [url (str "https://fixturedownload.com/download/" competition)]
  (println url)
  (spit (form-path competition)
        (:body (client/post
                url
                {:form-params {:timezome "UTC" :cookie {:timezone "UTC"}}}
                ))))
  )

(defn form-url [competition]
  "Produce data URL from competition details."
  (str "https://fixturedownload.com/download/"
       (form-filename competition)
       )
  )

(defn download-fixtures [comp]
  (println (form-url comp))
  (spit (form-path comp)
        (:body (client/get (form-url comp))))
  )

;; (require '[sporting-fixtures.utils])
;; (ns sporting-fixtures.utils)
;; Download the index file: data/download/00-index-competitions.txt
;; (download-competition-index)

;; Download the fixtures from a competition
;; - Need to get dat generated on site first by visiting data page.
;; (download-fixtures "wbbl-2019")
;; (download-fixtures "aleague-2019")
;; (download-fixtures "wleague-2019")
