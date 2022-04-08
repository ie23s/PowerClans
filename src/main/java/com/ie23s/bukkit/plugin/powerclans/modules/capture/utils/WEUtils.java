package com.ie23s.bukkit.plugin.powerclans.modules.capture.utils;

import com.ie23s.bukkit.plugin.powerclans.Plugins;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;

import java.util.Objects;

public class WEUtils {
    public static Region getPlayerSelection(Player player) {
        LocalSession localSession = Objects.requireNonNull(Plugins.getWE()).getSession(player);

        Region selection;

        try {
            selection = localSession.getSelection(localSession.getSelectionWorld());
        } catch (IncompleteRegionException e) {
            return null;
        }

        if (!(selection instanceof CuboidRegion)) {
            return null;
        }
        return selection;
    }
}
