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

public class LifelinePowerListener implements Listener {

    private final HashMap<UUID, Long> lifelineCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 300000;
    @EventHandler
    public void onLifelineTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("lifeline")) return;

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            long currentTime = System.currentTimeMillis();

            if (lifelineCooldowns.containsKey(player.getUniqueId())) {
                long timeLeft = lifelineCooldowns.get(player.getUniqueId()) - currentTime;
                if (timeLeft > 0) {
                    double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                    player.sendMessage(ChatColor.RED + " wait you on cooldown! ready in " + secondsLeft + "s");
                    return; 
            }

            event.setCancelled(true);
            player.setHealth(8.0);

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 3, false, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1, false, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, true, true));

            for (Entity entity : player.getNearbyEntities(13.0, 13.0, 13.0)) {
                if (entity instanceof Player && !entity.equals(player)) {
                    Player nearbyPlayer = (Player) entity;
                    
                    double currentHealth = nearbyPlayer.getHealth();
                    double maxHealth = nearbyPlayer.getMaxHealth();
                    nearbyPlayer.setHealth(Math.min(currentHealth + 8.0, maxHealth));

                    nearbyPlayer.sendMessage(ChatColor.GREEN + " A teammate's Lifeline has healed you!");
                    nearbyPlayer.getWorld().spawnParticle(Particle.HEART, nearbyPlayer.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.05);
                }
            }

            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);

            player.sendMessage(ChatColor.GREEN + " LIFELINE: Death prevented! Nearby players healed.");
            lifelineCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        }
    }

