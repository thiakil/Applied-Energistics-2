package appeng.integration.modules.theoneprobe.tile;


import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;

import appeng.tile.AEBaseTile;


/**
 * Created by Thiakil on 14/07/2017.
 */
public class CustomNameProvider implements ITileProbInfoProvider
{
	@Override
	public void addProbeInfo( AEBaseTile tile, ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data )
	{
		if (tile.hasAEDisplayName()){
			probeInfo.text( TextStyleClass.NAME+ TextFormatting.ITALIC.toString()+tile.getAEDisplayName() );
		}
	}
}
