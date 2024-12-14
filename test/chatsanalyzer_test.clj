(ns chatsanalyzer-test
  (:require [midje.sweet :refer :all]
            [chatsanalyzer :refer :all]
            [maincode :refer :all] 
            [clojure.java.shell :as shell]
            [db.core :as db]))

(fact "Calculates average chat rate correctly"
      (calculate-chat-rate [{:messageRate 1} {:messageRate 2} {:messageRate 3}]) => 2)

(fact "Returns 0 for calculate-chat-rate when no messages exist"
      (calculate-chat-rate []) => 0)

(fact "Generates tactics for mostly negative messages"
      (generate-gaming-tactics [{:messageRate 1} {:messageRate 1} {:messageRate 2}]) =>
      {:tactics ["Chill out a bit—take a breather, it's just a game."
                 "Try hyping them up instead of calling them out."
                 "Focus on the next match instead of tilting over this one."]})

(fact "Generates tactics for mostly positive messages"
      (generate-gaming-tactics [{:messageRate 4} {:messageRate 5} {:messageRate 5}]) =>
      {:tactics ["GG vibes all around—keep the energy up!"
                 "Who’s MVP? Let’s keep feeding their plays."
                 "Keep the squad hyped up—'Victory Royale incoming!'"]})

(fact "Generates tactics for mixed messages"
      (generate-gaming-tactics [{:messageRate 3} {:messageRate 2} {:messageRate 4}]) =>
      {:tactics ["Mood’s all over—try a quick pep talk. Find out what's bothering the team and fix it."
                 "Mid-game? Suggest small tweaks without overcomplicating."
                 "Post-match, check what's been working or not."]})

(fact "Generates tactics when no messages exist"
      (generate-gaming-tactics []) =>
      {:tactics ["Patience! This player is still silent."]})

(fact "Processes a message and updates chat rate"
      (let [mocked-db-update (fn [& args] true)
            mocked-find-chat (fn [col query] {:messages [{:message "Hello" :messageRate 3}
                                                         {:message "How are you?" :messageRate 4}]})]
        (with-redefs [db/update mocked-db-update
                      db/find-one-as-map mocked-find-chat]
          (compute-message-rate "chat-id" "Hello") => 3)))

(fact "Handles first message and computes initial message rate"
      (let [mocked-db-update (fn [& args] true)
            mocked-find-chat (fn [col query] {:messages []})] 
        (with-redefs [db/update mocked-db-update
                      db/find-one-as-map mocked-find-chat]
          (compute-message-rate "chat-id" "Hello") => 3)))

(fact "Generates tactics when high rate of negative messages"
      (generate-gaming-tactics [{:messageRate 1} {:messageRate 1} {:messageRate 3} {:messageRate 4}]) =>
      {:tactics ["Mute toxic comms and focus on your own plays."
                 "Remind them: 'We're all noobs compared to the pros.'"]})

