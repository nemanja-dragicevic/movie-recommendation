(ns movie-recommendation.core)

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

(defn matrix-multiply
  "Multiplying two matrices, where m2 is already transposed"
  [m1 m2]
  (let [cols-m1 (count (first m1))
        rows-m2 (count (first m2))]
    (if (= cols-m1 rows-m2)
      (mapv (fn [row]
              (mapv (fn [col] (dot-product row col)) m2))
            m1)
      (throw (IllegalArgumentException. (str "Matrices cannot be multiplied" rows-m2 cols-m1))))))
(matrix-multiply testR (transpose testV))

(defn identity-matrix [n lambda]
  (mapv (fn [i]
          (mapv (fn [j] (if (= i j) (+ 1 lambda) 0)) (range n)))
        (range n)))
(identity-matrix 3 lambda)

(reduce + 0 [1 2 3 4]) 

(defn fix-V-solve-U [R V] 
  (for [row R]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) row)
          indexes (mapv first pairs)
          values (mapv second pairs)]
      (do
        (let [temp-V (mapv #(nth V % 0) indexes)
              result (matrix-multiply (transpose temp-V) (transpose temp-V))
              id-mat (identity-matrix (count temp-V) lambda)]
            (println "Result: " result, "ID-Matrix:" id-mat))))))
        
(fix-V-solve-U testR testV)

(mapv (fn [x] x) (range 10)) 





