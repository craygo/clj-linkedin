(ns linkedin.routes
  (:require [compojure.core :refer [defroutes GET ANY]]
            [linkedin.core :refer [get-request-token get-auth-url get-access-token]]
            [noir.session :as sess]
            [noir.response :refer [redirect]]
            [clojure.tools.logging :refer [debug info]]
  ))

(defn init-linkedin-routes [{:keys [app-success-url app-failed-url linkedin-auth-url linkedin-auth-callback-url ]
                             :as m}]
  (debug "init-linkedin-routes m " m)
  (defroutes linkedin-routes
    (GET linkedin-auth-url [] 
         (let [rt (get-request-token)
               url (get-auth-url rt)]
           (sess/put! :linkedin-rt rt)
           (redirect url)))
    (ANY linkedin-auth-callback-url req 
         (let [rt (sess/get! :linkedin-rt)]
           (info linkedin-auth-callback-url " rt " rt)
           (if-let [linkedin-access-token (get-access-token req rt)]
             (do
               (sess/put! :linkedin-access-token linkedin-access-token)
               (redirect app-success-url))
             (redirect app-failed-url))))))


