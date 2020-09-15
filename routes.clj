(ns ixio.routes
  (:require [ixio.core :as ixio]
            [ixio.views :as views]
            [hiccup.middleware :as mw]
            [compojure.core :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(http/defroutes main-routes
  (http/GET "/" [] (views/index-page))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (mw/wrap-base-url)))
