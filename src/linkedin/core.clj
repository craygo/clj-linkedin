(ns linkedin.core
  (:use [clojure.tools.logging]
        [cheshire.core :only [parse-string]])
  (import [org.scribe.oauth OAuthService]
          [org.scribe.builder ServiceBuilder ]
          [org.scribe.builder.api LinkedInApi]
          [org.scribe.model Verifier Token Verb OAuthRequest]
          ))

(defn- init-service [linkedin-key linkedin-secret callback-url & {:keys [scope]}]
  (def service 
    (let [service-builder (doto (ServiceBuilder.)
                            (.provider LinkedInApi)
                            (.apiKey linkedin-key)
                            (.apiSecret linkedin-secret)
                            (.callback callback-url))]
      (if scope
        (.scope service-builder scope))
      (.build service-builder)))
    (info "init-service service " (type service)))

(defn init-linkedin-service [{:keys [linkedin-key linkedin-secret 
                                     base-url linkedin-auth-callback-url scope] :as m}]
  (debug "init-linkedin-service m " m)
  (init-service linkedin-key linkedin-secret (str base-url linkedin-auth-callback-url) :scope scope))

(defn get-request-token []
  (let [rt (.getRequestToken service)]
    (info "get-request-token rt " (type rt) (bean rt))
    rt))

(defn get-auth-url [rt]
  (.getAuthorizationUrl service rt))

(defn get-access-token [req rt]
  (let [{:keys [oauth_token oauth_verifier]} (:params req)]
    (if (and oauth_token oauth_verifier)
      (.getAccessToken service rt (Verifier. oauth_verifier)))))

(defn make-access-token [user-token user-secret]
  (Token. user-token user-secret))

(defn get-url [url access-token]
  (let [request (OAuthRequest. Verb/GET url)]
    (.addHeader request "x-li-format" "json")
    (.signRequest service access-token request)
    (let [response (.send request)
          code (.getCode response)
          headers (.getHeaders response)
          body (.getBody response)]
      (debug "get-url code " code " headers " headers)
      (parse-string body true))))

(defn repeated [base-url access-token & {:keys [max-repeats]}]
  (let [incr 25]
    (loop [start 0 cnt incr res [] repeats 0]
      (let [url (str base-url "&start=" start "&count=" cnt)
            sr (get-url url access-token)
            data (second (first sr))
            {:keys [_total values]} data
            res (concat res values)]
        (if (or (>= (+ start cnt) _total)
                (>= repeats max-repeats))
          res
          (recur (+ start incr) cnt res (inc repeats)))))))
