package com.robomwm.mlgdamageindicators;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/7/2017.
 *
 * @author RoboMWM
 */
public class MLGDamageIndicators extends JavaPlugin implements Listener
{
    JavaPlugin instance;
    Set<TextDisplay> activeHolograms = new HashSet<>();
    DecimalFormat df = new DecimalFormat("#.#");

    public void onEnable()
    {
        instance = this;
        instance.getServer().getPluginManager().registerEvents(this, instance);
        if (instance.getServer().getPluginManager().getPlugin("AbsorptionShields") != null)
            new ShieldDamageListener(this);
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public void onDisable()
    {
        getLogger().info("Cleaning up any active damage indicator holograms...");
        getLogger().info(String.valueOf(cleanupDamageIndicators()) + " holograms removed.");
    }

    public int cleanupDamageIndicators()
    {
        for (TextDisplay hologram : activeHolograms)
        {
            hologram.remove();
        }
        int i = activeHolograms.size();
        activeHolograms.clear();
        return i;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onDisplayDamageIndicator(EntityDamageByEntityEvent event)
    {
        if (event.getFinalDamage() <= 0.05D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        if (event.getEntityType() == EntityType.ARMOR_STAND) //Besides not actually being alive, can lead to AoE damage causing "damage" to the damage indicators.
            return;
        LivingEntity livingEntity = (LivingEntity)event.getEntity();
        Location location;
        if (event.getEntityType() == EntityType.PLAYER)
            location = livingEntity.getLocation().add(0, 2.2D, 0);
        else
            location = livingEntity.getEyeLocation();
        displayIndicator(location, event.getFinalDamage() / 2D, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onDisplayHealthRegenIndicator(EntityRegainHealthEvent event)
    {
        if (event.getAmount() <= 0.05D)
            return;
        if (!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity livingEntity = (LivingEntity)event.getEntity();
        Location location;
        if (event.getEntityType() == EntityType.PLAYER)
            location = livingEntity.getLocation().add(0, 2.2D, 0);
        else
            location = livingEntity.getEyeLocation();
        displayIndicator(location, event.getAmount() / 2D, false);
    }

    public static Double r4nd0m(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public void displayIndicator(final Location location, final double value, final boolean isDamage)
    {
        TextColor color;
        if (isDamage)
            color = NamedTextColor.RED;
        else
            color = NamedTextColor.GREEN;
        displayIndicator(location, value, isDamage, color);
    }

    public void displayIndicator(final Location location, final double value, final boolean isDamage, TextColor color)
    {
        double x = r4nd0m(-0.3D, 0.3D);
        double z = r4nd0m(-0.3D, 0.3D);
        long duration = ((long)value / 2) + 20L; //Increase display duration by a second per 40 hearts of damage.
        if (duration > 100L) duration = 100L; //Cap to 5 seconds max (cookiez r insanely op, y'uh know)

        Component text = Component.text((isDamage ? "-" : "+") + df.format(value), color);

        TextDisplay hologram = location.getWorld().spawn(location.add(x, 0D, z), TextDisplay.class, display -> {
            display.text(text);
            display.setBillboard(Display.Billboard.CENTER);
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0)); // Transparent background
            display.setShadowed(true);
            display.setInterpolationDuration(10); // Smooth movement
            display.setInterpolationDelay(-1); // Start immediately
        });

        activeHolograms.add(hologram);

        new BukkitRunnable()
        {
            int phase = 0;

            public void run()
            {
                phase++;
                if (phase >= 2)
                {
                    hologram.remove();
                    activeHolograms.remove(hologram);
                    this.cancel();
                    return;
                }
                // Use transformation for smooth floating up
                org.bukkit.util.Transformation transformation = hologram.getTransformation();
                transformation.getTranslation().add(new Vector3f(0, 1.0f, 0));
                hologram.setTransformation(transformation);
            }
        }.runTaskTimer(instance, 1L, duration);
    }
}
