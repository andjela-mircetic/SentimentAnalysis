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

(fact "return all messages sent and received by a user"
      (get-all-messages-of-user
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
        {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}]
       "Andjela")
      => [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
          {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
          {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}])

(fact "return an empty list if the user has no messages"
      (get-all-messages-of-user
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}]
       "Dusan")
      => ())

(fact "return all messages between two users"
      (get-all-messages-between-two-users
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
        {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}]
       "Andjela" "Katarina")
      => [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
          {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}])

(fact "return no messages if users have not interacted"
      (get-all-messages-between-two-users
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}]
       "Andjela" "Dusan")
      => ())

(fact "handle messages between two users in any direction"
      (get-all-messages-between-two-users
       [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}
        {:sentFrom "Katarina" :sentTo "Andjela" :time "2024-11-18 12:05" :message "Hello!"}
        {:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}
        {:sentFrom "Andjela" :sentTo "Dusan" :time "2024-11-18 12:15" :message "Hey!"}]
       "Andjela" "Dusan")
      => [{:sentFrom "Dusan" :sentTo "Andjela" :time "2024-11-18 12:10" :message "Good day!"}
          {:sentFrom "Andjela" :sentTo "Dusan" :time "2024-11-18 12:15" :message "Hey!"}])

(fact "send a message when users exist"
      (send-message #{"Andjela" "Katarina"}
                    [] "Andjela" "Katarina" "2024-11-18 12:00" "Hi!")
      => [{:sentFrom "Andjela" :sentTo "Katarina" :time "2024-11-18 12:00" :message "Hi!"}])

(fact "return error when sender does not exist"
      (send-message #{"Andjela"} [] "Dusan" "Andjela" "2024-11-18 12:05" "Hello!")
      => {:error "Sender or recipient does not exist"})


