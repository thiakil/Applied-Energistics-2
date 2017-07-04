package cofh.api.block;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IBlockInfo {
   void getBlockInfo(List var1, IBlockAccess var2, BlockPos var3, EnumFacing var4, EntityPlayer var5, boolean var6);
}
