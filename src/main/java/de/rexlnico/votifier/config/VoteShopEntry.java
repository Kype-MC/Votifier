package de.rexlnico.votifier.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
@AllArgsConstructor
public class VoteShopEntry {

    private String name;
    private Material icon;
    private int slot;
    private int cost;
    private List<String> commands;

}
