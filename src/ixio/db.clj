 (ns ixio.db
   (:require
    [clojure.java.jdbc :as jdbc]
    [cemerick.friend :as friend]
    [cemerick.friend.credentials :refer (hash-bcrypt)]))

(def my-db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/pastes_development.db"})


(def user-role-map  (group-by :role_name  (vec (clojure.java.jdbc/query my-db [ "SELECT * FROM users_roles ;" ]))))
;; => {"admin" [{:id 1, :role_name "admin"}], "user" [{:id 2, :role_name "user"}], "anon" [{:id 3, :role_name "anon"}]}

(defn fully-qualify-role-keyword [rk]
  (keyword (str "ixio.auth/" rk)))

;;; TODO put it in a test 
(comment (= (fully-qualify-role-keyword "users")
  :ixio.auth/users))
;; => true
(comment (= (role-id->fully-qualified-role-name 0
  (fully-qualify-role-keyword "users"))))

(symbol (name ::users))

;; => true

(defn fully-qualified-role->role-id [flqr]
  (first (map :id (get user-role-map (name flqr)))))

(defn role-id->fully-qualified-role-name [id]
  (fully-qualify-role-keyword
    (first (map :role_name (clojure.java.jdbc/query my-db [ "SELECT * FROM users_roles WHERE id = ? ;" id])))))


(map role-id->fully-qualified-role-name
  (clojure.java.jdbc/query my-db  ["SELECT role_id FROM users;"]))

(defn get-all-pastes []
  (jdbc/query my-db ["SELECT * FROM pastes WHERE 1=1 AND private = False;"]))

(defn get-paste-by-id [id]
    (jdbc/query my-db [ "SELECT * FROM pastes WHERE 1=1 AND id = ? AND private = 0;" id ]))

(defn get-last-paste []
  (jdbc/query my-db "SELECT * FROM pastes WHERE ID =(SELECT MAX(ID)  AND private = FALSE FROM pastes);"))

(defn create-paste [req]
  ;; req  
  (jdbc/insert! my-db :pastes
    (if-let [id (friend/identity req)]
      (merge {:userid id} req)
      (merge {:userid 0} req))))

(defn get-all-users []
  (jdbc/query my-db ["SELECT * FROM users;"]))


(defn count-users []
  (jdbc/query my-db ["SELECT COUNT(*) FROM users;"]))


(defn get-user-by-id [id]
  (jdbc/query my-db [ "SELECT * FROM users WHERE id= ?" id ]))

(defn get-users []
  (jdbc/query my-db [ "SELECT * FROM users; "]))


(defn get-user-by-username [username]
  (jdbc/query my-db [ "SELECT * FROM users WHERE username= ?" username ]))


(defn get-users-role-id [username role_id]
  (jdbc/query my-db [ "SELECT * FROM users WHERE users.role_id= ?" role_id ]))

(defn get-user-by-username-hydrated-roles [username]  
  (let [dbe (get-user-by-username username)
        role_id ((juxt #(get :role_id %) identity) dbe)]
    dbe))
     
(let [m (first   (get-user-by-username "mjk"))]
  (assoc m :roles (role-id->fully-qualified-role-name (:role_id m))))



;;(update (get-user-by-username "m") :roles 0)


(take 1
  (:roles
   (ixio.db/get-user-by-username "mjk")
   (ixio.db/get-user-by-username-hydrated-roles "mjk")
  ))


(defn get-last-user []
  (jdbc/query my-db "SELECT * FROM    users WHERE ID = (SELECT MAX(ID) FROM users);"))

(defn create-user [{:keys [username password role_id]}]
    ;; req
  (jdbc/insert! my-db :users
    {:username username
     :password (hash-bcrypt password)
     :role_id role_id}))

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
