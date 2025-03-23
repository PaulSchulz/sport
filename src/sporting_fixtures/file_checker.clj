(ns sporting-fixtures.file-checker
  (:require [clojure.java.io :as io]))

(defn read-file-with-error-reporting [filename]
  (with-open [rdr (io/reader filename)]
    (let [pushback-rdr (java.io.PushbackReader. rdr)]
      (loop [line-num 1]
        (let [form (try
                     (read pushback-rdr false nil)
                     (catch Exception e
                       (println (format "Error on line %d: %s" line-num (.getMessage e)))
                       nil))]
          (when form
            (recur (inc line-num))))))))
