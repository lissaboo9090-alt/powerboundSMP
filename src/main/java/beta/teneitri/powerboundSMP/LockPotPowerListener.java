package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class LockPotPowerListener implements Listener {

    private final HashMap<UUID, Long> lockCooldowns = new HashMap<>();
    private final HashSet<UUID> trappedPlayers = new HashSet<>();
    private final long COOLDOWN_TIME = 20000;

    @EventHandler
    public void onLockPotKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("lock pot")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (lockCooldowns.containsKey(player.getUniqueId()) && lockCooldowns.get(player.getUniqueId()) > currentTime) {
            double remaining = (lockCooldowns.get(player.getUniqueId()) - currentTime) / 1000.0;
            player.sendMessage(ChatColor.DARK_GREEN + " Trap Bubble ready in " + Math.round(remaining * 10) / 10.0 + "s!");
            return;
        }

        if (player.getTargetEntity(10) instanceof Player target) {
            UUID targetUUID = target.getUniqueId();
            trappedPlayers.add(targetUUID);
            
            player.sendMessage(ChatColor.GREEN + "You trapped " + target.getName() + " inside a Potion Lock Bubble!");
            target.sendMessage(ChatColor.RED + "You are locked inside a trap bubble! Your potions will fail!");
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 0.5f);

            org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("powerboundSMP");
            if (plugin != null) {
                int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    if (trappedPlayers.contains(targetUUID)) {
                        Location loc = target.getLocation();
                        for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                            double x = Math.cos(i) * 1.2;
                            double z = Math.sin(i) * 1.2;
                            loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc.getX() + x, loc.getY() + 1, loc.getZ() + z, 1, 0, 0, 0, 0);
                        }
                    }
                }, 0L, 5L);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    trappedPlayers.remove(targetUUID);
                    Bukkit.getScheduler().cancelTask(taskId);
                    target.sendMessage(ChatColor.GREEN + "The trap bubble popped!");
                }, 80L);
            }
            lockCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        }
    }

    @EventHandler
    public void onTrappedPlayerMove(PlayerMoveEvent event) {
        if (trappedPlayers.contains(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                Location newLoc = from.clone();
                newLoc.setYaw(to.getYaw());
                newLoc.setPitch(to.getPitch());
                event.setTo(newLoc);
            }
        }
    }

    @EventHandler
    public void onPotionThrow(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof ThrownPotion potion && potion.getShooter() instanceof Player shooter) {
            if (trappedPlayers.contains(shooter.getUniqueId())) {
                event.setCancelled(true);
                shooter.getWorld().playSound(shooter.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
                shooter.sendMessage(ChatColor.RED + " Your potion bounced off the bubble walls and dissolved!");
            }
        }
    }
}
