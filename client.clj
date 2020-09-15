(ns ixio.client
  (:require [clojure.edn :as edn]))
(def contents (slurp url))
(let [content (edn/read-string contents)]
  (str
    (:body content)) 
  (str url (:id content)))
