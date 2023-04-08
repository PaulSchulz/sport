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
 [:update-match-result 29 {:nm  "11.18(84)" :car "16.11(107)"} "CAR won by 23 points"]
 [:update-match-result 30 {:adl "17.9(111)" :fre "10.12(72)"}  "ADL won by 39 points"]

 ;; [:no-action]
 ;;
 ]
