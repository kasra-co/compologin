(ns token-mizer.handler-test
    (:require [clojure.test :refer :all]
              [clj-http.client :as client]
              [ring.mock.request :as mock]
              [token-mizer.app :refer :all]))

(deftest test-app
    (testing "main route"
        (let [response (app (mock/request :get "/"))]
            (is (= (:status response) 200))))

    (testing "not-found route"
        (let [response (app (mock/request :get "/invalid"))]
            (is (= (:status response) 404)))))
