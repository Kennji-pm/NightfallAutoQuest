# NightfallAutoQuest PlaceholderAPI Expansion

This document outlines the available placeholders for the NightfallAutoQuest plugin's PlaceholderAPI expansion.

## Plugin Information

*   **Identifier**: `nautoquest`
*   **Author**: `_kennji`
*   **Version**: (Dynamically obtained from the plugin's description)

## Available Placeholders

The following placeholders can be used with the `nautoquest` identifier:

### Player-Specific Placeholders

These placeholders require a player context.

*   `%nautoquest_completed%`
    *   Returns the number of quests completed by the player.
    *   Example: `5`
*   `%nautoquest_failed%`
    *   Returns the number of quests failed by the player.
    *   Example: `2`
*   `%nautoquest_completion_rate%`
    *   Returns the completion rate of the player's quests (completed / (completed + failed)).
    *   Example: `71.43%`
*   `%nautoquest_current%`
    *   Returns the name of the player's currently active quest.
    *   Example: `Kill_Zombies` or `None` if no active quest.
*   `%nautoquest_progress%`
    *   Returns the current progress of the player's active quest in the format `current_amount/total_amount`.
    *   Example: `3/10` or `-/-` if no active quest.
*   `%nautoquest_time_remaining%`
    *   Returns the time remaining for the player's active quest in `HH:MM:SS` format.
    *   Example: `01:30:15` or `00:00:00` if no active quest or no expiration.

### Global Placeholders

These placeholders do not require a player context.

*   `%nautoquest_total_players%`
    *   Returns the total number of players with quest completions.
    *   Example: `150`
*   `%nautoquest_total_pages%`
    *   Returns the total number of pages for the top players list, based on 10 players per page.
    *   Example: `15`

### Top Players Placeholders

These placeholders are used to display information about players in the top rankings. The `slot` refers to the overall rank (e.g., 1 for the top player, 11 for the first player on the second page).

*   `%nautoquest_top_name_[slot]%`
    *   Returns the name of the player at the specified rank slot.
    *   Example: `%nautoquest_top_name_1%` might return `Player1`.
*   `%nautoquest_top_quest_[slot]%`
    *   Returns the number of completed quests for the player at the specified rank slot.
    *   Example: `%nautoquest_top_quest_1%` might return `100`.
*   `%nautoquest_top_rate_[slot]%`
    *   Returns the completion rate for the player at the specified rank slot.
    *   Example: `%nautoquest_top_rate_1%` might return `95.50%`.
