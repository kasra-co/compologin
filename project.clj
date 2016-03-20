(defproject compologin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [com.cemerick/url "0.1.1"]
                 [com.cemerick/piggieback "0.2.1"]
                 [clj-http "2.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.memoize "0.5.8"]]
  :plugins [[lein-ring "0.9.7"]
            [com.jakemccrary/lein-test-refresh "0.14.0"]
            [lein-cloverage "1.0.6"]]
  :ring {:handler compologin.demo/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
