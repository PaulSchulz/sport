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
            :result {
                     :nor {:position  1 :time "1:42:06.304" :points 25}
                     :ver {:position  2 :time "+0.895s"     :points 18}
                     :rus {:position  3 :time "+8.481s"     :points 15}
                     :ant {:position  4 :time "+10.135s"    :points 12}
                     :alb {:position  5 :time "+12.773s"    :points 10}
                     :str {:position  6 :time "+17.413s"    :points  8}
                     :hul {:position  7 :time "+18.423s"    :points  6}
                     :lec {:position  8 :time "+19.826s"    :points  4}
                     :pia {:position  9 :time "+20.448s"    :points  2}
                     :ham {:position 10 :time "+22.473s"    :points  1}
                     :gas {:position 11 :time "+26.502s"    :points  0}
                     :tsu {:position 12 :time "+29.884s"    :points  0}
                     :oco {:position 13 :time "+33.161s"    :points  0}
                     :bea {:position 14 :time "+40.351s"    :points  0}
                     :law {:position 15 :time :dnf          :points  0}
                     :bor {:position 16 :time :dnf          :points  0}
                     :alo {:position 17 :time :dnf          :points  0}
                     :sai {:position 18 :time :dnf          :points  0}
                     :doo {:position 19 :time :dnf          :points  0}
                     :had {:position 20 :time :dnf          :points  0}
                     }
            }

           {:round 2
            :name "Chinese Grand Prix"
            :circuit "Shanghai International Circuit, Shanghai"
            :date "23 March"
            :results
            {
             :sprint {
                      :ham {:position  1 :time "30:39.965"   :grid  1  :points 8}
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

             :race {
                    :pia {:position  1 :time "1:30:55.026" :points 25}
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

           {:round  3 :name "Japanese Grand Prix" ￼     :circuit "Suzuka International Racing Course, Suzuka" :date "6 April"}
           {:round  4 :name "Bahrain Grand Prix" ￼      :circuit "Bahrain International Circuit, Sakhir"	   :date "13 April"}
           {:round  5 :name "Saudi Arabian Grand Prix"  :circuit "Jeddah Corniche Circuit, Jeddah"	           :date "20 April"}
           {:round  6 :name "Miami Grand Prix" ￼        :circuit "Miami International Autodrome, Miami Gardens, Florida" :date "4 May"}
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
