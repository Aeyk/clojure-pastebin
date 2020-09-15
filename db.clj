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
        #{[:timestamp :datetime :default :current_timestamp ]
          [:id "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"]
         [:body :clob]}))
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

(defn get-pastes-by-id [id]
  (query my-db [(str "SELECT * FROM pastes WHERE id="id ";")]))

(defn create-paste [req]
   ;; req
  (insert! my-db :pastes req)) ;; TODO how to sanitize

#_(create-db)
#_(create-paste {:body "Hello, World"})
#_(drop-pastes)
