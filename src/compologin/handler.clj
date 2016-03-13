(ns compologin.handler
  (:require [cemerick.url :refer (url url-encode)]
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
      fb-graph-api "https://graph.facebook.com/v2.3"
      app-token (get (json/read-str (:body (client/get (str fb-graph-api "/oauth/access_token?"
                                                            (form-encode {:client_id client-id
                                                                          :client_secret client-secret
                                                                          :grant_type "client_credentials"})))))
                     "access_token")
      fb-req-params {:client_id client-id
                     :redirect_uri "http://localhost:3000/fb-auth"}
      fb-oauth-params {:scope (join "," ['email 'public_profile])
                       :response_type "code granted_scopes"}
      fb-login-url (str "https://www.facebook.com/dialog/oauth?"
                        (form-encode (merge fb-req-params fb-oauth-params)))
      landing-page (page/html5 [:head [:title "FB login demo"]]
                               [:body
                                [:p "Haro"]
                                [:a {:href fb-login-url} "Log in"]])]
  (defroutes app-routes
    (GET "/" [] landing-page)
    (GET "/fb-auth" req
         (let [code (get (:params req) :code)
               granted-scopes (get (:params req) :granted_scopes)
               access-token-params {:client_secret client-secret
                                    :code code}
               access-token-url (str fb-graph-api "/oauth/access_token?"
                                     (form-encode (merge fb-req-params access-token-params)))
               access-token (get (json/read-str (get (client/get access-token-url
                                                          {:accept :json
                                                           :throw-entire-message? true})
                                              :body))
                                 "access_token")
               token-info (json/read-str (get (client/get (str "https://graph.facebook.com/debug_token?"
                                                          (form-encode {:input_token access-token
                                                                        :access_token app-token})))
                                              :body))]
           (println "Login with scopes:" granted-scopes "OAuth code:" code "Access token:" access-token)
           (str "Hi, " token-info)))
    (route/not-found "Not Found")))

(def app
  (wrap-defaults app-routes site-defaults))
