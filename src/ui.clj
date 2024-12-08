(ns ui
  (:require [seesaw.core :as seesaw]
            [maincode :as maincode]
            [gniazdo.core :as ws]
            [cheshire.core :as json]))

(def window-width 640)
(def window-height 640)

(defonce ws-connection (atom nil))
(defonce current-chat (atom nil))
(defonce logged-in-username (atom nil))


(defn handle-incoming-message [message]
  "Handle incoming WebSocket messages."
  (let [data (json/decode message true)
        sender (get data "sentFrom")  
        message-text (get data "message")]
    (println "Received WebSocket message:" data)
    (when @current-chat
      (swap! current-chat update :messages conj {:sentFrom sender :message message-text}))))  ;; Append to the end of the list


  (defn start-websocket-client []
    "Starts a WebSocket client to communicate with the server."
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
    "Sends a message via WebSocket."
    (if-let [conn @ws-connection]
      (ws/send-msg conn (json/encode message))
      (println "WebSocket is not connected.")))

  (defn update-chat-panel [chat central-panel]
    "Updates the central panel with chat messages and input field."
    (let [sorted-messages (sort-by :time (:messages chat))  
          message-list (map (fn [{:keys [sentFrom message]}]
                              (seesaw/label :text (str sentFrom ": " message)))
                            sorted-messages)
          chat-panel (seesaw/vertical-panel :items message-list)
          message-field (seesaw/text :columns 30 :id :message-field)
          send-button (seesaw/button :text "Send"
                                     :listen [:action (fn [e]
                                                        (let [msg (seesaw/text message-field)]
                                                          (send-message-via-websocket {:type "message"
                                                                                       :sentFrom @logged-in-username
                                                                                       :sentTo (:partner chat)
                                                                                       :message msg})
                                                          (swap! current-chat update :messages conj {:sentFrom @logged-in-username :message msg})

                                                          (seesaw/text! message-field "")
                                                        
                                                          (update-chat-panel @current-chat central-panel)))])

          new-content (seesaw/border-panel
                       :center chat-panel
                       :south (seesaw/horizontal-panel
                               :items [message-field send-button]))] 
      (seesaw/config! central-panel :items [new-content])
      (seesaw/repaint! central-panel))) 

  (defn show-greeting [username]
    "Displays a dashboard for user's chats."
    (reset! logged-in-username username) 
    (let [chat-data (maincode/get-chat-partners-with-last-message username)
          central-panel (seesaw/vertical-panel) 
          chat-list (map (fn [{:keys [partner lastMessage]}]
                           (seesaw/button :text (str "Chat with: " partner)
                                          :listen [:action
                                                   (fn [e]
                                                     (reset! current-chat {:partner partner
                                                                           :messages (maincode/get-all-messages-between-two-users username partner)})
                                                   
                                                     (update-chat-panel @current-chat central-panel))]))
                         chat-data)
          frame (seesaw/frame :title (str "Dashboard - " username)
                              :content (seesaw/border-panel
                                        :west (seesaw/scrollable
                                               (seesaw/vertical-panel :items chat-list))
                                        :center central-panel) 
                              :size [window-width :by window-height]
                              :on-close :exit)]
      (seesaw/show! frame)))

  (defn prompt-for-username []
    "Asks the user to enter a username and calls the register-user function."
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
                                                              (seesaw/alert "Input Error" "Username cannot be empty"))))])
          frame (seesaw/frame :title "Enter your username"
                              :content (seesaw/vertical-panel
                                        :items [(seesaw/label "Enter your username:")
                                                username-field
                                                submit-button])
                              :size [640 :by 480]
                              :on-close :exit)]
      (seesaw/show! frame)))

  (defn -main []
    "The main entry point of the app."
    (prompt-for-username))