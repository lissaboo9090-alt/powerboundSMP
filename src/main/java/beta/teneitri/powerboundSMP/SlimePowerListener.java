package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.UUID;

public class SlimePowerListener implements Listener {
    private final HashMap<UUID, Long> slimeCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 10000; // 10 seconds

    public SlimePowerListener() {
        Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("powerboundSMP"), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getScoreboardTags().contains("slime")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 1, false, false, true));
                }
            }
        }, 0L, 40L);
    }

    @EventHandler
    public void onSlimeJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getScoreboardTags().contains("slime")) {
            // FIXED: Corrected PotionEffectType initialization to PotionEffect!
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 1, false, false, true));
        }
    }

    @EventHandler
    public void onSlimeKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("slime")) return;
        event.setCancelled(true);
        long currentTime = System.currentTimeMillis();
        if (slimeCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = slimeCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.GREEN + "⏳ Cooldown: " + secondsLeft + "s");
                return;
            }
        }
        player.setVelocity(new Vector(player.getVelocity().getX(), 0.8, player.getVelocity().getZ()));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.5f, 1.0f);
        player.getWorld().spawnParticle(Particle.SLIME, player.getLocation().add(0, 0.5, 0), 20, 0.3, 0.3, 0.3, 0.1);
        for (Entity entity : player.getNearbyEntities(6.0, 6.0, 6.0)) {
            if (entity instanceof Player && !entity.equals(player)) {
                Player target = (Player) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 2, false, false, true));
                target.getWorld().spawnParticle(Particle.SLIME, target.getLocation().add(0, 1, 0), 15, 0.2, 0.4, 0.2, 0.05);
                target.sendMessage(ChatColor.GREEN + "🤢 You were slimed!");
                player.sendMessage(ChatColor.GREEN + "🟢 Slimed " + target.getName() + "!");
                break;
            }
        }
        slimeCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
