package com.ie23s.bukkit.plugin.powerclans.command.clan;

import com.ie23s.bukkit.plugin.powerclans.Core;
import com.ie23s.bukkit.plugin.powerclans.api.ClanDataKey;
import com.ie23s.bukkit.plugin.powerclans.clan.Clan;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * /clan commands that manage the clan treasury: viewing the balance,
 * depositing funds, and withdrawing funds.
 */
public final class EconomyCommands {

    private EconomyCommands() {}

    // -------------------------------------------------------------------------

    /**
     * /clan balance — displays the clan's current treasury balance.
     */
    public static class Balance extends BaseClanCommand {

        /** @param core the plugin core */
        public Balance(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            return perm(s, args) && inClan(s, clan);
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            s.sendMessage(core.lang("clan.balance", clan.getDouble(ClanDataKey.BALANCE)));
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan deposit &lt;amount&gt; — transfers funds from the sender's personal balance
     * into the clan treasury. Requires Vault.
     */
    public static class Deposit extends BaseClanCommand {

        /** @param core the plugin core */
        public Deposit(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !vaultOk(s) || !inClan(s, clan)) return false;
            if (args.length == 1) { s.sendMessage(core.lang("clan.deposit_1")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            int amount = parsePositiveInt(s, args[1], "clan.deposit_2");
            if (amount < 0) return;
            if (!playerHasFunds(s, amount)) return;
            transferToTreasury(s, clan, amount);
            s.sendMessage(core.lang("clan.deposit_4", amount));
        }

        /**
         * Checks that the sender has at least {@code amount} in their personal balance.
         * Sends {@code clan.deposit_3} and returns {@code false} if not.
         */
        @SuppressWarnings("java:S108")
        private boolean playerHasFunds(CommandSender s, int amount) {
            try {
                if (!Core.getVault().has((OfflinePlayer) s, amount)) {
                    s.sendMessage(core.lang("clan.deposit_3")); return false;
                }
            } catch (Exception ignore) {}
            return true;
        }

        /**
         * Withdraws {@code amount} from the sender and credits it to the clan treasury.
         */
        @SuppressWarnings("java:S108")
        private void transferToTreasury(CommandSender s, Clan clan, int amount) {
            try {
                Core.getVault().withdrawPlayer((OfflinePlayer) s, amount);
                clan.update(ClanDataKey.BALANCE, clan.getDouble(ClanDataKey.BALANCE) + amount);
            } catch (Exception ignore) {}
        }
    }

    // -------------------------------------------------------------------------

    /**
     * /clan take &lt;amount&gt; — withdraws funds from the clan treasury into the leader's
     * personal balance. Requires leader rank and Vault.
     */
    public static class Take extends BaseClanCommand {

        /** @param core the plugin core */
        public Take(Core core) { super(core); }

        @Override
        public boolean validate(CommandSender s, String[] args, Clan clan, String user) {
            if (!perm(s, args) || !vaultOk(s) || !inClan(s, clan)
                    || !isLeader(s, clan, "errors._36")) return false;
            if (args.length == 1) { s.sendMessage(core.lang("clan.take_1")); return false; }
            return true;
        }

        @Override
        public void execute(CommandSender s, String[] args, Clan clan, String user) {
            int amount = parsePositiveInt(s, args[1], "clan.take_2");
            if (amount < 0) return;
            if (!treasuryHasFunds(s, clan, amount)) return;
            withdrawFromTreasury(s, clan, amount);
            s.sendMessage(core.lang("clan.take_4", amount));
        }

        /**
         * Checks that the clan treasury has at least {@code amount}.
         * Sends {@code clan.take_3} and returns {@code false} if not.
         */
        private boolean treasuryHasFunds(CommandSender s, Clan clan, int amount) {
            if (clan.getDouble(ClanDataKey.BALANCE) < amount) {
                s.sendMessage(core.lang("clan.take_3")); return false;
            }
            return true;
        }

        /**
         * Deposits {@code amount} into the sender's account and deducts it from the treasury.
         */
        @SuppressWarnings("java:S108")
        private void withdrawFromTreasury(CommandSender s, Clan clan, int amount) {
            try { Core.getVault().depositPlayer((OfflinePlayer) s, amount); } catch (Exception ignore) {}
            clan.update(ClanDataKey.BALANCE, clan.getDouble(ClanDataKey.BALANCE) - amount);
        }
    }
}
