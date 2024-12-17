package blockplacerbreakerpatch.screen.slot;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ToolSlot extends Slot {
    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.contains(DataComponentTypes.TOOL);
    }

    public ToolSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
}
