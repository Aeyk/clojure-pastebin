(ns ixio.views
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(defn index-page []
  (page/html5
    [:head
      [:title "Hello World"]
      (page/include-css "/css/style.css")]
    [:body
      [:h1 "Hello World"]]))
