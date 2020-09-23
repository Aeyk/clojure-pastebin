 (ns ixio.db
  (:require [clojure.java.jdbc :refer :all])
  (:gen-class))


(def my-db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/pastes_development.db"})

;;wish this worked
#_(defn tables
  []
  (query my-db [".tables"]))

;; todo how to get table names / column names

(defn create-db
  "create db and table"
  []
  (try
    (db-do-commands my-db
      (create-table-ddl :pastes
        [[:timestamp :datetime :default :current_timestamp ]
         [:id "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"]
         [:body "CLOB NOT NULL"]]))
       (catch Exception e
         (println (.getMessage e)))))

(defn drop-pastes
  []
  (try
    (db-do-commands my-db
      (drop-table-ddl :pastes))
       (catch Exception e
         (println (.getMessage e)))))

(defn get-all-pastes []
  (query my-db ["SELECT * FROM pastes;"]))

(defn get-paste-by-id [id]
  (query my-db [ "SELECT * FROM pastes WHERE id= ?" id ]))

(defn get-last-paste []
  (query my-db "SELECT * FROM    pastes WHERE ID = (SELECT MAX(ID) FROM pastes);"))

(defn create-paste [req]
   ;; req
  (insert! my-db :pastes {:body req})) ;; TODO how to sanitize



(defn get-all-users []
  (query my-db ["SELECT * FROM users;"]))

(defn get-user-by-id [id]
  (query my-db [ "SELECT * FROM users WHERE id= ?" id ]))

(defn get-user-by-username [username]
  (clojure.java.jdbc/query my-db [ "SELECT * FROM users WHERE username= ?" username ]))


(defn get-last-user []
  (query my-db "SELECT * FROM    users WHERE ID = (SELECT MAX(ID) FROM users);"))

(defn create-user [req]
   ;; req
  (insert! my-db :users req)) 




#_(create-db)
#_(create-paste {:body "Hello, World"})
#_(drop-pastes)

#_(get-pastes-by-id 1)
