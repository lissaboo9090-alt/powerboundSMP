package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class TimePowerListener implements Listener {

    private final Plugin plugin;
    private final HashMap<UUID, Long> timeCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 30000;

    public TimePowerListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTimeKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("time")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (timeCooldowns.containsKey(player.getUniqueId()) && timeCooldowns.get(player.getUniqueId()) > currentTime) {
            double remaining = (timeCooldowns.get(player.getUniqueId()) - currentTime) / 1000.0;
            player.sendMessage(ChatColor.GOLD + "Temporal is cooling down! Wait " + Math.round(remaining * 10) / 10.0 + "s");
            return;
        }

        Location anchorLoc = player.getLocation().clone();
        double anchorHealth = player.getHealth();
        player.sendMessage(ChatColor.GOLD + "Temporal anchor set! Rewinding in 5 seconds...");
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !player.isDead()) {
                player.teleport(anchorLoc);
                player.setHealth(anchorHealth);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                player.sendMessage(ChatColor.YELLOW + " TIME REWIND! You returned to your anchor state.");
            }
        }, 100L);

        timeCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
