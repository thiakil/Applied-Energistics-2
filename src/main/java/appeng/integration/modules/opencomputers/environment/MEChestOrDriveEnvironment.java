package appeng.integration.modules.opencomputers.environment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractValue;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.integration.modules.opencomputers.ConverterCellInventory;
import appeng.me.storage.DriveWatcher;
import appeng.tile.AEBaseInvTile;


/**
 * Created by Thiakil on 26/05/2017.
 */
public class MEChestOrDriveEnvironment extends li.cil.oc.api.prefab.AbstractManagedEnvironment implements NamedBlock
{

	public static final String ENVIRONMENT_NAME_DRIVE = "me_drive";
	public static final String ENVIRONMENT_NAME_CHEST = "me_chest";
	protected World worldObj;
	protected IChestOrDrive drive;
	protected BlockPos drivePos;
	protected int driveDimension;
	protected Type myType;

	private ArrayList<CellInfo> loadNeeded = new ArrayList<>();

	@Override
	public void load(NBTTagCompound nbt) {
		super.load(nbt);

		if (this.worldObj == null){
			AELog.error("World obj is null in ENvironment load!!");
		}
	}

	public enum Type {
		DRIVE,
		CHEST,
	}

	private static final Object[] notPowered = new Object[] {null, "not powered"};

	public MEChestOrDriveEnvironment(World world, BlockPos pos, IChestOrDrive tile, Type type) {
		this.worldObj = world;
		this.drivePos = pos;
		this.drive = tile;
		this.driveDimension = worldObj.provider.getDimension();
		this.myType = type;
		setNode( Network.newNode(this, Visibility.Network).
				withComponent(this.myType == Type.DRIVE ? ENVIRONMENT_NAME_DRIVE : ENVIRONMENT_NAME_CHEST).
				create());
	}

	protected boolean isPowered(){//MUST be called before returning other data
		return this.drive.isPowered();
	}

	@Callback(doc = "function():boolean -- Tests if the chest or drive is powered and usable")
	public Object[] isPowered(Context context, Arguments args) {
		return new Object[]{ this.isPowered() };
	}

	@Callback(doc = "function():Integer -- Get the number of cells inserted")
	public Object[] getCellCount(Context context, Arguments args) {
		if (!isPowered())
			return notPowered;

		int foundCells = 0;

		for (int i = 0; i < this.drive.getCellCount(); i++){
			if (this.drive.getCellStatus(i) > 0){
				foundCells++;
			}
		}

		return new Object[]{ foundCells };
	}

	private List<IMEInventoryHandler> getCells(){
		List<IMEInventoryHandler> cellArray = this.drive.getCellArray( StorageChannel.ITEMS);
		cellArray.addAll(this.drive.getCellArray( StorageChannel.FLUIDS));
		return cellArray;
	}

	protected static int getHandlerSlot(IMEInventoryHandler handler, IChestOrDrive drive, Type tileType){
		ICellInventory cellInv = null;
		if (handler instanceof ICellInventoryHandler ){
			cellInv = ((ICellInventoryHandler) handler).getCellInv();
		} else if (handler instanceof DriveWatcher ) {
			IMEInventory internalHandler = ((DriveWatcher) handler).getInternal();
			if (internalHandler instanceof ICellInventoryHandler ){
				cellInv = ((ICellInventoryHandler) internalHandler).getCellInv();
			}
		}
		int cellSlot = -1;
		if (cellInv != null && drive instanceof AEBaseInvTile ) {
			ItemStack is = cellInv.getItemStack();
			AEBaseInvTile driveInv = (AEBaseInvTile)drive;
			int startSlot = tileType == Type.DRIVE ? 0 : 1;//ME Chest cell is at slot 1! 0 is for inserting items externally
			for (int slot = startSlot; slot < driveInv.getSizeInventory(); slot++){
				if (is.equals(driveInv.getStackInSlot(slot))){
					cellSlot = slot;
					break;
				}
			}
		}
		return cellSlot;
	}

	@Callback(doc = "function():table -- Get the cells in the drive or chest")
	public Object[] getCells(Context context, Arguments args) {
		if (!isPowered())
			return notPowered;

		try {
			ArrayList<CellInfo> cells = new ArrayList<>();

			List<IMEInventoryHandler> cellList = getCells();
			for (IMEInventoryHandler handler : cellList){
				cells.add(new CellInfo(handler, this.driveDimension, drivePos, this.drive, getHandlerSlot(handler, this.drive, this.myType)));
			}

			/*if (this.drive instanceof AEBaseInvTile) {
				int startSlot = this.myType == Type.DRIVE ? 0 : 1;//ME Chest cell is at slot 1! 0 is for inserting items externally
				for (int slot = 0; slot < ((AEBaseInvTile) this.drive).getSizeInventory(); slot++) {
					ItemStack is = ((AEBaseInvTile) this.drive).getStackInSlot(slot);
					if (is == null)
						continue;
					ICellHandler handler = api.registries().cell().getHandler( is );
					if (handler == null)
						continue;
					IMEInventoryHandler cell = handler.getCellInventory( is, this.drive, StorageChannel.ITEMS );
					if (cell == null)
						cell = handler.getCellInventory( is, this.drive, StorageChannel.FLUIDS );
					if (cell == null)
						continue;
					cells.add(new CellInfo(cell, this.driveDimension, drivePos, this.drive, slot));
				}
			}*/


			return new Object[]{ cells };
		} catch (Throwable t){
			AELog.error("Error caught while enumerating cells", t);
			return new Object[] { null, "unknown error" };
		}

	}

	@Override
	public String preferredName() {
		return this.myType == Type.DRIVE ? ENVIRONMENT_NAME_DRIVE : ENVIRONMENT_NAME_CHEST;
	}

	@Override
	public int priority() {
		return 5;
	}


	public static class CellInfo extends AbstractValue
	{

		private int containerSlot;
		private IMEInventoryHandler handler = null;
		private int containerDim;
		private BlockPos containerPos = null;
		private IChestOrDrive container = null;

		public CellInfo(){//for OC loading ONLY
		}

		public CellInfo(IMEInventoryHandler cell, int dim, BlockPos pos, IChestOrDrive container, int slot){
			this.containerSlot = slot;
			this.handler = cell;
			this.containerDim = dim;
			this.containerPos = pos;
			this.container = container;
		}

		@Callback(doc = "function():Integer -- Get the slot in which this cell exist in its drive/chest")
		public Object[] getSlot(Context context, Arguments args) {
			return new Object[]{this.containerSlot};
		}

		@Callback(doc = "function():table -- Get the cell stats")
		public Object[] getInfo(Context context, Arguments args) {
			if (this.handler == null || this.container == null) load();
			if (this.handler == null || this.container == null){
				return new Object[] { null, "CellInfo not loaded correctly" };
			}
			try {
				HashMap<Object, Object> output = new HashMap<>();
				ICellInventory cellInv = null;
				if (this.handler instanceof ICellInventoryHandler ){
					cellInv = ((ICellInventoryHandler) this.handler).getCellInv();
				} else if (this.handler instanceof DriveWatcher ) {
					IMEInventory internalHandler = ((DriveWatcher) this.handler).getInternal();
					if (internalHandler instanceof ICellInventoryHandler ){
						cellInv = ((ICellInventoryHandler) internalHandler).getCellInv();
					}
				} else {
					/*ItemStack cellStack = ((AEBaseInvTile) this.container).getStackInSlot(this.containerSlot);
					if (cellStack != null) {
						IMEInventoryHandler handler = api.registries().cell().getCellInventory(cellStack, this.container, StorageChannel.ITEMS);
						if (handler == null)
							handler = api.registries().cell().getCellInventory(cellStack, this.container, StorageChannel.FLUIDS);
						if (handler != null && handler instanceof ICellInventoryHandler) {
							cellInv = ((ICellInventoryHandler) handler).getCellInv();
						}
					}*/
				}
				if (cellInv != null) {
					ConverterCellInventory.convertCellInv(cellInv, output);
					IInventory formatInv = cellInv.getConfigInventory();
					ArrayList<ItemStack> formatted = new ArrayList<>();
					for (int slot = 0; slot<formatInv.getSizeInventory(); slot++){
						ItemStack is = formatInv.getStackInSlot(slot);
						if (is != null){
							formatted.add(is);
						}
					}
					output.put("preformattedItems", formatted);
					return new Object[]{output};
				}
			} catch (Throwable t){
				AELog.error("Error caught while getting cell info", t);
			}
			return new Object[]{};
		}

		@Callback(doc = "function():table -- Get the list of items/fluids stored in this cell")
		public Object[] getItemsStored(Context context, Arguments args) {
			if (this.handler == null || this.container == null) load();
			if (this.handler == null){
				return new Object[] { null, "CellInfo not loaded correctly" };
			}
			try {
				IItemList storedList = null;
				if (this.handler.getChannel() == StorageChannel.ITEMS)
					storedList = AEApi.instance().storage().createItemList();
				else if (this.handler.getChannel() == StorageChannel.FLUIDS)
					storedList = AEApi.instance().storage().createFluidList();
				else
					return new Object[] { null, "Unknown storage channel" };

				this.handler.getAvailableItems(storedList);

				return new Object[] { storedList };
			} catch (Throwable t){
				AELog.error("Error caught while getting cell contents", t);
			}
			return new Object[]{};
		}

		private void load(){
			try {
				WorldServer myWorld = DimensionManager.getWorld(this.containerDim);
				TileEntity tile = myWorld.getChunkFromBlockCoords(this.containerPos).getTileEntity(this.containerPos, Chunk.EnumCreateEntityType.CHECK);
				if (tile != null && tile instanceof IChestOrDrive && tile instanceof AEBaseInvTile ) {
					this.container = (IChestOrDrive) tile;
					ItemStack is = ((AEBaseInvTile) tile).getStackInSlot(this.containerSlot);
					if (is != null) {
						IMEInventoryHandler handler = AEApi.instance().registries().cell().getCellInventory(is, this.container, StorageChannel.ITEMS);
						if (handler == null)
							handler = AEApi.instance().registries().cell().getCellInventory(is, this.container, StorageChannel.FLUIDS);
						if (handler != null && handler instanceof ICellInventoryHandler ) {
							this.handler = handler;
							/*ICellInventory cellInv = ((ICellInventoryHandler) handler).getCellInv();
							if (cellInv != null) {
								this.handler = (IMEInventoryHandler) cellInv;
							}*/
						}
					}
				}
			} catch (Throwable t) {
				AELog.error("Could not reload CellInfo properly", t);
			}
		}

		@Override
		public void load(NBTTagCompound nbt) {
			super.load(nbt);
			this.containerSlot = nbt.getInteger("slot");
			this.containerDim = nbt.getInteger("dimension");
			this.containerPos = NBTUtil.getPosFromTag(nbt.getCompoundTag("pos"));
		}

		@Override
		public void save(NBTTagCompound nbt) {
			super.save(nbt);
			nbt.setInteger("slot", this.containerSlot);
			nbt.setInteger("dimension", this.containerDim);
			nbt.setTag("pos", NBTUtil.createPosTag(this.containerPos));
		}
	}

}
