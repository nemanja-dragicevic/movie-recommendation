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

(defn transpose [matrix] 
  (if (empty? matrix)
    []
    (apply mapv vector matrix)))

(defn multiply-matrices [m1 m2]
  (let [m1-col (count (first m1))
        m2-row (count m2)]
    (if (and (= m1-col m2-row) (> m1-col 0))
      (m/mmul m1 m2)
      (str "Cannot multiply matrices with dimensions: "
           "[", (count m1) " " (count (first m1)), "]"
           "[" (count m2) " " (count (first m2)) "]"))))

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


(defn get-train-test [mat min-avg min-ratings]
  (let [rows (m/row-count mat)
        cols (m/column-count mat)]
    (loop [r 0
           train []
           test []
           indexes []]
      (if (= r rows)
        {:train (vec train)
         :test (vec test)
         :indexes indexes}
        (let [row (m/get-row mat r)
              ratings (keep-indexed (fn [i v] (when (> v 0) [i v])) row)
              num-ratings (count ratings)
              shuffled (shuffle ratings)

              k (dec (min (int (* 0.8 (count ratings)))
                          (dec (count ratings))))

              total (reduce + 0 (map second ratings))
              avg (double (/ total (count ratings)))]

          (if (or (< num-ratings min-ratings)
               (< avg min-avg))
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
     :test (drop-cols te rem-cols)
     :rem-cols rem-cols}))

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

(defn initialize-feature-matrix [rows cols]
  (vec (for [_ (range rows)]
         (vec (for [_ (range cols)]
                (rand 1))))))

(defn als-iteration [R V n test-set lambda]
  (loop [i 0
         mat-V V
         res-U []
         res-V []
         rmse-val Integer/MAX_VALUE]
    (if (>= i n)
      (do
        (println "Max iterations reached")
        (println "Data: ", {:U res-U :V res-V :rmse rmse-val})
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


(defn content-based-filtering []
  (let [movies-json (json/generate-string @dataset/movies)
        users-json (json/generate-string (vals @dataset/users))
        ratings-json (json/generate-string @dataset/ratings) 
        result (sh "./movie-venv/bin/python3" "src/movie_recommendation/similarity.py" movies-json users-json ratings-json)]
    (json/parse-string (:out result) true)))
;; (def predictions (content-based-filtering))
;; predictions

(defn clean-R [mat idxs cols]
  (let [n-rows (m/row-count mat)
        n-cols (m/column-count mat)
        left-rows (remove (set idxs) (range n-rows))
        left-cols (remove (set cols) (range n-cols))]
    (m/select mat left-rows left-cols)))

(defn mask-ratings [R R-pred]
  (let [mask (m/emap #(if (zero? %) 1 0) R)]
    (m/mul R-pred mask)))

(defn top-rated-movies [n watched-ids]
  (let [top-movie-ids (->> @dataset/ratings 
                           (remove #(some #{(:movie-id %)} watched-ids)) 
                           (group-by :movie-id) 
                           (map (fn [[movie-id rat]] 
                                  {:movie-id movie-id 
                                   :avg-rating (/ (reduce + (map :rating rat)) 
                                                  (count rat))})) 
                           (sort-by :avg-rating >) 
                           (take n) 
                           (mapv :movie-id))]
    (filter #(some #{(:id %)} top-movie-ids) @dataset/movies)))


(defn average [coll]
  (let [c (filter (fn [v] (not (zero? v))) coll)
        n (count c)
        total (reduce + c)]
    (float (/ total n))))

(defn normalize [mat]
  (mapv (fn [row]
          (mapv #(/ % 5.0) row))
        mat))

(defn find-index [idx removed]
  (let [n-rem (count (filter #(< % idx) removed))]
    (- idx n-rem)))

(defn insert-at-indexes [coll indexes val]
  (let [sorted-indexes (sort indexes)]
    (reduce (fn [c [offset idx]]
              (let [pos (+ idx offset)]
                (vec (concat (subvec c 0 pos)
                             [val]
                             (subvec c pos)))))
            (vec coll)
            (map-indexed vector sorted-indexes))))

(defn get-movie-recom-info [recs]
  (map (fn [{:keys [movie-id similarity]}]
         (let [movie (some #(when (= (:id %) movie-id) %) @dataset/movies)]
           (merge movie {:similarity (format "%.2f%%" (* similarity 100))})))
       recs))

(defn merge-scores [user-content user-colab alpha-cf alpha-cb]
  (->> user-content
       (map (fn [[idx val]]
              {:movie-id (inc idx)
               :similarity (+ (* alpha-cb val)
                              (* alpha-cf (nth user-colab idx 0.0)))}))
       (sort-by :similarity >)
       vec))

;; :indexes starts with 0
;; :rem-cols starts with 0
;; idx = user-id - 1
(defn recommend [idx colab prep-data content]
  (let [row-idx (find-index idx (:indexes prep-data))
        user-colab (m/get-row colab row-idx) ;; colab recommendations
        user-content (some #(when (= (:id %) (inc idx)) (:recs %)) content) ;; content recommendations
        ;; content-ids (map first user-content) ;; movie-idx iz contenta
        colab-avg (average user-colab)
        user-colab-filled (insert-at-indexes user-colab (:rem-cols prep-data) colab-avg)
        alpha-cf 0.7
        alpha-cb 0.3
        recommendations (merge-scores user-content user-colab-filled alpha-cf alpha-cb)]
    (get-movie-recom-info recommendations)))

(defn movie-recommendation [id]
  (let [l [1 0.1 0.01 0.001 0.0001]
        factors (reverse (drop 1 (range (count @dataset/movies))))
        R (fill-matrix! (zero-matrix (count @dataset/users) (count @dataset/movies))
                        dataset/ratings)
        data (get-train-test R 2.5 3)
        prep-data (assoc (clean-zero-cols (:train data) (:test data)) :indexes (:indexes data))
        train-set (:train prep-data)
        test-set (:test prep-data)
        results (als train-set test-set factors l)
        new-R (clean-R R (:indexes prep-data) (:rem-cols prep-data))
        R-pred (multiply-matrices (:U results) (transpose (:V results)))
        predictions (content-based-filtering)]
    (recommend id
               (normalize (mask-ratings new-R R-pred))
               prep-data
               (:result predictions))))

(defn get-recom-for-user [id min-ratings]
  (let [watched-ids (map :movie-id (filter #(= (:user-id %) id) @dataset/ratings))
        n-ratings (count watched-ids)
        u-ratings (map :rating (filter #(= (:user-id %) id) @dataset/ratings))
        avg-rating (float (/ (reduce + u-ratings) (count u-ratings)))]
    (if (or (< n-ratings min-ratings) (< avg-rating 2.6))
      (top-rated-movies 3 watched-ids)
      (movie-recommendation (dec id)))))
;; (get-recom-for-user 5 3)



