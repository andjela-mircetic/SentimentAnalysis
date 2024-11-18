(ns maincode
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]))

(defn register-user [users username]
  (if (contains? users username)
    {:error "Username already exists"}
    (assoc users username [])))

(def users {})
(def users (register-user users "Andjela"))
(def users (register-user users "Katarina"))


