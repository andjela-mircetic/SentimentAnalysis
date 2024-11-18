(ns maincode
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]))

(def messages
  [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hello!"}
   {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hi Angie!"}])

(def users #{"Andjela" "Katarina"})

(defn register-user [users username]
  (if (contains? users username)
    {:error "Username already exists"}
    (conj users username)))

(defn getAllMessagesByUser [messages username]
  (filter #(or (= (:sentFrom %) username)
               (= (:sentTo %) username))
          messages))

(defn messagesSentBy [messages username]
  (filter #(= (:sentFrom %) username) messages))

(defn messagesReceivedBy [messages username]
  (filter #(= (:sentTo %) username) messages))
