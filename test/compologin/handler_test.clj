(ns compologin.handler-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [ring.mock.request :as mock]
            [ring.util.codec :refer [form-encode]]
            [compologin.handler :refer :all]))

(let [fb-graph-api "https://graph.facebook.com/v2.5"
      client-id (get (System/getenv) "APP_ID")
      client-secret (get (System/getenv) "APP_SECRET")
      app-token (atom nil)
      ghost (atom nil)]

  (letfn [(get-app-token ; Generate a Facebook application token for this app
            []
            (or
              (deref app-token)
              (->> {:client_id client-id
                    :client_secret client-secret
                    :grant_type "client_credentials"}
                   (form-encode)
                   (str fb-graph-api "/oauth/access_token?")
                   (#(client/get % {:throw-entire-message? true}))
                   :body
                   (json/read-str)
                   (#(get % "access_token"))
                   (reset! app-token ))))

          (spawn-ghost ; Create an authenticated test user. Requires an app token that has been generated for a test app; this will not work with a live app.
            [app-token]
            (or
              (deref ghost)
              (->> {"installed" "true"
                    "permissions" "email"
                    "access_token" app-token}
                   (form-encode)
                   (assoc {:throw-entire-message? true} :body)
                   (client/post (str fb-graph-api "/" client-id "/accounts/test-users"))
                   :body
                   (json/read-str)
                   (reset! ghost))))

          (release-ghost ; Delete the test user
            [app-token]
            (when ((complement nil?) (deref ghost))
              (->> {:access_token app-token}
                   (form-encode)
                   (assoc {:throw-entire-message? true} :body)
                   (client/delete (str fb-graph-api "/" (get (deref ghost) "id"))))
              (reset! ghost nil)))]

    (deftest test-app
      (testing "main route"
        (let [response (app (mock/request :get "/"))]
          (is (= (:status response) 200))))

      (testing "not-found route"
        (let [response (app (mock/request :get "/invalid"))]
          (is (= (:status response) 404)))))

    (deftest test-facebook-integration
      (testing "get-app-token gets a good looking token"
        (is (->> (get-app-token) (re-find #"^\d+\|\w+-\w+$") ((complement nil?)))))

      (testing "spawn-ghost and destroy-ghost creates and destroys a test user"
        (is (->> (get-app-token) (spawn-ghost) ((complement nil?))))
        (is (->> (get-app-token) (release-ghost) (nil?)))
        (is (nil? (deref ghost)))))))
