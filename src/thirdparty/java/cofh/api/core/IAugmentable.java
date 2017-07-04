package cofh.api.core;

import net.minecraft.item.ItemStack;

public interface IAugmentable {
   boolean installAugment(ItemStack var1);

   boolean isValidAugment(ItemStack var1);

   ItemStack[] getAugmentSlots();

   void updateAugmentStatus();
}
