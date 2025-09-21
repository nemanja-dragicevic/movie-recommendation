(ns movie-recommendation.dataset)

(def movies (atom [{:id 1
                    :title "Mall Cop"
                    :genres ["Comedy", "Action"]
                    :plot "A bumbling mall security guard must step up to protect shoppers when a heist threatens his mall."
                    :cast ["Kevin James", "Keir O'Donnell", "Jayma Mays", "Bobby Cannavale"]
                    :director "Steve Carr"} 
                   {:id 2 
                    :title "Twister"
                    :genres ["Action", "Adventure", "Disaster"]
                    :plot "A group of storm chasers race against time to launch a new tornado-monitoring device while battling deadly twisters in the Midwest."
                    :cast ["Helen Hunt", "Bill Paxton", "Philip Seymour Hoffman", "Cary Elwes"]
                    :director "Jan de Bont"} 
                   {:id 3 
                    :title "Jaws"
                    :genres ["Thriller", "Horror", "Adventure"]
                    :plot "A great white shark terrorizes a small beach town, forcing a police chief, a marine biologist, and a professional shark hunter to work together to stop it."
                    :cast ["Roy Scheider", "Robert Shaw", "Richard Dreyfuss", "Lorraine Gary"]
                    :director "Steven Spielberg"} 
                   {:id 4 
                    :title "Observe and Report"
                    :genres ["Dark Comedy", "Crime"]
                    :plot "A mall security guard with delusions of grandeur attempts to take down a flasher while struggling with his own personal issues."
                    :cast ["Seth Rogen", "Anna Faris", "Ray Liotta", "Michael Peña"]
                    :director "Jody Hill"} 
                   {:id 5 
                    :title "Pirates of the Caribbean: The Curse of the Black Pearl"
                    :genres ["Action", "Adventure", "Fantasy"]
                    :plot "Blacksmith Will Turner teams up with eccentric pirate Captain Jack Sparrow to save Elizabeth Swann from cursed pirates."
                    :cast ["Johnny Depp", "Orlando Bloom", "Keira Knightley"]
                    :director "Gore Verbinski"}
                   {:id 6 
                    :title "Sharknado"
                    :genres ["Action", "Comedy", "Horror", "Sci-Fi"]
                    :plot "A freak hurricane causes sharks to be swept up into tornadoes, threatening to destroy Los Angeles and leaving a group of survivors to fight back."
                    :cast ["Ian Ziering", "Tara Reid", "John Heard", "Cassie Scerbo"]
                    :director "Anthony C. Ferrante"} 
                   {:id 7 
                    :title "Pirates of the Caribbean: At World's End"
                    :genres ["Action", "Adventure", "Fantasy"]
                    :plot "Captain Barbossa, Will Turner and Elizabeth Swann must sail off the edge of the map, navigate treachery and betrayal, to save Jack Sparrow."
                    :cast ["Johnny Depp", "Orlando Bloom", "Keira Knightley"]
                    :director "Gore Verbinski"} 
                   {:id 8 
                    :title "The Lord of the Rings: The Fellowship of the Ring"
                    :genres ["Epic fantasy", "Adventure"]
                    :plot "A young hobbit named Frodo Baggins inherits the One Ring and, along with a fellowship of companions, must journey across Middle-earth to destroy it in the fires of Mount Doom before the Dark Lord Sauron can claim it."
                    :cast ["Elijah Wood", "Ian McKellen", "Viggo Mortensen", "Sean Astin", "Orlando Bloom"]
                    :director "Peter Jackson"} 
                   {:id 9 
                    :title "The Lord of the Rings: The Two Towers"
                    :genres ["Fantasy", "Adventure", "Epic"]
                    :plot "fter the Fellowship is broken, Frodo and Sam journey toward Mordor with Gollum as their guide, while Aragorn, Legolas, and Gimli aid the kingdom of Rohan and fight back against Saruman’s forces."
                    :cast ["Elijah Wood", "Ian McKellen", "Viggo Mortensen", "Sean Astin", "Orlando Bloom"]
                    :director "Peter Jackson"} 
                   {:id 10 
                    :title "The Hobbit: An Unexpected Journey"
                    :genres ["Adventure", "Fantasy"]
                    :plot "A reluctant hobbit, Bilbo Baggins, is swept into a quest with a company of thirteen dwarves and the wizard Gandalf to reclaim the lost Dwarf Kingdom of Erebor from the dragon Smaug, and along the way he encounters Gollum and acquires a mysterious ring"
                    :cast ["Martin Freeman", "Ian McKellen", "Richard Armitage"]
                    :director "Peter Jackson"}]))

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
                     :movie-id 2
                     :rating 2}
                    {:user-id 1 
                     :movie-id 3
                     :rating 1}
                    {:user-id 1
                     :movie-id 5
                     :rating 5}
                    {:user-id 1
                     :movie-id 6
                     :rating 4}
                    {:user-id 1
                     :movie-id 7
                     :rating 5}
                    {:user-id 1
                     :movie-id 8
                     :rating 5}
                    {:user-id 2
                     :movie-id 1
                     :rating 5}
                    {:user-id 2
                     :movie-id 2
                     :rating 5}
                    {:user-id 2
                     :movie-id 5
                     :rating 4}
                    {:user-id 2
                     :movie-id 9
                     :rating 5}
                    {:user-id 2
                     :movie-id 10
                     :rating 5}
                    {:user-id 3
                     :movie-id 5
                     :rating 5}
                    {:user-id 3
                     :movie-id 7
                     :rating 5}
                    {:user-id 3
                     :movie-id 8
                     :rating 5}
                    {:user-id 3
                     :movie-id 10
                     :rating 4}
                    {:user-id 4
                     :movie-id 1
                     :rating 3}
                    {:user-id 4
                     :movie-id 2
                     :rating 4}
                    {:user-id 4
                     :movie-id 3
                     :rating 3}
                    {:user-id 4
                     :movie-id 4
                     :rating 4}
                    {:user-id 4
                     :movie-id 6
                     :rating 4}
                    {:user-id 4
                     :movie-id 7
                     :rating 2}
                    {:user-id 4
                     :movie-id 8
                     :rating 4}
                    {:user-id 4
                     :movie-id 9
                     :rating 3}
                    {:user-id 5 
                     :movie-id 8
                     :rating 5}]))


