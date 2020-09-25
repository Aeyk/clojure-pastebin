(ns ixio.routes
  (:require [ixio.core :as ixio]
            [ixio.db :as db]
            [ixio.views :as views]
            [hiccup.middleware :as mw]
            [ring.middleware.session :as session]
            [compojure.core :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(http/defroutes main-routes
  (http/GET "/" [] (views/index-page)#_(db/get-all-pastes))
  (http/GET "/pastes" []    
    (hiccup.page/html5

      (for [p (db/get-all-pastes)]
        [:div [:p ][:pre (str (:id p) "\t" (:body p) "\n")]])))

  (http/GET "/paste/:id" id
    (views/individual-paste (:id (:params id))))
  
  (http/POST "/paste" req      
    (if (empty? (:body (:params req)))
      (views/index-page)
      (str ixio/url "paste/"
        ((keyword "last_insert_rowid()")
         (first (db/create-paste (:form-params req)))) "\n")      
      #_(do (db/create-paste (:form-params req))
          (str (into {}
                 (first (db/get-last-paste)))))
      #_(get 
          "body")
      
      #_(do
          (let [ins (db/create-paste
                      (into {}
                        (clojure.edn/read-string (:body (:params req)))))
                id (db/get-last-paste)]      
            (str ixio/url "/paste/"((keyword "last_insert_rowid()")
                                    (first ins)))))))
  (http/GET "/favicon.ico" []
    "Hello World") 
  (http/GET "/:id" [id]
    (views/individual-paste id)
    #_(db/get-pastes-by-id id))
  (http/GET "/user/:id" [id]
    (views/individual-user id)
    #_(db/get-pastes-by-id id))
  (http/POST "/user/" req
    #_(str (:params req))
    #_(views/new-account-page req)
    (if (empty? (:params req))
      (views/new-account-page req)
      (do
        (let [ins (db/create-user (:params req))
              id (db/get-last-user)]      
          (str ixio/url "user/"(:id (first id)) "\n"
            #_req)))))
  (http/POST "/login/" req
    req
    (if (= (:pwordhash (:params req))
          (:pwordhash (first (db/get-user-by-username
                               (:username (:params req))))))
      (views/logged-in-successful)
      (views/logged-in-unsuccessful)))
  
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (mw/wrap-base-url)))
