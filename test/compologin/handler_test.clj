(ns compologin.handler-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [ring.mock.request :as mock]
            [ring.util.codec :refer [form-encode]]
            [compologin.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "login"
    (let [client-id (get (System/getenv) "APP_ID")
          client-secret (get (System/getenv) "APP_SECRET")
          fb-graph-api "https://graph.facebook.com/v2.5"
          app-token (-> (->> {:client_id client-id
                              :client_secret client-secret
                              :grant_type "client_credentials"}
                             (form-encode)
                             (str fb-graph-api "/oauth/access_token?")
                             (client/get))
                        :body
                        (json/read-str)
                        (get "access_token"))
          user-res (->> {"installed" "true"
                        "permissions" "email"
                        "access_token" app-token}
                        (form-encode)
                        (assoc {:throw-entire-message? true} :body)
                        (client/post (str fb-graph-api "/" client-id "/accounts/test-users")))
          ]
      (println "app-token" app-token)
      (println "user" user-res)
      (is true))))
