(ns linkedin.routes
  (:require [compojure.core :refer [defroutes GET ANY]]
            [linkedin.core :refer [get-request-token get-auth-url get-access-token init-service]]
            [noir.session :as sess]
            [noir.response :refer [redirect]]
            [clojure.tools.logging :refer [info]]
  ))

;(def success-url )
;(def failed-url )
;(def auth-callback-url )
;(def auth-url )

(defn init-linkedin-routes [{:keys [app-success-url app-failed-url linkedin-auth-url linkedin-auth-callback-url ]
                             :as m}]
  (info "init-linkedin-routes m " m)
;  (def success-url app-success-url)
;  (def failed-url app-failed-url)
;  (def auth-callback-url linkedin-auth-callback-url)
;  (def auth-url linkedin-auth-url)
  (defroutes linkedin-routes
    (GET linkedin-auth-url [] (let [rt (get-request-token)
                           url (get-auth-url rt)]
                       (sess/put! :linkedin-rt rt)
                       (redirect url)))
    (ANY linkedin-auth-callback-url req (let [rt (sess/get! :linkedin-rt)]
                                 (info linkedin-auth-callback-url " rt " rt)
                                 (if-let [linkedin-access-token (get-access-token req rt)]
                                   (do
                                     (sess/put! :linkedin-access-token linkedin-access-token)
                                     (redirect app-success-url))
                                   (do
                                     (redirect app-failed-url))))))
  )

(defn init-linkedin-service [{:keys [linkedin-key linkedin-secret 
                                     base-url linkedin-auth-callback-url scope] :as m}]
  (info "init-linkedin-service m " m)
  (init-service linkedin-key linkedin-secret (str base-url linkedin-auth-callback-url) :scope scope))

