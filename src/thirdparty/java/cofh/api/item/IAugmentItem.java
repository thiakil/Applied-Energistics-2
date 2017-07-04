package cofh.api.item;

import net.minecraft.item.ItemStack;

public interface IAugmentItem {
   IAugmentItem.AugmentType getAugmentType(ItemStack var1);

   String getAugmentIdentifier(ItemStack var1);

   public static enum AugmentType {
      BASIC,
      ADVANCED,
      MODE,
      ENDER,
      CREATIVE;
   }
}
