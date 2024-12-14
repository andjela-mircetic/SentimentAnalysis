(ns maincode-test
  (:require [midje.sweet :refer :all]
            [maincode :refer :all]))

(fact "find received messages for a specific user"
      (find-received-messages
       [{:messages [{:sentFrom "Andjela" :message "Hello!"}
                    {:sentFrom "Katarina" :message "Hi!"}]}
        {:messages [{:sentFrom "Andjela" :message "How are you?"}]}]
       "Andjela")
      => [{:sentFrom "Andjela" :message "Hello!"}
          {:sentFrom "Andjela" :message "How are you?"}])

(fact "return empty list if no messages are sent by the user"
      (find-received-messages
       [{:messages [{:sentFrom "Katarina" :message "Hi!"}
                    {:sentFrom "Dusan" :message "Good morning!"}]}]
       "Andjela")
      => [])

(fact "return empty list when there are no chat logs"
      (find-received-messages [] "Andjela")
      => [])
