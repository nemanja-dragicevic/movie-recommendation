(ns movie-recommendation.core-test
  (:require [clojure.test :refer :all]
            [movie-recommendation.core :refer :all]
            [midje.sweet :refer :all]
            [clojure.core.matrix :as m]))

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

(facts "Test identity matrix"
       (fact "n is higher than zero"
             (identity-matrix 3 1) => [[1 0 0] [0 1 0] [0 0 1]])
       (fact "works with different values on diagonal"
             (identity-matrix 3 7) => [[7 0 0] [0 7 0] [0 0 7]])
       (fact "n is zero"
             (identity-matrix 0 1) => []))

(facts "Test adding two matrices"
       (fact "Two empty matrices"
             (add-matrices [] []) => [])
       (fact "Two matrices of the same size"
             (add-matrices [[1 2 3] [4 5 6]] [[7 8 9] [10 11 12]]) => [[8 10 12] [14 16 18]])
       (fact "Two matrices of not the same size"
             (add-matrices [[1 2 3] [4 5 6]] [[7 8] [10 11]]) => "Cannot add matrices with dimensions: [2 3][2 2]")
       (fact "One matrix is empty"
             (add-matrices [[1]] []) => "Cannot add matrices with dimensions: [1 1][0 0]"))

(fact "Test merge into matrix an output from a function"
      (merge-into-matrix (fix-V-solve-U [[5 0 3] [4 1 0]]
                                        [[1 0] [0 1] [1 1]]
                                        0.1))
      => :expected
      (provided
       (fix-V-solve-U [[5 0 3] [4 1 0]]
                      [[1 0] [0 1] [1 1]]
                      0.1) => :mocked
       (merge-into-matrix :mocked) => :expected))

(fact "Test Fix V solve U step of ALS method. 
       Number of rows in U is same as number of users in R 
       and number of columns is the same as number of columns in V"
      (let [R [[5 0 3] [4 1 0]]
            V [[1 0] [0 1] [1 1]]
            lambda 0.1
            result (fix-V-solve-U R V lambda)]
        (count result) => 2
        (every? vector? result) => true))

(fact "Test Fix U solve V step of ALS method. 
       Number of rows in V is same as number of movies in R 
       and number of columns is the same as number of columns in U"
      (let [R [[5 0 3] [4 1 0]]
            U [[4.43 -1.3] [3.64 0.91]]
            lambda 0.1
            result (fix-U-solve-V R U lambda)]
        (fact "There are 3 movies"
         (count result) => 3)
        (fact "Every element is a vector"
              (every? vector? result) => true)))

(facts "Test creating a zero matrix"
       (fact "r and c are both higher than 0"
             (m/to-nested-vectors (zero-matrix 3 2)) => [[0.0,0.0],
                                   [0.0,0.0],
                                   [0.0,0.0]])
       (fact "r is negative"
             (zero-matrix -3 2) => "Cannot create zero matrix")
       (fact "c is negative"
             (zero-matrix 3 -2) => "Cannot create zero matrix")
       (fact "r is zero"
             (zero-matrix 0 2) => "Cannot create zero matrix")
       (fact "c is zero"
             (zero-matrix 3 0) => "Cannot create zero matrix"))

(facts "Test filling a zero matrix"
       (fact "There are user ratigns"
             (m/to-nested-vectors (fill-matrix! (zero-matrix 2 2)
                                                (atom [{:user-id 1
                                                        :movie-id 1
                                                        :rating 2}
                                                       {:user-id 1
                                                        :movie-id 2
                                                        :rating 4}
                                                       {:user-id 2
                                                        :movie-id 2
                                                        :rating 5}]))) => [[2.0, 4.0] [0.0, 5.0]])
       (fact "There are no user ratigns"
             (m/to-nested-vectors (fill-matrix! (zero-matrix 2 2)
                           (atom []))) => [[0.0, 0.0] [0.0, 0.0]]))

(facts "Test separate dataset on train and test sets"
       (fact "There are no removed users"
        (let [res (get-train-test [[5 4 0 3 0 2 1 0]
                                   [4 0 5 0 3 0 2 4]
                                   [1 0 5 0 0 2 0 3]
                                   [3 4 2 5 0 0 4 0]
                                   [0 2 0 0 5 4 0 3]]
                                  2.5 3)]
          (fact "Indexes should be empty"
                (count (:indexes res)) => 0)
          (fact "Train and test should have same number of rows"
                (= (count (:train res)) (count (:test res))) => true)
          (fact "Train and test should have same number of columns"
                (= (count (first (:train res))) (count (first (:test res)))) => true)))
       (fact "There are users with less than required number of ratings"
             (let [R [[5 4 0 3 0 2 1 0]
                      [4 0 5 0 3 0 2 4]
                      [1 0 0 0 0 2 0 3]
                      [3 4 2 5 0 0 4 0]
                      [0 2 0 0 5 4 0 3]]
                   res (get-train-test R
                                       2.5 3)]
               (fact "Indexes should not be empty"
                     (pos? (count (:indexes res))) => true)
               (fact "User with index 2 is removed"
                     (:indexes res) => [2])
               (fact "Train and test have one row less than R matrix"
                     (and (= (count (:train res)) (count (:test res))) 
                          (= (dec (count R)) (count (:test res)))) => true)))
       (fact "There are users with very low ratings"
             (let [R [[5 4 0 3 0 2 1 0]
                      [4 0 5 0 3 0 2 4]
                      [1 0 3 5 0 2 0 3]
                      [3 4 2 5 0 0 4 0]
                      [0 1 0 0 2 1 0 3]]
                   res (get-train-test R
                                       2.5 3)]
               (fact "Indexes should not be empty"
                     (pos? (count (:indexes res))) => true)
               (fact "User with index 4 is removed"
                     (:indexes res) => [4])
               (fact "Train and test have one row less than R matrix"
                     (and (= (count (:train res)) (count (:test res)))
                          (= (dec (count R)) (count (:test res)))) => true))))







