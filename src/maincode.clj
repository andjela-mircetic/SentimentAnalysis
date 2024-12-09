(ns maincode
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [chatsanalyzer :as analyzer]
            [db.core :as db]
            [java-time :as time]
            [org.httpkit.server :as ws]
            [cheshire.core :as json]))

;; Keep track of connected WebSocket clients
(defonce clients (atom #{}))

(defn broadcast-to-clients [data]
  "Send a message to all connected clients."
  (doseq [client @clients]
    (ws/send! client (json/encode data))))

(defn register-user [username]
  "Registers a new user in the system."
  (if (db/find-one-as-map "users" {:username username})
    {:success "Username already exists"}
    (do
      (db/insert "users" {:username username})
      {:success "User registered"})))

(defn send-message [sentFrom sentTo message]
  "Sends a message and broadcasts it to WebSocket clients."
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

(defn ws-handler [ring-req]
  (if-not (:websocket? ring-req)
    {:status 200 :body "Welcome to the chatroom! JS client connecting..."}
    (ws/as-channel ring-req
                   {:on-open (fn [ch]
                              (swap! clients conj ch))
                    :on-receive (fn [ch message]
                                  (let [data (json/decode message true)]
                                    (case (:type data)
                                      "register" (let [response (register-user (:username data))]
                                                   (ws/send! ch (json/encode response)))
                                      "message" (let [response (send-message (:sentFrom data)
                                                                             (:sentTo data)
                                                                             (:message data))]
                                                  (broadcast-to-clients response))
                                      (ws/send! ch (json/encode {:error "Unknown message type"}))))) 
                    :on-close (fn [ch status-code]
                                (swap! clients disj ch))})))

(defn start-websocket-server []
  (let [port 8080]
    (ws/run-server ws-handler {:port port})
    (println "WebSocket server started on port" port)))

(defn get-chat-partners-with-last-message [username]
  "Fetches chat partners and their last message for a user."
  (let [chats (db/find-maps "chats" {:actors username})
        result (map (fn [chat]
                      (let [partner (first (remove #(= % username) (:actors chat)))
                            last-message (last (sort-by :time (:messages chat)))]
                        {:partner partner
                         :lastMessage last-message}))
                    chats)]
    result))

(defn get-all-messages-between-two-users [user1 user2]
  "Fetches all messages and chatRate between two users."
  (let [chats (db/find-maps "chats" {:actors {:$all [user1 user2]}})
        chat (first chats)
        messages (mapcat :messages chats)
        chat-rate (:chatRate chat)]
    {:messages messages
     :chatRate chat-rate}))


(defn create-chat [user1 user2] 
  (let [user1-exists (db/find-one-as-map "users" {:username user1})
        user2-exists (db/find-one-as-map "users" {:username user2})
        existing-chat (db/find-one-as-map "chats" {:actors {:$all [user1 user2]}})]
    (cond
      (not user1-exists) {:error (str "User " user1 " does not exist")}
      (not user2-exists) {:error (str "User " user2 " does not exist")}
      existing-chat {:error "Chat already exists between these users"}
      :else (do
              (db/insert "chats" {:actors [user1 user2]
                                  :messages []
                                  :tactics []
                                  :chatRate 0})
              {:success "Chat created successfully"}))))


;(defn get-recent-messages [messages n]
 ; (take-last n messages))

;(defn get-messages-sent-by-user [username]
 ;(db/find-maps "messages" {:sentFrom username}))

;(defn get-messages-received-by-user [username]
;  (db/find-maps "messages" {:sentTo username}))

