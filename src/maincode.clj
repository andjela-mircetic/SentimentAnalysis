(ns maincode
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [chatsanalyzer :as analyzer]
            [db.core :as db]
            [java-time :as time]))

;This namespace is responsible for registering user and sending messages

(defn register-user [username]
  (if (db/find-one-as-map "users" {:username username})
    {:success "Username already exists"}
    (do
      (db/insert "users" {:username username})
      {:success "User registered"})))

(defn get-chat-partners-with-last-message [username]
  (let [chats (db/find-maps "chats" {:actors username})
        result (map (fn [chat]
                      (let [partner (first (remove #(= % username) (:actors chat)))
                            last-message (last (sort-by :time (:messages chat)))] 
                        {:partner partner
                         :lastMessage last-message}))
                    chats)]
    result))

(defn get-all-messages-between-two-users [user1 user2]
  (let [chats (db/find-maps "chats"
                            {:actors {:$all [user1 user2]}})
        messages (mapcat :messages chats)] 
    (filter #(or (and (= (:sentFrom %) user1) (= (:sentTo %) user2))
                 (and (= (:sentFrom %) user2) (= (:sentTo %) user1)))
            messages)))

(defn send-message [sentFrom sentTo message]
  (if (and (db/find-one-as-map "users" {:username sentFrom})
           (db/find-one-as-map "users" {:username sentTo}))
    (let [existing-chat (db/find-one-as-map "chats" {:actors {:$all [sentFrom sentTo]}})
          message-rate (analyzer/compute-message-rate (:_id existing-chat) message) 
          message-data {:sentFrom sentFrom
                        :sentTo sentTo
                        :time (time/local-date-time)
                        :message message
                        :messageRate message-rate}]
      (if existing-chat
  
        (do
          (db/update "chats"
                     {:_id (:_id existing-chat)}
                     {"$push" {:messages message-data}})
          (let [updated-chat (db/find-one-as-map "chats" {:_id (:_id existing-chat)})
                new-chat-rate (analyzer/calculate-chat-rate (:messages updated-chat))]
            (db/update "chats"
                       {:_id (:_id existing-chat)}
                       {"$set" {:chatRate new-chat-rate}}))
          {:success "Message sent"})

        (let [new-chat {:actors [sentFrom sentTo]
                        :chatRate message-rate
                        :messages [message-data]
                        :tactics []}]
          (db/insert "chats" new-chat)
          {:success "Message sent to new chat"})))
    {:error "Sender or recipient does not exist"}))



;(defn get-recent-messages [messages n]
 ; (take-last n messages))

;(defn get-messages-sent-by-user [username]
 ;(db/find-maps "messages" {:sentFrom username}))

;(defn get-messages-received-by-user [username]
;  (db/find-maps "messages" {:sentTo username}))

