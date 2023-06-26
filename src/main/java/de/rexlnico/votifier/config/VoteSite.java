package de.rexlnico.votifier.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public class VoteSite {

    private String name;
    private Material icon;
    private int slot;
    private String url;

}
