(ns token-mizer.fb-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [ring.mock.request :as mock]
            [ring.util.codec :refer [form-encode]]
            [token-mizer.fb :as fb]))

(let [fb-graph-api "https://graph.facebook.com/v2.5"
      client-credentials (fn [] {:client-id (System/getenv "APP_ID")
                                 :client-secret (System/getenv "APP_SECRET")})
      ghost (atom nil)
      app-token (atom nil)]

  (letfn [(get-app-token ; Generate a Facebook application token for this app
            []
            (or
              (deref app-token)
              (reset! app-token (fb/request-app-token (client-credentials)))))

          (spawn-ghost ; Create an authenticated test user. Requires an app token that has been generated for a test app; this will not work with a live app.
            [app-token]
            (or
              (deref ghost)
              (->> {"installed" "true"
                    "permissions" "email"
                    "access_token" app-token}
                   (form-encode)
                   (assoc {:throw-entire-message? true} :body)
                   (client/post (str fb-graph-api "/" (System/getenv "APP_ID") "/accounts/test-users"))
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

    (deftest test-facebook-integration
      (testing "get-app-token gets a good looking token"
        (is (->> (get-app-token) (re-find #"^\d+\|\w+-\w+$") ((complement nil?)))))

      (testing "with-app-token can be called twice without failing"
        (is (string? (fb/with-app-token (client-credentials) identity)))
        (is (string? (fb/with-app-token (client-credentials) identity))))

      (testing "spawn-ghost and destroy-ghost creates and destroys a test user"
        (is (->> (get-app-token) (spawn-ghost) ((complement nil?))))
        (is (->> (get-app-token) (release-ghost) (nil?)))
        (is (nil? (deref ghost))))

      (testing "inspect a token"
        (is (->> (get-app-token) (spawn-ghost) ((complement nil?))))
        (is (-> (get (deref ghost) "access_token")
                ((partial fb/request-token-info (client-credentials) (get-app-token)))
                 ((complement nil?))))
        (is (->> (get-app-token) (release-ghost) (nil?))))

      (testing "exchange a ghost's token for a long lived token"
        (is (->> (get-app-token) (spawn-ghost) ((complement nil?))))
        (is (-> ghost
                (deref)
                (get "access_token")
                ((partial fb/request-long-token (client-credentials)))
                ((complement nil?))))
        (is (->> (get-app-token) (release-ghost) (nil?)))))))
