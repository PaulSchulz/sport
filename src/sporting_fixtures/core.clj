(ns sporting-fixtures.core
  (:gen-class)
  (:require [clj-yaml.core :as yaml]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

#_(clojure.pprint/pprint (yaml/parse-string (slurp "data/2014-bra-fifa-worldcup.yml")))
#_(clojure.pprint/pprint (yaml/parse-string (slurp "data/2015-aus-afl-finals.yml")))

