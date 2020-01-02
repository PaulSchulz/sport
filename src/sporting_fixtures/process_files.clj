(ns sporting-fixtures.process-files
  (:gen-class)
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clj-yaml.core    :as yaml]
            [clojure.string   :as str]
            [clj-time.core    :as t]
            [clj-time.format  :as f]
            [clj-time.local   :as l]
            )
  (:use [clojure.java.shell :only [sh]]
        [clojure.pprint :only [pprint]])
  )

(def data-path     "data/")
(def download-path "data/download/")

(def competitions
  {:bbl {:tag "aus-bbl"
         :header
         {:title    "2018-9 Big Bash League"
          :location "Australia"
          :code     "cricket-bbl"
          :date     {:from "19 Dec 2019"
                     :to "17 Feb 2020"}    
          :url      "http://www.bigbash.com.au/fixtures?gender=men"
          :version  1.0
          }
         }
   
         :wbbl   "aus-wbbl"
   }
  )

;; Read CSV file into map structure
(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
	  (rest csv-data)))

(defn format-fixture [index fixture]
  (str
          "  - round:   " (nth fixture 0) "\n"
          "    time:    \"" (nth fixture 1) \""\n"
          "    venue:   \"" (nth fixture 2) \""\n"
          "    home:    \"" (nth fixture 3) \""\n"
          "    away:    \"" (nth fixture 4) \""\n"
          "    score:   " "{}\n"
          "    result:  " "{}\n"
          "    summary: " "\"\"\n"
          ))


(defn parse-csv [competition]
  (let [competition-name (name competition)
        filename         (str download-path competition-name "-UTC.csv")
        ]
    (flatten
     [
      "---\n"
      "title:    \"\"\n"
      "location: \"\"\n"
      "code:     \"\"\n"
      "date:     {from: \"\", to: \"\"}\n"
      "url:      \"\"\n"
      "version:  \"1.0\"\n"
      "\n"
      "teams:    {}\n"
      "\n"
      "venues:   {}\n"
      "\n"
      
      (str "name:     " competition-name "\n")
      (str "filename: " filename         "\n")
      "games:\n"
      (map-indexed format-fixture
                   (with-open [reader (io/reader filename)]
                     (doall
                      (csv/read-csv reader)))
                   )
     ])
    
    )
)

(defn process-csv [competition]
  (let [filename (str download-path (name competition) "-UTC.yml")]
    (println (str "Output: " filename))
    (spit filename (apply str (parse-csv competition)))))
