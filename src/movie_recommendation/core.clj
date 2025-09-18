(ns movie-recommendation.core
  (:require
   [movie-recommendation.dataset :as dataset]
   [clojure.core.matrix :as m]
   [clojure.java.shell :refer [sh]]
   [cheshire.core :as json]))

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

;; (defn transpose [matrix odakle]
;;   (println "To transpose:", matrix)
;;   (println odakle)
;;   (print-matrix matrix)
;;   (println "----------------------")
;;   (apply mapv vector matrix))

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

(defn merge-into-matrix [v]
  (vec (for [row v]
         (vec (mapcat identity row)))))

(defn fix-V-solve-U [R V lambda]
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

(defn fix-U-solve-V [R U lambda]
  (for [col (transpose R)]
    (let [pairs (keep-indexed (fn [i v] (when (> v 0) [i v])) col)
          indexes (mapv first pairs)
          values (vector (mapv second pairs))
          temp-U (mapv #(nth U % 0) indexes)
          result (multiply-matrices (transpose temp-U) temp-U)
          id-mat (identity-matrix (count result) lambda)
          A (add-matrices (multiply-matrices (transpose temp-U) temp-U) id-mat)
          B (multiply-matrices (transpose temp-U) (transpose values))]
      (multiply-matrices (m/inverse A) B))))

(defn zero-matrix [r c]
  (m/zero-matrix r c))
(m/zero-matrix (count @dataset/users) (count @dataset/movies))

(defn fill-matrix! [mat ratings]
  (doseq [{:keys [user-id movie-id rating]} @ratings]
    (m/mset! mat (dec user-id) (dec movie-id) rating))
  mat)

(def R (fill-matrix! (zero-matrix (count @dataset/users) (count @dataset/movies))
                     dataset/ratings))
R



(def C [[5 4 0 3 0 2 1 0]
        [4 0 5 0 3 0 2 4]
        [1 0 1 0 0 2 0 3]
        [3 4 2 5 0 0 4 0]
        [0 2 0 0 5 4 0 3]])

(defn get-train-test [mat min-avg]
  (let [rows (count mat)
        cols (count (first mat))]
    (loop [r 0
           train []
           test []
           indexes []]
      (if (= r rows)
        {:train (vec train)
         :test (vec test)
         :indexes indexes}
        (let [row (nth mat r)
              ratings (keep-indexed (fn [i v] (when (> v 0) [i v])) row)
              shuffled (shuffle ratings)

              ;; k (int (Math/ceil (* 0.8 (count ratings))))
              k (dec (min (int (* 0.8 (count ratings))) (dec (count ratings))))

              total (reduce + 0 (map second ratings))
              avg (double (/ total (count ratings)))]

          (if (< avg min-avg)
            (recur (inc r) train test (conj indexes r))

            (let [not-chosen (drop k shuffled)
                  leftovers (into {} not-chosen)

                  train-row (mapv (fn [i v]
                                    (if (contains? leftovers i) 0 v))
                                  (range cols) row)
                  test-row  (mapv (fn [i _]
                                    (if (contains? leftovers i)
                                      (get leftovers i)
                                      0))
                                  (range cols) row)]
              ;; (println leftovers, ":", not-chosen, indexes)
              (recur (inc r)
                     (conj train train-row)
                     (conj test test-row)
                     indexes))))))))

(defn clean-zero-cols [tr te]
  (let [train-col (transpose tr)
        rem-cols (keep-indexed (fn [i col]
                                 (when (every? zero? col) i))
                               train-col)
        drop-cols (fn [matrix idxs]
                    (mapv (fn [row]
                            (vec (keep-indexed
                                  (fn [i v]
                                    (when-not (some #{i} idxs) v))
                                  row)))
                          matrix))]
    {:train (drop-cols tr rem-cols)
     :test (drop-cols te rem-cols)}))

(defn get-rmse-mat [A B]
  (mapv (fn [row-a row-b]
          (mapv (fn [a b]
                  (if (zero? b)
                    0
                    (abs (- a b))))
                row-a row-b))
        A B))

(defn rmse [m]
  (let [values (mapcat identity m)
        n      (count values)]
    (Math/sqrt
     (/ (reduce + (map #(* % %) values))
        n))))

(rmse [[1 0 3 0]
       [0 0 2 2]
       [5 0 1 0]])

(clean-zero-cols [[1 0 3 0]
                  [0 0 2 2]
                  [5 0 1 0]] [[0 0 1 7]
                              [3 0 4 8]])


(def data (get-train-test C 2.5))
(def prep-data (assoc (clean-zero-cols (:train data) (:test data)) :indexes (:indexes data)))

data
prep-data

(def train-set (:train prep-data))
(def test-set (:test prep-data))

(print-matrix train-set)

(defn initialize-feature-matrix [rows cols]
  (vec (for [_ (range rows)]
         (vec (for [_ (range cols)]
                (rand 1))))))

(print-matrix train-set)
(print-matrix test-set)

(defn als-iteration [R V n test-set lambda]
  (loop [i 0
         mat-V V
         res-U []
         res-V []
         rmse-val Integer/MAX_VALUE]
    (if (>= i n)
      (do
        (println "Max iterations reached")
        (println "Data: ")
        {:U res-U :V res-V :rmse rmse-val})
      (let [temp-U (merge-into-matrix (fix-V-solve-U R mat-V lambda))
            temp-V (merge-into-matrix (fix-U-solve-V R temp-U lambda))
            pred-R (multiply-matrices temp-U (transpose temp-V))
            R-diff (get-rmse-mat pred-R test-set)
            rmse (rmse R-diff)]
        (println "Error:", rmse)
        (if (< rmse rmse-val)
          (recur (inc i) temp-V temp-U temp-V rmse)
          (recur (inc i) temp-V res-U res-V rmse-val))))))
;; (als-iteration train-set my-V 100 test-set lambda)

(defn als [train-set test-set factors l]
  (apply min-key :rmse
         (for [latent-factor factors
               param l]
           (let [V (initialize-feature-matrix (count (first train-set)) latent-factor)]
             (als-iteration train-set V 100 test-set param)))))

(def l [1 0.1 0.01 0.001 0.0001])
(def factors (reverse (drop 1 (range (count @dataset/movies)))))
(def results (als train-set test-set factors l))
(apply min-key :rmse results)

results


(defn content-based-filtering [n]
  (let [movies-json (json/generate-string @dataset/movies)
        users-json (json/generate-string (vals @dataset/users))
        ratings-json (json/generate-string @dataset/ratings)
        top-n (str n)
        result (sh "./movie-venv/bin/python3" "src/movie_recommendation/similarity.py" movies-json users-json ratings-json top-n)]
    (json/parse-string (:out result) true)))
(def predictions (content-based-filtering 3))
predictions

(defn rem-not-pred-users [m idxs]
  (let [n (m/row-count m)
        left-idx (remove (set idxs) (range n))]
    (m/select m left-idx :all)))

(def my-R (rem-not-pred-users C (:indexes prep-data)))
(print-matrix (multiply-matrices (:U results) (transpose (:V results))))
(def R-pred (multiply-matrices (:U results) (transpose (:V results))))



