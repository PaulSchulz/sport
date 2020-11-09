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
  ;; Used for coding Team names (and other things)
  (:require [sporting-fixtures.utils :as utils]) 
  (:use [clojure.java.shell :only [sh]]
        [clojure.pprint :only [pprint]])
  )

;; To use this file
;; => (require 'sporting-fixtures.process-files)
;; => (ns sporting-fixtures.process-files)

;; To reload namespace
;; => (use 'sporting-fixtures.process-files :reload)

;; Display defined competitions
;; => (clojure.pprint/pprint competitions)

;; Process file, where csv file has been downloaded with UTC
;; timezome from https://fixturedownload.com/
;; => (process-csv "aflw-2020")
;;
;; Output filename displayed (.yml).
 
(def data-path     "data/")
(def download-path "data/download/")

(def competitions
  {:bbl       {:tag "aus-bbl"
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
   :wbbl      "aus-wbbl"
   :aflw-2020 {:tag "aus-aflw"
               :header
               {:title    "2020 AFL Womens"
                :location "Australia"
                :code     "afl"
                :date    {:from "7 Feb 2020"
                          :to   "18 Apr 2020"}
                :url      ""
                :version  1.0
                }
               }
   :aleague-2019 {:tag      "aus-aleague"
                  :title    "A League"
                  :location "Australia"
                  :code     "soccer"
                  :date     {:from "11 Oct 2019" :to "16 May 2020"}
                  :url      ""
                  :version  1.0
                  }
   :womens-t20-world-cup-2020 {:tag      "womens-t20-world-cup"
                               :title    "Womens T20 World Cup"
                               :location "Australia"
                               :code     "cricket"
                               :date     {:from "21 feb 2020" :to "08 Mar 2020"}
                               :url      ""
                               :version  1.0
                               :file-out "2019-fra-fifa-women-worldcup.yml"
                               }
   }
  )

;; Read CSV file into map structure
(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
	  (rest csv-data)))

(defn keyword-team [team]
  "Convert team name (string) to short keyword."
  (keyword (clojure.string/lower-case (subs
                              (clojure.string/replace team #" " "") ; Remove whitespace
                              0 3)))
  )

(defn format-fixture [index fixture]
  (let [round    (nth fixture 0)
        time     (nth fixture 1)
        venue    (nth fixture 2)
        home     (nth fixture 3)
        away     (nth fixture 4)
        home-key (name (utils/keyword-team home))
        away-key (name (utils/keyword-team away))
        ]
  (str
          "  - round:   " (nth fixture 0) "\n"
          "    time:    \"" (nth fixture 1) \""\n"
          "    venue:   \"" (nth fixture 2) \""\n"
          "    home:    \"" (nth fixture 3) \""\n"
          "    away:    \"" (nth fixture 4) \""\n"
          "    score:   {" home-key ": -, " away-key ": -}\n"
          "    result:  {" home-key ": -, " away-key ": -}\n"
          "    summary: \"\"\n"
          )))

(defn parse-csv [competition]
  ""
  (let [competition-name    competition
        competition-keyword (keyword competition)
        details             ((keyword competition) competitions)
        header              (:header details)
        filename            (str download-path competition-name "-UTC.csv")
        games      (drop 1 (map-indexed format-fixture
                                (with-open [reader (io/reader filename)]
                                  (doall
                                   (csv/read-csv reader)))
                                ))

        ]
    (flatten
     [
      "---\n"
      "title:    \"" (:title    details)    "\"\n"
      "location: \"" (:location details) "\"\n"
      "code:     \"" (:code     details)     "\"\n"
      "date:     {from: \"" (:from (:date details)) "\", to: \"" (:to (:date details)) "\"}\n"
      "url:      \""  (:url details)     "\"\n"
      "version:  \"1.0\"\n"
      (str "name:     " competition-name "\n")
      (str "filename: " filename         "\n")
      "\n"
      "teams:    {}\n"
      "\n"
      "venues:   {}\n"
      "\n"      
      "games:\n"
      games
     ])
    
    )
)

(defn process-csv [competition]
  ""
  (let [filename (str download-path (name competition) "-UTC.yml")]
    (println (str "Output: " filename))
    (spit filename (apply str (parse-csv competition)))))

