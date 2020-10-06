(ns ^{:name "Interactive form"
       :doc "Typical username/password authentication + logout + a pinch of authorization functionality"}
    ixio.auth
  (:require
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

(defn- create-user
  [{:keys [username password admin] :as user-data}]
  (-> (dissoc user-data :admin)
    (assoc :identity username
      :password (creds/hash-bcrypt password)
      :roles (into #{::user} (when admin [::admin])))))

(create-user {:username "Doofy" "passwords_suck!" false}) 

(def users
  (atom
    {"friend"
     {:username "friend"
      :password (hash-bcrypt "clojure")
      :pin "1234" ;; only used by multi-factor
      :roles #{::user}}
     "friend-admin"
     {:username "friend-admin"
      :password (hash-bcrypt "clojure")
      :pin "1234" ;; only used by multi-factor
      :roles #{::admin}}}))

(derive ::admin ::user)

(defn- signup-form
  [flash]
  [:div {:class "row"}
   [:div {:class "columns small-12"}
    [:h3 "Sign up "
     [:small "(Any user/pass combination will do, as you are creating a new account or profile.)"]]
    [:div {:class "row"}
     [:form {:method "POST" :action "signup" :class "columns small-4"}
      [:div "Username" [:input {:type "text" :name "username" :required "required"}]]
      [:div "Password" [:input {:type "password" :name "password" :required "required"}]]
      [:div "Confirm" [:input {:type "password" :name "confirm" :required "required"}]]
      [:div "Make you an admin? " [:input {:type "checkbox" :name "admin"}]]
      [:div
       [:input {:type "submit" :class "button" :value "Sign up"}]
       [:span {:style "padding:0 0 0 10px;color:red;"} flash]]]]]])

(def login-form
  [:div {:class "row"}
   [:div {:class "columns small-12"}
    [:h3 "Login"]
    [:div {:class "row"}
     [:form {:method "POST" :action "login" :class "columns small-4"}
      [:div "Username" [:input {:type "text" :name "username"}]]
      [:div "Password" [:input {:type "password" :name "password"}]]
      [:div [:input {:type "submit" :class "button" :value "Login"}]]]]]])

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
      (signup-form (:flash req))
      [:h3 "Current Status " [:small "(this will change when you log in/out)"]]
      [:p (if-let [identity (friend/identity req)]
            (apply str "Logged in, with these roles: "
              (-> identity friend/current-authentication :roles))
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
    (h/html5 login-form))
  (POST "/signup"
    {{:keys [username password confirm] :as params}  :params :as req}
    (if (and (not-any? str/blank? [username password confirm])
          (= password confirm))
      (let [user (create-user (select-keys params [:username :password :admin]))]
        ;; HERE IS WHERE YOU'D PUSH THE USER INTO YOUR DATABASES if desired
        (friend/merge-authentication
          (resp/redirect (context-uri req username))
          user))
      (assoc (resp/redirect (str (:context req) "/")) :flash "passwords don't match!")))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  (GET "/requires-authentication" req
    (friend/authenticated "Thanks for authenticating!"))
  (GET "/role-user" req
    (friend/authorize #{::user} "You're a user!"))
  (GET "/role-admin" req
    (friend/authorize #{::admin} "You're an admin!")))

(def page (handler/site
            (friend/authenticate
              routes
              {:allow-anon? true
               :login-uri "/login"
               :default-landing-uri "/"
               :unauthorized-handler #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
                                        resp/response
                                        (resp/status 401))
               :credential-fn #(creds/bcrypt-credential-fn @users %)
               :workflows [(workflows/interactive-form)]})))

(defn run
  []
  (defonce ^:private server
    (ring.adapter.jetty/run-jetty #'page {:port 8080 :join? false}))
  server)

(run)
