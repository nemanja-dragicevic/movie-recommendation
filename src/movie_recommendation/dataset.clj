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
            {"Cone" {:id 1
                     :first_name "Nemanja"
                     :last_name "Dragicevic"
                     :username "Cone"
                     :password "Vozd"}
             "John" {:id 2
                     :first_name "John"
                     :last_name "Doe"
                     :username "John"
                     :password "Doe"}}))

;; TODO: When user doesn't have enough ratings, recommend the most popular movies
;; TODO: Connect Python with Clojure
;; User ratings

