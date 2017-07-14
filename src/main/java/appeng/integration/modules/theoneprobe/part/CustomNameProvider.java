package appeng.integration.modules.theoneprobe.part;


import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;

import appeng.api.parts.IPart;


/**
 * Created by Thiakil on 14/07/2017.
 */
public class CustomNameProvider implements IPartProbInfoProvider
{
	@Override
	public void addProbeInfo( IPart part, ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data )
	{
		ItemStack is = data.getPickBlock();
		if (is != null && is.hasDisplayName()){
			String name = is.getDisplayName();
			is.clearCustomName();
			probeInfo.text( TextStyleClass.NAME+TextFormatting.ITALIC.toString()+name );
		}
	}
}
