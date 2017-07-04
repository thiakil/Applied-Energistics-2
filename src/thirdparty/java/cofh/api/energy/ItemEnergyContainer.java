package cofh.api.energy;

import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Reference implementation of {@link IEnergyContainerItem}. Use/extend this or implement your own.
 *
 * @author King Lemming
 *
 */
public class ItemEnergyContainer extends Item implements IEnergyContainerItem {
   protected int capacity;
   protected int maxReceive;
   protected int maxExtract;

   public ItemEnergyContainer() {
   }

   public ItemEnergyContainer(int capacity) {
      this(capacity, capacity, capacity);
   }

   public ItemEnergyContainer(int capacity, int maxTransfer) {
      this(capacity, maxTransfer, maxTransfer);
   }

   public ItemEnergyContainer(int capacity, int maxReceive, int maxExtract) {
      this.capacity = capacity;
      this.maxReceive = maxReceive;
      this.maxExtract = maxExtract;
   }

   public ItemEnergyContainer setCapacity(int capacity) {
      this.capacity = capacity;
      return this;
   }

   public ItemEnergyContainer setMaxTransfer(int maxTransfer) {
      this.setMaxReceive(maxTransfer);
      this.setMaxExtract(maxTransfer);
      return this;
   }

   public ItemEnergyContainer setMaxReceive(int maxReceive) {
      this.maxReceive = maxReceive;
      return this;
   }

   public ItemEnergyContainer setMaxExtract(int maxExtract) {
      this.maxExtract = maxExtract;
      return this;
   }

	/* IEnergyContainerItem */
	@Override
   public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
      if(!container.hasTagCompound()) {
         container.setTagCompound(new NBTTagCompound());
      }

      int energy = container.getTagCompound().getInteger("Energy");
      int energyReceived = Math.min(this.capacity - energy, Math.min(this.maxReceive, maxReceive));
      if(!simulate) {
         energy = energy + energyReceived;
         container.getTagCompound().setInteger("Energy", energy);
      }

      return energyReceived;
   }

   public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
      if(container.getTagCompound() != null && container.getTagCompound().hasKey("Energy")) {
         int energy = container.getTagCompound().getInteger("Energy");
         int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
         if(!simulate) {
            energy = energy - energyExtracted;
            container.getTagCompound().setInteger("Energy", energy);
         }

         return energyExtracted;
      } else {
         return 0;
      }
   }

   public int getEnergyStored(ItemStack container) {
      return container.getTagCompound() != null && container.getTagCompound().hasKey("Energy")?container.getTagCompound().getInteger("Energy"):0;
   }

   public int getMaxEnergyStored(ItemStack container) {
      return this.capacity;
   }
}
