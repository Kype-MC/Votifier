package de.rexlnico.votifier.items;

import de.rexlnico.votifier.Votifier;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.function.Consumer;

public class InventoryBuilder {

    private final Inventory inventory;
    private boolean deleteOnClose = false;
    private final ArrayList<Listener> listeners;
    private Listener closeListener;
    private Listener deleteCloseListener;

    public InventoryBuilder(Inventory inventory) {
        listeners = new ArrayList<>();
        this.inventory = inventory;
    }

    public InventoryBuilder(InventoryHolder owner, int size, Component title) {
        listeners = new ArrayList<>();
        this.inventory = Bukkit.createInventory(owner, size, title);
    }

    public InventoryBuilder(int size, Component title) {
        listeners = new ArrayList<>();
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    public InventoryBuilder(int size, Component title, boolean deleteOnClose) {
        listeners = new ArrayList<>();
        this.inventory = Bukkit.createInventory(null, size, title);
        if (deleteOnClose) {
            this.deleteOnClose = true;
            deleteCloseListener = new Listener() {
                @EventHandler
                public void onClose(InventoryCloseEvent event) {
                    if (event.getInventory() == inventory) {
                        listeners.forEach(HandlerList::unregisterAll);
                        listeners.clear();
                        if (closeListener != null) {
                            HandlerList.unregisterAll(closeListener);
                            HandlerList.unregisterAll(deleteCloseListener);
                        }
                    }
                }
            };
            Bukkit.getPluginManager().registerEvents(deleteCloseListener, Votifier.getInstance());
        }
    }

    public InventoryBuilder deleteOnClose() {
        if (deleteOnClose) {
            return this;
        }
        this.deleteOnClose = true;
        deleteCloseListener = new Listener() {
            @EventHandler
            public void onClose(InventoryCloseEvent event) {
                if (event.getInventory() == inventory) {
                    listeners.forEach(HandlerList::unregisterAll);
                    listeners.clear();
                    if (closeListener != null) {
                        HandlerList.unregisterAll(closeListener);
                        HandlerList.unregisterAll(deleteCloseListener);
                    }
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(deleteCloseListener, Votifier.getInstance());
        return this;
    }

    public InventoryBuilder setItem(int slot, ItemStack itemStack) {
        inventory.setItem(slot, itemStack);
        return this;
    }

    public InventoryBuilder fillInv(ItemStack itemStack) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, itemStack);
        }
        return this;
    }

    public InventoryBuilder fillInv(ItemBuilder itemBuilder) {
        fillInv(itemBuilder.build());
        return this;
    }

    public InventoryBuilder setItem(int slot, ItemBuilder itemBuilder) {
        inventory.setItem(slot, itemBuilder.build());
        return this;
    }

    public InventoryBuilder setItems(int from, int to, ItemStack itemStack) {
        for (int i = from; i <= to; i++) {
            inventory.setItem(i, itemStack);
        }
        return this;
    }

    public InventoryBuilder setItems(int from, int to, ItemBuilder itemBuilder) {
        return setItems(from, to, itemBuilder.build());
    }

    public InventoryBuilder open(Player player) {
        player.openInventory(inventory);
        return this;
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public void clearListeners() {
        listeners.forEach(HandlerList::unregisterAll);
        if (closeListener != null) {
            HandlerList.unregisterAll(closeListener);
        }
        listeners.clear();
    }

    public InventoryBuilder addClickListener(int slot, Consumer<InventoryClickEvent> onClick) {
        Listener listener = new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent event) {
                if (event.getClickedInventory() == inventory && event.getSlot() == slot) {
                    onClick.accept(event);
                }
            }
        };
        listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, Votifier.getInstance());
        return this;
    }

    public InventoryBuilder addClickListener(Consumer<InventoryClickEvent> onClick) {
        Listener listener = new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent event) {
                if (event.getClickedInventory() == inventory) {
                    onClick.accept(event);
                }
            }
        };
        listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, Votifier.getInstance());
        return this;
    }

    public InventoryBuilder addOpenListener(Consumer<InventoryOpenEvent> onOpen) {
        Listener listener = new Listener() {
            @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
            public void onClick(InventoryOpenEvent event) {
                if (event.getInventory() == inventory) {
                    onOpen.accept(event);
                }
            }
        };
        listeners.add(listener);
        Bukkit.getPluginManager().registerEvents(listener, Votifier.getInstance());
        return this;
    }

    public InventoryBuilder setCloseListener(Consumer<InventoryCloseEvent> onClose) {
        if (closeListener != null) {
            HandlerList.unregisterAll(closeListener);
        }
        Listener listener = new Listener() {
            @EventHandler
            public void onClose(InventoryCloseEvent event) {
                if (event.getInventory() == inventory) {
                    onClose.accept(event);
                }
            }
        };
        closeListener = listener;
        Bukkit.getPluginManager().registerEvents(listener, Votifier.getInstance());
        return this;
    }

}