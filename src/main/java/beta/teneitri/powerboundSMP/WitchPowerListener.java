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

public class WitchPowerListener implements Listener {

    private final HashMap<UUID, Long> hexCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 25000; 
    private final Random random = new Random();

    @EventHandler
    public void onWitchTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!player.getScoreboardTags().contains("witch")) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.MAGIC || event.getCause() == EntityDamageEvent.DamageCause.POISON || event.getCause() == EntityDamageEvent.DamageCause.WITHER) {
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage * 0.65); 
        }
    }

    @EventHandler
    public void onWitchKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("witch")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (hexCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = hexCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.LIGHT_PURPLE + " Cauldron is brewing! ready in " + secondsLeft + "s");
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
            int curseChoice = random.nextInt(3);
            if (curseChoice == 0) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 120, 1, false, true, true));
            } else if (curseChoice == 1) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 1, false, true, true));
            } else {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 4, false, true, true));
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 1, false, false, true));

            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.8f);
            target.getWorld().spawnParticle(Particle.SPELL_WITCH, target.getLocation().add(0, 1, 0), 35, 0.4, 0.5, 0.4, 0.05);

            player.sendMessage(ChatColor.LIGHT_PURPLE + " WITCH'S HEX: Applied hex to target!");
            hexCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "No targets were close enough to hex.");
        }
    }
}
