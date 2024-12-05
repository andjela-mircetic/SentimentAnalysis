(ns ui
  (:require [seesaw.core :as seesaw]
            [maincode :as maincode]))

(def window-width 640)
(def window-height 640)

(defn show-greeting [username]
  "Displays a dashboard for user's chats."
  (let [frame (seesaw/frame :title "Dashboard"
                            :content (seesaw/label (str "Hello, " username "!"))
                            :size [window-width :by window-height]
                            :on-close :exit)]
    (seesaw/show! frame)))

(defn prompt-for-username []
  "Asks user to enter a username and calls the register-user function."
  (let [username-field (seesaw/text :columns 20) 
        submit-button (seesaw/button :text "Register"
                                     :listen [:action (fn [e]
                                                        (let [username (seesaw/text username-field)]
                                                          (println "Username entered:" username) 
                                                          (if (not (clojure.string/blank? username))
                                                            (do
                                                              (println "Attempting to register user...")
                                                              (let [response (maincode/register-user username)]
                                                                (println "Response from register-user:" response)
                                                                (if (:error response)
                                                                  (seesaw/alert :title "Error" :message (:error response))
                                                                  (show-greeting username))))
                                                            (seesaw/alert :title "Input Error" :message "Username cannot be empty"))))])]
    (let [frame (seesaw/frame :title "Enter your username"
                              :content (seesaw/vertical-panel
                                        :items [(seesaw/label "Enter your username:") 
                                                username-field
                                                submit-button]) 
                              :size [640 :by 480] 
                              :on-close :exit)]
      (seesaw/show! frame))))

(defn -main []
  "The main entry point of the app."
  (prompt-for-username))
