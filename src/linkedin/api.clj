(ns linkedin.api
  (:require [linkedin.core :as lc]
            [ring.util.codec :as ruc])
  )

(def field-selector "(id,headline,first-name,last-name,picture-url)")

(defn get-connections [access-token]
  (lc/get-url (str
                "http://api.linkedin.com/v1/people/~/connections:"
                field-selector) access-token))

(defn search-people [access-token keyword]
  (lc/repeated (str "http://api.linkedin.com/v1/people-search:(people:" field-selector ")?keywords=" keyword)
            access-token))

(defn search-companies [access-token name]
  (let [fields "id,name,industries,employee-count-range,locations:(address:(city,country-code))"
        name (ruc/url-encode name)]
    (lc/repeated (str "http://api.linkedin.com/v1/company-search:(companies:(" fields
                      "))?keywords={" name "}") access-token :max-repeats 1)))
