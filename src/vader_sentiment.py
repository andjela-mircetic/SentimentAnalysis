from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
import sys

analyzer = SentimentIntensityAnalyzer()

# Read input text from command line argument
text = sys.argv[1]

sentiment = analyzer.polarity_scores(text)

print(sentiment['compound'])
