;; Custom functions for manipulating data
;; Needs to return 'unknown' (usually :home or :away) so that
;; score and scoreboard maps can be built without duplicate keys
(defn map-team-id [name unknown]
  (case name
    "Adelaide Crows" :adl
    "Brisbane Lions" :bri
    "Carlton" :car
    "Collingwood" :col
    "Essendon" :ess
    "Fremantle" :fre
    "GWS GIANTS" :gws
    "Geelong Cats" :gee
    "Gold Coast SUNS" :gcs
    "Hawthorn" :haw
    "Melbourne" :mel
    "North Melbourne" :nm
    "Port Adelaide" :pa
    "Richmond" :ric
    "St Kilda" :stk
    "Sydney Swans" :syd
    "West Coast Eagles" :wce
    "Western Bulldogs" :wb
    "To be announced" :tba
    unknown))
