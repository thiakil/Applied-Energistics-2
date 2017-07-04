package cofh.api.item;

import net.minecraft.item.ItemStack;

public interface IUpgradeItem {
   IUpgradeItem.UpgradeType getUpgradeType(ItemStack var1);

   int getUpgradeLevel(ItemStack var1);

   public static enum UpgradeType {
      INCREMENTAL,
      FULL,
      CREATIVE;
   }
}
