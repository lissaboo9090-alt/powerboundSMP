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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class GuardianAngelPowerListener implements Listener {

    private final HashMap<UUID, Long> blastCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 600000;
    private final Random random = new Random();

    @EventHandler
    public void onGuardianAngelDeathGamble(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("guardian_angel")) return;

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            
            if (random.nextInt(4) == 0) {
                event.setCancelled(true);
                player.setHealth(2.0); 

                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1, false, true, true)); 
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, true, true));

                player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.15);

                player.sendMessage(ChatColor.YELLOW + " GUARDIAN ANGEL: Divine intervention saved your life!");
            }
        }
    }

    @EventHandler
    public void onGuardianAngelBlastKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("guardian_angel")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (blastCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = blastCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double minutesLeft = Math.round((timeLeft / 60000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.YELLOW + " Holy Blast is recharging! ready in " + minutesLeft + "m");
                return;
            }
        }

        boolean targetsFound = false;

        for (Entity entity : player.getNearbyEntities(10.0, 10.0, 10.0)) {
            if (entity instanceof Player && !entity.equals(player)) {
                Player nearbyPlayer = (Player) entity;
                targetsFound = true;

                Vector pushDirection = nearbyPlayer.getLocation().toVector().subtract(player.getLocation().toVector());

                if (pushDirection.lengthSquared() > 0) {
                    pushDirection.normalize();
                } else {
                    pushDirection = new Vector(1, 0, 0);
                }

                pushDirection.multiply(2.2).setY(0.6);
                nearbyPlayer.setVelocity(pushDirection);

                nearbyPlayer.sendMessage(ChatColor.YELLOW + " You were blasted back by a Guardian Angel's holy shockwave!");
                nearbyPlayer.getWorld().spawnParticle(Particle.CLOUD, nearbyPlayer.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.2);
            }
        }

        if (targetsFound) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.5f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.8f, 0.5f);
            player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().add(0, 0.5, 0), 5, 0.1, 0.1, 0.1, 0.0);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 60, 1.5, 0.5, 1.5, 0.1);

            player.sendMessage(ChatColor.GOLD + " HOLY BLAST: You unleashed a divine shockwave!");
            blastCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "No targets nearby to blast away.");
        }
    }
}

