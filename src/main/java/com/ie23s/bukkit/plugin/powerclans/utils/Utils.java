package com.ie23s.bukkit.plugin.powerclans.utils;

import com.ie23s.bukkit.plugin.powerclans.Core;

public class Utils {
    private final Core core;

    private final Logger logger;
    private final Warm warm;

    public Utils(Core core) {
        this.core = core;
        logger = new Logger(core);
        warm = new Warm(core);
    }

    public Logger getLogger() {
        return logger;
    }

    public Warm getWarm() {
        return warm;
    }
}
