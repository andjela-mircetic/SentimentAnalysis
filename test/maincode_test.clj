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




