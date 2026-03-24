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

        String soundName = plugin.getConfigManager().getConfig().getString("sounds." + type);
        if (soundName == null) return;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) plugin.getConfigManager().getConfig().getDouble("sounds.volume", 1.0);
            float pitch = (float) plugin.getConfigManager().getConfig().getDouble("sounds.pitch", 1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {}
    }
}
