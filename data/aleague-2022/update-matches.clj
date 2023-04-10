;; Details to update game results
;; These details will be read and applied as changes to the data.
;;
;; Actions:
;;   :update-match-result - 'scoreboard' and 'summary' values replace  existing data.
;;     match-number
;;     scoreboard - map of the teams scoreboard strings
;;     summary - string
;;     eg. [:update-match-result 19 {:wb  "10.7(67)"   :bri "7.11(53)"}  "WB won by 14 points"]
;;
;;   :no-action - Do nothing
[;;
 [:update-match-result 36 {:per "2" :bri "1 R"}  ""]
 [:update-match-result 84 {:mc "3"  :adl "3"}    ""]
 [:update-match-result 98 {:sfc "1"  :bri "1"}   ""]
 ;; [:no-action]
 ;;
 ]
