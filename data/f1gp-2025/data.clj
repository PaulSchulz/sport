{:event   "f1gp-2025",
 :data    "data/f1gp-2025/data.clj",
 :details {
           :date     "2025",
           :title    "Formula One World Champtionship 2025",
           :datadir  "data/f1gp-2025/",
           :url      "https://www.afl.com.au",
           :code     :f1gp,
           :location "World",
           }
 :teams   [
           {:id :alp :name "Alpine"       :engine "Renalt"     :country :fra}
           {:id :ast :name "Aston Martin" :engine "Mercedes"   :country :uk}
           {:id :fer :name "Ferrari"      :engine "Ferrari"    :country :ita}
           {:id :haa :name "Haas"         :engine "Ferrari"    :country :usa}
           {:id :kic :name "Kick Sauber"  :engine "Ferrari"    :country :swi}
           {:id :mcl :name "McLaren"      :engine "Mercedes"   :country :uk}
           {:id :red :name "Red Bull"     :engine "Honda RBPT" :country :aut}
           {:id :wil :name "Williams"     :engine "Mercedes"   :country :uk}
           {:id :mer :name "Mercedes"     :engine "Mercedes"   :country :ger}
           ]
 :drivers [
           {:id :alb :name "Alex Albon"            :number 23 :team :wil :country :tha}
           {:id :alo :name "Fernando Alonso"       :number 14 :team :ast :country :esp}
           {:id :ant :name "Andrea Kimi Antonelli" :number 12 :team :mer :country :ita}
           {:id :bea :name "Oliver Bearman"        :number 87 :team :haa :country :uk}
           {:id :bor :name "Gabriel Bortoleto"     :number  5 :team :kic :country :bra}
           {:id :doo :name "Jack Doohan"           :number  7 :team :alp :country :aus}
           {:id :gas :name "Pierre Gasly"          :number 10 :team :alp :country :fra}
           {:id :had :name "Isack Hadjar"          :number  6 :team :rb  :country :fra}
           {:id :ham :name "Lewis Hamilton"        :number 44 :team :fer :country :uk}
           {:id :hul :name "Nico Hulkenberg"       :number 27 :team :kic :country :ger}
           {:id :law :name "Liam Lawson"           :number 30 :team :red :country :nz}
           {:id :lec :name "Charles Leclerc"       :number 16 :team :fer :country :mon}           ￼
           {:id :nor :name "Lando Norris"          :number  4 :team :mcl :country :uk}
           {:id :oco :name "Esteban Ocon"          :number 31 :team :haa :country :fra}
           {:id :pia :name "Oscar Piastri"         :number 81 :team :mcl :country :aus}
           {:id :rus :name "George Russell"        :number 63 :team :mer :country :uk}
           {:id :sai :name "Carlos Sainzx Jr."     :number 55 :team :wil :country :esp}
           {:id :str :name "Lance Stroll"          :number 18 :team :ast :country :can}
           {:id :tsu :name "Yuki Tsunoda"          :number 22 :team :rb  :country :jap}
           {:id :ver :name "Max Verstappen"        :number  1 :team :red :country :ned}
           ]
 :results [
           {:round   1
            :name    "Australian Grand Prix"
            :circuit "Albert Park Circuit, Melbourne"
            :date    "16 March"
            :dateutc "2025-03-16 11:00Z",
            :results
            {:race
             {:nor {:position  1   :time "1:42:06.304" :grid  1   :points 25}
              :ver {:position  2   :time "+0.895s"     :grid  3   :points 18}
              :rus {:position  3   :time "+8.481s"     :grid  4   :points 15}
              :ant {:position  4   :time "+10.135s"    :grid 16   :points 12}
              :alb {:position  5   :time "+12.773s"    :grid  6   :points 10}
              :str {:position  6   :time "+17.413s"    :grid 13   :points  8}
              :hul {:position  7   :time "+18.423s"    :grid 17   :points  6}
              :lec {:position  8   :time "+19.826s"    :grid 7    :points  4}
              :pia {:position  9   :time "+20.448s"    :grid 2    :points  2}
              :ham {:position 10   :time "+22.473s"    :grid 8    :points  1}
              :gas {:position 11   :time "+26.502s"    :grid 9    :points  0}
              :tsu {:position 12   :time "+29.884s"    :grid 5    :points  0}
              :oco {:position 13   :time "+33.161s"    :grid 19   :points  0}
              :bea {:position 14   :time "+40.351s"    :grid :pl  :points  0}
              :law {:position 15   :time :dnf          :grid :pl  :points  0}
              :bor {:position 16   :time :dnf          :grid 15   :points  0}
              :alo {:position 17   :time :dnf          :grid 12   :points  0}
              :sai {:position 18   :time :dnf          :grid 10   :points  0}
              :doo {:position 19   :time :dnf          :grid 14   :points  0}
              :had {:position :dns :time :dnf          :grid :dns :points  0}
              }
             }
            }

           {:round 2
            :name "Chinese Grand Prix"
            :circuit "Shanghai International Circuit, Shanghai"
            :date "23 March"
            :dateutc "2025-03-23 07:00Z",
            :results
            {:sprint
             {:ham {:position  1 :time "30:39.965"   :grid  1  :points 8}
              :pia {:position  2 :time "+6.889s"     :grid  3  :points 7}
              :ver {:position  3 :time "+9.804s"     :grid  2  :points 6}
              :rus {:position  4 :time "+11.592s"    :grid  5  :points 5}
              :lec {:position  5 :time "+12.190s"    :grid  4  :points 4}
              :tsu {:position  6 :time "+22.288s"    :grid  8  :points 3}
              :ant {:position  7 :time "+23.038s"	 :grid  7  :points 2}
              :nor {:position  8 :time "+23.471s"    :grid  6  :points 1}
              :str {:position  9 :time "+24.916s"    :grid 10  :points 0}
              :alo {:position 10 :time "+38.218s"    :grid 11  :points 0}
              :alb {:position 11 :time "+39.292s"    :grid  9  :points 0}
	          :gas {:position 12 :time "+39.649s"    :grid 17  :points 0}
              :had {:position 13 :time "+42.400s"    :grid 15  :points 0}
              :law {:position 14 :time "+44.904s"    :grid 19  :points 0}
              :bea {:position 15 :time "+45.649s"    :grid 12  :points 0}
              :oco {:position 16 :time "+46.182s"    :grid 18  :points 0}
              :sai {:position 17 :time "+51.376s"    :grid 13  :points 0}
              :bor {:position 18 :time "+53.940s"    :grid 14  :points 0}
              :hul {:position 19 :time "+56.682s"    :grid :pl :points 0}
              :doo {:position 20 :time "+1:10.2121s" :grid 16  :points 0}
              }

             :race
             {:pia {:position  1 :time "1:30:55.026" :points 25}
              :nor {:position  2 :time "+9.748s"     :points 18}
              :rus {:position  3 :time "+11.097s"    :points 15}
              :ver {:position  4 :time "+16.656s"    :points 12}
              :lec {:position  5 :time "+23.211s"    :points 10}
              :ham {:position  6 :time "+25.381s"    :points  8}
              :oco {:position  7 :time "+49.969s"    :points  6}
              :ant {:position  8 :time "+53.748s"    :points  4}
              :alb {:position  9 :time "+56.321s"    :points  2}
              :bea {:position 10 :time "+61.303s"    :points  1}
              :gas {:position 11 :time "+67.195s"    :points  0}
              :str {:position 12 :time "+70.204s"    :points  0}
              :sai {:position 13 :time "+76.387s"    :points  0}
              :had {:position 14 :time "+78.875s"    :points  0}
              :law {:position 15 :time "+81.147s"    :points  0}
              :doo {:position 16 :time "+88.401s"    :points  0}
              :bor {:position 17 :time "+1 lap"      :points  0}
              :hul {:position 18 :time "+1 lap"      :points  0}
              :tsu {:position 19 :time "+1 lap"      :points  0}
              :alo {:position 20 :time :dnf          :points  0}
              }
             }

            }

           {:round  3
            :name "Japanese Grand Prix"
            :circuit "Suzuka International Racing Course, Suzuka"
            :date "6 April"
            :dateutc "2025-04-06 05:00Z",
            :results
            {:race
             {
              :ver {:position  1 :time "1:22:06.983" :points 25 :grid  1}
              :nor {:position  2 :time "+1.423s"     :points 18 :grid  2}
              :pia {:position  3 :time "+2.129s"     :points 15 :grid  3}
              :lec {:position  4 :time "+16.097s"    :points 12 :grid  4}
              :rus {:position  5 :time "+17.362s"    :points 10 :grid  5}
              :ant {:position  6 :time "+18.671s"    :points  8 :grid  6}
              :ham {:position  7 :time "+29.182s"    :points  6 :grid  8}
              :had {:position  8 :time "+37.134s"    :points  4 :grid  7}
              :alb {:position  9 :time "+40.367s"    :points  2 :grid  9}
              :bea {:position 10 :time "+54.529s"    :points  1 :grid 10}
              :alo {:position 11 :time "+57.333s"    :points  0 :grid 12}
              :tsu {:position 12 :time "+58.401s"    :points  0 :grid 14}
              :gas {:position 13 :time "+62.122s"    :points  0 :grid 11}
              :sai {:position 14 :time "+74.129s"    :points  0 :grid 15}
              :doo {:position 15 :time "+81.314s"    :points  0 :grid 19}
              :hul {:position 16 :time "+81.957s"    :points  0 :grid 16}
              :law {:position 17 :time "+82.438s"    :points  0 :grid 13}
              :oco {:position 18 :time "+83.897s"    :points  0 :grid 18}
              :bor {:position 19 :time "+83.897s"    :points  0 :grid 17}
              :str {:position 20 :time "+1 lap"      :points  0 :grid 20}
              }
             }

            {:round   4
             :name    "Bahrain Grand Prix" ￼
             :circuit "Bahrain International Circuit, Sakhir"
             :date    "13 April"
             :dateutc "2025-04-13 15:00:00Z",
             :result
             {:race
              :pia {:position  1 :time ""    :points 25 :grid  1}
              :rus {:position  2 :time ""    :points 18 :grid  3}
              :nor {:position  3 :time ""    :points 15 :grid  6}
              :lec {:position  4 :time ""    :points 12 :grid  2}
              :ham {:position  5 :time ""    :points 10 :grid  9}
              :ver {:position  6 :time ""    :points  8 :grid  7}
              :gas {:position  7 :time ""    :points  6 :grid  4}
              :oco {:position  8 :time ""    :points  4 :grid 14}
              :tsu {:position  9 :time ""    :points  2 :grid 10}
              :bea {:position 10 :time ""    :points  1 :grid 20}
              :ant {:position 11 :time ""    :points  0 :grid  5}
              :alb {:position 12 :time ""    :points  0 :grid 15}
              :hul {:position 13 :time ""    :points  0 :grid 16}
              :had {:position 14 :time ""    :points  0 :grid 12}
              :doo {:position 15 :time ""    :points  0 :grid 11}
              :alo {:position 16 :time ""    :points  0 :grid 12}
              :law {:position 17 :time ""    :points  0 :grid 17}
              :str {:position 18 :time ""    :points  0 :grid 19}
              :bor {:position 19 :time ""    :points  0 :grid 18}
              :sai {:position 20 :time :dnf  :points  0 :grid  8}
              }
             }

            {:round    5
             :name    "Saudi Arabian Grand Prix"
             :circuit "Jeddah Corniche Circuit, Jeddah"
             :date    "20 April"
             :result
             {race:
              :pia {:position  1 :time "1:21:06.758" :points 25 :grid  2}
              :ver {:position  2 :time "+2.843s"     :points 18 :grid  1}
              :lec {:position  3 :time "+8.104s"     :points 15 :grid  4}
              :nor {:position  4 :time "+9.196s"     :points 12 :grid 10}
              :rus {:position  5 :time "+27.236s"    :points 10 :grid  3}
              :ant {:position  6 :time "+34.688s"    :points  8 :grid  5}
              :ham {:position  7 :time "+39.073s"    :points  6 :grid  7}
              :sai {:position  8 :time "+64.630s"    :points  4 :grid  6}
              :alb {:position  9 :time "+66.515s"    :points  2 :grid 11}
              :had {:position 10 :time "+67.091s"    :points  1 :grid 14}
              :alo {:position 11 :time "+75.917s"    :points  0 :grid 13}
              :law {:position 12 :time "+78.451s"    :points  0 :grid 12}
              :bea {:position 13 :time "+79.194s"    :points  0 :grid 15}
              :oco {:position 14 :time "+99.723s"    :points  0 :grid 19}
              :hul {:position 15 :time "+1 Lap"      :points  0 :grid 18}
              :str {:position 16 :time "+1 Lap"      :points  0 :grid 16}
              :doo {:position 17 :time "+1 Lap"      :points  0 :grid 17}
              :bor {:position 18 :time "+1 Lap"      :points  0 :grid 20}
              :tsu {:position 19 :time ""            :points  0 :grid  8}
              :gas {:position 20 :time ""            :points  0 :grid  9}
              }
             }

            {:round  6
             :name "Miami Grand Prix" ￼
             :circuit "Miami International Autodrome, Miami Gardens, Florida"
             :date "4 May"
             :results
             {:sprint
              {
               :nor {:position  1 :time "36:37.647" :grid  3  :points 8}
               :pia {:position  2 :time "+0.672s"   :grid  2  :points 7}
               :ham {:position  3 :time "+1.073s"   :grid  7  :points 6}
               :rus {:position  4 :time "+3.127s"   :grid  5  :points 5}
               :str {:position  5 :time "+3.412s"   :grid 16  :points 4}
               :tsu {:position  6 :time "+5.153s"   :grid :pl :points 3}
               :ant {:position  7 :time "+5.635s"   :grid  1  :points 2}
               :gas {:position  8 :time "+5.973s"   :grid 13  :points 1}
               :hul {:position  9 :time "+6.153s"   :grid 11  :points 0}
               :had {:position 10 :time "+7.502s"   :grid  9  :points 0}
               :alb {:position 11 :time "+7.522s"   :grid  8  :points 0}
               :oco {:position 12 :time "+8,998s"   :grid 12  :points 0}
               :law {:position 13 :time "+9.024s"   :grid 14  :points 0}
               :bea {:position 14 :time "+9.218s"   :grid 19  :points 0}
               :bor {:position 15 :time "+9.675s"   :grid 18  :points 0}
               :doo {:position 16 :time "+9.909s"   :grid 17  :points 0}
               :ver {:position 17 :time "+12.059s"  :grid  4  :points 0}
               :alo {:position 18 :time :dnf :grid 10  :points 0}
               :sai {:position 19 :time :dnf :grid 15  :points 0}
               :lec {:position 20 :time :dnf :grid  6  :points 0}
               }

              :race
              {
               :ver {:position  1 :time "" :grid  1 :points  0}
               :nor {:position  2 :time "" :grid  2 :points  0}
               :ant {:position  3 :time "" :grid  3 :points  0}
               :pia {:position  4 :time "" :grid  4 :points  0}
               :rus {:position  5 :time "" :grid  5 :points  0}
               :sai {:position  6 :time "" :grid  6 :points  0}
               :alb {:position  7 :time "" :grid  7 :points  0}
               :lec {:position  8 :time "" :grid  8 :points  0}
               :oco {:position  9 :time "" :grid  9 :points  0}
               :tsu {:position 10 :time "" :grid 10 :points  0}
               :had {:position 11 :time "" :grid 11 :points  0}
               :ham {:position 12 :time "" :grid 12 :points  0}
               :bor {:position 13 :time "" :grid 13 :points  0}
               :doo {:position 14 :time "" :grid 14 :points  0}
               :law {:position 15 :time "" :grid 15 :points  0}
               :hul {:position 16 :time "" :grid 16 :points  0}
               :alo {:position 17 :time "" :grid 17 :points  0}
               :gas {:position 18 :time "" :grid 18 :points  0}
               :str {:position 19 :time "" :grid 19 :points  0}
               :bea {:position 20 :time "" :grid 20 :points  0}
               }
              }

             }

            {:round  7 :name "Emilia Romagna Grand Prix" :circuit "Imola Circuit, Imola"                       :date "18 May"}
            {:round  8 :name "Monaco Grand Prix"	￼    :circuit "Circuit de Monaco, Monaco"                  :date "25 May"}
            {:round  9 :name "Spanish Grand Prix" ￼      :circuit "Circuit de Barcelona-Catalunya, Montmeló"   :date "1 June"}
            {:round 10 :name "Canadian Grand Prix" ￼     :circuit "Circuit Gilles Villeneuve, Montreal"	       :date "15 June"}
            {:round 11 :name "Austrian Grand Prix" ￼     :circuit "Red Bull Ring, Spielberg"                   :date "29 June"}
            {:round 12 :name "British Grand Prix" ￼      :circuit "Silverstone Circuit, Silverstone"	       :date "6 July"}
            {:round 13 :name "Belgian Grand Prix" ￼      :circuit "Circuit de Spa-Francorchamps, Stavelot"     :date "27 July"}
            {:round 14 :name "Hungarian Grand Prix"	￼    :circuit "Hungaroring, Mogyoród"                      :date "3 August"}
            {:round 15 :name "Dutch Grand Prix"	￼        :circuit "Circuit Zandvoort, Zandvoort"               :date "31 August"}
            {:round 16 :name "Italian Grand Prix" ￼      :circuit "Monza Circuit, Monza"	                   :date "7 September"}
            {:round 17 :name "Azerbaijan Grand Prix" ￼   :circuit "Baku City Circuit, Baku"                    :date "21 September"}
            {:round 18 :name "Singapore Grand Prix"	￼    :circuit "Marina Bay Street Circuit, Singapore"       :date "5 October"}
            {:round 19 :name "United States Grand Prix"	 :circuit "Circuit of the Americas, Austin, Texas"	   :date "19 October"}
            {:round 20 :name "Mexico City Grand Prix" ￼  :circuit "Autódromo Hermanos Rodríguez, Mexico City"  :date "26 October"}
            {:round 21 :name "São Paulo Grand Prix"	￼    :circuit "Interlagos Circuit, São Paulo"              :date "9 November"}
            {:round 22 :name "Las Vegas Grand Prix"	￼    :circuit "Las Vegas Strip Circuit, Paradise, Nevada"  :date "22 November"}
            {:round 23 :name "Qatar Grand Prix"	￼        :circuit "Lusail International Circuit, Lusail"       :date "30 November"}
            {:round 24 :name "Abu Dhabi Grand Prix"	￼    :circuit "Yas Marina Circuit, Abu Dhabi"              :date "7 December"}
           ]
 }
