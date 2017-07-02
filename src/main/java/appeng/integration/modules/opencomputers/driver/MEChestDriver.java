package appeng.integration.modules.opencomputers.driver;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.network.ManagedEnvironment;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.core.AELog;
import appeng.integration.modules.opencomputers.environment.MEChestOrDriveEnvironment;
import appeng.tile.storage.TileChest;


/**
 * Created by Thiakil on 23/04/2017.
 */
public class MEChestDriver implements DriverBlock {

	@Override
	public boolean worksWith(World world, BlockPos pos, EnumFacing side) {

		if (side == EnumFacing.NORTH)
			return false;

		TileEntity tile = world.getTileEntity(pos);

		return tile != null && tile instanceof TileChest;
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		if (worksWith(world,pos,side)) {
			try {
				IChestOrDrive tile = (IChestOrDrive)world.getTileEntity(pos);
				if (tile != null) {
					return new MEChestOrDriveEnvironment(world, pos, tile, MEChestOrDriveEnvironment.Type.CHEST);
				}
			} catch (Exception e){
				AELog.error("Error occurred during MEChestOrDriveEnvironment creation.", e);
			}
		}
		return null;
	}

}
