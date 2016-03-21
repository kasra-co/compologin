(ns token-mizer.views-test
    (:require [clojure.test :refer :all]
              [clojure.string :refer [includes?]]
              [token-mizer.views.views :refer [page-layout home-page]]))

(deftest test-views
    (testing "page layout"
        (is (includes? (page-layout "test-title" "body") "<!DOCTYPE html>"))
        (is (includes? (page-layout "test-title" "body") "test-title")))

    (testing "home page"
        (is (includes? (home-page) "<p>"))
        (is (includes? (home-page) "href="))))
