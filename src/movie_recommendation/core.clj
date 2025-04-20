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

(defn fix-V-solve-U [R V] 
  (for [row R]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) row)
          indexes (mapv first pairs)
          values (mapv second pairs)]
      (do
        (println "Row:" row, "Indexes:" indexes "Values:" values)
        (let [temp-V (mapv #(nth V % 0) indexes)]
          (println "Temp V:" temp-V)
        )))))
(fix-V-solve-U testR testV)


