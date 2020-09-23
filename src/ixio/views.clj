(ns ixio.views
  (:require
   [ixio.db :as db]
   [hiccup.core :as hiccup]
   [hiccup.page :as page]))

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

(defn individual-paste [row]
  (page/html5
    (:body
     (clojure.edn/read-string 
       (str (first (db/get-paste-by-id row)))))))
;; #object[org.eclipse.jetty.server.HttpInput]

(defn new-account-page [row]
  (page/html5
    [:body
     [:pre
      (clojure.edn/read-string 
       (str (first (db/get-last-user))))]]))

(defn logged-in-successful []
  (page/html5 [:body "SUCCESS!"]))
(defn logged-in-unsuccessful []
  (page/html5 [:body "Please try again, sir."]))
(defn individual-user [row]
  (page/html5
    (:body
     (db/get-user-by-id row))))
;; #object[org.eclipse.jetty.server.HttpInput]


