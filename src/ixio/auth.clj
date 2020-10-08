(ns ^{:name "Interactive form"
      :doc "Typical username/password authentication + logout + a pinch of authorization functionality"}
    ixio.auth
  (:require
   [ixio.views :as views]
   [ixio.db :as db]
   [hiccup.page :as h]
   [hiccup.element :as e] 
   [compojure.core :as compojure :refer (GET POST ANY defroutes)]
   (compojure [handler :as handler]
     [route :as route])
   [cemerick.friend :as friend]
   (cemerick.friend
     [workflows :as workflows]
     [credentials :as creds])
   [cemerick.friend.credentials :refer (hash-bcrypt)]
   [cemerick.friend.workflows :refer [make-auth]]
   [ring.util.response :as resp]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [clojure.string :as str]
   [ring.adapter.jetty :refer [run-jetty]])
  (:import java.net.URI))

;; ((keyword "count(*)") (first (db/count-users)))

(defn- create-user
  [{:keys [username password admin role_id] :as user-data}]    
  (let [user-auth-data (-> (dissoc user-data :admin)
                         (assoc :identity username
                           :password (creds/hash-bcrypt password)
                           :users.role_id role_id
                           :roles (into #{::user} (when admin [::admin]))))]
    (let [user-data user-data]
      (db/create-user user-auth-data))
    user-auth-data))

;;(create-user  {:username "do" :password "m" :role_id 0})

;; (let [m (first   (get-user-by-username "mjk"))]
;;   )

(def *db-users* ^:dynamic
  #_(into (hash-map))
  #_(map
      (juxt :id identity))
  (let [m (group-by :id
            (vec (for [i (db/get-users)]
                   (assoc  i
                     :roles #{::users}))))]
    (assoc m :roles (db/role-id->fully-qualified-role-name (:role_id m)))))

(db/role-id->fully-qualified-role-name 2)
 *db-users*

;; (def users
;;   (atom
;;     {"friend"
;;      {:username "friend"
;;       :password (hash-bcrypt "clojure")
;;       :pin "1234" ;; only used by multi-factor
;;       :roles #{::user}}
;;      "friend-admin"
;;      {:username "friend-admin"
;;       :password (hash-bcrypt "clojure")
;;       :pin "1234" ;; only used by multi-factor
;;       :roles #{::admin}}}))

;;;clojure.core/derive
;;; [tag parent]
;;; [h tag parent]
;;;Added in 1.0
;;;  Establishes a parent/child relationship between parent and
;;;  tag. Parent must be a namespace-qualified symbol or keyword and
;;;  child can be either a namespace-qualified symbol or keyword or a
;;;  class. h must be a hierarchy obtained from make-hierarchy, if not
;;;  supplied defaults to, and modifies, the global hierarchy.

(derive ::admin ::user)

((workflows/http-basic :realm "/") {:authorization {:username "m" :password "m"}})
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



(defroutes routes
  (GET "/" req
    (h/html5
      [:h2 "Interactive form authentication"]
      [:p "This app demonstrates typical username/password authentication, and a pinch of Friend's authorization capabilities."]
      views/login-form
      (views/signup-form (:flash req))
      [:h3 "Current Status " [:small "(this will change when you log in/out)"]]
      [:p (if-let [id (friend/identity req)]
            (str id )
            #_(friend/merge-authentication
                        (resp/redirect (context-uri req id))
                        (workflows/make-auth {:identity id
                                              :roles (db/role-id->fully-qualified-role-name id)}))
                        #_{(str (symbol (:username (-> id friend/current-authentication))))
               (-> id friend/current-authentication)} 
            "anonymous user")]
      [:h3 "Authorization demos"]
      [:p "Each of these links require particular roles (or, any authentication) to access. "
       "If you're not authenticated, you will be redirected to a dedicated login page. "
       "If you're already authenticated, but do not meet the authorization requirements "
       "(e.g. you don't have the proper role), then you'll get an Unauthorized HTTP response."]
      
      [:ul
       [:li
        (e/link-to
          (context-uri req "role-user") "Requires the `user` role")]
       [:li (e/link-to (context-uri req "role-admin") "Requires the `admin` role")]
       [:li (e/link-to (context-uri req "requires-authentication")
              "Requires any authentication, no specific role requirement")]]
      [:h3 "Logging out"]
      [:p (e/link-to (context-uri req "logout") "Click here to log out") "."]))
  (GET "/login" req   
    (h/html5 (if-let [id (friend/identity req)]
               (str id 
                 (friend/merge-authentication
                   (resp/redirect (context-uri req id))
                   (workflows/make-auth {:identity id
                                         :roles (db/role-id->fully-qualified-role-name (:role_id id))})))
            "anonymous user")
      #_views/login-form))
  (GET "/signup" req
    (h/html5 (views/signup-form (:flash req))))
  (POST "/signup"
    {{:keys [username password confirm role_id] :as params}  :params :as req}
    (if (and (not-any? str/blank? [username password])
          (= password confirm))       
      #_(str (create-user (select-keys params [:username :password :admin :role_id])))
      (let [user (create-user (select-keys params [:username :password :admin :role_id]))]
        (friend/merge-authentication
          (resp/redirect (context-uri req username))
          (workflows/make-auth {:identity username
                                :roles (db/role-id->fully-qualified-role-name role_id)})))
      (assoc (resp/redirect (str (:context req) "/")) :flash "passwords don't match!")))
  (POST "/test"
    {{:keys [username password confirm role_id] :as params} :params :as req}
    (let [role_id (db/fully-qualified-role->role-id (db/fully-qualify-role-keyword "user"))
          role_name (db/role-id->fully-qualified-role-name role_id) 
          user (select-keys params [:username :password :role_id])
          user (assoc params :role_id role_id)
          fuser (update user :roles conj (symbol (db/role-id->fully-qualified-role-name role_id)))]      
      (friend/merge-authentication
        (resp/redirect (context-uri req username))
         (workflows/make-auth {:identity username
                               :roles #{(db/role-id->fully-qualified-role-name role_id)}}))))  
  
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  (GET "/requires-authentication" req
    (friend/authenticated "Thanks for authenticating!"))
  (GET "/role-user" req
    (friend/authorize #{::user} "You're a user!"))
  (GET "/role-admin" req
    (friend/authorize #{::admin} "You're an admin!"))
  (GET "/role-who" req
    (h/html5
      (str
        (db/role-id->fully-qualified-role-name (inc (:role_id (friend/current-authentication))))
       )))

  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/")) ))
  (GET "/:user"
    req
    (h/html5
      (friend/authenticated
        (friend/identity req)))
    #_(friend/authenticated
        (let [user (:user (:params req))
              role_id (:role_id (:params req))
              fuser (merge user {:roles #{(symbol (db/role-id->fully-qualified-role-name  (context-uri req role_id)))}})]
          (str  fuser (friend/current-authentication))
          #_(if (= user (:username (friend/current-authentication)))
	      (h/html5
	        [:h2 (str "Hello, new user " user "!")]
	        [:p "Return to the " (e/link-to (context-uri req "") "example") 
	         ", or " (e/link-to (context-uri req "logout") "log out") "."]))
          #_(resp/redirect (str (:context req) "/"))))))




(def page (handler/site
            (friend/authenticate
              routes
              {:allow-anon? true
               :login-uri "/login"
               :default-landing-uri "/"
               :unauthorized-handler #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
                                        resp/response
                                        (resp/status 401))
               :credential-fn #(creds/bcrypt-credential-fn (comp first db/get-user-by-username) %)

               
               :workflows [(workflows/interactive-form)]})))
(defn run
  []
  (defonce ^:private server
    (ring.adapter.jetty/run-jetty #'page {:port 8080 :join? false}))
  server)

(run)

