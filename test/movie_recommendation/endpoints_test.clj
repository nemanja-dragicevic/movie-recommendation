(ns movie-recommendation.endpoints-test
  (:require [midje.sweet :refer :all]
            [movie-recommendation.endpoints :refer :all]))

(facts "Test get movie by id"
      (fact "Movie doesn't exist"
            (get-movie-by-id nil) => {:status 404, 
                                      :headers {"Content-Type" "application/json"}, 
                                      :body "There is not movie with id: "})
       (fact "Movie exists"
             (= 200 (:status (get-movie-by-id 1)))))

(facts "Get watched movies for a certain user"
       (fact "User id is nil"
              (= 404 (:status (get-user-watched nil))))
       (fact "User have already watched movies"
             (= 200 (:status (get-user-watched 1)))))

(facts "Add movie rating for a user"
       (fact "User already watched a movie"
             (= 400 (:status (add-rating 1 {:body {:movie-id 7 :rating 4}}))))
       (fact "User rates new movie"
             (= 200 (:status (add-rating 1 {:body {:movie-id 9 :rating 4}})))))
