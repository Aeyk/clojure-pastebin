(ns ixio.views-test
  (:require
   [ixio.views :refer :all]
   [ixio.routes :refer :all]
   [clojure.test :refer :all]))

(defn get-request [resource web-app & params]
   (app {:request-method :get :uri resource :params (first params)}))

(defn post-paste-request [web-app & params]
  (web-app {:request-method :post :uri "/paste"  :params (first params)}))

(def routes ["/pastes" "/paste" "/paste/:id"
             "/user" ;;TODO should show current user settings OR redirect to /login
             "/login" ;; TODO view          
             ])

(deftest every-get-route-has-ok-status
  (is (every? #(= % 200)
        (map #(:status (get-request % app)) routes))))



(run-tests)
;; => {:test 1, :pass 1, :fail 0, :error 0, :type :summary}

