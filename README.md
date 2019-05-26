### DangoBot is a levelling bot with candy!
DangoBot will sit in your server, counting messages. Every 100th message gets a point. Then he starts from zero. As simple as that, right?

## Invite Link
[Click here](https://discordapp.com/oauth2/authorize?client_id=487745829617139722&scope=bot&permissions=85056) to invite DangoBot to your discord guild

## Setting up DangoBot
DangoBot works right outside the box. However, there are some things that you can modify:

| Modification                             | Description                                                              | How to do                                                                           |
|------------------------------------------|--------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Changing the Emoji                       | The emoji that DangoBot uses for representing levels, default: 🍡         | Change the `dango.emoji` property: `dango!property dango.emoji <Emoji>`             |
| Changing the Message Limit               | The limit at which a new point is given, default: 100                    |  Change the `dango.limit` property: `dango!property dango.limit <Limit>`            |
| Defining a custom prefix for your Server | A custom command prefix that DangoBot will react to, only in your server |  Change the `bot.customprefix` property: `dango!property bot.customprefix <Prefix>` |

Note: To be able to use `dango!property`, you must be able to effectively use the `Manage Server` permission.

## All DangoBot commands
### Dango commands

| Command       | Description                                  | Notes |
|---------------|----------------------------------------------|-------|
| `dango!stats` | Shows server-wide Dango stats and highscores |       |
| `dango!own`   | Shows your own Dango score                   |       |

### Other commands

| Command                                        | Description                                                           | Notes                                             |
|------------------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------|
| `dango!help`                                   | Shows all available commands, or information about a specific command |                                                   |
| `dango!property [<Property Name> [New Value]]` | Setup command                                                         | Requires the `Manage Server` permission for usage |
| `dango!invite`                                 | Sends you an Invite-link for the bot via DM                           |                                                   |
| `dango!about`                                  | Basic information about the bot, useful links                         |                                                   |
| `dango!bug`                                    | Found a bug? Report it there!                                         |                                                   |
