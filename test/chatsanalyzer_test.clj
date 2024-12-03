(ns chatsanalyzer-test
  (:require [midje.sweet :refer :all]
            [chatsanalyzer :refer :all]
            [maincode :refer :all]
            [code :refer :all]))

(fact "Computes chat rate based on sentiment analysis"
      (compute-chat-rate
       [{:sentFrom "Andjela" :sentTo "Katarina" :message "You did great!"}
        {:sentFrom "Andjela" :sentTo "Katarina" :message "Not bad."}
        {:sentFrom "Andjela" :sentTo "Katarina" :message "That was terrible."}]
       "Andjela"
       sentiment-analyzer)
      => 2)

