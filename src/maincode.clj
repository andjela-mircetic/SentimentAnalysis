(ns maincode
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            ;[chatsanalyzer :refer :all] 
            [db.core :as db]
            [java-time :as time]))

;This namespace is responsible for registering user and sending messages

(defn register-user [username]
  (if (db/find-one-as-map "users" {:username username})
    {:error "Username already exists"}
    (do
      (db/insert "users" {:username username})
      {:success "User registered"})))

(defn get-all-messages-of-user [username]
  (db/find-maps "messages"
                {:$or [{:sentFrom username} {:sentTo username}]}))

(defn get-messages-sent-by-user [username]
  (db/find-maps "messages" {:sentFrom username}))

(defn get-messages-received-by-user [username]
  (db/find-maps "messages" {:sentTo username}))

(defn get-all-messages-between-two-users [user1 user2]
  (db/find-maps "messages"
                {:$or [{:sentFrom user1, :sentTo user2}
                      {:sentFrom user2, :sentTo user1}]}))

(defn send-message [sentFrom sentTo message]
  (if (and (db/find-one-as-map "users" {:username sentFrom})
           (db/find-one-as-map "users" {:username sentTo}))
    (do
      (db/insert "messages" {:sentFrom sentFrom
                                :sentTo sentTo
                                :time (time/local-date-time)
                                :message message})
      {:success "Message sent"})
    {:error "Sender or recipient does not exist"}))

;(defn get-recent-messages [messages n]
 ; (take-last n messages))

