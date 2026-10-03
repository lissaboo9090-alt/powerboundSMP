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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ParrotPowerListener implements Listener {

    private final HashMap<UUID, Long> mimicCooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 15000; // 15 seconds
    private final Random random = new Random();

    private final Sound[] MIMIC_SOUNDS = {
        Sound.ENTITY_CREEPER_PRIMED,
        Sound.ENTITY_ZOMBIE_AMBIENT,
        Sound.ENTITY_SKELETON_AMBIENT,
        Sound.ENTITY_SPIDER_AMBIENT,
        Sound.ENTITY_ENDERMAN_STARE
    };

    @EventHandler
    public void onParrotFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (player.getScoreboardTags().contains("parrot") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 0.8f, 1.2f);
        }
    }

    @EventHandler
    public void onParrotKeyBind(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!player.getScoreboardTags().contains("parrot")) return;
        event.setCancelled(true);

        long currentTime = System.currentTimeMillis();
        if (mimicCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = mimicCooldowns.get(player.getUniqueId()) - currentTime;
            if (timeLeft > 0) {
                double secondsLeft = Math.round((timeLeft / 1000.0) * 10) / 10.0;
                player.sendMessage(ChatColor.GREEN + " Vocal cords recovering! Squawk ready in " + secondsLeft + "s");
                return;
            }
        }

        player.setVelocity(new Vector(player.getVelocity().getX(), 0.65, player.getVelocity().getZ()));
        
        Sound fakeNoise = MIMIC_SOUNDS[random.nextInt(MIMIC_SOUNDS.length)];
        player.getWorld().playSound(player.getLocation(), fakeNoise, 1.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PARROT_IMITATE_CREEPER, 1.2f, 1.0f);
        player.getWorld().spawnParticle(Particle.NOTE, player.getLocation().add(0, 1, 0), 15, 0.4, 0.4, 0.4, 0.1);

        double radius = 6.0;
        boolean hitTarget = false;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity target = (LivingEntity) entity;
                hitTarget = true;

                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false, true));
                target.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, target.getLocation().add(0, 1.5, 0), 5, 0.2, 0.2, 0.2, 0.0);

                if (target instanceof Player) {
                    ((Player) target).sendMessage(ChatColor.RED + " You were completely disoriented by a loud Parrot Mimic Squawk!");
                }
            }
        }

        if (hitTarget) {
            player.sendMessage(ChatColor.GREEN + " MIMIC SQUAWK: You tricked and blinded nearby enemies!");
        } else {
            player.sendMessage(ChatColor.GRAY + "You squawked loudly, but no enemies were in range to trick.");
        }

        mimicCooldowns.put(player.getUniqueId(), currentTime + COOLDOWN_TIME);
    }
}
