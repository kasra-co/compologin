(ns compologin.fb
  (:require [clj-http.client :as client]
             [clojure.data.json :as json]
             [ring.util.codec :refer [form-encode]]))

(let [fb-graph-api "https://graph.facebook.com/v2.5"]

  (defn request-long-token
    "Exchange a short term access token for a long term access token"
    [access-token client-credentials]
    (-> {"client_id" (:client-id client-credentials)
         "client_secret" (:client-secret client-credentials)
         "grant_type" "fb_exchange_token"
         "fb_exchange_token" access-token}
        (form-encode)
        (#(str fb-graph-api "/oauth/access_token?" %))
        (client/get)
        :body
        (json/read-str)
        (get "access_token"))))
