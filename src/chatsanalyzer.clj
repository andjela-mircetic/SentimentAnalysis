(ns chatsanalyzer
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [clojure.string :as str] 
            [clojure.java.shell :as shell]
            [db.core :as db]
            ))


;This namespace is responsible for analyzing tactics and sentiment of messages

(defn analyze-sentiment [text]
  (let [python-script "/Users/andjelamircetic/Desktop/sentimentanalysis/src/vader_sentiment.py"
        result (shell/sh "python3" python-script text)] 
    (if (= 0 (:exit result))
      (do
        (println (:out result))
        (:out result))
      (do
        (println "Error: " (:err result))
        "Neutral"))))


(defn calculate-message-rate [message]
  "Calculates the sentiment rate for a given message."
  (cond
    (str/includes? message "excellent") 5
    (str/includes? message "great") 4
    (str/includes? message "okay") 3
    (str/includes? message "bad") 2
    (str/includes? message "terrible") 1
    :else 3))

(defn calculate-chat-rate [messages]
  "Calculates the average rate for all messages in the chat."
  (if (empty? messages)
    0 ; Default chat rate if no messages exist
    (/ (reduce + (map :messageRate messages))
       (count messages))))

(defn compute-message-rate [chat-id message]
  "Processes the message, updates the chat rate, and returns the message rate."
  (let [rate (calculate-message-rate message)
        chat (db/find-one-as-map "chats" {:_id chat-id})
        all-messages (:messages chat)
        updated-messages (conj all-messages {:message message :messageRate rate})
        avg-rate (calculate-chat-rate updated-messages)] 
    (db/update "chats"
               {:_id chat-id}
               {"$set" {:chatRate avg-rate}})
    rate))






;(defn get-tactics-for-chat [user-tactics logged-in-user chat-with]
 ; (db/find-maps "tactics"
 ;               {:loggedInUser logged-in-user, :chatWith chat-with}))

;(defn add-tactic [logged-in-user chat-with tactic]
;  (db/insert "tactics" {:loggedInUser logged-in-user
;                        :chatWith chat-with
;                        :tactic tactic}))

(defn compute-chat-rate [username sentiment-analyzer]
  (let [messages (db/find-maps "messages" {:sentFrom username})
        scores (map #(case (sentiment-analyzer (:message %))
                       "Negative" 1
                       "Neutral" 2
                       "Positive" 3) messages)
        avg (if (empty? scores) 0 (/ (reduce + scores) (count scores)))]
    (cond
      (> avg 2.5) 3
      (> avg 1.5) 2
      :else 1)))

(defn analyze-sentiment-by-user [username sentiment-analyzer]
  (let [messages (db/find-maps "messages" {:sentFrom username})]
    (map #(sentiment-analyzer (:message %)) messages)))

(defn sentiment-analyzer [message]
  (cond
    (clojure.string/includes? message "great") "Positive"
    (clojure.string/includes? message "bad") "Negative"
    :else "Neutral"))



