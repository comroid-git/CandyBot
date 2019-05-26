### CandyBot is a levelling bot with candy!
CandyBot will sit in your server, counting messages. Every 100th message gets a point. Then he starts from zero. As simple as that, right?

## Invite Link
[Click here](https://discordapp.com/oauth2/authorize?client_id=487745829617139722&scope=bot&permissions=85056) to invite CandyBot to your discord guild

## Setting up CandyBot
CandyBot works right outside the box. However, there are some things that you can modify:

| Modification                             | Description                                                              | How to do                                                                           |
|------------------------------------------|--------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Changing the Emoji                       | The emoji that CandyBot uses for representing levels, default: 🍨         | Change the `candy.emoji` property: `candy!property candy.emoji <Emoji>`             |
| Changing the Message Limit               | The limit at which a new point is given, default: 100                    |  Change the `candy.limit` property: `candy!property candy.limit <Limit>`            |
| Defining a custom prefix for your Server | A custom command prefix that CandyBot will react to, only in your server |  Change the `bot.customprefix` property: `candy!property bot.customprefix <Prefix>` |

Note: To be able to use `candy!property`, you must be able to effectively use the `Manage Server` permission.

## All CandyBot commands
### Candy commands

| Command       | Description                                  | Notes |
|---------------|----------------------------------------------|-------|
| `candy!stats` | Shows server-wide Candy stats and highscores |       |
| `candy!own`   | Shows your own Candy score                   |       |

### Other commands

| Command                                        | Description                                                           | Notes                                             |
|------------------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------|
| `candy!help`                                   | Shows all available commands, or information about a specific command |                                                   |
| `candy!property [<Property Name> [New Value]]` | Setup command                                                         | Requires the `Manage Server` permission for usage |
| `candy!invite`                                 | Sends you an Invite-link for the bot via DM                           |                                                   |
| `candy!about`                                  | Basic information about the bot, useful links                         |                                                   |
| `candy!bug`                                    | Found a bug? Report it there!                                         |                                                   |
