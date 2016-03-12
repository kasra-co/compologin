(ns compologin.handler
  (:require [cemerick.url :refer (url url-encode)]
            [clojure.string :refer [join]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.page :as page]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.codec :refer [form-encode]]))

(let [params {:client_id (get (System/getenv) "APP_ID")
              :redirect_uri "http://localhost:3000/fb-login"
              :scope (join "," ['email 'public_profile])
              :response_type "code token granted_scopes"}
      fb-login-url (str "https://www.facebook.com/dialog/oauth?" (form-encode params))
      users (atom {})]
  (defroutes app-routes
    (GET "/" [] (page/html5 [:head [:title "FB login demo"]]
                            [:body
                             [:p "Haro"]
                             [:a {:href fb-login-url} "Log in"]]))
    (GET "/fb-login" [] (fn [req] "Hi!"))
    (route/not-found "Not Found")))

(def app
  (wrap-defaults app-routes site-defaults))
