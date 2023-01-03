## Performance and Scalability

These are the average Firebase hosting costs calculated using the Firebase Database Profiling tool. Firebase charge 1 USD per GB of data downloaded (excluding images or files). We've tabulated a few common operations and how many of them can be performed per 1 USD of monthly hosting cost:

- Messages Received (1kb) = 1,000,000
- Logins (10kb) = 100,000
- Profile Update (0.2kb) = 5,000,000
- User search (2kb) = 500,000 

What's possible on the Firebase free plan (10GB / month):

**500k logins, 5 million messages.** 

What's possible on the Flame plan (20GB / month / 20 USD):

**1 million logins, 10 million messages.**

The real-time database will support up to **100k concurrent users**. From our experience, 1 concurrent connection is enough to support 10 users per minute. That means that at peak capacity, the Chat SDK could support **1 million users per minute** and well over **20 million monthly users**. 