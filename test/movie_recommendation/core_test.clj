(ns movie-recommendation.core-test
  (:require [clojure.test :refer :all]
            [movie-recommendation.core :refer :all]
            [midje.sweet :refer :all]))

(facts "Test transposing of the matrix with different dimensions"
       (fact "Transposing 2x2 matrix"
             (transpose [[1 2] [3 4]]) => [[1 3] [2 4]])
       (fact "Transposing rectangular matrix"
             (transpose [[1 2 3] [4 5 6] [7 8 9]]) => [[1 4 7] [2 5 8] [3 6 9]])
       (fact "Transposing an empty matrix" 
             (transpose []) => [])
       (fact "Transpose single row matrix"
             (transpose [[1 2 3]]) => [[1] [2] [3]])
       (fact "transpose single column matrix"
             (transpose [[1] [2] [3] [4]]) => [[1 2 3 4]]))

(facts "Test multiplying matrices"
       (fact "Multiplying of two compatible matrices"
             (multiply-matrices [[1 2] [3 4]] [[5 6] [7 8]]) 
             => [[19.0 22.0] [43.0 50.0]])
       (fact "Multiplying of two non-squared matrices"
             (multiply-matrices [[1 2 3] [3 4 5]] [[5 6 4] [7 8 6] [9 10 10]])
             => [[46.0 52.0 46.0] [88.0 100.0 86.0]])
       (fact "Unequal number of columns in first and rows in second matrix"
             (multiply-matrices [[1 2 3] [4 5 6]] [[1 2 3] [4 5 6]]) => "Cannot multiply matrices with dimensions: [2 3][2 3]")
       (fact "Empty matrices"
             (multiply-matrices [] []) => "Cannot multiply matrices with dimensions: [0 0][0 0]"))





