(ns maincode
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [chatsanalyzer :refer :all]
            [monger.core :as mg]
            [monger.collection :as mc]))

;This namespace is responsible for registering user and sending messages


(defonce conn (mg/connect))
(defonce db (mg/get-db conn "ChatApp"))

; Simulated data
(def messages
  [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hello!"}
   {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hi Angie!"}])

(def users #{"Andjela" "Katarina"})

(defn register-user [users username]
  (if (contains? users username)
    {:error "Username already exists"}
    (conj users username)))

(defn get-all-messages-of-user [messages username]
  (filter #(or (= (:sentFrom %) username)
               (= (:sentTo %) username))
          messages))

(defn get-messages-sent-by-user [messages username]
  (filter #(= (:sentFrom %) username) messages))

(defn get-messages-received-by-user [messages username]
  (filter #(= (:sentTo %) username) messages))

(defn get-all-messages-between-two-users [messages user1 user2]
  (filter #(or (and (= (:sentFrom %) user1) (= (:sentTo %) user2))
               (and (= (:sentFrom %) user2) (= (:sentTo %) user1)))
          messages))

(defn send-message [users messages sentFrom sentTo time message]
  (if (and (contains? users sentFrom) (contains? users sentTo))
    (conj messages {:sentFrom sentFrom :sentTo sentTo :time time :message message})
    {:error "Sender or recipient does not exist"}))

(defn get-recent-messages [messages n]
  (take-last n messages))

(defn analyze-sentiment-by-user [messages username sentiment-analyzer]
  (map #(sentiment-analyzer (:message %))
       (filter #(= (:sentFrom %) username) messages)))

(defn sentiment-analyzer [message]
  (cond
    (clojure.string/includes? message "great") "Positive"
    (clojure.string/includes? message "bad") "Negative"
    :else "Neutral"))

  
(analyze-sentiment-by-user messages "Andjela" sentiment-analyzer)
