(ns token-mizer.controller-test
    (:require [clojure.test :refer :all]
              [org.httpkit.fake :refer [with-fake-http]]
              [token-mizer.controller :as controller]))

(deftest test-controller
    (testing "submit-token"
        (with-fake-http [(System/getenv "TOKEN_RECIPIENT") {:status 200}]
            (is ((complement nil?) (controller/submit-token "some-token"))))))
