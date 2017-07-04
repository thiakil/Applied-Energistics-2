package cofh.api.block;

import java.util.ArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IDismantleable {
   ArrayList dismantleBlock(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, boolean var5);

   boolean canDismantle(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4);
}
