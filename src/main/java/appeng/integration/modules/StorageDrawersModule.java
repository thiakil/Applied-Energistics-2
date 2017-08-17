package appeng.integration.modules;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.me.storage.ITickingMonitor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository;
import com.jaquadro.minecraft.storagedrawers.api.capabilities.IItemRepository.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.*;


/**
 * Wraps an Storage Drawers IDrawerGroup's IItemRepository cap in such a way that it can be used as an IMEInventory for items.
 */
public class StorageDrawersModule implements IIntegrationModule {

	public StorageDrawersModule(){
		IntegrationHelper.testClassExistence( this, IItemRepository.class );
	}
	@CapabilityInject(IItemRepository.class)
	public static Capability<IItemRepository> ITEM_REPOSITORY_CAPABILITY = null;

	public static boolean canHandle(ICapabilityProvider provider, EnumFacing side){
		return ITEM_REPOSITORY_CAPABILITY != null && provider.hasCapability(ITEM_REPOSITORY_CAPABILITY, side);
	}

	public static IMEInventory<IAEItemStack> getHandler(ICapabilityProvider provider, EnumFacing side){
		if (ITEM_REPOSITORY_CAPABILITY == null){
			return null;
		}
		IItemRepository repository = provider.getCapability(ITEM_REPOSITORY_CAPABILITY, side);
		return repository != null ? new RepositoryInventory(repository) : null;
	}

	private static class RepositoryInventory implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor {

		private final IItemRepository repo;

		private final Map<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();

		private BaseActionSource mySource;

		private ItemStack[] cachedStacks = new ItemStack[0];

		private IAEItemStack[] cachedAeStacks = new IAEItemStack[0];

		RepositoryInventory(IItemRepository r){
			this.repo = r;
		}

		@Override
		public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src) {
			IAEItemStack res =  AEItemStack.create(repo.insertItem(input.getItemStack(), type == Actionable.SIMULATE));
			if( type == Actionable.MODULATE )
			{
				this.onTick( input.getItemStack() );
			}
			return res;
		}

		@Override
		public IAEItemStack extractItems(IAEItemStack request, Actionable type, BaseActionSource src) {
			IAEItemStack res =   AEItemStack.create(repo.extractItem(request.getItemStack(), (int)request.getStackSize(), type == Actionable.SIMULATE));
			if( type == Actionable.MODULATE )
			{
				this.onTick( request.getItemStack() );
			}
			return res;
		}

		@Override
		public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
			NonNullList<ItemRecord> list = repo.getAllItems();
			for (ItemRecord record : list){
				IAEItemStack aeStack = AEItemStack.create(record.itemPrototype);
				if (aeStack != null) {
					aeStack.setStackSize(record.count);
					out.add(aeStack);
				}
			}
			return out;
		}

		@Override
		public StorageChannel getChannel() {
			return StorageChannel.ITEMS;
		}

		/* copied and modified from ItemHandlerAdapter below here */
		public TickRateModulation onTick(){
			return this.onTick(null);
		}

		public TickRateModulation onTick(@Nullable ItemStack expectedChange)
		{
			LinkedList<IAEItemStack> changes = new LinkedList<>();

			NonNullList<ItemRecord> repoList = repo.getAllItems();

			int slots = repoList.size();

			// Make room for new slots
			if( slots > cachedStacks.length )
			{
				cachedStacks = Arrays.copyOf( cachedStacks, slots );
				for (int i = 0; i<cachedStacks.length; i++){
					if (cachedStacks[i] == null)
						cachedStacks[i] = ItemStack.EMPTY;
				}
				cachedAeStacks = Arrays.copyOf( cachedAeStacks, slots );
			}

			for( int slot = 0; slot < slots; slot++ )
			{
				// Save the old stuff
				ItemStack oldIS = cachedStacks[slot];
				IAEItemStack oldAeIS = cachedAeStacks[slot];

				ItemStack newIS = repoList.get(slot).itemPrototype.copy();
				newIS.setCount(repoList.get(slot).count);

				// if we're expecting a change, make sure it's the expected one, else we leave it til the next onTick
				if ( expectedChange == null || Platform.itemComparisons().isSameItem( newIS, expectedChange ) || Platform.itemComparisons().isSameItem(oldIS, expectedChange ))
				{
					if( this.isDifferent( newIS, oldIS ) )
					{
						addItemChange( slot, oldAeIS, newIS, changes );
					}
					else if( !newIS.isEmpty() && !oldIS.isEmpty() )
					{
						addPossibleStackSizeChange( slot, oldAeIS, newIS, changes );
					}
				}
			}

			// Handle cases where the number of slots actually is lower now than before
			if( slots < cachedStacks.length )
			{
				for( int slot = slots; slot < cachedStacks.length; slot++ )
				{
					IAEItemStack aeStack = cachedAeStacks[slot];
					if( aeStack != null )
					{
						IAEItemStack a = aeStack.copy();
						a.setStackSize( -a.getStackSize() );
						changes.add( a );
					}
				}

				// Reduce the cache size
				cachedStacks = Arrays.copyOf( cachedStacks, slots );
				cachedAeStacks = Arrays.copyOf( cachedAeStacks, slots );
			}

			if( !changes.isEmpty() )
			{
				this.postDifference( changes );
				return TickRateModulation.URGENT;
			}
			else
			{
				return TickRateModulation.SLOWER;
			}
		}

		private void addItemChange( int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes )
		{
			// Completely different item
			cachedStacks[slot] = newIS;
			cachedAeStacks[slot] = AEItemStack.create( newIS );

			// If we had a stack previously in this slot, notify the newtork about its disappearance
			if( oldAeIS != null )
			{
				oldAeIS.setStackSize( -oldAeIS.getStackSize() );
				changes.add( oldAeIS );
			}

			// Notify the network about the new stack. Note that this is null if newIS was null
			if( cachedAeStacks[slot] != null )
			{
				changes.add( cachedAeStacks[slot] );
			}
		}

		private void addPossibleStackSizeChange( int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes )
		{
			// Still the same item, but amount might have changed
			long diff = newIS.getCount() - oldAeIS.getStackSize();

			if( diff != 0 )
			{
				IAEItemStack stack = oldAeIS.copy();
				stack.setStackSize( newIS.getCount() );

				cachedStacks[slot] = newIS;
				cachedAeStacks[slot] = stack;

				final IAEItemStack a = stack.copy();
				a.setStackSize( diff );
				changes.add( a );
			}
		}

		private boolean isDifferent( final ItemStack a, final ItemStack b )
		{
			if(a == b && b.isEmpty() )
			{
				return false;
			}

			return a.isEmpty() || b.isEmpty() || !Platform.itemComparisons().isSameItem(a, b);
		}

		private void postDifference( Iterable<IAEItemStack> a )
		{
			final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.listeners.entrySet().iterator();
			while( i.hasNext() )
			{
				final Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> l = i.next();
				final IMEMonitorHandlerReceiver<IAEItemStack> key = l.getKey();
				if( key.isValid( l.getValue() ) )
				{
					key.postChange( this, a, mySource );
				}
				else
				{
					i.remove();
				}
			}
		}

		@Override
		public void setActionSource( final BaseActionSource mySource )
		{
			this.mySource = mySource;
		}

		@Override
		public void addListener( final IMEMonitorHandlerReceiver<IAEItemStack> l, final Object verificationToken )
		{
			this.listeners.put( l, verificationToken );
		}

		@Override
		public void removeListener( final IMEMonitorHandlerReceiver<IAEItemStack> l )
		{
			this.listeners.remove( l );
		}
	}
}
