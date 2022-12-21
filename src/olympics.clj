(ns olympics
  (:gen-class)
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [clj-time.core   :as t]
            [clj-time.format :as f]
            [clj-time.local  :as l]
            )
  (:use [clojure.java.shell :only [sh]]
        [clojure.pprint :only [pprint]])
  )

;; (ns sporting-fixtures.olympics-2020)
;; (use 'sporting-fixtures/olympics-2020 :reload)

;; Read Data
(def nocs
  (yaml/parse-string
   (slurp "data/2020-olympics/national-olympic-committees.yaml")))

(defn noc-table [nocs]
  ;; (pprint noc)
  (println
   (str
    (format "Code | Name")))
  (println "------------------------------------------------------------------")
  (println
   (apply str
          (map (fn [noc]
                 (format " %3s | %s\n"
                         (:code noc)
                         (:name noc)))
               (:noc nocs)
               )
          ))
  )

(defn -main []
  (noc-table nocs)
  )
