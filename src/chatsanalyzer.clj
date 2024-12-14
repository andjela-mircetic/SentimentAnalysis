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

(defn generate-gaming-tactics [messages]
  "Generates gaming-focused tactics based on the sentiment rates of messages sent by another user.
   If there are no messages, returns a specific message indicating silence."
  (if (empty? messages)
    {:tactics ["Patience! This player is still silent."]}
    (let [rates (map :messageRate messages)
          rate-distribution (frequencies rates)
          total-messages (count messages)
          rate-percentage (fn [rate]
                            (if (zero? total-messages)
                              0
                              (double (/ (get rate-distribution rate 0) total-messages))))
          tactics (cond
                    ;; Mostly negative messages (rates 1 and 2 dominate)
                    (>= (+ (rate-percentage 1) (rate-percentage 2)) 0.6)
                    ["Chill out a bit—take a breather, it's just a game."
                     "Try hyping them up instead of calling them out."
                     "Focus on the next match instead of tilting over this one."]

                    ;; Mostly neutral messages (rate 3 dominates)
                    (>= (rate-percentage 3) 0.7)
                    ["Break it down: 'What's the play for this map?'"
                     "Mid-game? Time for some hype calls!"
                     "After this, maybe brainstorm some new strats."]

                    ;; Mostly positive messages (rates 4 and 5 dominate)
                    (>= (+ (rate-percentage 4) (rate-percentage 5)) 0.6)
                    ["GG vibes all around—keep the energy up!"
                     "Who’s MVP? Let’s keep feeding their plays."
                     "Keep the squad hyped up—'Victory Royale incoming!'"]

                    ;; Mixed messages (balanced rates)
                    (every? #(<= (rate-percentage %) 0.4) [1 2 3 4 5])
                    ["Mood’s all over—try a quick pep talk. Find out what's bothering the team and fix it."
                     "Mid-game? Suggest small tweaks without overcomplicating."
                     "Post-match, check what's been working or not."]

                    ;; High rate of very positive (rate 5 > 40%)
                    (>= (rate-percentage 5) 0.4)
                    ["Momentum's good—go for some aggressive moves."
                     "Everyone’s vibing—suggest crazy comps or strats."
                     "Positive vibes = ranked grind time!"]

                    ;; High rate of very negative (rate 1 > 40%)
                    (>= (rate-percentage 1) 0.4)
                    ["Mute toxic comms and focus on your own plays."
                     "Remind them: 'We're all noobs compared to the pros.'"]

                    ;; Predominantly slightly negative (rate 2 > 40%)
                    (>= (rate-percentage 2) 0.4)
                    ["Looks like frustrations creeping in—remind them to focus." 
                     "Encourage them to try again—'We're still in this!'"
                     "Bad vibes? Joke it off with some light banter."]

                    ;; Predominantly slightly positive (rate 4 > 40%)
                    (>= (rate-percentage 4) 0.4)
                    ["Squad's in a good spot — suggest a sneaky strat."
                     "Stay focused — no distractions."
                     "Let’s lock it in and close this match strong!"]

                    ;; Rapid swings (negative and positive alternating)
                    (and (>= (rate-percentage 1) 0.2)
                         (>= (rate-percentage 5) 0.2))
                    ["Team’s mood’s a rollercoaster—help stabilize it."
                     "Keep calls short and clear—minimize confusion."
                     "Post-game? Suggest a cooldown game or a strat talk."]

                    ;; Default: Balanced with no clear trend
                    :else
                    ["Call out good plays to keep the squad motivated."
                     "Don’t overthink—stick to basics and have fun." 
                     "Suggest a debrief after to level up gameplay."])]
      {:tactics tactics})))
