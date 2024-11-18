(ns maincode-test
  (:require [midje.sweet :refer :all]
            [maincode :refer :all]))

(fact "register a new user"
      (register-user #{} "Andjela") => #{"Andjela"})

(fact "register multiple users"
      (-> #{}
          (register-user "Andjela")
          (register-user "Katarina")) => #{"Andjela" "Katarina"})

(fact "test if duplicate usernames are allowed"
      (register-user #{"Andjela"} "Andjela") => {:error "Username already exists"})

(fact "test if existing users are unchanged"
      (register-user #{"Andjela" "Katarina"} "Dusan")
      => #{"Andjela" "Katarina" "Dusan"})

(ns maincode-test
  (:require [midje.sweet :refer :all]
            [maincode :refer :all]))

(fact "returns all messages sent and received by a user"
      (getAllMessagesByUser
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
        {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}]
       "Andjela")
      => [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
          {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
          {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}])

(fact "returns an empty list if the user has no messages"
      (getAllMessagesByUser
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}]
       "Dusan")
      => ())

(fact "returns all messages between two users"
      (getMessagesBetween
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
        {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}]
       "Andjela" "Katarina")
      => [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
          {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}])

(fact "returns no messages if users have not interacted"
      (getMessagesBetween
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}]
       "Andjela" "Dusan")
      => ())

(fact "handles messages between two users in any direction"
      (getMessagesBetween
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
        {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}
        {:sentFrom "Andjela" :sentTo "Dusan" :time "2024-11-18 12:15" :message "Hey!"}]
       "Andjela" "Dusan")
      => [{:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}
          {:sentFrom "Andjela" :sentTo "Dusan" :time "2024-11-18 12:15" :message "Hey!"}])

(fact "sends a message when users exist"
      (sendMessage #{"Andjela" "Katarina"}
                    [] "Andjela" "Katarina" "2024-11-18 12:00" "Hi!")
      => [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}])

(fact "returns error when sender does not exist"
      (sendMessage #{"Andjela"} [] "Dusan" "Andjela" "2024-11-18 12:05" "Hello!")
      => {:error "Sender or recipient does not exist"})


