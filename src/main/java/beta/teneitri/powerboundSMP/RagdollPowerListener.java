package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class RagdollPowerListener implements Listener {

    private final HashMap<UUID, Long> flopCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 20000;

    @EventHandler
    public void onRagdollHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && player.getScoreboardTags().contains("ragdoll")) {
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("powerboundSMP"), () -> {
                Vector vel = player.getVelocity();
                player.setVelocity(new Vector(vel.getX() * 0.2, vel.getY(), vel.getZ() * 0.2));
            }, 1L);
        }
    }

    @EventHandler
    public void onRagdollKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("ragdoll")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (flopCooldowns.containsKey(player.getUniqueId()) && flopCooldowns.get(player.getUniqueId()) > currentTime) {
            double remaining = (flopCooldowns.get(player.getUniqueId()) - currentTime) / 1000.0;
            player.sendMessage(ChatColor.GRAY + "Flop shockwave is cooling down! Wait " + Math.round(remaining * 10) / 10.0 + "s");
            return;
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 1.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 20, 1.0, 0.1, 1.0, 0.05);

        for (Entity entity : player.getNearbyEntities(6.0, 6.0, 6.0)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                Vector throwVector = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                throwVector.multiply(1.8).setY(0.5);
                target.setVelocity(throwVector);
                if (target instanceof Player) {
                    target.sendMessage(ChatColor.RED + "OOF! A Ragdoll user flopped and blasted you away!");
                }
            }
        }
        player.sendMessage(ChatColor.GOLD + "FLOP SHOCKWAVE: You collapsed and blasted enemies away!");
        flopCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
