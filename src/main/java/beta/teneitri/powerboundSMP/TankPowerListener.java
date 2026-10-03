package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class TankPowerListener implements Listener {

    private final HashMap<UUID, Long> tankCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 15000;

    @EventHandler
    public void onTankKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("tank")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (tankCooldowns.containsKey(player.getUniqueId()) && tankCooldowns.get(player.getUniqueId()) > currentTime) {
            double remaining = (tankCooldowns.get(player.getUniqueId()) - currentTime) / 1000.0;
            player.sendMessage(ChatColor.RED + "Shield is cooling down! Wait " + Math.round(remaining * 10) / 10.0 + "s");
            return;
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 15, 0.5, 0.1, 0.5, 0.05);

        for (Entity entity : player.getNearbyEntities(6.0, 6.0, 6.0)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                Vector throwVector = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                throwVector.multiply(1.5).setY(0.4);
                target.setVelocity(throwVector);
                if (target instanceof Player) {
                    ((Player) target).sendMessage(ChatColor.RED + "You were blasted back by a Tank Ground Slam!");
                }
            }
        }
        player.sendMessage(ChatColor.GOLD + "GROUND SLAM: You blasted nearby enemies away!");
        tankCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
