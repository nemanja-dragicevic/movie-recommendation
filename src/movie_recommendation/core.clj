(ns movie-recommendation.core
  (:require [movie-recommendation.dataset :as dataset]
            [clojure.core.matrix :as m]))

(m/set-current-implementation :vectorz)

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

(defn multiply-matrices [m1 m2]
  (try
    (m/mmul m1 m2)
    (catch Exception _
      (str "Cannot multiply matrices with dimensions: ", "[", (count m1), " ", (count (first m1)), "]",
           "[", (count m2), " ", (count (first m2)), "]"))))

(defn identity-matrix [n lambda]
  (mapv (fn [i]
          (mapv (fn [j] (if (= i j) (* 1 lambda) 0)) (range n)))
        (range n)))

(defn add-matrices [m1 m2]
  (try
    (m/add m1 m2)
    (catch java.lang.Exception _
      (str "Cannot add matrices with dimensions: ", "[", (count m1), " ", (count (first m1)), "]",
           "[", (count m2), " ", (count (first m2)), "]"))))

(defn scale-matrix [matrix scalar]
  (mapv (fn [row]
          (mapv #(* % scalar) row))
        matrix))

(defn merge-into-matrix [v]
  (vec (for [row v]
         (vec (mapcat identity row)))))

(defn fix-V-solve-U [R V]
  (for [row R]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) row)
          indexes (mapv first pairs)
          values (vector (mapv second pairs))
          temp-V (mapv #(nth V % 0) indexes)
          result (multiply-matrices (transpose temp-V) temp-V)
          id-mat (identity-matrix (count result) lambda)
          A (add-matrices result id-mat)
          B (multiply-matrices (transpose temp-V) (transpose values))]
      ;; (println "Row: ", row)
      ;; (println "Pairs: ", pairs, " Indexes: ", indexes, " Values: ", values)
      ;; (println "Result: ", result)
      ;; (println "A: ", A)
      ;; (println "B: ", B)
      ;; (println "Values: ", values, " temp-v: ", temp-V)
      ;; (multiply-matrices (invert-2x2 (matrix-add result id-mat)) (transpose (multiply-matrices (transpose temp-V) values)))
      (multiply-matrices (m/inverse A) B))))

(defn fix-U-solve-V [R U]
  (for [col (transpose R)]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) col)
          indexes (mapv first pairs)
          values (vector (mapv second pairs))
          temp-U (mapv #(nth U % 0) indexes)
          result (multiply-matrices (transpose temp-U) temp-U)
          id-mat (identity-matrix (count result) lambda)
          A (add-matrices (multiply-matrices (transpose temp-U) temp-U) id-mat)
          B (multiply-matrices (transpose temp-U) (transpose values))]
      ;; (println "Col: ", col)
      ;; (println "Pairs: ", pairs, " Indexes: ", indexes, " Values: ", values, " id mat: ", id-mat)
      ;; (println A)
      ;; (println B)
      (multiply-matrices (m/inverse A) B))))

(def new-U (merge-into-matrix (fix-V-solve-U testR testV)))
new-U
(def new-V (merge-into-matrix (fix-U-solve-V testR new-U)))
new-V
;; (merge-into-matrix (fix-V-solve-U testR testV))

(multiply-matrices new-U (transpose new-V))


(m/inverse [[0.6 -0.7] [-0.2 0.4]])

(def A [[1 2 3]
        [4 5 6]])

(def B [[7 8]
        [9 10]
        [11 12]])

(identity-matrix 3 lambda)
(add-matrices B A)

(multiply-matrices [[0.6 -0.7] [-0.2 0.4]] [[4.0 7.0] [2.0 6.0]])








