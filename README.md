**EliteBans**
![elitebans](https://github.com/user-attachments/assets/f31e52fd-0982-48df-b914-936bc7bd0faa)

NOTE: CURRENTLY ONLY MADE FOR 1.21 AND HIGHER, WILL BE ADDING ALL VERSION SUPPORT IN THE FUTURE.
**Core Punishment Features** 

Ban System:
Permanent bans (/ban). Usage: /ban <player> <reason>
Temporary bans (/tban) Usage: /tban <player> <time> <reason> (e.g., 30m, 2h, 1d)
Customizable ban messages and appeal information.

Mute System:
Permanent mutes (/mute) Usage: /mute <player> <reason>
Temporary mutes (/tmute) Usage: /tmute <player> <time> <reason> (e.g., 30m, 2h, 1d)
Chat blocking for muted players

Warning System:
Issue warnings to players (/warn) Usage: /warn <player> <reason>
Warnings are stored in history


**History Management**

Dual History View Options (configurable in config.yml)
GUI-based history (/histgui) with interactive menu
Text-based history (/hist)
Shows active and historical punishments
Detailed punishment information including:
Type of punishment
Reason
Issuer
Duration
Expiration time
Unban/unmute details


**Punishment Management**

Unban command (/unban) Usage: /unban <player> <reason>
Unmute command (/unmute) Usage: /unmute <player> <reason>
History pruning (/prunehistory) Usage: /prunehistory <player> <amount>
Silent punishment notifications (staff-only visibility)

**Dual Database System**
MongoDB support
MySQL support
Automatic table/collection creation
Efficient data management

**Technical Features**

Automatic Expiration:
Background task for removing expired punishments
Automatic archiving of expired punishments

Permission System:
Granular permission controls
Staff notification system
Command-specific permissions

UUID Support:
Full UUID compatibility
Offline player support
Name-based fallback system

**Quality of Life Features**

Debug Mode:
Configurable debug logging
Detailed error tracking

Customization:
Fully customizable messages
Color code support
Configurable database settings

GUI:
Unmuting/Unbanning straight from the gui, by right clicking an active punishment.

Rollbackpunishments:
Types: tmute,mute,ban,tban,warn or all.
Example: /rollbackpunishments 1d all
Note: Rollbackpunishments removes all active and historical punishments within a set time period.

Discord Logging:
Intergration for discord webhook logging for commands check config.yml.


Showcase: 

![image](https://github.com/user-attachments/assets/1abdb890-b9de-4e19-9d8e-ca7c47409897)
![image](https://github.com/user-attachments/assets/18091e45-c647-41cb-b509-0472aa9e31ba)
![image](https://github.com/user-attachments/assets/845d6a5a-7ba9-4390-8f00-5da59719096c)
![image](https://github.com/user-attachments/assets/5d67bc0e-a431-484f-910f-d29662c6249d)
![image](https://github.com/user-attachments/assets/87cebb77-505c-4b17-b5a3-28944f4a1b80)
![image](https://github.com/user-attachments/assets/72ec68c7-604f-4ed4-9e7a-91b04f6bfab3)
![image](https://github.com/user-attachments/assets/acf181fb-e378-4232-80b2-1d959acd4d17)
![image](https://github.com/user-attachments/assets/ac3f6219-7c7f-4b89-a292-6eb2cd0ed9a2)




