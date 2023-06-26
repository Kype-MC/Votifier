package de.rexlnico.votifier.items;

import de.rexlnico.votifier.MessageParser;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(@NotNull final ItemStack itemStack) {
        this.itemStack = itemStack;
        if (!itemStack.hasItemMeta()) {
            this.itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
        }
    }

    public ItemBuilder(@NotNull final Material type) {
        this.itemStack = new ItemStack(type);
        this.itemStack.setItemMeta(Bukkit.getItemFactory().getItemMeta(type));
    }

    public ItemStack build() {
        return itemStack.clone();
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    public ItemBuilder amount(final int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder clone() {
        return new ItemBuilder(itemStack.clone());
    }

    public ItemBuilder type(@NotNull final Material type) {
        itemStack.setType(type);
        return this;
    }

    public ItemBuilder material(@NotNull final Material material) {
        return type(material);
    }

    public ItemBuilder itemMeta(@NotNull final ItemMeta itemMeta) {
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder blockState(@NotNull final BlockState blockState) {
        BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
        meta.setBlockState(blockState);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder name(@NotNull final String displayName) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(MessageParser.parse(displayName));
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    @NotNull
    public ItemBuilder name(@NotNull final Component displayName) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    @NotNull
    public ItemBuilder noName() {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.empty());
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder lore(@NotNull final List<String> lines) {

        final ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setLore(lines);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder lore(@NotNull final String... lines) {

        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(Arrays.stream(lines).toList());
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder lore(@NotNull final Component... lines) {

        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.lore(Arrays.stream(lines).toList());
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addLore(@Nullable Component... lines) {
        if (lines == null || lines.length == 0) {
            return this;
        }
        ArrayList<Component> newLore = new ArrayList<>(Arrays.stream(lines).toList());
        ArrayList<Component> lore = new ArrayList<>();

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasLore()) {
            lore.addAll(itemMeta.lore());
        }
        lore.addAll(newLore);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder damage(final int damage) {

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof Damageable)) return this;
        ((Damageable) itemMeta).setDamage(damage);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder leatherColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(color);
        return this;
    }

    public ItemBuilder attributeModifier(Attribute attribute, AttributeModifier attributeModifier) {

        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addAttributeModifier(attribute, attributeModifier);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder enchant(@NotNull final Enchantment enchantment, final int level, final boolean ignoreLevelRestriction) {

        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder enchant(@NotNull final Enchantment enchantment, final int level) {
        return enchant(enchantment, level, true);
    }

    public ItemBuilder enchant(@NotNull final Enchantment enchantment) {
        return enchant(enchantment, 1, true);
    }

    public ItemBuilder enchantBook(@NotNull final Enchantment enchantment, int level) {
        if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            meta.addStoredEnchant(enchantment, level, true);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder enchantBook(@NotNull final Enchantment enchantment) {
        return enchantBook(enchantment, 1);
    }

    public ItemBuilder enchantIfTrue(boolean bool) {
        if (bool) {
            enchant(Enchantment.ARROW_DAMAGE);
            itemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder itemFlags(@NotNull final ItemFlag[] itemFlags) {

        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(itemFlags);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder itemFlags(@NotNull final List<ItemFlag> itemFlags) {
        return itemFlags(itemFlags.toArray(new ItemFlag[0]));
    }

    public ItemBuilder itemFlags(@NotNull final ItemFlag itemFlag) {
        return itemFlags(Collections.singletonList(itemFlag));
    }

    public <T> ItemBuilder persistentData(NamespacedKey key, PersistentDataType<?, T> type, T data) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, type, data);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    @Nullable
    public static <T> T persistentData(ItemStack itemStack, NamespacedKey key, PersistentDataType<?, T> type) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;
        final ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta.getPersistentDataContainer().get(key, type);
    }

    @Nullable
    public static <T> T persistentData(ItemBuilder itemBuilder, NamespacedKey key, PersistentDataType<?, T> type) {
        if (itemBuilder.itemStack == null || !itemBuilder.itemStack.hasItemMeta()) return null;
        final ItemMeta itemMeta = itemBuilder.itemStack.getItemMeta();
        return itemMeta.getPersistentDataContainer().get(key, type);
    }

    public ItemBuilder unbreakable(final boolean state) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(state);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    @NotNull
    public ItemBuilder glowing() {
        Enchantment enchantment;
        if (itemStack.getType() == Material.BOW) {
            enchantment = Enchantment.LURE;
        } else {
            enchantment = Enchantment.ARROW_INFINITE;
        }
        enchant(enchantment, 0, true);
        itemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    @NotNull
    public ItemBuilder skullOwner(@NotNull final OfflinePlayer offlinePlayer) {

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof SkullMeta)) return this;

        ((SkullMeta) itemMeta).setOwningPlayer(offlinePlayer);
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public static byte[] serializeItemStack(ItemStack itemStack) {
        var outputStream = new ByteArrayOutputStream();
        try (var dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(itemStack);
            return outputStream.toByteArray();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new byte[0];
    }

    public static ItemStack deserializeItemStack(byte[] data) {
        var inputStream = new ByteArrayInputStream(data);
        try (var dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}