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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class LightPowerListener implements Listener {

    private final HashMap<UUID, Long> lightCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 20000; 

    public LightPowerListener(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getScoreboardTags().contains("light")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false, true));
                }
            }
        }, 0L, 40L);
    }

    @EventHandler
    public void onLightJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getScoreboardTags().contains("light")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0, false, false, true));
        }
    }

    @EventHandler
    public void onLightKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("light")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (lightCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = lightCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.YELLOW + "Solar is on cool down! Wait " + secondsLeft + "s");
                return;
            }
        }

        double radius = 8.0;
        boolean flashedAnyone = false;
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.5f, 1.2f);

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;
                flashedAnyone = true;

                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false, true));
                target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0, 1, 0), 15, 0.2, 0.4, 0.2, 0.02);

                if (target instanceof Player) {
                    ((Player) target).sendMessage(ChatColor.YELLOW + "SOLAR FLARE: You were completely blinded!");
                }
            }
        }

        if (flashedAnyone) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false, true));
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.05);
            player.sendMessage(ChatColor.YELLOW + "SOLAR FLARE: You blinded nearby enemies!");
            lightCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
        } else {
            player.sendMessage(ChatColor.GRAY + "You ignited a solar spark, but nobody was nearby.");
        }
    }
}
