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
 [:update-match-result 28 {:bri "18.8(116)" :col "11.17(83)"}  "BRI won by 33 points"]
 ;; [:no-action]
 ;;
 ]
