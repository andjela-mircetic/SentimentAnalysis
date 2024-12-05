# vader_sentiment.py
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
import sys

analyzer = SentimentIntensityAnalyzer()

# Read input text from command line argument
text = sys.argv[1]

# Analyze sentiment
sentiment = analyzer.polarity_scores(text)
if sentiment['compound'] >= 0.05:
    print("Positive")
elif sentiment['compound'] <= -0.05:
    print("Negative")
else:
    print("Neutral")
