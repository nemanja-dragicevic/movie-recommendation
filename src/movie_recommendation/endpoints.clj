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
            [movie-recommendation.dataset :refer [users]]))

(def secret-key (:secret-key env))

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
                    :expire (-> 1
                                (* 24 60 60)
                                (+ (quot (System/currentTimeMillis) 1000)))}
            token (jwt/sign claims secret-key)]
        {:status 200
         :body {:token token}}))))

(defroutes app-routes
  (GET "/users" [] (get-users))
  (POST "/register" request-body (register request-body))
  (POST "/login" request-body (login request-body))
  (route/not-found {:status 404
                    :body {:error "Page not found"}}))

(def app
  (-> app-routes
      (wrap-json-response) 
      (wrap-json-body {:keywords? true})
      (wrap-defaults api-defaults)))

(defn -main [& args]
  (jetty/run-jetty app {:port 3000 :join? false}))

