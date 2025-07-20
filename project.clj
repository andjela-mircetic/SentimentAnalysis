(defproject sentimentanalysis "0.1.0-SNAPSHOT"
  :description "A simple chat app that analyses sentiment of messages"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [nrepl "1.1.1"] 
                 [midje "1.10.9"]
                 [criterium "0.4.6"]
                 [com.novemberain/monger "3.5.0"]
                 [clojure.java-time "0.3.3"]
                 [seesaw "1.5.0"] 
                 [http-kit "2.9.0-alpha1"] 
                 [cheshire "5.10.0"]
                 [stylefruits/gniazdo "1.2.2"]
                 [clj-python/libpython-clj "2.026" :exclusions [org.clojure/clojure org.checkerframework/checker-qual]]] 
  :plugins [[cider/cider-nrepl "0.47.1"] 
            [lein-midje "3.2.1"]] 
  :main ui)

