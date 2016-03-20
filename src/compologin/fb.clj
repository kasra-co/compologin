(ns compologin.fb
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.core.memoize :refer [memo memo-clear!]]
            [ring.util.codec :refer [form-encode]]))

(let [fb-graph-api "https://graph.facebook.com/v2.5"]

  (defn request-app-token
    "Request an app token from FB. Useful for app-global actions, such as creating test users."
    [client-credentials]
    (-> {"client_id" (:client-id client-credentials)
          "client_secret" (:client-secret client-credentials)
          "grant_type" "client_credentials"}
         (form-encode)
         ((partial str fb-graph-api "/oauth/access_token?"))
         (client/get {:throw-entire-message? true})
         :body
         (json/read-str)
         (get "access_token")))

  (def reuse-app-token (memo request-app-token))

  (defn with-app-token [client-credentials f]
    (try (f (reuse-app-token client-credentials))
         (catch Exception e ; I know I should be more specific here, I only want to catch HTTP 400 responses
           (memo-clear! reuse-app-token client-credentials)
           (partial f (reuse-app-token client-credentials)))))

  (defn request-access-token
    "Request an access token for an OAuth2 code. Useful for user-specific actions. `oauth-callback-url` must match the callback URL that was used to receive the OAuth2 code."
    [oauth-callback-url client-credentials oauth-code]
    (-> {"client_id" (:client-id client-credentials)
         "client_secret" (:client-secret client-credentials)
         "redirect_uri" oauth-callback-url
         :code oauth-code}
        (form-encode)
        ((partial str fb-graph-api "/oauth/access_token?"))
        (client/get)
        :body
        (json/read-str)
        (get "access_token")))

  (defn request-long-token
    "Exchange a short term access token for a long term access token."
    [client-credentials access-token]
    (-> {"client_id" (:client-id client-credentials)
         "client_secret" (:client-secret client-credentials)
         "grant_type" "fb_exchange_token"
         "fb_exchange_token" access-token}
        (form-encode)
        ((partial str fb-graph-api "/oauth/access_token?"))
        (client/get {:throw-entire-message? true})
        :body
        (json/read-str)
        (get "access_token")))

  (defn request-token-info
    "Fetch basic information about any kind of graph API token, and its context."
    [client-credentials client-token subject-token]
    (-> {"input_token" subject-token
         "access_token" client-token}
        (form-encode)
        ((partial str "https://graph.facebook.com/debug_token?"))
        (client/get {:throw-entire-message? true})
        :body
        (json/read-str))))
