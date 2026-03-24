package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import com.ie23s.bukkit.plugin.powerclans.utils.Request;
import com.ie23s.bukkit.plugin.powerclans.utils.RequestType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /clan commands that do not fit a more specific group: PvP toggle and clan upgrade.
 */
public final class MiscCommands {

    private MiscCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan pvp — toggles clan PvP mode on or off.
     * Requires leader or moderator rank.
     */
    public static class Pvp extends BaseClanCommand {

        /** @param core the plugin core */
        public Pvp(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return perm(s, args) && inClan(s, clan) && isLeaderOrModer(s, clan, "errors._39");
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            clan.setPvp(!clan.isPvp());
            String key = clan.isPvp() ? "clan.pvp_1" : "clan.pvp_2";
            clan.broadcast(core.lang(key, s.getName()));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan upgrade — shows upgrade requirements, then queues a
     * {@link com.ie23s.bukkit.plugin.powerclans.utils.RequestType#UPGRADE} confirmation request.
     * Requires leader rank and all upgrade prerequisites to be met.
     */
    public static class Upgrade extends BaseClanCommand {

        /** @param core the plugin core */
        public Upgrade(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !inClan(s, clan) || !isLeader(s, clan, "errors._37")) return false;
            showUpgradeInfo((Player) s);
            if (!core.getLevelModule().getRequirements().canUpgrade(clan)) {
                s.sendMessage(core.lang("level.upgrade.cannot")); return false;
            }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            new Request(clan, (Player) s, user, RequestType.UPGRADE, args).send();
            s.sendMessage(core.lang("level.upgrade.cost",
                    core.getLevelModule().getRequirements().upgradeCost(clan)));
            s.sendMessage(core.lang("command.request"));
        }

        /**
         * Sends current upgrade requirements and ability previews to the player.
         */
        private void showUpgradeInfo(Player player) {
            core.getLevelModule().getRequirements().upgradeRequirements(player);
            player.sendMessage(core.lang("level.upgrade.ulget"));
            core.getLevelModule().getAbilities().upgradeAbilities(player, true);
        }
    }
}
