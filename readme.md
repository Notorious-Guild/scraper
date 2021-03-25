## Notorious

### See Customized Recruits
Using filters customize who you want to see and how often you want to keep track of their guild status.

### Get The Information You Care About
Immediately see the information that matters most to your recruitment needs.
![post image](https://i.imgur.com/5uiuBus.png)


### Configuration
The following information should help you to configure and start running your own player reports.

**JAR Environment Variables**

| Variable           | Explanation
|:------------------:|:---------------------------------------------------------------------------------------------------------------:
| LOG_LEVEL          | Level of logs you want to see outputted. Valid values are TRACE, DEBUG, INFO, WARN, ERROR.
| HTTP_TIMEOUT       | Amount of milliseconds the http client will wait to establish a connection e.g. 10000 for 10 seconds.
| FREQUENCY          | Amount of seconds between each run of the application.
| DB_URL             | The JDBC url for the database the application will use, includes ip/port, username/password, and current schema.
| DISCORD_WEBHOOK    | The webhook for the discord channel you wish to post in.
| WCL_SECRET         | Id and password used to connect to the Warcraftlogs API. Format is <id>:<password>
| BNET_SECRET        | Id and password used to connect to the Battle.net API. Format is <id>:<password>
| PERFORMANCE_FILTER | Minimum Warcraftlogs average performance for a player to be returned by the scraper.
| ITEM_LEVEL_FILTER  | Minimum item level that a player must have to be returned by the scraper.
| MIN_PROGRESS       | Minimum raid progress that a player must have to be returned by the scraper.
