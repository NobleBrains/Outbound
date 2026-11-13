package net.noblebrains.outbound.shulker;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.noblebrains.outbound.config.OutboundConfig;

public final class ShulkerPickItemHelper {

    public static final int SHULKER_SLOTS = 27;
    public static final int HOTBAR_SIZE = 9;
    public static final int MAIN_INVENTORY_SIZE = 36;

    private ShulkerPickItemHelper() {
    }

    public static boolean isShulkerBox(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    /**
     * Pure helper for testing Variant A slot-placement logic without a full ServerPlayer.
     *
     * @param hotbarItemEmpty     whether the chosen hotbar slot is currently empty
     * @param freeMainInventorySlot index in [9, 35] or -1 if main inventory is full
     * @return true if extracting into the hotbar slot is safe without losing/dropping items
     */
    public static boolean canPlaceExtractedItem(boolean hotbarItemEmpty, int freeMainInventorySlot) {
        return hotbarItemEmpty || freeMainInventorySlot >= HOTBAR_SIZE;
    }

    /**
     * Finds an empty slot in the main inventory (slots 9..35), excluding the hotbar.
     */
    public static int findFreeMainInventorySlot(Inventory inventory) {
        if (inventory == null) {
            return -1;
        }
        for (int slot = HOTBAR_SIZE; slot < MAIN_INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Attempts to extract a matching ItemStack from any Shulker Box in the player's inventory
     * and place it into a suitable hotbar slot (Variant A: displaced hotbar items are moved to
     * a free main inventory slot; aborts safely if the inventory is completely full).
     *
     * @return true if an item was extracted and placed into the hotbar
     */
    public static boolean tryPickFromShulkerBox(ServerPlayer player, ItemStack target) {
        if (!OutboundConfig.get().enableShulkerBoxPickBlock || player == null || target == null || target.isEmpty()) {
            return false;
        }
        if (player.hasInfiniteMaterials()) {
            return false;
        }

        Inventory inventory = player.getInventory();
        if (inventory.findSlotMatchingItem(target) != -1) {
            return false;
        }

        int targetHotbarSlot = inventory.getSuitableHotbarSlot();
        ItemStack displacedStack = inventory.getItem(targetHotbarSlot);
        int freeMainSlot = displacedStack.isEmpty() ? -1 : findFreeMainInventorySlot(inventory);

        if (!canPlaceExtractedItem(displacedStack.isEmpty(), freeMainSlot)) {
            return false;
        }

        // Search main inventory + hotbar (0..35) and offhand
        for (int invSlot = 0; invSlot < MAIN_INVENTORY_SIZE; invSlot++) {
            if (tryExtractFromSlot(inventory, invSlot, target, targetHotbarSlot, displacedStack, freeMainSlot)) {
                return true;
            }
        }
        return tryExtractFromSlot(inventory, Inventory.SLOT_OFFHAND, target, targetHotbarSlot, displacedStack, freeMainSlot);
    }

    private static boolean tryExtractFromSlot(
            Inventory inventory,
            int shulkerSlot,
            ItemStack target,
            int targetHotbarSlot,
            ItemStack displacedStack,
            int freeMainSlot
    ) {
        ItemStack shulkerStack = inventory.getItem(shulkerSlot);
        if (!isShulkerBox(shulkerStack) || shulkerStack.getCount() != 1) {
            return false;
        }

        ItemContainerContents contents = shulkerStack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return false;
        }

        NonNullList<ItemStack> items = NonNullList.withSize(SHULKER_SLOTS, ItemStack.EMPTY);
        contents.copyInto(items);

        for (int innerSlot = 0; innerSlot < items.size(); innerSlot++) {
            ItemStack candidate = items.get(innerSlot);
            if (candidate.isEmpty() || !ItemStack.isSameItemSameComponents(target, candidate)) {
                continue;
            }

            // Found matching item inside shulker box: update shulker contents
            items.set(innerSlot, ItemStack.EMPTY);
            shulkerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));

            // Move displaced hotbar item to free main inventory slot if needed (Variant A)
            if (!displacedStack.isEmpty() && freeMainSlot >= HOTBAR_SIZE) {
                inventory.setItem(freeMainSlot, displacedStack);
            }

            // Place extracted stack into chosen hotbar slot and select it
            inventory.setItem(targetHotbarSlot, candidate);
            inventory.setSelectedSlot(targetHotbarSlot);
            return true;
        }

        return false;
    }
}
