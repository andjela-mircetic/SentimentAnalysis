(ns ui
  (:require [seesaw.core :as seesaw]))

(defn -main []
  (let [frame (seesaw/frame :title "Proba title"
                            :content (seesaw/label "Hello!")
                            :on-close :exit)]
    (seesaw/show! frame)))