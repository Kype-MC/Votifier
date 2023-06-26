package de.rexlnico.votifier.commands;

import cloud.commandframework.annotations.CommandMethod;
import de.rexlnico.votifier.Const;
import de.rexlnico.votifier.MessageParser;
import de.rexlnico.votifier.Votifier;
import de.rexlnico.votifier.items.InventoryBuilder;
import de.rexlnico.votifier.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class VoteCommands {

    @CommandMethod(value = "vote", requiredSender = Player.class)
    public void voteCommand(Player player) {
        var inventory = new InventoryBuilder(9 * 3, MessageParser.parse(Votifier.getInstance().messages.voteGuiTitle));
        inventory.addClickListener(clickEvent -> clickEvent.setCancelled(true));
        inventory.fillInv(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()));
        Votifier.getInstance().voteConfig.voteSites.forEach(voteSite -> {
            inventory.setItem(
                    voteSite.getSlot(),
                    new ItemBuilder(voteSite.getIcon()).name(MessageParser.parse(voteSite.getName()))
            );
            inventory.addClickListener(voteSite.getSlot(), clickEvent -> {
                player.sendMessage(MessageParser.parse(
                        "<click:open_url:'" +
                                voteSite.getUrl().replace("%username%", player.getName())
                                + "'>" + voteSite.getName() + "</click>")
                );
            });
        });
        inventory.setItem(26, new ItemBuilder(Material.BOOK).name(Votifier.getInstance().messages.voteShopGui));
        inventory.addClickListener(26, clickEvent -> openVoteShop(player));
        inventory.open(player);
    }

    private void openVoteShop(Player player) {
        Votifier.getInstance().databaseConnection.get(Const.GET_COINS, new String[]{"coins"}, player.getUniqueId().toString()).thenAccept(objects -> {
            int coins = Integer.parseInt(objects[0].toString());
            var inventory = new InventoryBuilder(9 * 3, MessageParser.parse(Votifier.getInstance().messages.voteShopGui.replace("%coins%", String.valueOf(coins))));
            inventory.fillInv(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()));
            Votifier.getInstance().voteConfig.voteShopEntries.forEach(entry -> {
                inventory.setItem(entry.getSlot(), new ItemBuilder(entry.getIcon()).name(entry.getName()));
                inventory.addClickListener(entry.getSlot(), clickEvent -> {
                    if (coins < entry.getCost()) {
                        player.sendMessage(MessageParser.parse(Votifier.getInstance().messages.notEnoughCoins));
                        return;
                    }
                    entry.getCommands().forEach(s -> {
                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                s.replace("%name%", player.getName()).replace("%uuid%", player.getUniqueId().toString())
                        );
                    });
                    Votifier.getInstance().databaseConnection
                            .execute(Const.REMOVE_COINS, player.getUniqueId().toString(), entry.getCost(), entry.getCost())
                            .thenAccept(aBoolean -> openVoteShop(player));
                });
            });
        });
    }

}
