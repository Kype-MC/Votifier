/*
 * Copyright (C) 2011 Vex Software LLC
 * This file is part of Votifier.
 *
 * Votifier is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Votifier is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Votifier.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rexlnico.votifier.model.listeners;

import java.util.logging.Logger;

import de.rexlnico.votifier.Const;
import de.rexlnico.votifier.Votifier;
import de.rexlnico.votifier.model.Vote;
import de.rexlnico.votifier.model.VoteListener;

public class BasicVoteListener implements VoteListener {

    /**
     * The logger instance.
     */
    private Logger log = Logger.getLogger("BasicVoteListener");

    public void voteMade(Vote vote) {
        log.info("Received: " + vote);
        Votifier.getInstance().databaseConnection.execute(Const.ADD_COIN_WITH_USERNAME, vote.getUsername()).thenAccept(aBoolean -> {
            if (aBoolean) {
                log.info("Added 1 Votecoin to " + vote.getUsername());
            } else {
                log.info("Could not add Votecoin to " + vote.getUsername());
            }
        }).exceptionally(throwable -> {
            log.info("Could not add Votecoin to " + vote.getUsername());
            throwable.printStackTrace();
            return null;
        });
    }

}
