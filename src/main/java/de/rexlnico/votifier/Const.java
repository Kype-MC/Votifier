package de.rexlnico.votifier;

import org.intellij.lang.annotations.Language;

public interface Const {

    @Language("SQL")
    String CREATE_VOTE_TABLE = """
        CREATE TABLE IF NOT EXISTS vote_coins(
        id int NOT NULL AUTO_INCREMENT,
        uuid VARCHAR(36) NOT NULL,
        coins int DEFAULT 0,
        PRIMARY KEY (id), UNIQUE KEY (uuid)
        )
        """;

    @Language("SQL")
    String GET_COINS = """
        SELECT IFNULL((SELECT coins FROM vote_coins WHERE uuid = ?), 0) AS coins;
        """;

    @Language("SQL")
    String ADD_COINS = """
        INSERT INTO vote_coins (uuid, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = coins + ?
        """;

    @Language("SQL")
    String ADD_COIN = """
        INSERT INTO vote_coins (uuid, coins) VALUES (?, 1) ON DUPLICATE KEY UPDATE coins = coins + 1
        """;

    @Language("SQL")
    String ADD_COIN_WITH_USERNAME = """
        INSERT INTO vote_coins (uuid, coins) VALUES ((SELECT uuid FROM Players WHERE username = ?), 1) ON DUPLICATE KEY UPDATE coins = coins + 1
        """;

    @Language("SQL")
    String REMOVE_COINS = """
            INSERT INTO vote_coins (uuid, coins) VALUES (?, (-1) * ?) ON DUPLICATE KEY UPDATE coins = coins - ?
            """;

    @Language("SQL")
    String SET_COINS = """
            INSERT INTO vote_coins (uuid, coins) VALUES (?, ?) ON DUPLICATE KEY UPDATE coins = ?
            """;

}
