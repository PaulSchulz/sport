(ns sporting-fixtures.core
  (:gen-class)
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str])
  (:use [clojure.java.shell :only [sh]]
        [clojure.pprint :only [pprint]])
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn help []
  (println
   (str/join
    "\n"
    ["==============="
     "Useful Commands"
     "==============="
     "(list-events)                - Show current event files"
     "(read-event \"<filename>\")    - Load event details" 
     "(display-event \"<filename>\") - Display evenmt details"
     "(events-table)               - List events"
     ]
    )))

(defn get-events []
  (str/split (:out (sh "bash" "-c" "ls data/20*.yml")) #"\n"))

(defn list-events []
  (map #(println (format "* %s" %)) (get-events)))

(defn read-event [event]
  (yaml/parse-string (slurp event)))

(defn display-event [event]
  (pprint (read-event event)))

(defn event-details [fmt event]
  (let [data (read-event event)]
    (format fmt
            (str (:title data) " / " (:location data))
            (:from (:date data))
            (:to   (:date data))
            (:code data)
            (:version data)
            )))

(defn events-table []
  (let [fmt "  #  %-56s  %-12s %-12s %16s %-6s"
        width 110]
    (println
     (str/join "\n"
               [(str/join "" (repeat width "-"))
                (format fmt "Name / Location" "From" "To" "Code" "Ver")
                (str/join "" (repeat width "-"))
                (str/join "\n" (map #(event-details fmt %) (get-events)))
                ]))))


