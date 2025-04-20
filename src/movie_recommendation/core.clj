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

(defn transpose [matrix]
  (apply mapv vector matrix))

(defn fix-V-solve-U [R V] 
  (for [row R]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) row)
          indexes (mapv first pairs)
          values (mapv second pairs)]
      (do
        (println "Row:" row, "Indexes:" indexes "Values:" values)
        (let [temp-V (mapv #(nth V % 0) indexes)]
          (println "Temp V:" temp-V, "Transposed V:" (transpose temp-V))
        )))))
(fix-V-solve-U testR testV)

(defn dot-product [v1 v2]
  (reduce + (map * v1 v2)))

(defn matrix-multiply [m1 m2]
  "Multiplying two matrices, where m2 is already transposed"
  (mapv (fn [row]
          (mapv (fn [col] (dot-product row col)) m2))
        m1))
(matrix-multiply testR (transpose testV))


