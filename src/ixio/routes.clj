(ns ixio.routes
  (:require [ixio.core :as ixio]
            [ixio.db :as db]
            [ixio.views :as views]
            [hiccup.middleware :as mw]
            [ring.middleware.session :as session]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.response :refer [redirect]]
            [compojure.core :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defn is-logged-in? [] true)

(http/defroutes main-routes
  (http/GET "/" [] (views/index-page))  
  (http/GET "/pastes" []    
    (views/pastes-page))
  (http/GET "/paste/:id" id
    (views/individual-paste (:id (:params id))))  
  (http/POST "/paste" req      
    (if (empty? (:body (:params req)))
      (views/index-page)
      (str ixio/url "paste/"
        ((keyword "last_insert_rowid()")
         (first (db/create-paste (:form-params req)))) "\n")))
  (http/GET "/favicon.ico" []
    "Hello World") 
  (http/GET "/signup" []
    (views/create-account-page))
  (http/POST "/signup" req
    (if (empty? (:params req))
      (views/new-account-page req)
      (do
        (let [ins (db/create-user (:params req))
              id (db/get-last-user)]      
          (redirect (str ixio/url "user"))))))
  (http/GET "/login" []
    (views/login-page))
  (http/POST "/login" req
    req
    (if (= (:pwordhash (:params req))
          (:pwordhash (first (db/get-user-by-username
                               (:username (:params req))))))
      (str  req)
      (str  req)
      #_(views/logged-in-successful)
      #_(views/logged-in-unsuccessful)))
  (http/GET "/user" []
    (if (is-logged-in?)
      (do
        (views/current-user-page (ixio.sessions/current-user)))
      (redirect "/login")))
  (http/GET "/what" req
    (str req))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (->
    (handler/site main-routes)
    (wrap-cookies)
    (wrap-reload)
    (wrap-session)
    (mw/wrap-base-url)))
