package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class GravityPowerListener implements Listener {

    private final HashMap<UUID, Long> gravityCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 15000;

    @EventHandler
    public void onGravityFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getScoreboardTags().contains("gravity") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 0.5f);
        }
    }

    @EventHandler
    public void onGravityKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("gravity")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (gravityCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = gravityCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.LIGHT_PURPLE + " Gravity Well is on cooldown for " + secondsLeft + "s!");
                return;
            }
        }

        double radius = 10.0;
        boolean hitTarget = false;

        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 1, 0.1, 1, 0.5);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 0.5f);

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;
                hitTarget = true;

                Vector pullVector = player.getLocation().toVector().subtract(target.getLocation().toVector());
                pullVector.normalize().multiply(1.2);
                pullVector.setY(0.3);
                target.setVelocity(pullVector);

                target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 0, false, false, true));
                target.getWorld().spawnParticle(Particle.WITCH, target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.01);

                if (target instanceof Player) {
                    ((Player) target).sendMessage(ChatColor.LIGHT_PURPLE + "You are caught in a heavy Gravity Well!");
                }
            }
        }

        if (hitTarget) {
            player.sendMessage(ChatColor.DARK_PURPLE + " GRAVITY WELL: You pulled and suspended nearby enemies!");
            gravityCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "The gravitational field expanded, but found no targets.");
        }
    }
}
