package beta.teneitri.powerboundSMP;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class CheesecakePowerListener implements Listener {

    private final HashMap<UUID, Long> sugarCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 15000; 

    public CheesecakePowerListener(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getScoreboardTags().contains("cheesecake")) {
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                }
            }
        }, 0L, 40L);
    }

    @EventHandler
    public void onCheesecakeJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getScoreboardTags().contains("cheesecake")) {
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getScoreboardTags().contains("cheesecake")) {
                event.setCancelled(true);
                player.setFoodLevel(20);
            }
        }
    }

    @EventHandler
    public void onCheesecakeKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("cheesecake")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (sugarCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = sugarCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.GOLD + "Sugar crash is on cooldown! Wait " + secondsLeft + "s");
                return;
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 160, 1, false, false, true));

        player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 25, 0.4, 0.5, 0.4, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.2f);

        player.sendMessage(ChatColor.YELLOW + "CHEESECAKE: You activated a sweet Sugar Rush!(yummy [drool])");
        sugarCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
