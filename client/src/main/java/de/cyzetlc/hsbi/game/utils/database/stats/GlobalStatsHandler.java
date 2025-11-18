package de.cyzetlc.hsbi.game.utils.database.stats;

import de.cyzetlc.hsbi.game.Game;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

public class GlobalStatsHandler {
    public static void addTimePlayed(long timePlayed) {
        Game.getInstance().getQueryHandler().createBuilder(
                "UPDATE globalStats SET `time_played` += ?"
        ).addParameter(timePlayed).executeUpdateAsync();
    }

    public static void addLevelStarted(int amount) {
        Game.getInstance().getQueryHandler().createBuilder(
                "UPDATE globalStats SET `levels_started` += ?"
        ).addParameter(amount).executeUpdateAsync();
    }

    public static void addLevelFinished(int amount) {
        Game.getInstance().getQueryHandler().createBuilder(
                "UPDATE globalStats SET `levels_finished` += ?"
        ).addParameter(amount).executeUpdateAsync();
    }

    public static void addDeaths(int amount) {
        Game.getInstance().getQueryHandler().createBuilder(
                "UPDATE globalStats SET `deaths` += ?"
        ).addParameter(amount).executeUpdateAsync();
    }

    public static long getTimePlayed() {
        try {
            CachedRowSet rs = Game.getInstance().getQueryHandler().createBuilder(
                    "SELECT `time_played` FROM `globalStats`"
            ).executeQuerySync();
            return rs.getLong("time_played");
        } catch (SQLException e) {
            Game.getLogger().error(e.getMessage());
            return 0;
        }
    }
}
