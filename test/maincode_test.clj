(ns maincode-test
  (:require [midje.sweet :refer :all]
            [maincode :refer :all]))

(fact "registers a new user"
      (register-user {} "Andjela") => {"Andjela" []})

(fact "registers multiple users"
      (-> {}
          (register-user "Andjela")
          (register-user "Katarina")) => {"Andjela" [] "Katarina" []})

(fact "does not allow duplicate usernames"
      (register-user {"Andjela" []} "Andjela") => {:error "Username already exists"})

(fact "ensures existing users are unchanged"
      (register-user {"Andjela" [] "Katarina" []} "Dusan")
      => {"Andjela" [] "Katarina" [] "Dusan" []})

