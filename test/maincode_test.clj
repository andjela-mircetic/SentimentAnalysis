(ns maincode-test
  (:require [midje.sweet :refer :all]
            [maincode :refer :all]))

(fact "registers a new user"
      (register-user {} "Alice") => {"Alice" []})

(fact "registers multiple users"
      (-> {}
          (register-user "Alice")
          (register-user "Bob")) => {"Alice" [] "Bob" []})

(fact "does not allow duplicate usernames"
      (register-user {"Alice" []} "Alice") => {:error "Username already exists"})

(fact "ensures existing users are unchanged"
      (register-user {"Alice" [] "Bob" []} "Charlie")
      => {"Alice" [] "Bob" [] "Charlie" []})

