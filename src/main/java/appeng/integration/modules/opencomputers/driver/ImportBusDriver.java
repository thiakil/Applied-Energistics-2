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
import appeng.integration.modules.opencomputers.environment.ImportBusEnvironment;
import appeng.parts.automation.PartImportBus;


/**
 * Created by Thiakil on 23/04/2017.
 */
public class ImportBusDriver implements DriverBlock {

	public static boolean hasImportBus(TileEntity tile){
		if (tile == null)
			return false;///i mean really...
		if (tile instanceof IPartHost ){
			IPartHost host = (IPartHost)tile;
			for (EnumFacing side : EnumFacing.values()){
				IPart part = host.getPart(side);
				if (part != null && part instanceof PartImportBus )
					return true;
			}
		}
		return false;
	}

	public static boolean hasImportBus(World world, BlockPos pos){
		try {
			if ( AEApi.instance().definitions().blocks().multiPart().isSameAs(world, pos)){
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof IPartHost ){
					IPartHost host = (IPartHost)te;
					for (EnumFacing side : EnumFacing.values()){
						IPart part = host.getPart(side);
						if (part != null && part instanceof PartImportBus )
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
		return hasImportBus(world,pos);
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		if (worksWith(world,pos,side)) {
			try {
				TileEntity tile = world.getTileEntity(pos);
				if (tile != null) {
					return new ImportBusEnvironment(tile);
				}
			} catch (Exception e){
				AELog.error("Error occurred during ImportBusEnvironment creation.", e);
			}
		}
		return null;
	}

}
