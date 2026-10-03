package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class VampirePowerListener implements Listener {

    private final HashMap<UUID, Long> biteCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 15000; 

    @EventHandler
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player vampire = (Player) event.getDamager();

        if (!vampire.getScoreboardTags().contains("vampire")) return;

        if (!(event.getEntity() instanceof Player && ((Player) event.getEntity()).getScoreboardTags().contains("vampire"))) {
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage * 2.0);
        }
    }

    @EventHandler
    public void onVampireBiteKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("vampire")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (biteCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = biteCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.RED + "bite is on cool down! Bite ready in " + secondsLeft + "s");
                return;
            }
        }

        Vector direction = player.getLocation().getDirection().normalize();
        player.setVelocity(direction.multiply(1.4).setY(0.2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.0f, 0.5f);

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("powerboundSMP"), () -> {
            if (!player.isOnline()) return;

            Player target = null;
            double closestDistance = Double.MAX_VALUE;

            for (Entity entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
                if (entity instanceof Player && !entity.equals(player)) {
                    double dist = player.getLocation().distance(entity.getLocation());
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        target = (Player) entity;
                    }
                }
            }

            if (target != null) {
                target.damage(4.0, player); 
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));
                
                double currentHealth = player.getHealth();
                double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                player.setHealth(Math.min(maxHealth, currentHealth + 4.0));

                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BAT_DEATH, 1.5f, 0.5f);
                target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 12, 0.2, 0.2, 0.2, 0.1);
                
                player.sendMessage(ChatColor.RED + "VAMPIRE BITE! You siphoned health from " + target.getName() + "!");
                target.sendMessage(ChatColor.DARK_RED + " Your blood was drained by a Vampire!");
            }
        }, 3L);

        biteCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
