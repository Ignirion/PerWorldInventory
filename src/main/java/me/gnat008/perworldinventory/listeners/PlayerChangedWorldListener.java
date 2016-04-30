/*
 * Copyright (C) 2014-2015  Gnat008
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.gnat008.perworldinventory.listeners;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.config.Settings;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerChangedWorldListener implements Listener {

    private GroupManager manager;
    private PWIPlayerManager playerManager;
    private PerWorldInventory plugin;

    public PlayerChangedWorldListener(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGroupManager();
        this.playerManager = plugin.getPlayerManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldFrom = event.getFrom().getName();
        String worldTo = player.getWorld().getName();
        Group groupFrom = manager.getGroupFromWorld(worldFrom);
        Group groupTo = manager.getGroupFromWorld(worldTo);

        if (groupFrom == null) { // If true, world is unconfigured
            if (manager.getGroup("__unconfigured__") != null) {
                groupFrom = manager.getGroup("__unconfigured__");
                groupFrom.addWorld(worldFrom);
            } else {
                List<String> worlds = new ArrayList<>();
                worlds.add(worldFrom);
                groupFrom = new Group("__unconfigured__", worlds, GameMode.SURVIVAL);
            }
        }

        playerManager.addPlayer(player, groupFrom);

        if (player.hasPermission("perworldinventory.bypass.world"))
            return;

        if (groupFrom.getName().equals("__unconfigured__") && Settings.getBoolean("share-if-unconfigured")) {
            return;
        }

        if (!groupFrom.containsWorld(worldTo)) {
            if (groupTo == null) { // If true, world is unconfigured
                if (manager.getGroup("__unconfigured__") != null) {
                    groupTo = manager.getGroup("__unconfigured__");
                    groupTo.addWorld(worldTo);
                } else {
                    List<String> worlds = new ArrayList<>();
                    worlds.add(worldTo);
                    groupTo = new Group("__unconfigured__", worlds, GameMode.SURVIVAL);
                }
            }

            if (groupTo.getName().equals("__unconfigured__") && Settings.getBoolean("share-if-unconfigured")) {
                return;
            }

            if (Settings.getBoolean("separate-gamemode-inventories")) {
                playerManager.getPlayerData(groupTo, player.getGameMode(), player);

                if (Settings.getBoolean("manage-gamemodes")) {
                    player.setGameMode(groupTo.getGameMode());
                }
            } else {
                playerManager.getPlayerData(groupTo, GameMode.SURVIVAL, player);
            }
        }
    }
}
