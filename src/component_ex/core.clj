(ns component-ex.core
  (:require
   [ring.adapter.jetty :as jetty]

   [com.stuartsierra.component :as component]

   [hikari-cp.core :as hikari]

   [component-ex.api :as api])
  (:gen-class))

(defrecord Database [config datasource]
  component/Lifecycle

  (start [this]
    (if datasource
      this
      (do
        (println "Creating datasource with config =" config)
        (let [datasource (hikari/make-datasource config)]
          (do
            (api/create-table! datasource)
            (assoc this :datasource (hikari/make-datasource config)))))))

  (stop [this]
    (when datasource
      (hikari/close-datasource datasource)
      (assoc this :datasource nil))))

(defrecord WebServer [config jetty database]
  component/Lifecycle

  (start [this]
    (if jetty
      this
      (do
        (println "Starting up jetty...")
        (assoc this :jetty (jetty/run-jetty (api/make-app (:datasource database)) config)))))

  (stop [this]
    (when jetty
      (println "Stopping jetty...")
      (.stop jetty))))

(defn system [config]
  (component/system-map
   :database (map->Database {:config (:db config)})
   :server (component/using
            (map->WebServer {:config (:server config)})
            ;; declare dependencies of the server on other managed components.
            {:database :database})))

(def db-settings
  {:adapter "h2"
   :url     "jdbc:h2:~/test"})

(defn -main
  [& args]
  (component/start
   (system {:server {:port 3000}
            :db db-settings})))
