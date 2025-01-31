EliteBans

A powerful and customizable punishment system for Minecraft servers.

	⚠️ NOTE: Currently supports Minecraft 1.21+. Older version support will be added in the future.

Features

🔨 Core Punishment System
	•	Ban System
	•	Permanent bans: /ban <player> <reason>
	•	Temporary bans: /tban <player> <time> <reason> (e.g., 30m, 2h, 1d)
	•	Customizable ban messages and appeal info
	•	Mute System
	•	Permanent mutes: /mute <player> <reason>
	•	Temporary mutes: /tmute <player> <time> <reason>
	•	Prevents muted players from chatting
	•	Warning System
	•	Issue warnings: /warn <player> <reason>
	•	Stores all warnings in player history

📜 Punishment History Management
	•	View history in two ways:
	•	GUI-based: /histgui (interactive menu)
	•	Text-based: /hist
	•	Detailed punishment records including:
	•	Punishment type, reason, issuer
	•	Duration, expiration time
	•	Unban/unmute details

⚖️ Punishment Management
	•	Unban players: /unban <player> <reason>
	•	Unmute players: /unmute <player> <reason>
	•	Prune history: /prunehistory <player> <amount>
	•	Silent punishments (staff-only notifications)

🗄️ Dual Database Support
	•	MongoDB & MySQL support
	•	Automatic table/collection creation
	•	Efficient data management

🛠 Technical Features
	•	Automatic Expiration:
	•	Background task removes expired punishments
	•	Expired punishments are archived automatically
	•	Permission System:
	•	Granular permission controls
	•	Staff notification system
	•	Command-specific permissions
	•	UUID Support:
	•	Fully compatible with UUID-based player identification
	•	Offline player support with name-based fallback

💡 Quality of Life Improvements
	•	Debug Mode:
	•	Configurable debug logging for detailed error tracking
	•	Customization:
	•	Fully customizable messages (color code support)
	•	Configurable database settings
	•	GUI Enhancements:
	•	Unban/unmute players directly from the history GUI (right-click an active punishment)
	•	Rollback Punishments:
	•	Remove active/historical punishments within a set time range
	•	Example: /rollbackpunishments 1d all (removes all punishments from the past day)
	•	Supported types: tmute, mute, ban, tban, warn, all
	•	Discord Logging:
	•	Webhook integration for punishment logs (configure in config.yml)

📸 Showcase

https://github.com/user-attachments/assets/1abdb890-b9de-4e19-9d8e-ca7c47409897
https://github.com/user-attachments/assets/18091e45-c647-41cb-b509-0472aa9e31ba
https://github.com/user-attachments/assets/845d6a5a-7ba9-4390-8f00-5da59719096c
https://github.com/user-attachments/assets/5d67bc0e-a431-484f-910f-d29662c6249d
https://github.com/user-attachments/assets/87cebb77-505c-4b17-b5a3-28944f4a1b80
https://github.com/user-attachments/assets/72ec68c7-604f-4ed4-9e7a-91b04f6bfab3
https://github.com/user-attachments/assets/acf181fb-e378-4232-80b2-1d959acd4d17
https://github.com/user-attachments/assets/ac3f6219-7c7f-4b89-a292-6eb2cd0ed9a2

🚀 Future Plans
	•	Support for older Minecraft versions
	•	More punishment customization options
	•	Advanced reporting and analytics