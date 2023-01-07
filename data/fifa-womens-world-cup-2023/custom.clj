;; Custom functions for manipulating data
;; Needs to return 'unknown' (usually :home or :away) so that
;; score and scoreboard maps can be built without duplicate keys
(defn map-team-id [name unknown]
  (case name
    "1A" :1a
    "1B" :1b
    "1C" :1c
    "1D" :1d
    "1E" :1e
    "1F" :1f
    "1G" :1g
    "1H" :1h
    "Argentina" :arg
    "Australia" :aus
    "Brazil" :bra
    "Canada" :can
    "China PR" :chn
    "Colombia" :col
    "Costa Rica" :cos
    "Denmark" :den
    "England" :eng
    "France" :fra
    "Germany" :ger
    "Group A Play-Off Winner" :group-a-play-off-winner
    "Group B Play-Off Winner" :group-b-play-off-winner
    "Group C Play-Off Winner" :group-c-play-off-winner
    "Italy" :ita
    "Jamaica" :jam
    "Japan" :jpn
    "Korea Republic" :kor
    "Morocco" :mar
    "Netherlands" :ned
    "New Zealand" :nzl
    "Nigeria" :nig
    "Norway" :nor
    "Philippines" :phi
    "Republic of Ireland" :irl
    "South Africa" :rsa
    "Spain" :esp
    "Sweden" :swe
    "Switzerland" :sui
    "USA" :usa
    "Vietnam" :vie
    "Zambia" :zam
    unknown))
