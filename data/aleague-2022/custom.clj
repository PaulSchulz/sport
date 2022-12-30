;; Custom functions for manipulating data
;; Needs to return 'unknown' (usually :home or :away) so that
;; score and scoreboard maps can be built without duplicate keys
(defn map-team-id [name unknown]
  (case name
    "Central Coast Mariners"      :ccm
    "Sydney FC"                   :sfc
    "Western Sydney Wanderers FC" :wsw
    "Macarthur FC"                :mfc
    "Adelaide United"             :adl
    "Melbourne City FC"           :mc
    "Melbourne Victory"           :mv
    "Western United FC"           :wu
    "Newcastle Jets"              :new
    "Brisbane Roar FC"            :bri
    "Perth Glory"                 :per
    "Wellington Phoenix"          :wel
    "To be announced"             unknown
    unknown))
