(ns movie-recommendation.core)

(defn initialize-feature-matrix [rows cols scale]
  (vec (for [_ (range rows)]
         (vec (for [_ (range cols)]
                (rand scale))))))

(initialize-feature-matrix 4 2 1)

