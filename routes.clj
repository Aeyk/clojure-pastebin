(ns ixio.routes
  (:require [ixio.core :as ixio]
            [ixio.db :as db]
            [ixio.views :as views]
            [hiccup.middleware :as mw]
            [compojure.core :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))
(def url "http://localhost:3000/")

(http/defroutes main-routes
  (http/GET "/" [] (views/index-page)#_(db/get-all-pastes))
  (http/POST "/" req  
    (db/create-paste (:params req))
    (println req))
  (http/GET "/favicon.ico" []
    "Hello World") 
  (http/GET "/:id" [id]
    (println id)
    (db/get-pastes-by-id id))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (mw/wrap-base-url)))
