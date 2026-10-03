package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class TelepathyPowerListener implements Listener {

    private final Plugin plugin;
    private final HashMap<UUID, Long> telepathyCooldowns = new HashMap<>();
    private final HashSet<UUID> activeMindReaders = new HashSet<>();
    private final long COOLDOWN_TIME = 2100000; 

    public TelepathyPowerListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTelepathyKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("telepathy")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (telepathyCooldowns.containsKey(player.getUniqueId()) && telepathyCooldowns.get(player.getUniqueId()) > currentTime) {
            long left = telepathyCooldowns.get(player.getUniqueId()) - currentTime;
            player.sendMessage(ChatColor.LIGHT_PURPLE + " Telepathy cooldown: " + (left / 60000) + "m " + ((left % 60000) / 1000) + "s");
            return;
        }

        if (player.getTargetEntity(15) instanceof Player target) {
            player.openInventory(target.getInventory());
            activeMindReaders.add(player.getUniqueId());
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Reading the mind of " + target.getName() + "...");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.5f);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.closeInventory();
                activeMindReaders.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GRAY + "Your telepathic link has dissolved.");
            }, 80L);

            telepathyCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        }
    }

    @EventHandler
    public void onTelepathChat(AsyncPlayerChatEvent event) {
        if (activeMindReaders.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot type while your mind is linked!");
        }
    }
}
