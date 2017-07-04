package cofh.api.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IToolHammer {
   boolean isUsable(ItemStack var1, EntityLivingBase var2, BlockPos var3);

   boolean isUsable(ItemStack var1, EntityLivingBase var2, Entity var3);

   void toolUsed(ItemStack var1, EntityLivingBase var2, BlockPos var3);

   void toolUsed(ItemStack var1, EntityLivingBase var2, Entity var3);
}
