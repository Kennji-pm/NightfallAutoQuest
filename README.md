<div align="center">

# 🌙 NightfallAutoQuest 🌙

[![Spigot](https://img.shields.io/badge/Spigot-1.21-red.svg)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Version](https://img.shields.io/badge/Version-1.3.0-blue.svg)](https://github.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

### **Advanced Automatic Quest System for Minecraft**

*Automatic quest management with rich features and soft integration with economy plugins*

[📥 Download](https://github.com/Kennji-pm/releases) • [📚 Wiki](https://github.com/Kennji-pm/wiki) • [🐛 Issues](https://github.com/Kennji-pm/NightfallAutoQuest/issues) • [💬 Discord](https://discord.gg/paxFuNpXvb)

---

</div>

## ✨ Features

### 🎯 **Core Features**
- 🔄 **Automatic Quest Assignment** - Random quests assigned at configurable intervals
- 🏆 **Rich Quest System** - 11 different quest types with customizable rewards
- 🎮 **Integrated GUI** - BossBar progress display with real-time updates
- 🔊 **Sound Effects** - Configurable audio feedback for all quest events
- 🗺️ **World Restrictions** - Quest activity limited to specific worlds
- ⚡ **Performance Optimized** - Caching system with automatic save intervals

### 🛠️ **Quest Types**
| Type | Description | Configuration |
|------|-------------|---------------|
| 🏗️ **Mining** | Break specific blocks | Multiple block types |
| 🧱 **Placing** | Place specific blocks | Multiple block types |
| ⚒️ **Crafting** | Craft specific items | Multiple item types |
| 🌾 **Farming** | Harvest crops | Crop types with growing checks |
| 🎣 **Fishing** | Catch fish | Specific fish items |
| ⚔️ **Damage** | Deal damage to entities | Entity types and damage amounts |
| ✦ **Enchanting** | Enchant items | Multiple enchantment types |
| 🐲 **Mob Killing** | Kill specific mobs | Entity types and counts |
| 🚶 **Walking** | Walk certain distance | Block distance goals |
| 🔥 **Smelting** | Smelt items in furnace | Multiple smeltable items |
| ⭐ **Placeholder** | Custom plugin integration | PlaceholderAPI integration |

### 📊 **Player Statistics**
- ✅ **Quest Completion Tracking** - Detailed statistics for each player
- 🏅 **Leaderboards** - Top quest completers with pagination
- 📈 **Success Rates** - Completion rate calculations
- 🔄 **Active Quest Monitor** - Current progress and time remaining

### 🔧 **Administration**
- ⚙️ **Module Management** - Enable/disable quest types individually
- 📋 **Data Management** - View player data and purge databases
- 🔄 **Hot Reloading** - Reload quests and configuration without restart
- 🗃️ **Database Support** - H2 (file-based) or MySQL databases

## 🚀 Installation

### Requirements
- **Minecraft Server**: Spigot/Paper 1.21+
- **Java**: Version 21 or higher
- **Optional**: PlaceholderAPI for enhanced placeholders

### Installation Steps

1. **Download the plugin from [here](https://github.com/Kennji-pm/releases)**

2. **Place in plugins folder**

3. **Start your server**

4. **Configure** (see Configuration section below)

5. **Add quests** to `plugins/NightfallAutoQuest/quests/` folder

## 📋 Configuration

### config.yml
```yaml
# Plugin settings
prefix: "&6&lNightfall&8AutoQuest &7» &r"  # Message prefix
config-version: 1.5  # Do not change

# Quest assignment settings
quest:
  assign-interval: 120    # Seconds between assignment checks
  assign-percentage: 50.0 # % of players to assign quests to

# World restrictions
allowed_worlds:  # Leave empty for all worlds
  - world
  - world_nether
  - world_the_end

# Database configuration
database:
  type: "h2"  # or "mysql"
  mysql:      # Only if type is "mysql"
    host: "localhost"
    port: 3306
    database: "dailyquests"
    username: "root"
    password: ""

# BossBar settings
bossbar:
  enabled: true
  color: "BLUE"      # BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW
  style: "SOLID"     # SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20

# Sounds (true/false to enable/disable)
sounds:
  enabled: true
  assign:
    sound: "ENTITY_PLAYER_LEVELUP"
    volume: 1.0
    pitch: 1.0
  complete:
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 1.0
    pitch: 1.0
  # ... etc

# Module settings
modules:
  mining: true
  crafting: true
  # Enable/disable each quest type
```

### messages.yml
Customize all plugin messages, including colors and formatting.

## 🎯 Quest Creation

### Quest File Structure
Create `.yml` files in `plugins/NightfallAutoQuest/quests/`

### Basic Quest Template
```yaml
display:
  name: "&aYour Quest Name"
  description: "Quest description here!"

quests:
  type: "quest_type"  # See quest types above
  min-amount: 5       # Minimum goal amount
  max-amount: 20      # Maximum goal amount
  min-time: 3         # Minimum time in minutes
  max-time: 10        # Maximum time in minutes
  # Additional type-specific config below...

rewards:
  commands:
    - "give %player% diamond 5"
    - "eco give %player% 100"
```

### Quest Examples

#### 🔫 Zombie Killing Quest
```yaml
display:
  name: "Zombie Killing Quest"
  description: "Kill zombies to complete this quest!"
quests:
  type: "mobkilling"
  min-amount: 5
  max-amount: 20
  min-time: 3
  max-time: 5
  entities:
    - zombie
rewards:
  commands:
    - "give %player% rotten_flesh 10"
    - "eco give %player% 100"
```

#### 💰 Vault Balance Quest (PlaceholderAPI)
```yaml
display:
  name: "Vault Balance Quest"
  description: "Achieve a target balance in your economy account!"
quests:
  type: "placeholder"
  placeholders:
    values:
      - "vault_eco_balance_fixed"
  min-amount: 100
  max-amount: 1000
  min-time: 5
  max-time: 10
rewards:
  commands:
    - "give %player% diamond 5"
```

#### 🎣 Fishing Quest
```yaml
display:
  name: "&aFishing Quest"
  description: "Catch some fish to complete this quest!"
quests:
  type: "fishing"
  min-amount: 5
  max-amount: 20
  min-time: 3
  max-time: 5
  items:
    - cod
    - salmon
    - tropical_fish
    - pufferfish
rewards:
  commands:
    - "give %player% fishing_rod 1"
    - "eco give %player% 75"
```

### Quest Types Configuration

#### 🏗️ Mining/Placing/Crafting/Smelting
```yaml
items:
  - stone
  - cobblestone
  - dirt
```

#### 🌾 Farming
```yaml
items:
  - wheat
  - carrot
  - potato
  - beetroot
```

#### ⚔️ Deal Damage
```yaml
entities:
  - zombie
  - skeleton
damage: 100  # Damage amount needed
```

#### ✦ Enchanting
```yaml
enchantments:
  - sharpness
  - protection
  - efficiency
```

#### 🚶 Walking
```yaml
distance: 1000  # Blocks to walk
```

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/naq help` | `nightfallautoquest.use` | Show help menu |
| `/naq quest` | `nightfallautoquest.use` | View current quest |
| `/naq stats` | `nightfallautoquest.stats` | View personal statistics |
| `/naq top [page]` | `nightfallautoquest.top` | View leaderboards |
| `/naq giveup` | `nightfallautoquest.giveup` | Abandon current quest |
| `/naq reload` | `nightfallautoquest.admin` | Reload configuration |
| `/naq reloadquests` | `nightfallautoquest.admin` | Reload quest files |
| `/naq module <list/enable/disable>` | `nightfallautoquest.admin` | Manage quest modules |
| `/naq purge confirm` | `nightfallautoquest.admin` | Delete all quest data |
| `/naq getdata <player>` | `nightfallautoquest.admin` | View player data |

### Command Aliases
- Main: `/nightfallautoquest`, `/nq`, `/autoquest`
- Help: `/naq h`
- Stats: `/naq st`, `/naq statistics`
- Top: `/naq leaderboard`, `/naq lb`

## 📊 PlaceholderAPI Integration

### Player Placeholders
```
%nautoquest_completed%     - Total completed quests
%nautoquest_failed%        - Total failed quests
%nautoquest_completion_rate% - Success rate percentage
%nautoquest_current%       - Current quest name
%nautoquest_progress%      - Current progress (X/Y)
%nautoquest_time_remaining% - Time left (HH:MM:SS)
```

### Leaderboard Placeholders
```
%nautoquest_total_players% - Total players with stats
%nautoquest_total_pages%   - Total leaderboard pages
%nautoquest_top_name_X%    - Player name at position X
%nautoquest_top_quest_X%   - Quest completions at position X
%nautoquest_top_rate_X%    - Completion rate at position X
```

## 🚀 Building from Source

### Prerequisites
- **Maven 3.6+**
- **Java 21 JDK**

### Build Steps
```bash
# Clone the repository
git clone https://github.com/Kennji-pm/NightfallAutoQuest.git
cd NightfallAutoQuest

# Compile and package
mvn clean package

# Find the jar in target/ folder
ls -la target/NightfallAutoQuest-*.jar
```

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **_kennji** - Plugin author and maintainer
- **SpigotMC Community** - Plugin development resources
- **Paper/Spigot Teams** - Excellent server software

---

**Made with ❤️ for the Minecraft community**

*Report bugs or request features in our [Issues](https://github.com/Kennji-pm/NightfallAutoQuest/issues) section!*
