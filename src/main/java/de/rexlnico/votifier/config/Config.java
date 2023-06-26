package de.rexlnico.votifier.config;

import org.bukkit.Material;

import java.util.List;

public class Config {

    public List<VoteSite> voteSites = List.of(new VoteSite("test", Material.GOLD_BLOCK, 10, "https://google.de/search?q=%username%"));
    public List<VoteShopEntry> voteShopEntries = List.of(new VoteShopEntry("test", Material.GOLD_BLOCK, 10, 10,
            List.of("say %player% hat was gekauft in vote shop oder so", "test command with %uuid%")
    ));

}
