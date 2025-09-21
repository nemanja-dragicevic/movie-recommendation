(ns movie-recommendation.endpoints
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [config.core :refer [env]]
            [cheshire.core :as json]
            [clojure.string :as string]
            [movie-recommendation.dataset :refer [users, ratings, movies]]
            [movie-recommendation.core :as main-fn]))

(def secret-key (:secret-key env))

(defn unauthorized []
  {:status 401
   :headers {"Content-Type" "application/json"}
   :body {:error "Unauthorized"}})

(defn forbidden []
  {:status 403
   :headers {"Content-Type" "application/json"}
   :body {:error "Cannot access other users’ data"}})

(defn bad-request [msg]
  {:status 404
   :headers {"Content-Type" "application/json"}
   :body msg})

(defn jwt-auth [handler]
  (fn [request]
    (let [auth-header (get-in request [:headers "authorization"])]
      (if (and auth-header (string/starts-with? auth-header "Bearer "))
        (let [token (subs auth-header 7)]
          (try
            (let [claims (jwt/unsign token secret-key)]
              (handler (assoc request :identity claims)))
            (catch Exception _
              (unauthorized))))
        (unauthorized)))))

(defn get-users []
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/generate-string @users)})

(defn register [request]
  (let [{:keys [firstname lastname username password]} (:body request)]
    (cond
      (string/blank? username)
      {:status 400
       :body {:error "Username is required"}}

      (string/blank? password)
      {:status 400
       :body {:error "Password is required"}}

      (contains? @users username)
      {:status 400
       :body {:error "Username is already taken"}}

      :else
      (let [hashed-pwd (hashers/derive password)]
        (swap! users assoc username {:first-name firstname
                                     :last-name lastname
                                     :username username
                                     :password hashed-pwd})
        {:status 200
         :body {:message "User registered successfully"}}))))

(defn login [request]
  (let [{:keys [username password]} (:body request)
        user (get @users username)]
    (cond
      (nil? user)
      {:status 401 :body {:error "Invalid username or password"}}

      (not (hashers/verify password (:password user)))
      {:status 401 :body {:error "Invalid username or password"}}

      :else
      (let [claims {:user username
                    :id (:id user)
                    :expire (-> 1
                                (* 24 60 60)
                                (+ (quot (System/currentTimeMillis) 1000)))}
            token (jwt/sign claims secret-key)]
        {:status 200
         :body {:token token}}))))

(defn get-movie-by-id [id]
  (let [movie (filter #(= (:id %) id) @movies)]
    (if (empty? movie)
      (bad-request (str "There is not movie with id: " id))
      {:status 200
       :body movie})))

(defn get-user-watched [id]
  (let [rated-movies (->> @ratings
                          (filter #(= (:user-id %) id))
                          (map :movie-id)
                          set)]
    {:status 200
     :body (map :title (filter #(rated-movies (:id %)) @movies))}))

(defn add-rating [id req]
  (let [{:keys [movie-id rating]} (:body req)]
    (println id, movie-id, rating)
    (if (some #(= (:id %) movie-id) @movies)
      (do
        (swap! ratings conj {:user-id id
                             :movie-id movie-id
                             :rating rating})
        {:status 200
         :body "Successfully added rating"})
      (bad-request (str "Movie with id ", movie-id, " doesn't exist")))))

(defn restrict-to-user [id req fun]
  (let [user-id (get-in req [:identity :id])]
    (if (= id user-id)
      (try
        (fun id req)
        (catch clojure.lang.ArityException _
          (fun id)))
      (forbidden))))

(defroutes public-routes
  (POST "/register" req (register req))
  (POST "/login" req (login req))
  (GET "/api/movie/:id" [id]
    (get-movie-by-id (Integer/parseInt id))))

(defroutes protected-routes
  (GET "/users" [] (get-users))
  (GET "/api/watched/:id" [id :as req]
    (restrict-to-user (Integer/parseInt id) req get-user-watched))
  (POST "/api/rating/:id" [id :as req]
    (restrict-to-user (Integer/parseInt id) req add-rating)))

(defroutes app-routes
  public-routes
  (jwt-auth protected-routes)
  (route/not-found {:status 404
                    :body {:error "Page not found"}}))

(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (wrap-defaults api-defaults)))

(defn -main [& args]
  (jetty/run-jetty app {:port 3000 :join? false}))

