;; Custom functions for manipulating data
;; Needs to return 'unknown' (usually :home or :away) so that
;; score and scoreboard maps can be built without duplicate keys
(defn map-team-id [name unknown]
  (case name
    "Adelaide Strikers"   :str
    "Brisbane Heat"       :hea
    "Hobart Hurricanes"   :hur
    "Melbourne Renegades" :ren
    "Melbourne Stars"     :sta
    "Perth Scorchers"     :sco
    "Sydney Sixers"       :six
    "Sydney Thunder"      :thu
    "To be announced"     unknown
    :unknown))
