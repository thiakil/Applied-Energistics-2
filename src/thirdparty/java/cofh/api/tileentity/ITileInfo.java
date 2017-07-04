package cofh.api.tileentity;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

public interface ITileInfo {
   void getTileInfo(List var1, EnumFacing var2, EntityPlayer var3, boolean var4);
}
