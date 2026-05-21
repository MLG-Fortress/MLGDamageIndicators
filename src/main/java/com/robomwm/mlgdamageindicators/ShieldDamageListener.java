package com.robomwm.mlgdamageindicators;

import com.robomwm.absorptionshields.event.ShieldDamageEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created on 7/7/2017.
 *
 * @author RoboMWM
 */
public class ShieldDamageListener implements Listener
{
    MLGDamageIndicators instance;

    ShieldDamageListener(MLGDamageIndicators plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        instance = plugin;
    }

    @EventHandler
    private void onShieldDamage(ShieldDamageEvent event)
    {
        if (!(event.getBaseEvent() instanceof EntityDamageByEntityEvent))
            return;
        if (event.getDamage() <= 0.05D)
            return;
        instance.displayIndicator(event.getVictim().getLocation().add(0, 2.2, 0), event.getDamage() / 2D, true, NamedTextColor.GOLD);
    }
}
