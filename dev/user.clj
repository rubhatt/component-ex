(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
                (:require
                  [clojure.java.io :as io]
                  [clojure.java.javadoc :refer (javadoc)]
                  [clojure.pprint :refer (pprint)]
                  [clojure.reflect :refer (reflect)]
                  [clojure.repl :refer (apropos dir doc find-doc pst source)]
                  [clojure.set :as set]
                  [clojure.string :as str]
                  [clojure.test :as test]
                  [clojure.tools.namespace.repl :refer (refresh refresh-all)]
                  [com.stuartsierra.component :as component]
                  [component-ex.core :as example]
                  ))

(def system nil)

(def db-settings
  {:adapter "h2"
   :url     "jdbc:h2:~/test"})

(defn dev-system
  "Constructs a system map suitable for interactive development."
  []
  (example/system {:db db-settings
                       :server {:port 3000 :join? false}}))

(defn init []
  (alter-var-root #'system
                  (constantly (dev-system))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
(refresh :after 'user/go))
