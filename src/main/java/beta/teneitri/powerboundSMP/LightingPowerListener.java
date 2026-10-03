package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class LightingPowerListener implements Listener {

    private final HashMap<UUID, Long> lightingCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 45000; 

    @EventHandler
    public void onLightingKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("lighting")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (lightingCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = lightingCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.YELLOW + "Lighting is on cool down! Wait " + secondsLeft + "s");
                return;
            }
        }

        Player target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            if (onlinePlayer.getWorld().equals(player.getWorld())) {
                double distance = player.getLocation().distance(onlinePlayer.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    target = onlinePlayer;
                }
            }
        }

        if (target != null) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false, true));
            target.getWorld().strikeLightningEffect(target.getLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);

            int targetX = target.getLocation().getBlockX();
            int targetZ = target.getLocation().getBlockZ();
            player.sendMessage(ChatColor.YELLOW + "[Lighting] Found " + target.getName() + " at X: " + targetX + ", Z: " + targetZ + "! They are highlighted!");
            target.sendMessage(ChatColor.GOLD + "A pure tracking beam has highlighted you through the static!");

            lightingCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "The air crackles with energy, but no other players were found on the grid.");
        }
    }
}
