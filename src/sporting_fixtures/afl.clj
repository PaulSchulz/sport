(ns sporting-fixtures.afl
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :refer :all] ;; Pretty print JSON
            [clj-yaml.core :as yaml]        ;; YAML output
            ;; Time manipulation
            [clj-time.core   :as t]
            [clj-time.format :as f]
            [clj-time.local  :as l]
            [clojure.pprint]
            )
  (:gen-class)
  )

;; Process downloaded file
;; Use from command line with:
;;   lein run -m sporting-fixtures.afl
;;
;; Development
;;   (ns sporting-fixtures.afl)
;;   (load "afl")
;;   (use 'sporting-fixtures.afl)
;;   (use 'clojure.core)
;;   (clojure.pprint/pprint (read-event-data filename))
;;   (clojure.pprint/pprint (rest (reduce conj [] (read-event-data filename)) ))

(def event-name     "2019-aus-afl-mens")
(def event-data-csv "data/download/afl-2019-CenAustraliaStandardTime.csv")

(def preamble
  {:title    "2016 AFL Draw & Fixtures"
   :location "Australia"
   :code      "afl"
   :date      {:from "24 Mar 2016"
               :to "28 Aug 2016"}
   :format    "afl-mens-2019"
   :version   0.1
   })

(def teams
  {:fre {:name "Fremantle"}
   :wce {:name "West Coast Eagles"}
   :haw {:name "Hawthorn"}
   :syd {:name "Sydney Swans"}
   :ric {:name "Richmond"}
   :wbd {:name "Western Bulldogs"}
   :adl {:name "Adelaide Crows"}
   :nmb {:name "North Melbourne"}
   :pta {:name "Port Adelaide"}
   :gee {:name "Geelong Cats"}
   :gws {:name "GWS Giants"}
   :col {:name "Collingwood"}
   :mel {:name "Melbourne"}
   :stk {:name "St Kilda"}
   :ess {:name "Essendon"}
   :gcs {:name "Gold Coast Suns"}
   :bri {:name "Brisbane Lions"}
   :car {:name "Carlton"}
   }
)

(def venues
  {:mcg {:name "MCG"}
   :adl {:name "Adelaide Oval"}
   :mrv {:name "Marvel Stadium"}
   :gab {:name "Gabba"}
   :syd {:name "Sydney Showground Stadium"}
   :opt {:name "Optus Stadium"}
   :scg {:name "SCG"}
   :gee {:name "GMHBA Stadium"}
   :met {:name "Metricon Stadium"}
   :can {:name "UNSW Canberra Oval"}
   :tas {:name "University of Tasmania Stadium"}
   :mar {:name "Mars Stadium"}
   :blu {:name "Blundstone Arena"}
   :jia {:name "Adelaide Arena at Jiangwan Stadium"}
   :riv {:name "Riverway Stadium"}
   :tra {:name "TIO Traeger Park"}
   :tio {:name "TIO Stadium"}
   })

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-lookup-team-id [teams]
  (reduce conj {}
          (map (fn [[k v]] [(:name v) k]) teams)
          )
  )

(def lookup-team-id (create-lookup-team-id teams))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-lookup-venue-id [venues]
  (reduce conj {}
          (map (fn [[k v]] [(:name v) k]) venues)
          )
  )

(def lookup-venue-id (create-lookup-venue-id venues))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn convert-result [result]
  (let [string-split  (str/split result #" ")]
    [(Integer/parseInt (string-split 0))
     (Integer/parseInt (string-split 2))]
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn read-event-data [filename]
  ;;  (println (slurp filename))
  (reduce conj [] (rest
   (with-open [reader (io/reader filename)]
     (doall
      (csv/read-csv reader)))))
  )

(defn parse-number
  "Reads an integer from a string. Returns string if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    (read-string s)
    s))

;; FIXME - Change this to use headers from first line in file.
(defn parse-record [record]
  (reduce conj
          {}
          [[:round-number (if (integer? (parse-number (record 0)))
                            (parse-number (record 0))
                            (record 0))]
           [:date         (record 1)]
           (if (not= (record 2) "TBA")
             [:location
              (if (lookup-venue-id (record 2))
                (lookup-venue-id (record 2))
                (record 2))
                ]
             nil
             )
           ;; lookup-team-id returns 'nil' for 'To be determined'
           (if (or (lookup-team-id (record 3))
                   (lookup-team-id (record 4)))
             [:teams [(lookup-team-id (record 3))
                      (lookup-team-id (record 4))]]
             nil)
           (if (not= (record 5) "")
             [:result       (convert-result (record 5))]
             nil
             )
           ]))

(defn parse-fixture-data [data]
  (map parse-record data))

(defn write-event-json [data]
  (spit "data/2019-aus-afl-mens.json"
        (generate-string
         data
         {:pretty true})
        ))

(defn file-write-yaml [data]
  (spit "data/2019-aus-afl-mens.yml"
        (str
         "---\n"
        (yaml/generate-string data)
        )))

(defn create-event-data [filename]
  (conj preamble
        ;; [:teams teams]
        ;; [:venues venues]
        [:games (parse-fixture-data (read-event-data filename))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2019 AFL Mens
;;
(defn event-id-to-filename [id]
  (str "data/" id ".yml"))

(defn read-event [event]
  (yaml/parse-string (slurp event)))

(defn get-event   [] (read-event (event-id-to-filename event-name)))

;;(defn event-games [] (:games (get-event)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Consider changing to 'clojure-java-time'
;;   See: https://github.com/dm3/clojure.java-time
;; Time parsing an manipulation
;; Formatters can be listed with: (f/show-formatters)
(defn localtime [datetime]
  (let [formatter (f/formatter :rfc822)
        my-formatter (f/with-zone
                       (f/formatter "dd/MM/yyyy hh:mm")
                       (t/default-time-zone))
        ]
    (f/unparse my-formatter
               (f/parse formatter datetime))             
    ))

;; Create results table
(defn event-games-table-header []
  (str
   (format " %3s | %3s | %-17s | %-10s | %-9s    | %s"
           "Id"
           "Rnd"
           "Time"
           "Teams"
           "Score"
           "Result")
   "\n"
   (str (str/join "" (repeat 78 "-")) "\n")
   "\n"
   )
  )

(defn event-games-table [event]
  (let [games (:games event)]
    (str
     (event-games-table-header)
     (str/join
      "\n"
      (map-indexed
       (fn [i x]
         (str
          (if (= i 0)   (str "----- Minor Rounds -----"
                             (str/join (repeat 50 "-")) "\n") "")
          (if (= i 198)  (str "----- Finals -----------"
                             (str/join (repeat 50 "-")) "\n") "")
          (if (= i 202)  (str "----- Semifinals -------"
                             (str/join (repeat 50 "-")) "\n") "")
          (if (= i 204)  (str "----- Preliminary Final "
                             (str/join (repeat 50 "-")) "\n") "")
          (if (= i 206)  (str "----- Grand Final ------"
                             (str/join (repeat 50 "-")) "\n") "")
          (format " %3d | %3s | %s | %-10s |  %4s %4s "
                  (inc i)
                  (:round-number x)
                  ;;(localtime (:date x))
                  (:date x)
                  (str (if (nth (:teams x) 0)
                         (str/upper-case (nth (:teams x) 0))
                         "---")
                       " vs "
                       (if (nth (:teams x) 1)
                         (str/upper-case (nth (:teams x) 1))
                         "---"))
                  (format "%3s"
                          (if (nth (:result x) 0)
                            (nth (:result x) 0)
                            "-"))
                  (format "%3s"
                          (if (nth (:result x) 1)
                            (nth (:result x) 1)
                            "-"))
                  )))
       games)
      )
     "\n"
     (str (str/join "" (repeat 78 "-")) "\n")
     )
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn -main []
  (println "Event: " event-name)
  
  ;; Create YAML data file from downloaded CSV
    (if false
    (let []
      (println "Create YAML data file")
      (println "  CSV:  " event-data-csv)
      (println "  YAML: " "data/2019-aus-afl-mens.yml")
      (file-write-yaml (create-event-data event-data-csv))))

  ;; (clojure.pprint/pprint (:games (get-event)))
  (println (event-games-table (get-event)))
  )

