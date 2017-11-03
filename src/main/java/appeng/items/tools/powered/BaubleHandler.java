package appeng.items.tools.powered;

import appeng.capabilities.Capabilities;
import baubles.api.BaubleType;
import baubles.api.cap.BaubleItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class BaubleHandler implements ICapabilityProvider {

	private final @Nullable
	ICapabilityProvider parent;

	private final BaubleItem bauble;

	public BaubleHandler(ICapabilityProvider p, BaubleType bType){
		parent = p;
		bauble = new BaubleItem( bType ) {
			@Override
			public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player )
			{
				return true;
			}
		};
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing )
	{
		return capability == Capabilities.CAPABILITY_ITEM_BAUBLE || parent != null && parent.hasCapability( capability, facing );
	}

	@Nullable
	@Override
	public <T> T getCapability( @Nonnull Capability<T> capability, @Nullable EnumFacing facing )
	{
		if (capability == Capabilities.CAPABILITY_ITEM_BAUBLE){
			return Capabilities.CAPABILITY_ITEM_BAUBLE.cast(bauble);
		}
		return parent != null ? parent.getCapability( capability, facing ) : null;
	}
}
