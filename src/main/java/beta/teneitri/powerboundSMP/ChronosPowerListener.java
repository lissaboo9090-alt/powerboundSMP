package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ChronosPowerListener implements Listener {

    private final HashMap<UUID, Long> freezeCooldowns = new HashMap<>();
    private final HashSet<UUID> trappedPlayers = new HashSet<>();
    private final long COOLDOWN_TIME = 25000;

    @EventHandler
    public void onChronosKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("chronos")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (freezeCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = freezeCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.GOLD + "time is on cooldown! Wait " + secondsLeft + "s");
                return;
            }
        }

        double radius = 10.0;
        boolean frozeAnyone = false;
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 1.5f);

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player && !entity.equals(player)) {
                Player target = (Player) entity;
                UUID targetUUID = target.getUniqueId();
                frozeAnyone = true;

                trappedPlayers.add(targetUUID);
                target.sendMessage(ChatColor.RED + "Time has been frozen around you! You are locked for 3 seconds!");
                target.getWorld().spawnParticle(Particle.SPELL_INSTANT, target.getLocation().add(0, 1, 0), 20, 0.2, 0.4, 0.2, 0);

                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("powerboundSMP"), () -> {
                    trappedPlayers.remove(targetUUID);
                    if (target.isOnline()) {
                        target.sendMessage(ChatColor.GREEN + " Time resumes flowing for you.");
                    }
                }, 60L); 
            }
        }

        if (frozeAnyone) {
            player.sendMessage(ChatColor.GOLD + "TIME LOCK: You froze the timeline for nearby players!");
            player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation(), 45, 0.5, 0.5, 0.5, 0.1);
            freezeCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "Timeline focused, but no nearby players were caught in the ripple.");
        }
    }

    @EventHandler
    public void onTrappedPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (trappedPlayers.contains(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY())) {
                Location lockedLoc = from.clone();
                lockedLoc.setYaw(to.getYaw());
                lockedLoc.setPitch(to.getPitch());
                event.setTo(lockedLoc);
            }
        }
    }
}
