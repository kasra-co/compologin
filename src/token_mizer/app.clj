(ns token-mizer.app
  (:require [token-mizer.fb :as fb]
            [token-mizer.views.views :refer [home-page, success-page]]
            [token-mizer.controller :as controller]
            [cemerick.url :refer (url url-encode)]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :refer [join]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.page :as page]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]))

(let [users (atom {})
      client-credentials (fn [] {:client-id (System/getenv "APP_ID")
                                 :client-secret (System/getenv "APP_SECRET")})
      fb-graph-api "https://graph.facebook.com/v2.5"
      app-token (fb/request-app-token (client-credentials))
      request-access-token (partial fb/request-access-token "http://localhost:3000/fb-auth" (client-credentials))
      request-long-token (partial fb/request-long-token (client-credentials))
      request-token-info (partial (fb/with-app-token (client-credentials) fb/request-token-info) app-token)]

  (defroutes app-routes
    (GET "/" [] (home-page))
    (GET "/fb-auth" req
         (let [oauth-code (get (:params req) :code)
               granted-scopes (get (:params req) :granted_scopes)
               access-token (request-access-token oauth-code)
               token-info (request-token-info access-token)
               user-id (get-in token-info ["data" "user_id"])
               long-token (request-long-token access-token)]
           (println user-id "Login with scopes:" granted-scopes)
           (swap! users merge {user-id (merge (get users user-id {}) {:access-token long-token})})
           (controller/submit-token long-token)
           (redirect "/success")))
    (GET "/success" req
         (success-page))
    (route/not-found "Not Found")))

(def app
  (wrap-defaults app-routes site-defaults))
