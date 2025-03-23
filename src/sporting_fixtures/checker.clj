(ns checker
  (:require [clojure.edn :as edn]))

;; Usage example:
;; (read-file-with-error-reporting "your-file.clj")

(defn read-file-with-error-reporting [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (loop [line-num 1]
      (let [line (try
                   (read rdr false nil)
                   (catch Exception e
                     (println (format "Error on line %d: %s" line-num (.getMessage e)))
                     nil))]
        (when line
          (recur (inc line-num)))))))
