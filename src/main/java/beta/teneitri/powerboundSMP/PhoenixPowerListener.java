package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class PhoenixPowerListener implements Listener {

    private final Plugin plugin;
    private final HashMap<UUID, Long> flightCooldowns = new HashMap<>();
    private final HashMap<UUID, Long> resurrectionCooldowns = new HashMap<>();

    public PhoenixPowerListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhoenixDeath(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.getScoreboardTags().contains("phoenix")) return;

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            long currentTime = System.currentTimeMillis();
            if (!resurrectionCooldowns.containsKey(player.getUniqueId()) || resurrectionCooldowns.get(player.getUniqueId()) <= currentTime) {
                event.setCancelled(true);
                player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                player.setFireTicks(0);

                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 4, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, false, true));

                Location loc = player.getLocation();
                loc.getWorld().spawnParticle(Particle.FLAME, loc.add(0, 1, 0), 100, 0.5, 1.0, 0.5, 0.1);
                loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 1.0f);

                player.sendMessage(ChatColor.GOLD + " [Phoenix] You rose from the ashes! Resurrection triggered.");
                resurrectionCooldowns.put(player.getUniqueId(), currentTime + 600000);
            }
        }
    }

    @EventHandler
    public void onPhoenixKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("phoenix")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (flightCooldowns.containsKey(player.getUniqueId()) && flightCooldowns.get(player.getUniqueId()) > currentTime) {
            double remaining = (flightCooldowns.get(player.getUniqueId()) - currentTime) / 1000.0;
            player.sendMessage(ChatColor.GOLD + " Fire Flight is on cool down! Wait " + Math.round(remaining * 10) / 10.0 + "s");
            return;
        }

        player.setVelocity(player.getVelocity().setY(0.6));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.sendMessage(ChatColor.GOLD + " Fire Flight active for 6 seconds!");

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (player.isOnline() && player.isFlying()) {
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 5, 0.1, 0.1, 0.1, 0.02);
            }
        }, 0L, 2L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(taskId);
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                player.setFlying(false);
                player.setAllowFlight(false);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, false, false));
            }
            player.sendMessage(ChatColor.YELLOW + " Fire Flight has expired.");
        }, 120L);

        flightCooldowns.put(player.getUniqueId(), currentTime + 20000);
    }
}
