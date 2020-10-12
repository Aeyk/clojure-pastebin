(ns ixio.routes
  (:require [ixio.core :as ixio]
            [ixio.db :as db]
            [ixio.views :as views]
            [hiccup.page :as h]
            [hiccup.middleware :as mw]            

            [ring.middleware.session :as session]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.response :as resp]
            [ring.util.response :refer [redirect]]
            [compojure.core :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.string :as str]
            [cemerick.friend :as friend]
            (cemerick.friend
              [workflows :as workflows]
              [credentials :as creds])
            p[ring.adapter.jetty :refer [run-jetty]])
    (:import java.net.URI))

(defn is-logged-in? [] true)

;;(create-user  {:username "ddoo" :password "m" :role_id 0})
(defn resolve-uri
  [context uri]
  (let [context (if (instance? URI context) context (URI. context))]
    (.resolve context uri)))

(defn context-uri
  "Resolves a [uri] against the :context URI (if found) in the provided
   Ring request.  (Only useful in conjunction with compojure.core/context.)"
  [{:keys [context]} uri]
  (if-let [base (and context (str context "/"))]
    (str (resolve-uri base uri))
    uri))


(defn- create-user
  [{:keys [username password admin role_id] :as user-data}]    
  (let [user-auth-data (-> (dissoc user-data :admin)
                         (assoc :identity username
                           :password (creds/hash-bcrypt password)
                           :role_id 2
                           :roles (into #{::user} (when admin [::admin]))))]
    (let [user-data user-data]
      (db/create-user user-auth-data))
    user-auth-data))

;;(create-user (update (first (db/get-users)) :username (constantly "me") ))

(http/defroutes main-routes

  (http/GET "/" req
    (if-let [id (friend/identity req)]
      (views/index-page req)
      (views/anon-index-page)))    

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

  (http/GET "/signup" req
    (h/html5 (views/signup-form (:flash req))))

  (http/POST "/signup"
    {{:keys [username password confirm role_id] :as params}  :params :as req}
    (if (and (not-any? str/blank? [username password])
          (= password confirm))       
      (str
        (let [user (merge (create-user                            
                            (select-keys params [:username :password]))
                     {:role_id 2})]
          (friend/merge-authentication
            (resp/redirect (context-uri req username))
            (workflows/make-auth
              {:identity username
               :roles #{(db/role-id->fully-qualified-role-name 2)}}))))
      (assoc (resp/redirect (str (:context req) "/")) :flash "passwords don't match!")))

  (http/GET "/login" req
    (h/html5
      (if-let [id (`friend/identity req)]
        id
        views/login-form)))
  
  (http/POST "/login"
    {{:keys [username password confirm role_id] :as params} :params :as req}
    #_(h/html5 params)
    (let [role_id (db/fully-qualified-role->role-id
                    (db/fully-qualify-role-keyword "user"))
          role_name (db/role-id->fully-qualified-role-name role_id) 
          user (select-keys params [:username :password :role_id])
          user (assoc params :role_id role_id)
          fuser (update user :roles conj
                  (symbol (db/role-id->fully-qualified-role-name role_id)))]      
      (friend/merge-authentication
        (resp/redirect (context-uri req username))
         (workflows/make-auth {:identity username
                               :roles #{(db/role-id->fully-qualified-role-name role_id)}}))))  
  (http/GET "/:user" req
    (h/html5
      (friend/authenticated
        (friend/identity req))))

  (http/GET "/what" req
    (str req))

  (route/resources "/")

  (route/not-found "Page not found"))

(def app
  (->
     main-routes
    (wrap-cookies)
;;    (wrap-reload)
    (wrap-session)
    (mw/wrap-base-url)))


(def page
  (handler/site
    (friend/authenticate
      main-routes
      {:allow-anon? true
       :login-uri "/login"
       :default-landing-uri "/"
       :unauthorized-handler
       #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
          resp/response
          (resp/status 401))
       :credential-fn #(creds/bcrypt-credential-fn (comp first db/get-user-by-username ) %)       
       :workflows [(workflows/interactive-form)]})))


(defn run
  []
  (defonce ^:private server
    (ring.adapter.jetty/run-jetty #'page {:port 8080 :join? false}))
  server)

(run)
