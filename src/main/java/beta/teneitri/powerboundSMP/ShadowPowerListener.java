package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class ShadowPowerListener implements Listener {

    private final HashMap<UUID, Long> shadowCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 30000;

    @EventHandler
    public void onShadowKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("shadow")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (shadowCooldowns.containsKey(player.getUniqueId()) && shadowCooldowns.get(player.getUniqueId()) > currentTime) {
            double remaining = (shadowCooldowns.get(player.getUniqueId()) - currentTime) / 1000.0;
            player.sendMessage(ChatColor.GRAY + "Shadow Vanish is on cooldown for " + Math.round(remaining * 10) / 10.0 + "s!");
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false, true));
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.5f, 0.5f);

        player.sendMessage(ChatColor.DARK_GRAY + " You have invis");
        shadowCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }

    @EventHandler
    public void onShadowAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.getScoreboardTags().contains("shadow") && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.sendMessage(ChatColor.RED + " Your invisibility broke because you attacked!");
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GLASS_BREAK, 0.8f, 1.2f);
            }
        }
    }
}
