(ns sporting-fixtures.utils
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clj-yaml.core :as yaml])
  (:gen-class))

(defmacro back []
  (ns sporting-fixtures.core))

(defn reload []
  (require 'sporting-fixtures.utils :reload-all))

(defn help []
  (println "From the top-level REPL: (utils)")
  (println "(back)                 ; return to the top-level REPL")
  (println "(reload)               ; reload this namespace")
  (println "(lpr text)             ; print report text, portrait, double sided")
  (println "(set-default-printer)  ; set the default printer when none is set")
  (println "(current-printer)      ; the CUPS default printer")
  (println "(keyword-team name)    ; short keyword for a team name")
  (println "(get-event filename)   ; read a YAML event file"))

(defn keyword-team [team]
  "Convert team name (string) to short keyword."
  (case team
    ;; AFL/AFLW
    "West Coast Eagles"           :wce
    "Western Bulldogs"            :wbd
    ;; A League
    "Melbourne Victory"           :mbv
    "Melbourne City FC"           :mbc
    "Western Sydney Wanderers FC" :wsw
    "Western United FC"           :wes
    (keyword (str/lower-case
              (subs
               (str/replace team #" " "") ; Remove whitespace
               0 3)))))

(defn get-event [filename]
  "Read event data from file"
  (yaml/parse-string (slurp filename)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Queue used when no default printer is set.
;; Change this string to another queue from `lpstat -a`.
;; The Epson ET-4750 found by cups-browsed is named "EPSON_ET_4750_Series".
(def preferred-printer "EPSON_ET_4750_Series")

(defn current-printer
  "The CUPS default printer, or nil when Ubuntu has dropped the setting."
  []
  (let [{:keys [out]} (sh/sh "lpstat" "-d")
        [_ printer] (re-find #"destination:\s+(\S+)" out)]
    printer))

(defn set-default-printer
  "Set the default printer when none is set, and return the printer in use.
   Call this once if `(current-printer)` is nil. An existing default is left
   as it is. To choose a different printer, change `preferred-printer` above
   to a name listed by `lpstat -a`."
  []
  (if-let [current (current-printer)]
    current
    (let [{:keys [exit err]} (sh/sh "lpoptions" "-d" preferred-printer)]
      (if (zero? exit)
        (or (current-printer) preferred-printer)
        (str "lpoptions failed (" exit "): " err)))))

(defn lpr
  "Send report text to the default printer, portrait, double sided, on A4.
   A form feed in the text starts a new page.
   paps emits a page one point short of A4, which the Epson ET-4750 rejects
   when it turns the sheet. Ghostscript rewrites it as A4."
  [text]
  (let [{:keys [exit err]}
        (sh/sh "bash" "-c"
               (str "paps --font 'Liberation Mono 9' --paper a4"
                    " | gs -q -sDEVICE=pdfwrite -sPAPERSIZE=a4"
                    " -dFIXEDMEDIA -dPDFFitPage -o - -"
                    " | lpr -o media=A4 -o sides=two-sided-long-edge")
               :in text)]
    (if (zero? exit)
      "Sent the report to the default printer."
      (str "lpr failed (" exit "): " err))))
