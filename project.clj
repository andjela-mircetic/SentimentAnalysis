(defproject sentimentanalysis "0.1.0-SNAPSHOT"
  :description "A simple chat app that analyses sentiment of messages"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [nrepl "1.1.1"] 
                 [midje "1.10.9"]] 
  :plugins [[cider/cider-nrepl "0.47.1"] 
            [lein-midje "3.2.1"]]
  :main maincode)

;{:deps {ring/ring-core {:mvn/version "1.9.5"}
      ;  ring/ring-json {:mvn/version "0.5.0"}
      ;  compojure {:mvn/version "1.6.2"}}}
