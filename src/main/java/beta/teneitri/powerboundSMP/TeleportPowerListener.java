package beta.teneitri.powerboundSMP;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class TeleportPowerListener implements Listener {

    private final HashMap<UUID, Long> teleportCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 10000;

    @EventHandler
    public void onTeleportKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("teleport")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (teleportCooldowns.containsKey(player.getUniqueId()) && teleportCooldowns.get(player.getUniqueId()) > currentTime) {
            double remaining = (teleportCooldowns.get(player.getUniqueId()) - currentTime) / 1000.0;
            player.sendMessage(ChatColor.LIGHT_PURPLE + " Teleportation is on cooldown! Wait " + Math.round(remaining * 10) / 10.0 + "s");
            return;
        }

        if (player.getTargetEntity(12) instanceof LivingEntity target) {
            Location pLoc = player.getLocation();
            Location tLoc = target.getLocation();
            spawnWarpParticles(pLoc);
            spawnWarpParticles(tLoc);
            player.teleport(tLoc);
            target.teleport(pLoc);
            player.getWorld().playSound(pLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            player.sendMessage(ChatColor.LIGHT_PURPLE + " Translocation! Swapped positions with " + target.getName() + "!");
        } else {
            Location start = player.getLocation();
            Vector dir = start.getDirection().normalize();
            Location dest = start.clone().add(dir.multiply(7));
            Block block = dest.getBlock();
            if (block.getType().isSolid()) dest = start.clone().add(dir.multiply(5.5));
            dest.setYaw(start.getYaw());
            dest.setPitch(start.getPitch());

            spawnWarpParticles(start);
            player.teleport(dest);
            spawnWarpParticles(dest);
            player.getWorld().playSound(dest, Sound.ENTITY_FOX_TELEPORT, 1.0f, 1.5f);
            player.sendMessage(ChatColor.LIGHT_PURPLE + " Blink!");
        }
        teleportCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }

    private void spawnWarpParticles(Location loc) {
        loc.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 15, 0.2, 0.4, 0.2, 0.1);
    }
}
