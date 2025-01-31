Here’s a cleaner and more structured version of your README for EliteBans:

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

🚀 Future Plans
	•	Support for older Minecraft versions
	•	More punishment customization options
	•	Advanced reporting and analytics

This version improves readability with clear sections, proper spacing, and icons for better structure. Let me know if you want any further tweaks!