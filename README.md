# GamerChat - Tactical Chat and Sentiment Analysis for Gamers

Imagine a chat where every user is game lover, where every message holds insights into your teammates' intentions and plans. This app captures this potential by using sentiment analysis to delve into the tone, emotion, and strategy behind the words. Whether it’s building trust, deciphering tactics, or detecting hesitation, the app helps gamers understand more than what’s said— it reveals how it’s said and what it implies for gameplay.

## Why GamerChat
GamerChat is a chat application designed specifically for gamers to improve communication and strategy. It integrates analysis the content of messages to help teams understand the tone of their discussions and emotional meaning, and refine tactics in real time. Key thing in this app is *simplicity* - we all know how much gamers are impatient when playing.

## Video demo of basic usecases and description
In the video I made an overview of the app and its functionalities.

When you launch the application, it begins by displaying a prompt that invites you to enter a username. If the username is valid and available, the application establishes a WebSocket connection to enable real-time communication with the server. Simpliciy here is that there is no some special validation, it is checked if the username already exists, and if it doesn't - its being created. Only condition is that it's not empty.

Upon successful registration or login, the application transitions into a personalized dashboard. This dashboard serves as a central hub for all your chats. On the left, a sidebar lists all your ongoing conversations, showing the names of your chat partners. There is a 'Create new chat' button for strating chat with user that exists in system and with who you haven't interacted before.

The central panel of the dashboard is designed to display the messages of the currently selected chat. When you click on a chat partner, the panel updates to show a chronological view of the conversation. Messages are neatly labeled with the sender’s name and text. If the conversation grows lengthy, the panel allows scrolling to access older messages.

Above the conversation view, the application provides sentiment analytics. It calculates and displays the overall "chat rate," a measure of sentiment and tone of the conversation - from 1 (meaning bad tone) and 5 rating very positive tone of the chat. This chat rate updates as new messages are exchanged, offering a quantitative view of the interaction. 

For those who enjoy an element of strategy, the application introduces a unique feature: tactical suggestions. By analyzing messages from your chat partner, it generates insights or tactics that could guide your future responses. These tactics are based on frequency of messages sent with different message rates, and considers how many times are messages of specific tone sent, now just average chat rate. The sentiment analysis is done using the VADER library, lexicon and rule-based sentiment analysis tool that is specifically attuned to sentiments expressed in social media.

Sending a message is straightforward. A text field at the bottom of the panel lets you compose your message. Clicking the "Send" button instantly dispatches it to your chat partner while simultaneously updating the conversation view and recalculating the chat rate. The WebSocket connection ensures that messages appear in real-time on both ends.

Behind the scenes, the application relies on a MongoDB database to persistently store all user and chat data. It organizes this data into collections for users and chats, enabling efficient retrieval and analysis. The server-side logic ensures that the database is updated in real-time as messages are sent and received.

## Features
- **Chat Functionality**: Messaging between gamers.
- **Sentiment Analysis**: Analyze the tone of messages for strategic insights.
- **Tactical Feedback**: Detect trustworthiness, confidence, and emotions.
- **Chat Rate Review**: View sentiment score (chat rate) to improve future gameplay.
- **Negotiation Support**: Understand and improve in-game trades and alliances.

## Prerequisites for running
You will need Leiningen installed.

## Usage
To start the application, open the terminal and run: lein run. Since this is app made for chatting, you can open another terminal and run the same way so that chat among two users can be simulated. Each instance of the application acts as a separate user, allowing you to test chat features in real-time.
Also, before starting app, start MongoDB locally by running: mongod, since application is relying on MongoDB for storing data.

## Testing
Midje library was used for unit testing. Parts of the code that represent bussiness logic, but not direclty just communication with database, were tested.

## Benchmarking
Inspired by the benchmarking we did on classes, I wanted to compare two implementatons of the function that calculates chat rate, wanting to see which approach is better. Based on the benchmarking results, both implementations of `calculate-chat-rate` (using `reduce`/`map` and `loop/recur`) turned out to perform very similarly. However, the `reduce`/`map` version demonstrated slightly more stability in execution time, with fewer outliers and a smaller standard deviation, so it was used in this app.

## Conclusion
The goal of this project was to explore Clojure programming in the context of building a functional, real-time chat application with added value through sentiment analysis and tactical feedback for gamers. Throughout the process, I focused on integrating various libraries, such as Seesaw for the UI, Midje for testing, Criterium for benchmarking, to enhance the functionality of the chat app and to get better understanding of performance and decision making in software development. 

Along the way, I delved into areas such as WebSocket handling and database management, all while getting better acquainted with the nuances of Clojure.

This project has not only strengthened my understanding of Clojure but also expanded my knowledge in test driven development. Splitting some complex usecases into smaller ones, and then building fully functional application out of that has changed my way of thinking. Additionally, this project could be updated with many new features, and turned into cross-platform app.

## Sources
[1] https://clojure.org/

[2] Higginbotham, D. (2017). Clojure for the Brave and True. Independently published. ISBN: 978-0692645370.

[3] Hutto, C.J. & Gilbert, E.E. (2014). VADER: A Parsimonious Rule-based Model for Sentiment Analysis of Social Media Text. Eighth International Conference on Weblogs and Social Media (ICWSM-14). Ann Arbor, MI, June 2014. https://github.com/cjhutto/vaderSentiment#vader-sentiment-analysis

[4] https://clojars.org/

[5] https://github.com/clj-commons/seesaw?tab=readme-ov-file

[6] https://cljdoc.org/d/seesaw/seesaw/1.5.0/doc/readme

[7] https://github.com/marick/Midje

[8] https://github.com/hugoduncan/criterium

[9] https://docs.python.org/3/library/sys.html

[10] https://github.com/michaelklishin/monger