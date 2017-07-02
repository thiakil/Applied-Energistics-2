package appeng.integration.modules.opencomputers.driver;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.network.ManagedEnvironment;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.core.AELog;
import appeng.integration.modules.opencomputers.environment.ExportBusEnvironment;
import appeng.parts.automation.PartExportBus;


/**
 * Created by Thiakil on 23/04/2017.
 */
public class ExportBusDriver implements DriverBlock {

	public static boolean hasExportBus(TileEntity tile){
		if (tile == null)
			return false;///i mean really...
		if (tile instanceof IPartHost ){
			IPartHost host = (IPartHost)tile;
			for (EnumFacing side : EnumFacing.values()){
				IPart part = host.getPart(side);
				if (part != null && part instanceof PartExportBus )
					return true;
			}
		}
		return false;
	}

	public static boolean hasExportBus(World world, BlockPos pos){
		try {
			if ( AEApi.instance().definitions().blocks().multiPart().isSameAs(world, pos)){
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof IPartHost ){
					IPartHost host = (IPartHost)te;
					for (EnumFacing side : EnumFacing.values()){
						IPart part = host.getPart(side);
						if (part != null && part instanceof PartExportBus )
							return true;
					}
				}
			}
			return false;
		} catch (Exception e){
			return false;
		}
	}

	@Override
	public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
		return hasExportBus(world,pos);
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		if (worksWith(world,pos,side)) {
			try {
				TileEntity tile = world.getTileEntity(pos);
				if (tile != null) {
					return new ExportBusEnvironment(tile);
				}
			} catch (Exception e){
				AELog.error("Error occurred during ExportBusEnvironment creation.", e);
			}
		}
		return null;
	}

}
