(ns ixio.client
  (:require [clojure.edn :as edn]))

(def url "http://localhost:3000/")
(def contents (slurp url))
(let [content (edn/read-string contents)]
  (str
    (:body content)) 
  (str url (:id content)))
