(ns token-mizer.controller
    (:require [org.httpkit.client :as http]))

(defn submit-token
    "Submit a newly received token to the target recipient"
    [token]
    (if (and (System/getenv "TOKEN_RECIPIENT") (System/getenv "TOKEN_RECIPIENT_KEY"))
        @(http/put (System/getenv "TOKEN_RECIPIENT")
                   {:content-type :json
                    :form-params {"token" token
                                  "key" (System/getenv "TOKEN_RECIPIENT_KEY")}})
        (println "Not submitting token: TOKEN_RECIPIENT and TOKEN_RECIPIENT_KEY are not configured")))
