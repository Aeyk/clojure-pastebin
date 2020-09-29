 (ns ixio.db
  (:require [clojure.java.jdbc :as jdbc])
  (:gen-class))


(def my-db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/pastes_development.db"})

(defn get-all-pastes []
  (jdbc/query my-db ["SELECT * FROM pastes WHERE 1=1 AND private = False;"]))

(defn get-paste-by-id [id]
    (jdbc/query my-db [ "SELECT * FROM pastes WHERE 1=1 AND id = ? AND private = 0;" id ]))

(defn get-last-paste []
  (jdbc/query my-db "SELECT * FROM pastes WHERE ID =(SELECT MAX(ID)  AND private = FALSE FROM pastes);"))

(defn current-user []
  (if true ;; TODO: (logged-in?) (user-id) (anon-id)
    1))

(defn create-paste [req]
  ;; req  
  (jdbc/insert! my-db :pastes
    (merge {:userid (current-user)} req)))


(defn get-all-users []
  (jdbc/query my-db ["SELECT * FROM users;"]))

(defn get-user-by-id [id]
  (jdbc/query my-db [ "SELECT * FROM users WHERE id= ?" id ]))

(defn get-user-by-username [username]
  (jdbc/query my-db [ "SELECT * FROM users WHERE username= ?" username ]))


(defn get-last-user []
  (jdbc/query my-db "SELECT * FROM    users WHERE ID = (SELECT MAX(ID) FROM users);"))

(defn create-user [req]
   ;; req
  (jdbc/insert! my-db :users req)) 




;;wish this worked
#_(defn tables
  []
  (query my-db [".tables"]))

;; todo how to get table names / column names


#_(create-db)
#_(create-paste {:body "Hello, World"})
#_(drop-pastes)

#_(get-pastes-by-id 1)

;; (create-paste {:body "First anon-owned post" :private false})
;; (get-last-paste)
