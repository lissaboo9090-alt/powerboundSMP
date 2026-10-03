package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.HashSet;
import java.util.UUID;

public class FadeOutPowerListener implements Listener {

    private final Plugin plugin;
    private final HashMap<UUID, Long> fadeCooldowns = new HashMap<>();
    private final HashSet<UUID> phasedPlayers = new HashSet<>();
    private final long COOLDOWN_TIME = 45000;

    public FadeOutPowerListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhasedDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (phasedPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFadeKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("fade out")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (fadeCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = fadeCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.DARK_AQUA + "fade is on cool down! Wait " + secondsLeft + "s");
                return;
            }
        }

        UUID uuid = player.getUniqueId();
        phasedPlayers.add(uuid);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1, false, false, true));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.5f, 1.5f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.4, 0.5, 0.4, 0.1);
        player.sendMessage(ChatColor.DARK_AQUA + "PHANTOM SHIFT: You phased out of reality for 4 seconds!");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            phasedPlayers.remove(uuid);
            if (player.isOnline()) {
                player.sendMessage(ChatColor.AQUA + "You solidified back into reality.(sounds cringe saying ts out loud)");
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.0f, 1.0f);
            }
        }, 80L);

        fadeCooldowns.put(uuid, currentTime + COOLDOWN_TIME);
    }
}
