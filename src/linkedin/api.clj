(ns linkedin.api
  (:require [linkedin.core :as lc])
  )

(def field-selector "(id,headline,first-name,last-name,picture-url)")

(defn get-connections [access-token]
  (lc/get-url (str
                "http://api.linkedin.com/v1/people/~/connections:"
                field-selector) access-token))

(defn repeated [base-url access-token]
  (let [incr 25]
  (loop [start 0 cnt incr res []]
    (let [url (str base-url "&start=" start "&count=" cnt)
          sr (lc/get-url url access-token)
          data (second (first sr))
          {:keys [_total values]} data
          res (concat res values)]
      (if (>= (+ start cnt) _total)
        res
        (recur (+ start incr) cnt res))))))

(defn search-people [access-token keyword]
  (repeated (str "http://api.linkedin.com/v1/people-search:(people:" field-selector ")?keywords=" keyword)
            access-token))
