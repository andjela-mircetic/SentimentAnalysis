(ns chatsanalyzer
  (:require [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [clojure.string :as str] 
            [clojure.java.shell :as shell]
            [db.core :as db]
            [clojure.java.io :as io]
            ))


;This namespace is responsible for analyzing tactics and sentiment of messages

(defn calculate-message-rate [text]
  "Calculates the sentiment rate for a given message."
  (let [python-script (str (io/file "src/vader_sentiment.py"))
        result (shell/sh "python3" python-script text)]
    (if (= 0 (:exit result))
      (let [compound-score (Double/parseDouble (clojure.string/trim (:out result)))
            sentiment-score (cond
                              (<= compound-score -0.6) 1
                              (<= compound-score -0.2) 2
                              (<= compound-score 0.2) 3
                              (<= compound-score 0.6) 4
                              :else 5)]
        sentiment-score)
      (do
        (println "Error: " (:err result))
        nil))))

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

(defn calculate-chat-rate-using-loop [messages]
  "Calculates the average rate for all messages in the chat using loop/recur."
  (if (empty? messages)
    0
    (let [total (loop [msgs messages
                       acc 0]
                  (if (empty? msgs)
                    acc
                    (recur (rest msgs) (+ acc (:messageRate (first msgs))))))]
      (/ total (count messages)))))

(def default-rules
  [{:condition (fn [rate-percentage]
                 (>= (+ (rate-percentage 1) (rate-percentage 2)) 0.6))
    :tactics ["Chill out a bit—take a breather, it's just a game."
              "Try hyping them up instead of calling them out."
              "Focus on the next match instead of tilting over this one."]}

   {:condition (fn [rate-percentage]
                 (>= (rate-percentage 3) 0.7))
    :tactics ["Break it down: 'What's the play for this map?'"
              "Mid-game? Time for some hype calls!"
              "After this, maybe brainstorm some new strats."]}

   {:condition (fn [rate-percentage]
                 (>= (+ (rate-percentage 4) (rate-percentage 5)) 0.6))
    :tactics ["GG vibes all around—keep the energy up!"
              "Who’s MVP? Let’s keep feeding their plays."
              "Keep the squad hyped up—'Victory Royale incoming!'"]}

   {:condition (fn [rate-percentage]
                 (every? #(<= (rate-percentage %) 0.4) [1 2 3 4 5]))
    :tactics ["Mood’s all over—try a quick pep talk. Find out what's bothering the team and fix it."
              "Mid-game? Suggest small tweaks without overcomplicating."
              "Post-match, check what's been working or not."]}

   {:condition (fn [rate-percentage]
                 (>= (rate-percentage 5) 0.4))
    :tactics ["Momentum's good—go for some aggressive moves."
              "Everyone’s vibing—suggest crazy comps or strats."
              "Positive vibes = ranked grind time!"]}

   {:condition (fn [rate-percentage]
                 (>= (rate-percentage 1) 0.4))
    :tactics ["Mute toxic comms and focus on your own plays."
              "Remind them: 'We're all noobs compared to the pros.'"]}

   {:condition (fn [rate-percentage]
                 (>= (rate-percentage 2) 0.4))
    :tactics ["Looks like frustrations creeping in—remind them to focus."
              "Encourage them to try again—'We're still in this!'"
              "Bad vibes? Joke it off with some light banter."]}

   {:condition (fn [rate-percentage]
                 (>= (rate-percentage 4) 0.4))
    :tactics ["Squad's in a good spot — suggest a sneaky strat."
              "Stay focused — no distractions."
              "Let’s lock it in and close this match strong!"]}

   {:condition (fn [rate-percentage]
                 (and (>= (rate-percentage 1) 0.2)
                      (>= (rate-percentage 5) 0.2)))
    :tactics ["Team’s mood’s a rollercoaster—help stabilize it."
              "Keep calls short and clear—minimize confusion."
              "Post-game? Suggest a cooldown game or a strat talk."]}

   {:condition (fn [_] true)
    :tactics ["Call out good plays to keep the squad motivated."
              "Don’t overthink—stick to basics and have fun."
              "Suggest a debrief after to level up gameplay."]}])

(defn generate-gaming-tactics
  [messages & [rules]]
  "Generates gaming-focused tactics based on sentiment rates of messages using customizable rules.
   If there are no messages, returns a specific message indicating silence.
   By default it uses default-rules, that are currently specified for this app."
  (let [rules (or rules default-rules)]
    (if (empty? messages)
      {:tactics ["Patience! This player is still silent."]}
      (let [rates (map :messageRate messages)
            rate-distribution (frequencies rates)
            total-messages (count messages)
            rate-percentage (fn [rate]
                              (if (zero? total-messages)
                                0
                                (double (/ (get rate-distribution rate 0) total-messages))))]
        (->> rules
             (filter #(apply (:condition %) [rate-percentage]))
             (mapcat :tactics)
             (distinct)
             (vec)
             (hash-map :tactics))))))
