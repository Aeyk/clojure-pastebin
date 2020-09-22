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
  (http/GET "/" [] (views/index-page)#_(db/get-all-pastes))
  (http/POST "/" req  
    (if (empty? (:body (:params req)))
      (views/index-page)
      (do
        (let [ins (db/create-paste req)
              id (db/get-last-paste)]      
          (str ixio/url(:id (first id)) "\n"
            #_req)))))
  (http/GET "/favicon.ico" []
    "Hello World") 
  (http/GET "/:id" [id]
    (views/individual-paste id)
    #_(db/get-pastes-by-id id))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (mw/wrap-base-url)))
