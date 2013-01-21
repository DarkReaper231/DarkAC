/*
 * AntiCheat for Bukkit.
 * Copyright (C) 2012-2013 AntiCheat Team | http://gravitydevelopment.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.h31ix.anticheat.event;

import net.h31ix.anticheat.Anticheat;
import net.h31ix.anticheat.manage.Backend;
import net.h31ix.anticheat.manage.CheckManager;
import net.h31ix.anticheat.manage.CheckType;
import net.h31ix.anticheat.util.CheckResult;
import net.h31ix.anticheat.util.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener extends EventListener {
    
    private final Backend backend = getBackend();
    private final CheckManager checkManager = getCheckManager();
    private final Configuration config = Anticheat.getManager().getConfiguration();
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (checkManager.willCheck(player, CheckType.FAST_INVENTORY)) {
                CheckResult result = backend.checkInventoryClicks(player);
                if (result.failed()) {
                    if (!config.silentMode()) {
                        getUserManager().getUser(player.getName()).restore(event.getInventory());
                        player.getInventory().clear();
                        player.damage(99999);
                    }
                    log(result.getMessage(), player, CheckType.FAST_INVENTORY);
                } else {
                    decrease(player);
                }
            }
        }
        
        Anticheat.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        getUserManager().getUser(event.getPlayer().getName()).setSnapshot(event.getInventory().getContents());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        getUserManager().getUser(event.getPlayer().getName()).removeSnapshot();
    }
}
