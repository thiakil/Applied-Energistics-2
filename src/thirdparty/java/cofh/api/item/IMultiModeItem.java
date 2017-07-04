package cofh.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IMultiModeItem {
   int getMode(ItemStack var1);

   boolean setMode(ItemStack var1, int var2);

   boolean incrMode(ItemStack var1);

   boolean decrMode(ItemStack var1);

   int getNumModes(ItemStack var1);

   void onModeChange(EntityPlayer var1, ItemStack var2);
}
