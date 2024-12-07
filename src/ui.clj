(ns ui
  (:require [seesaw.core :as seesaw]
            [maincode :as maincode]
            [gniazdo.core :as ws]
            [cheshire.core :as json]))

(def window-width 640)
(def window-height 640)

(defonce ws-connection (atom nil))

(defn handle-incoming-message [message]
  "Handle incoming WebSocket messages."
  (let [data (json/decode message true)]
    (println "Received WebSocket message:" data) 
    ))

(defn start-websocket-client []
  (let [url "ws://localhost:8080"
        connection (ws/connect url
                               :on-receive handle-incoming-message
                               :on-error (fn [error]
                                           (println "WebSocket error:" error))
                               :on-close (fn [status]
                                           (println "WebSocket closed:" status)))]
    (reset! ws-connection connection)
    (println "Connected to WebSocket server at" url)))

(defn send-message-via-websocket [message]
  (if-let [conn @ws-connection]
    (ws/send-msg conn (json/encode message))
    (println "WebSocket is not connected.")))

(defn show-greeting [username]
  "Displays a dashboard for user's chats."
  (let [chat-data (maincode/get-chat-partners-with-last-message username)
        chat-list (map (fn [{:keys [partner lastMessage]}]
                         (seesaw/vertical-panel
                          :items [(seesaw/label (str "Chat with: " partner))
                                  (seesaw/label (str "Last message: " (:message lastMessage)))
                                  (seesaw/label (str "-----------------------"))]))
                       chat-data)
        frame (seesaw/frame :title (str "Dashboard - " username)
                            :content (seesaw/border-panel
                                      :west (seesaw/scrollable
                                             (seesaw/vertical-panel :items chat-list))
                                      :center (seesaw/label "Chat details will appear here."))
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
                                                                  (seesaw/alert "Error" (:error response))
                                                                  (do
                                                                    (start-websocket-client)
                                                                    (show-greeting username)))))
                                                            (seesaw/alert "Input Error" "Username cannot be empty"))))])]
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
