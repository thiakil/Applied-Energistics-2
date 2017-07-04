package cofh.api.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBlockConfigGui {
   boolean openConfigGui(IBlockAccess var1, BlockPos var2, EnumFacing var3, EntityPlayer var4);
}
