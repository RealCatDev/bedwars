package me.catdev.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class EnvEvents implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent ev) {
        // TODO: Maybe check if is in map wizard or add option to change it
        ev.setCancelled(true);
    }

}
