(defproject component-ex "0.1.0-SNAPSHOT"
  :description "Example compojure-api app using Component."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.stuartsierra/component "0.3.2"]

                 ;; [metosin/compojure-api "2.0.0-alpha15"]
                 [metosin/compojure-api "1.1.11"]
                 [ring "1.6.2"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [hikari-cp "1.8.3"]
                 [com.h2database/h2 "1.4.193"]
                 ]
  :main component-ex.core
  ;; :main ^:skip-aot component-ex.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]
                                  [cider/cider-nrepl "0.8.2"]]
                   :plugins [[lein-cljfmt "0.5.7"]]
                   }})
