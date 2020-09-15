(ns ixio.routes
  (:require [ixio.core :as ixio]
            [ixio.db :as db]
            [ixio.views :as views]
            [hiccup.middleware :as mw]
            [compojure.core :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(http/defroutes main-routes
  (http/GET "/" [] (db/get-all-pastes)#_(views/index-page))
  (http/POST "/" req  
    (db/create-paste (:params req))) 
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (mw/wrap-base-url)))
