(ns ui
  (:require [seesaw.core :as seesaw]
            [maincode :as maincode]
            [chatsanalyzer :as chatsanalyzer]
            [gniazdo.core :as ws]
            [clojure.string :as str] 
            [cheshire.core :as json]))

(def window-width 640)
(def window-height 640)

(defonce ws-connection (atom nil))
(defonce current-chat (atom nil))
(defonce logged-in-username (atom nil))
(defonce app-frame (atom nil))

(defn send-message-via-websocket [message]
    "Sends a message via WebSocket."
    (if-let [conn @ws-connection]
      (ws/send-msg conn (json/encode message))
      (println "WebSocket is not connected.")))
  
(defn show-tactics []
    "Analyzes the received messages and generates gaming tactics."
    (if-let [chat @current-chat]
      (let [logged-in-user @logged-in-username
            opponent (:partner chat)
            chat-logs (maincode/get-all-messages-between-two-users logged-in-user opponent)
            received-messages (maincode/find-received-messages [chat-logs] opponent)
            tactics-map (chatsanalyzer/generate-gaming-tactics received-messages)
            tactics (get tactics-map :tactics)]
        (if (seq tactics)
          (seesaw/alert "Tactics" (clojure.string/join "\n" tactics))
          (seesaw/alert "Tactics" "No specific tactics to suggest at the moment.")))
      (seesaw/alert "Error" "No active chat selected.")))

(defn update-chat-panel [chat central-panel]
  "Updates the central panel with chat details."
  (if chat
    (let [sorted-messages (sort-by :time (:messages chat)) 
          message-list (map (fn [{:keys [sentFrom message]}]
                              (seesaw/label :text (str sentFrom ": " message)))
                            sorted-messages)
          chat-panel (seesaw/vertical-panel :items message-list) 
          scrollable-chat-panel (seesaw/scrollable chat-panel) 
          chat-rate-label (seesaw/label :text (str "<html><b><font color='red'>Chat rate: "
                                                   (if-let [rate (:chatRate chat)]
                                                     (format "%.2f" (double rate))
                                                     "0.00")
                                                   "</font></b></html>"))
          tactics-button (seesaw/button :text "Tell me some tactics"
                                        :listen [:action
                                                 (fn [_] (show-tactics))]) 
          top-panel (seesaw/horizontal-panel :items [chat-rate-label tactics-button]) 
          message-field (seesaw/text :columns 30 :id :message-field)
          send-button (seesaw/button :text "Send"
                                     :listen [:action
                                              (fn [_]
                                                (let [msg (seesaw/text message-field)]
                                                  (send-message-via-websocket {:type "message"
                                                                               :sentFrom @logged-in-username
                                                                               :sentTo (:partner chat)
                                                                               :message msg})
                                                  (swap! current-chat update :messages conj {:sentFrom @logged-in-username :message msg})
                                                  (seesaw/text! message-field "")
                                                  (update-chat-panel @current-chat central-panel)))])
          
          bottom-panel (seesaw/horizontal-panel :items [message-field send-button])
          new-content (seesaw/border-panel
                       :north top-panel
                       :center scrollable-chat-panel
                       :south bottom-panel)] 
      (seesaw/config! central-panel :items [new-content])
      (seesaw/repaint! central-panel))
    (do
      (seesaw/config! central-panel :items [(seesaw/label :text "Detailed messages will appear here." :foreground :gray)])
      (seesaw/repaint! central-panel))))


(defn refresh-chat-list []
  "Refreshes the chat list when no chats are available."
  (let [chat-data (maincode/get-chat-partners-with-last-message @logged-in-username)
        chat-list (if (empty? chat-data)
                    [(seesaw/label :text "Chats will appear here." :foreground :gray)]
                    (map (fn [{:keys [partner lastMessage]}]
                           (seesaw/button :text (str "Chat with: " partner)
                                          :listen [:action
                                                   (fn [e]
                                                     (reset! current-chat {:partner partner
                                                                           :messages (:messages (maincode/get-all-messages-between-two-users @logged-in-username partner))
                                                                           :chatRate (:chatRate (maincode/get-all-messages-between-two-users @logged-in-username partner))})
                                                     (update-chat-panel @current-chat (seesaw/select @app-frame [:#central-panel])))]))
                         chat-data))
        chat-sidebar (seesaw/select @app-frame [:#chat-sidebar])]
    (seesaw/config! chat-sidebar :items chat-list)
    (seesaw/repaint! chat-sidebar)))

  (defn handle-incoming-message [message]
    "Handle incoming WebSocket messages."
    (let [data (json/decode message true)
          sender (get data "sentFrom")
          message-text (get data "message")]
      (println "Received WebSocket message:" data)
      (when @current-chat
        (when (= sender (:partner @current-chat))
          (swap! current-chat update :messages conj {:sentFrom sender :message message-text}) 
          (update-chat-panel @current-chat (seesaw/select @app-frame [:#central-panel]))))
      (refresh-chat-list)))
  
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
  
 (defn create-chat-popup []
   "Displays a popup for creating a new chat."
   (let [username-field (seesaw/text :columns 20)
         popup (atom nil)
         confirm-button (seesaw/button :text "Confirm"
                                       :listen [:action
                                                (fn [e]
                                                  (let [recipient-username (seesaw/text username-field)]
                                                    (if (clojure.string/blank? recipient-username)
                                                      (seesaw/alert "Input Error" "Please enter a valid username.")
                                                      (let [response (maincode/create-chat @logged-in-username recipient-username)]
                                                        (if (:error response)
                                                          (seesaw/alert "Error" (:error response))
                                                          (do
                                                            (refresh-chat-list)
                                                            (seesaw/dispose! @popup)))))))])
         close-button (seesaw/button :text "Close"
                                     :listen [:action
                                              (fn [e]
                                                (seesaw/dispose! @popup))])
         popup-frame (seesaw/frame :title "Create New Chat"
                                   :content (seesaw/vertical-panel
                                             :items [(seesaw/label "Who do you want to message?")
                                                     username-field
                                                     (seesaw/horizontal-panel
                                                      :items [confirm-button close-button])])
                                   :resizable? false
                                   :size [300 :by 150])]
     (reset! popup popup-frame) 
     (seesaw/show! popup-frame)))

(defn show-greeting [username]
  "Displays a dashboard for user's chats."
  (reset! logged-in-username username)
  (let [chat-data (maincode/get-chat-partners-with-last-message username)
        central-panel (seesaw/vertical-panel :id :central-panel)
        create-chat-button (seesaw/button :text "Create New Chat"
                                          :listen [:action (fn [_] (create-chat-popup))])
        chat-list (if (empty? chat-data)
                    [(seesaw/label :text "Chats will appear here." :foreground :gray)]
                    (map (fn [{:keys [partner lastMessage]}]
                           (seesaw/button :text (str "Chat with: " partner)
                                          :listen [:action
                                                   (fn [e]
                                                     (reset! current-chat {:partner partner
                                                                           :messages (:messages (maincode/get-all-messages-between-two-users @logged-in-username partner))
                                                                           :chatRate (:chatRate (maincode/get-all-messages-between-two-users @logged-in-username partner))})
                                                     (update-chat-panel @current-chat central-panel))]))
                         chat-data))
        frame (seesaw/frame :title (str "Dashboard - " username)
                            :content (seesaw/border-panel
                                      :north create-chat-button
                                      :west (seesaw/scrollable
                                             (seesaw/vertical-panel :id :chat-sidebar
                                                                    :items chat-list))
                                      :center central-panel)
                            :size [window-width :by window-height]
                            :on-close :exit)]
    (reset! app-frame frame)
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
     "The main entry point of the app, starts server and then UI." 
     (future
       (maincode/start-websocket-server))
     (prompt-for-username))
