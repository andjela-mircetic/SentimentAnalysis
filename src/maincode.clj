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

(defn getMessagesBetween [messages user1 user2]
  (filter #(or (and (= (:sentFrom %) user1) (= (:sentTo %) user2))
               (and (= (:sentFrom %) user2) (= (:sentTo %) user1)))
          messages))

(defn sendMessage [users messages sentFrom sentTo time message]
  (if (and (contains? users sentFrom) (contains? users sentTo))
    (conj messages {:sentFrom sentFrom :sentTo sentTo :time time :message message})
    {:error "Sender or recipient does not exist"}))

(defn getRecentMessages [messages n]
  (take-last n messages))

(defn analyzeSentimentByUser [messages username sentiment-analyzer]
  (map #(sentiment-analyzer (:message %))
       (filter #(= (:sentFrom %) username) messages)))

(defn sentiment-analyzer [message]
  (if (clojure.string/includes? message "Hi")
    "Positive"
    "Neutral"))
  
(analyzeSentimentByUser messages "Andjela" sentiment-analyzer)
