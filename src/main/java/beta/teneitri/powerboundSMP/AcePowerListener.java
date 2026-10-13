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
import java.util.Random;
import java.util.UUID;

public class AcePowerListener implements Listener {

    private final HashMap<UUID, Long> allOutCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 45000;
    private final Random random = new Random();

    @EventHandler
    public void onAceTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("ace")) return;

        double healthAfterDamage = player.getHealth() - event.getFinalDamage();

        if (healthAfterDamage <= 6.0 && healthAfterDamage > 0) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, true, true)); 
            }
            if (!player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 160, 1, false, true, true)); 
            }
            
            player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0.0);
        }
    }

    @EventHandler
    public void onAceAllOutKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("ace")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (allOutCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = allOutCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.RED + " Ace is on cooldown! ready in " + secondsLeft + "s");
                return;
            }
        }

        Player target = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
            if (entity instanceof Player && !entity.equals(player)) {
                double dist = player.getLocation().distance(entity.getLocation());
                if (dist < closestDistance) {
                    closestDistance = dist;
                    target = (Player) entity;
                }
            }
        }

        if (target != null) {
            boolean aceWins = random.nextBoolean();

            if (aceWins) {
                target.setHealth(0.0);
                
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.5f);
                target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1, 0), 100, 0.5, 0.8, 0.5, 0.1);
                
                player.sendMessage(ChatColor.GOLD + " ALL OUT: Complete success! Your target was obliterated.");
                target.sendMessage(ChatColor.DARK_RED + " Ace went all out... and you paid the ultimate price.");
            } else {
                player.setHealth(0.0);
                
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
                player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 100, 0.5, 0.8, 0.5, 0.1);
                
                player.sendMessage(ChatColor.DARK_RED + " ALL OUT: The gamble backfired. You met your demise.");
                target.sendMessage(ChatColor.GREEN + " Ace tried to go all out on you, but collapsed from exhaustion!");
            }

            allOutCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "your bad at this yo.");
        }
    }
}
