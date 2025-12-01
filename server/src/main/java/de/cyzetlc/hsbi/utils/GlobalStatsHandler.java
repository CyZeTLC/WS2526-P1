package de.cyzetlc.hsbi.utils;

import de.cyzetlc.hsbi.Server;

import java.util.Arrays;
import java.util.UUID;

public class GlobalStatsHandler {
    public static void saveBestScore(UUID uuid, long time, float dmg, int folders) {
        Server.getInstance().getQueryHandler().createBuilder(
                "INSERT INTO `stats` (`uuid`, `finish_time`, `dmg_taken`, `folders_collected`) VALUES (?,?,?,?);"
        ).addParameters(Arrays.asList(uuid.toString(), time, dmg, folders)).executeUpdateAsync();
    }
}
