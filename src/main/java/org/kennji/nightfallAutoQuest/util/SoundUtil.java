package org.kennji.nightfallAutoQuest.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

public final class SoundUtil {
    private final NightfallAutoQuest plugin;

    public SoundUtil(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void playSound(@NotNull Player player, @NotNull String type) {
        if (!plugin.getConfigManager().getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }

        var config = plugin.getConfigManager().getConfig();
        String path = "sounds." + type;

        String soundName;
        float volume;
        float pitch;

        if (config.isConfigurationSection(path)) {
            soundName = config.getString(path + ".sound");
            volume = (float) config.getDouble(path + ".volume", 1.0);
            pitch = (float) config.getDouble(path + ".pitch", 1.0);
        } else {
            soundName = config.getString(path);
            volume = (float) config.getDouble("sounds.volume", 1.0);
            pitch = (float) config.getDouble("sounds.pitch", 1.0);
        }

        if (soundName == null || soundName.isBlank()) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase().trim());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            plugin.getPluginLogger()
                    .warn("Could not play sound '" + soundName + "' for type '" + type + "': " + e.getMessage());
        }
    }
}
