;; Custom functions for manipulating data
;; Needs to return 'unknown' (usually :home or :away) so that
;; score and scoreboard maps can be built without duplicate keys
(defn map-team-id [name unknown]
  (case name
    "Adelaide Crows"    :adl
    "Geelong Cats"      :gee
    "West Coast Eagles" :wce
    "Fremantle"         :fre
    "Gold Coast Suns"   :gcs
    "Melbourne"         :mel
    "North Melbourne"   :nm
    "Richmond"          :ric
    "GWS Giants"        :gws
    "Hawthorn"          :haw
    "Sydney Swans"      :syd
    "St Kilda"          :stk
    "Essendon"          :ess
    "Western Bulldogs"  :wb
    "Brisbane Lions"    :bri
    "Port Adelaide"     :pa
    "Collingwood"       :col
    "Carlton"           :car
    "To be announced"   unknown
    unknown))
