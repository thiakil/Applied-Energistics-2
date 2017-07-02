package appeng.integration.modules.opencomputers.driver;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.network.ManagedEnvironment;

import appeng.core.AELog;
import appeng.integration.modules.opencomputers.environment.MEChestOrDriveEnvironment;
import appeng.tile.storage.TileDrive;


/**
 * Created by Thiakil on 23/04/2017.
 */
public class MEDriveDriver implements DriverBlock {

	@Override
	public boolean worksWith(World world, BlockPos pos, EnumFacing side) {

		TileEntity tile = world.getTileEntity(pos);

		return tile != null && tile instanceof TileDrive;
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		if (worksWith(world,pos,side)) {
			try {
				TileDrive tile = (TileDrive)world.getTileEntity(pos);
				if (tile != null) {
					return new MEChestOrDriveEnvironment(world, pos, tile, MEChestOrDriveEnvironment.Type.DRIVE);
				}
			} catch (Exception e){
				AELog.error("Error occurred during MEChestOrDriveEnvironment creation.", e);
			}
		}
		return null;
	}

}
