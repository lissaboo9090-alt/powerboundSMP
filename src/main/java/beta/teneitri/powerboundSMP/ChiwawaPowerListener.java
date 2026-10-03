package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class ChiwawaPowerListener implements Listener {

    private final Plugin plugin;
    private final HashMap<UUID, Long> barkCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 12000; 

    public ChiwawaPowerListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChiwawaKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("chiwawa")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (barkCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = barkCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.RED + " Ankle Biter is on cool down! Wait " + secondsLeft + "s");
                return;
            }
        }

        Vector direction = player.getLocation().getDirection().normalize();
        player.setVelocity(direction.multiply(1.5).setY(0.3));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.5f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
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
                target.damage(3.0, player); 
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));
                
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WOLF_BARK, 1.5f, 1.6f);
                target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.1);
                
                player.sendMessage(ChatColor.RED + "ANKLE BITER! You aggressively bit " + target.getName() + "!");
                target.sendMessage(ChatColor.DARK_RED + " You were bitten by a furious Chiwawa!");
            }
        }, 4L);

        barkCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
