(ns compologin.handler
  (:require [compologin.fb :as fb]
            [cemerick.url :refer (url url-encode)]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :refer [join]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.page :as page]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.codec :refer [form-encode]]))

(let [users (atom {})
      client-id (get (System/getenv) "APP_ID")
      client-secret (get (System/getenv) "APP_SECRET")
      client-credentials {:client-id client-id
                          :client-secret client-secret}
      fb-graph-api "https://graph.facebook.com/v2.5"
      app-token (fb/request-app-token client-credentials)
      fb-req-params {:client_id client-id
                     :redirect_uri "http://localhost:3000/fb-auth"}
      fb-oauth-params {:scope (join "," ['email 'public_profile])
                       :response_type "code granted_scopes"}
      fb-login-url (str "https://www.facebook.com/dialog/oauth?"
                        (form-encode (merge fb-req-params fb-oauth-params)))
      request-access-token (partial fb/request-access-token "http://localhost:3000/fb-auth" client-credentials)
      request-long-token (partial fb/request-long-token client-credentials)
      landing-page (page/html5 [:head [:title "FB login demo"]]
                               [:body
                                [:p "Haro"]
                                [:a {:href fb-login-url} "Log in"]])]
  (defroutes app-routes
    (GET "/" [] landing-page)
    (GET "/fb-auth" req
         (let [oauth-code (get (:params req) :code)
               granted-scopes (get (:params req) :granted_scopes)
               access-token (request-access-token oauth-code)
               token-info (json/read-str (get (client/get (str "https://graph.facebook.com/debug_token?"
                                                          (form-encode {:input_token access-token
                                                                        :access_token app-token})))
                                              :body))
               user-id (get-in token-info ["data" "user_id"])
               client-credentials {:client-id client-id
                                   :client-secret client-secret}
               long-token (request-long-token access-token)]
           (println user-id "Login with scopes:" granted-scopes "OAuth code:" oauth-code "Access token:" access-token)
           (swap! users merge {user-id (merge (get users user-id {}) {:access-token long-token})})
           (println "User:" (get (deref users) user-id))
           (println "Long:" long-token)
           (str "Hi, " token-info)))
    (route/not-found "Not Found")))

(def app
  (wrap-defaults app-routes site-defaults))
