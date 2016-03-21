(ns token-mizer.views.views
    (:require [clojure.string :refer [join]]
              [hiccup.page :as page]
              [ring.util.codec :refer [form-encode]]))

(letfn [(site-nav []
            [:p
             [:a {:href "/"} "Home"]])]

    (defn page-layout [title & body]
        (page/html5 [:head [:title (str (System/getenv "APP_TITLE") title)]]
                    [:body
                     (site-nav)
                     body]))

    (defn home-page []
        (page-layout "Home"
                     [:p "Fetch access token"]
                     [:a {:href (->> {"client_id" (System/getenv "APP_ID")
                                      "redirect_uri" "http://localhost:3000/fb-auth"
                                      "response_type" "code granted_scopes"
                                      "scope" (System/getenv "APP_SCOPE")}
                                     (form-encode)
                                     (str "https://www.facebook.com/dialog/oauth?"))}
                      "Acquire token"]))

    (defn success-page []
        (page-layout "Success"
                     [:p "An access token has been successfully retrieved, thank you."])))
