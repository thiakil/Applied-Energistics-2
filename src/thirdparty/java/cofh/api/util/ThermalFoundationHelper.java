package cofh.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ThermalFoundationHelper {
   public static void addBlacklistEntry(ItemStack entry) {
      if(!entry.isEmpty()) {
         NBTTagCompound toSend = new NBTTagCompound();
         toSend.setTag("entry", new NBTTagCompound());
         entry.writeToNBT(toSend.getCompoundTag("entry"));
         FMLInterModComms.sendMessage("thermalfoundation", "AddLexiconBlacklistEntry", toSend);
      }
   }

   public static void removeBlacklistEntry(ItemStack entry) {
      if(!entry.isEmpty()) {
         NBTTagCompound toSend = new NBTTagCompound();
         toSend.setTag("entry", new NBTTagCompound());
         entry.writeToNBT(toSend.getCompoundTag("entry"));
         FMLInterModComms.sendMessage("thermalfoundation", "RemoveLexiconBlacklistEntry", toSend);
      }
   }
}
