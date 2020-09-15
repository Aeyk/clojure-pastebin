(ns ixio.views
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(def url "http://localhost:3001/")

(defn index-page []
  (page/html5
    [:body
     [:pre
      (clojure.string/replace 
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
    please go away forever."
        "$URL_STRING", url)]]))

