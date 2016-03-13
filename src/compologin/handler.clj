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
      fb-req-params {:client_id (get (System/getenv) "APP_ID")
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
               access-token-params {:client_secret (get (System/getenv) "APP_SECRET")
                                    :code code}
               access-token-url (str "https://graph.facebook.com/v2.3/oauth/access_token?"
                                     (form-encode (merge fb-req-params access-token-params)))
               temp-token (json/read-str (get (client/get access-token-url
                                                          {:accept :json
                                                           :throw-entire-message? true})
                                              :body))]
           (println "Login with scopes:" granted-scopes "OAuth code:" code "Access token:" temp-token)
           (str "Hi, " (get temp-token "access_token"))))
    (route/not-found "Not Found")))

(def app
  (wrap-defaults app-routes site-defaults))
