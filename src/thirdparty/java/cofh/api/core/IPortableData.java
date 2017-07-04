package cofh.api.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IPortableData {
   String getDataType();

   void readPortableData(EntityPlayer var1, NBTTagCompound var2);

   void writePortableData(EntityPlayer var1, NBTTagCompound var2);
}
