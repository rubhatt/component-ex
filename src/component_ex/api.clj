(ns component-ex.api
  (:require
   [ring.util.http-response :refer :all]
   [compojure.api.sweet :refer :all]
   [ring.util.http-status :as http-status]
   [schema.core :as schema]

   [clojure.java.jdbc :as jdbc])

  (:gen-class))

(schema/defschema User
  {:id schema/Int
   :name schema/Str
   :email schema/Str})

(def NewUser (dissoc User :id))

(defn create-table!
  [datasource]
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (jdbc/db-do-commands conn
                         (jdbc/create-table-ddl :users
                                                [[:id "bigint primary key auto_increment"]
                                                 [:name "varchar(200)"]
                                                 [:email "varchar(300)"]]
                            ;; create if not exists
                                                {:conditional? true}))))

(def generated-key (keyword "scope_identity()"))

(defn- get-id
  [returned-row]
  (-> returned-row first generated-key))

(defn new-user
  [database new-user]
  (jdbc/with-db-connection [conn {:datasource database}]
    (->>
     (jdbc/insert! conn :users
                   {:name (:name new-user) :email (:email new-user)})
     get-id
     (assoc new-user :id))))

(defn get-users
  [database]
  (jdbc/with-db-connection [conn {:datasource database}]
    (jdbc/query conn "SELECT * FROM users")))

(defn get-user
  [database user-id])

(defn make-app
  [database]
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Component example."
                   :description "Component example api."}
            :tags [{:name "api", :description "apis"}]}}}
   (context "/api/v1" []
     :tags ["api/v1"]
     (routes

      (context "/users" []
        (resource
         {:tags ["users"]
          :post {:summary "Create new user."
                 :parameters {:body-params NewUser}
                 :responses {http-status/created {:schema User
                                                  :headers {"Location" schema/Str}}}
                 :handler (fn [{body :body-params}]
                            (let [user (new-user database body)]
                              (do
                                (println user)
                                (created (path-for ::user {:user_id (:id user)})
                                         user))))}
          :get {:summary "Get all users."
                :responses {http-status/ok {:schema [User]
                                            :description "List of users."}}
                :handler (fn [_] (ok (get-users database)))}}))
      (context "/users/:user_id" []
        :path-params [user_id :- schema/Int]
        (resource
         {:tags ["users"]
          :get {:x-name ::user
                :summary "Get a specific user."
                :responses {http-status/ok {:schema User}
                            http-status/not-found {:schema schema/Str}}
                :handler (fn [_]
                           (let [user (get-user database user_id)]
                             (if user
                               (ok user)
                               (not-found "Content not found."))))}}))))))
