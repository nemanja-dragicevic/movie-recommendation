(ns movie-recommendation.dataset)

(def movies (atom [{:id 1
                    :title "Matrix"
                    :genres ["Science fiction", "Action"]} 
                   {:id 2 
                    :title "John Wick"} 
                   {:id 3 
                    :title "Pirates of the Caribbean: The Curse of the Black Pearl"} 
                   {:id 4 
                    :title "A Moment to Remember"} 
                   {:id 5 
                    :title "On Your Wedding Day"}
                   {:id 6 
                    :title "Be With You"} 
                   {:id 7 
                    :title "The Wolf of Wall Street"} 
                   {:id 8 
                    :title "Shutter Island"} 
                   {:id 9 
                    :title "Jaws"} 
                   {:id 10 
                    :title "Saving Private Ryan"} 
                   {:id 11 
                    :title "Goodfellas"} 
                   {:id 12 
                    :title "The Godfather"} 
                   {:id 13 
                    :title "Interstellar"} 
                   {:id 14 
                    :title "The Lord of the Rings: The Fellowship of the Ring"} 
                   {:id 15 
                    :title "Smile"}]))

(def users (atom
            {"main-user" {:id 1
                     :first_name "Nemanja"
                     :last_name "Dragicevic"
                     :username "main-user"
                     :password "bcrypt+sha512$0b3ff79831dd5b27555d606e3bd58b31$12$0e9e537448a131bf67861a9c307938218254786070172f3b"}
             "Ana" {:id 2
                    :first_name "Ana"
                    :last_name "Marentis"
                    :username "Ana"
                    :password "bcrypt+sha512$0b3ff79831dd5b27555d606e3bd58b31$12$0e9e537448a131bf67861a9c307938218254786070172f3b"}
             "Betty" {:id 3
                    :first_name "Betty"
                    :last_name "Thorne"
                    :username "Betty"
                    :password "bcrypt+sha512$0b3ff79831dd5b27555d606e3bd58b31$12$0e9e537448a131bf67861a9c307938218254786070172f3b"}
             "Carlos" {:id 4
                    :first_name "Carlos"
                    :last_name "Virella"
                    :username "Carlos"
                    :password "bcrypt+sha512$0b3ff79831dd5b27555d606e3bd58b31$12$0e9e537448a131bf67861a9c307938218254786070172f3b"}
             "Dana" {:id 5
                       :first_name "Dana"
                       :last_name "Kessler"
                       :username "Dana"
                       :password "bcrypt+sha512$0b3ff79831dd5b27555d606e3bd58b31$12$0e9e537448a131bf67861a9c307938218254786070172f3b"}
             }))

(def ratings (atom [{:user-id 1
                     :movie-id 1
                     :rating 4}
                    {:user-id 1 
                     :movie-id 2
                     :rating 3}
                    {:user-id 1
                     :movie-id 4
                     :rating 5}]))

;; TODO: When user doesn't have enough ratings, recommend the most popular movies
;; TODO: Connect Python with Clojure
;; User ratings

