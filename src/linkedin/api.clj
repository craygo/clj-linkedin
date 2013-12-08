(ns linkedin.api
  (:require [linkedin.core :as lc]
            [ring.util.codec :as ruc]
            [clojure.tools.logging :refer [debug info]])
  )

(def field-selector "(id,headline,first-name,last-name,picture-url)")

(defn get-connections [access-token]
  (lc/get-url (str
                "http://api.linkedin.com/v1/people/~/connections:"
                field-selector) access-token))

(defn search-people [access-token keyword]
  (lc/repeated (str "http://api.linkedin.com/v1/people-search:(people:" field-selector ")?keywords=" keyword)
            access-token))

(def company-fields "id,name,industries,employee-count-range,logo-url,square-logo-url,locations:(address:(city,country-code))")

(defn search-companies [access-token name]
  (let [name (ruc/url-encode name)]
    (lc/repeated (str "http://api.linkedin.com/v1/company-search:(companies:(" company-fields
                      "))?keywords={" name "}") access-token :max-repeats 1)))

(defn get-company [access-token id]
  (lc/get-url (str "http://api.linkedin.com/v1/companies/" id ":(" company-fields ")") access-token))

(defn get-current-user [access-token]
  (lc/get-url (str "http://api.linkedin.com/v1/people/~:" field-selector) access-token))
