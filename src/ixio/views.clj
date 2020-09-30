(ns ixio.views
  (:require
   [ixio.core :as ixio]
   [ixio.db :as db]
   [hiccup.core :as hiccup]
   [hiccup.page :as page]
   [hiccup.form :as form]
   ))

(def url "http://localhost:3000/")
(def man-string (clojure.string/replace 
        "$URL_STRING(1)  	MAN	 	$URL_STRING(1)

NAME

    $URL_STRING command line pastebin.


TL;DR

    $ curl -X POST $URL_STRING -d \"body=$@\" %     
    http://$URL_STRING1


GET
	$URL_STRING:id
        	raw text

POST
	$ curl -X POST $URL_STRING -d \"body=$@\" %


EXAMPLES: TODO

CLIENT: TODO

CAVEATS:
    Paste at your risk. Be nice please. If you are distributing software that
    uses this automatically, talk to me first. If you are distributing malware
    please go away forever.
"
        "$URL_STRING", url))
(defn index-page []
  (page/html5
    [:body
     [:pre
      man-string]]))

(defn pastes-page []
  (page/html5
    (for [p (db/get-all-pastes)]
      [:div 
       [:table
        [:tr [:td "ID"] [:td "PRIVATE?"] [:td "BODY"]]
        [:tr
         [:td
          (:id p)]
         [:td
           (:private p)]
         [:td
          (:body p)]]]])))

(defn individual-paste [row]
  (let [paste (clojure.edn/read-string
                (str (first (db/get-paste-by-id row))))
        body (:body paste)
        id (:id paste)
        private (:private paste)]
    (page/html5
      [:body
       [:table
        [:tr [:td "ID"]
         [:td "PRIVATE? "] [:td "PASTE"]]
        [:tr
         [:td id]
         [:td  private]
         [:td body]]  
        ]])))

(defn new-account-page [row]
  (page/html5
    [:body
     [:pre
      (clojure.edn/read-string 
       (str (first (db/get-last-user))))]]))

(defn create-account-page []
  (page/html5
    [:body
     (form/form-to [:post "/signup"]
       (form/text-field "username")
       (form/password-field "password")
       (form/submit-button "Create Account"))]))


(defn login-page []
  (page/html5
    [:body
     (form/form-to [:post "/login"]
       (form/text-field "username")
       (form/password-field "password")
       (form/submit-button "Login"))]))

(defn logged-in-successful []
  (page/html5 [:body "SUCCESS!"]))

(defn logged-in-unsuccessful []
  (page/html5 [:body "Please try again, sir."]))

(defn individual-user [row]
  (page/html5
    (:body
     (db/get-user-by-id row))))
;; #object[org.eclipse.jetty.server.HttpInput]


