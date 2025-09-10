(ns movie-recommendation.core
  (:require [movie-recommendation.dataset :as dataset]))

(defn initialize-feature-matrix [rows cols scale]
  (vec (for [_ (range rows)]
         (vec (for [_ (range cols)]
                (rand scale))))))

(defn print-matrix [matrix]
  (doseq [row matrix]
    (println row)))

(def testR [[5 0 3]
            [4 1 0]])
(def testV [[1 0]
            [0 1]
            [1 1]])
(def lambda 0.1)

(defn transpose [matrix]
  (apply mapv vector matrix))

(defn dot-product [v1 v2]
  (reduce + (map * v1 v2)))

(defn multiply-matrices
  "Multiplying two matrices, where m2 is already transposed"
  [m1 m2]
  (let [cols-m1 (count (first m1))
        rows-m2 (count (first m2))]
    (if (= cols-m1 rows-m2)
      (mapv (fn [row]
              (mapv (fn [col] (dot-product row col)) m2))
            m1)
      (throw (IllegalArgumentException. (str "Matrices cannot be multiplied" rows-m2 cols-m1))))))
(multiply-matrices testR (transpose testV))

(defn identity-matrix [n lambda]
  (mapv (fn [i]
          (mapv (fn [j] (if (= i j) (* 1 lambda) 0)) (range n)))
        (range n)))
(identity-matrix 3 lambda)

(reduce + 0 [1 2 3 4]) 

(defn matrix-add [m1 m2]
  (if (and (= (count m1) (count m2))
            (every? (fn [[row1 row2]] (= (count row1) (count row2))) (map vector m1 m2)))
    (mapv (fn [row1 row2]
            (mapv + row1 row2))
          m1 m2)
    (throw (IllegalArgumentException. "Matrices cannot be added"))))

(defn scale-matrix [matrix scalar]
  (mapv (fn [row]
          (mapv #(* % scalar) row))
        matrix))

(defn invert-2x2 [matrix] 
  (let [[[a b] [c d]] matrix
        det (float (/ 1 (- (* a d) (* b c))))]
    (scale-matrix [[d (- b)] [(- c) a]] det)))
(invert-2x2 [[1 2] [3 4]])

(defn merge-into-matrix [v]
  (vec (for [row v]
         (vec (mapcat identity row)))))

(defn fix-V-solve-U [R V] 
  (for [row R]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) row)
          indexes (mapv first pairs)
          values (vector (mapv second pairs))
          temp-V (mapv #(nth V % 0) indexes)
          result (multiply-matrices (transpose temp-V) (transpose temp-V))
          id-mat (identity-matrix (count result) lambda)]
             (multiply-matrices (invert-2x2 (matrix-add result id-mat)) (transpose (multiply-matrices (transpose temp-V) values))))))
;; (merge-into-matrix (fix-V-solve-U testR testV))

(def new-U (merge-into-matrix (fix-V-solve-U testR testV)))

(defn fix-U-solve-V [R U]
  (for [col (transpose R)]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) col)
          indexes (mapv first pairs)
          values (vector (mapv second pairs))
          temp-U (mapv #(nth U % 0) indexes)
          result (multiply-matrices (transpose temp-U) (transpose temp-U))
          id-mat (identity-matrix (count result) lambda)]
      (multiply-matrices (invert-2x2 (matrix-add result (transpose id-mat))) (transpose (multiply-matrices (transpose temp-U) values))))))
(merge-into-matrix (fix-U-solve-V testR new-U))

(def new-V (merge-into-matrix (fix-U-solve-V testR new-U)))

(def max-iterations 1000)


(defn rmse [R U V]
  (let [predicted (multiply-matrices U V)
        errors (for [i (range (count R))
                     j (range (count (first R)))
                     :when (> (nth (nth R i) j) 0)]
                 (do
                   (println "R: " (nth (nth R i) j) ", Predicted: " (nth (nth predicted i) j))
                   (println "Error: " (- (nth (nth R i) j) (nth (nth predicted i) j)))
                 (- (nth (nth R i) j) (nth (nth predicted i) j))))]
    (Math/sqrt (/ (reduce + 0 (map #(* % %) errors)) (count errors)))))
(rmse testR new-U new-V)




(defn avg-rating [ratings]
  (->> ratings
       (group-by :user-id)
       (map (fn [[user-id user-ratings]]
              [user-id (double (/ (reduce + (map :rating user-ratings))
                          (count user-ratings)))]))
       (into {})))
(avg-rating @dataset/ratings)






