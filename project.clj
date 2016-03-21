(defproject token-mizer "0.1.0"
  :description "Ask a user for a long lived FB token, and hand it off to another service"
  :url "https://github.com/kasra-co/token-mizer"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [com.cemerick/url "0.1.1"]
                 [com.cemerick/piggieback "0.2.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.memoize "0.5.8"]
                 [cheshire "5.5.0"]
                 [http-kit "2.2.0-alpha1"]
                 [http-kit.fake "0.2.2"]
                 [clj-http "2.1.0"]
                 [com.hendrick/hendrick-ring-util "0.1.2"]]
  :plugins [[lein-ring "0.9.7"]
            [com.jakemccrary/lein-test-refresh "0.14.0"]
            [lein-cloverage "1.0.6"]]
  :ring {:handler token-mizer.app/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
