package org.kennji.nightfallAutoQuest.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

public class SoundUtil {
    private final NightfallAutoQuest plugin;

    public SoundUtil(NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void playSound(Player player, String soundKey) {
        if (!plugin.getConfigManager().getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }

        String soundName = plugin.getConfigManager().getConfig().getString("sounds." + soundKey + ".sound", "");
        float volume = (float) plugin.getConfigManager().getConfig().getDouble("sounds." + soundKey + ".volume", 1.0);
        float pitch = (float) plugin.getConfigManager().getConfig().getDouble("sounds." + soundKey + ".pitch", 1.0);

        if (soundName.isEmpty()) {
            plugin.getPluginLogger().warning("No sound defined for key: " + soundKey);
            return;
        }

        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getPluginLogger().warning("Invalid sound name in config for " + soundKey + ": " + soundName);
        }
    }
}