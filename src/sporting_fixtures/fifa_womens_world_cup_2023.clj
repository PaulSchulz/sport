(ns sporting-fixtures.fifa-womens-world-cup-2023
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh] ;; Printing reports
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn reload []
  (require ['sporting-fixtures.fifa-womens-world-cup-2023 :as 'fwwc2023] :reload-all)
  )

(defn -help []
  ;; Process downloaded file
  ;; Use from command line with:
  (println "Uaage: lein run -m sporting-fixtures.fifa-womens-world-cup-2023 [command]")
  (println "  where the optional 'command' is one of:")
  (println "    help")
  (println "    report")
  (println "    report-save")
  (println "    report-print")
  )
;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def event-name "fifa-womens-world-cup-2023")
(def event-title "FIFA Womens's World Cup 2023")
(def data (r/data-read-event event-name))

(defn report-games [data]
  (println event-title)
  (r/report-games data)
  )

(defn report-teams [data]
  (println event-title)
  (r/report-teams data)
  )

(defn report [data]
  (println event-title)
  (println (r/report-games data))
  (println (r/report-teams data))
  (println (r/report-games-timeline data))
  )

(defn report-games-save [data]
  (let [file (str (-> data :details :datadir) "report-games.txt")]
    (spit file (r/report-games data))
    ))

(defn report-teams-save [data]
  (let [file (str (-> data :details :datadir) "report-teams.txt")]
    (spit file (r/report-teams data))
    ))

(defn report-save [data]
  (report-games-save data)
  (report-teams-save data))

(defn report-games-print [data]
  (let [file (str (-> data :details :datadir) "report-games.txt")]
    (println "Printing games report")
    (sh/sh "/usr/bin/enscript" "-B" "-l" "--margins=0:0:0:0" file))
  )

(defn report-teams-print [data]
  (let [file (str (-> data :details :datadir) "report-teams.txt")]
    (println "Printing teams report")
    (sh/sh "/usr/bin/enscript" "-B" "-l" "--margins=0:0:0:0" file))
  )

(defn report-print [data]
  (println (report-games-print data))
  (println (report-teams-print data))
  )

(defn -main [& options]
(println "-" options "-")
(let [command (if options
                (first options)
                nil)]
  (case command
    "help"         (-help)
    "report"       (report data)
    "report-save"  (report-save data)
    "report-print" (report-print data)
    (-help)
    )
  ))
