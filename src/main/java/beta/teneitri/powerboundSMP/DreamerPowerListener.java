package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class DreamerPowerListener implements Listener {

    private final HashMap<UUID, Long> dreamCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 30000;

    @EventHandler
    public void onDreamerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("dreamer")) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR || event.getCause() == EntityDamageEvent.DamageCause.CONTACT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDreamerKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("dreamer")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (dreamCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = dreamCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.LIGHT_PURPLE + " Dreamscape is cool down! Wait " + secondsLeft + "s");
                return;
            }
        }

        Player target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : player.getNearbyEntities(10.0, 10.0, 10.0)) {
            if (entity instanceof Player && !entity.equals(player)) {
                double dist = player.getLocation().distance(entity.getLocation());
                if (dist < closestDistance) {
                    closestDistance = dist;
                    target = (Player) entity;
                }
            }
        }

        if (target != null) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 4, false, false, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false, true));

            target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.02);
            target.getWorld().spawnParticle(Particle.SPELL_WITCH, target.getLocation(), 20, 0.5, 1.0, 0.5, 0.05);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 0.5f);

            player.sendMessage(ChatColor.LIGHT_PURPLE + " DREAM TRANCE: You trapped " + target.getName() + " inside a nightmare!");
            target.sendMessage(ChatColor.DARK_PURPLE + " You slipped into a lucid nightmare!");

            dreamCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "Nobody was nearby to project a dream to.");
        }
    }
}
