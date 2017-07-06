/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.util.item;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.List;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.core.AELog;
import appeng.util.Platform;


public final class AEItemStack extends AEStack<IAEItemStack> implements IAEItemStack, Comparable<AEItemStack>
{

	private AEItemDef def;

	private static Field capsField = null;
	static {
		try
		{
			capsField = ItemStack.class.getDeclaredField( "capabilities" );
			capsField.setAccessible( true );
		}
		catch( NoSuchFieldException e )
		{
			AELog.warn( "Caps field has changed in ItemStack, you may lose some data!" );
		}
	}

	private enum VANILLA_NBT_KEYS
	{
		ITEM_ID( "id" ),
		DAMAGE( "Damage" ),
		STACKSIZE( "Count" ),
		COMPOUND( "tag" ),
		CAPS( "ForgeCaps" ),
		;

		private String val;

		VANILLA_NBT_KEYS( String val ){
			this.val = val;
		}

		public String toString()
		{
			return this.val;
		}
	}

	private enum AE2_NBT_KEYS
	{
		STACKSIZE( "Cnt" ),
		REQUESTABLE( "Req" ),
		CRAFTABLE( "Craft" ),
		COMBINED_TAG( "compound" ),
		COMBINED_CAPS( "caps" ),
		;

		private String val;

		AE2_NBT_KEYS( String val ){
			this.val = val;
		}

		public String toString()
		{
			return this.val;
		}
	}

	private AEItemStack( final AEItemStack is )
	{
		this.setDefinition( is.getDefinition() );
		this.setStackSize( is.getStackSize() );
		this.setCraftable( is.isCraftable() );
		this.setCountRequestable( is.getCountRequestable() );
	}

	private AEItemStack( final ItemStack is )
	{
		if (is.isEmpty()) {
			throw new InvalidParameterException("null is not a valid ItemStack for AEItemStack.");
		}

		final Item item = is.getItem();
		if( item == null )
		{
			throw new InvalidParameterException( "Contained item is null, thus not a valid ItemStack for AEItemStack." );
		}

		this.setDefinition( new AEItemDef( item ) );

		if( this.getDefinition().getItem() == null )
		{
			throw new InvalidParameterException( "This ItemStack is bad, it has a null item." );
		}

		/*
		 * Prevent an Item from changing the damage value on me... Either, this or a core mod.
		 */

		/*
		 * Super hackery.
		 * is.itemID = appeng.api.Materials.matQuartz.itemID; damageValue = is.getItemDamage(); is.itemID = itemID;
		 */

		/*
		 * Kinda hackery
		 */
		this.getDefinition().setDamageValue( is.itemDamage );
		if( !is.getItem().isDamageable() )
		{
			this.getDefinition().setDisplayDamage( Integer.MAX_VALUE );
		}
		else
		{
			this.getDefinition().setDisplayDamage( (int) ( is.getItem().getDurabilityForDisplay( is ) * Integer.MAX_VALUE ) );
		}
		this.getDefinition().setMaxDamage( is.getMaxDamage() );

		final NBTTagCompound tagCompound = is.getTagCompound();
		if( tagCompound != null )
		{
			this.getDefinition().setTagCompound( (AESharedNBT) AESharedNBT.getSharedTagCompound( tagCompound, is ) );
		}

		this.getDefinition().setCapsTag( getCapsTag( is ) );

		this.setStackSize( is.getCount() );
		this.setCraftable( false );
		this.setCountRequestable( 0 );

		this.getDefinition().reHash();
		this.getDefinition().setIsOre( OreHelper.INSTANCE.isOre( is ) );
	}

	private static AESharedNBT getCapsTag( ItemStack is )
	{
		if ( capsField != null )
		{
			try
			{
				Object capsFieldO = capsField.get( is );
				if ( capsFieldO != null && capsFieldO instanceof CapabilityDispatcher )
				{
					CapabilityDispatcher capsField = (CapabilityDispatcher)capsFieldO;
					return (AESharedNBT) AESharedNBT.getSharedTagCompound( capsField.serializeNBT(), is );
				}
				else if ( capsFieldO != null )
				{
					AELog.debug( "Caps filed has changed object type" );
				}
			}
			catch( IllegalAccessException e )
			{
				AELog.debug( "Could not get caps field, access denied" );
			}
		}
		return null;
	}

	public static IAEItemStack loadItemStackFromNBT( final NBTTagCompound i )
	{
		if( i == null )
		{
			return null;
		}

		final ItemStack itemstack = new ItemStack( i );
		if (itemstack.isEmpty()) {
			return null;
		}

		final AEItemStack item = AEItemStack.create( itemstack );
		// item.priority = i.getInteger( "Priority" );
		item.setStackSize( i.getLong( AE2_NBT_KEYS.STACKSIZE.toString() ) );
		item.setCountRequestable( i.getLong( AE2_NBT_KEYS.REQUESTABLE.toString() ) );
		item.setCraftable( i.getBoolean( AE2_NBT_KEYS.CRAFTABLE.toString() ) );
		return item;
	}

	@Nullable
	public static AEItemStack create( final ItemStack stack )
	{
		if (stack.isEmpty()) {
			return null;
		}

		return new AEItemStack( stack );
	}

	public static IAEItemStack loadItemStackFromPacket( final ByteBuf data ) throws IOException
	{
		final byte mask = data.readByte();
		// byte PriorityType = (byte) (mask & 0x03);
		final byte stackType = (byte) ( ( mask & 0x0C ) >> 2 );
		final byte countReqType = (byte) ( ( mask & 0x30 ) >> 4 );
		final boolean isCraftable = ( mask & 0x40 ) > 0;
		final boolean hasTagCompound = ( mask & 0x80 ) > 0;

		// don't send this...
		final NBTTagCompound d = new NBTTagCompound();

		// For some insane reason, Vanilla can only parse numeric item ids if they are strings
		short itemNumericId = data.readShort();
		d.setString( VANILLA_NBT_KEYS.ITEM_ID.toString(), String.valueOf( itemNumericId ) );
		d.setShort( VANILLA_NBT_KEYS.DAMAGE.toString(), data.readShort() );
		d.setByte( VANILLA_NBT_KEYS.STACKSIZE.toString(), (byte) 1 );//1.11: this is so isEmpty does not return true.

		if( hasTagCompound )
		{
			final int len = data.readInt();

			final byte[] bd = new byte[len];
			data.readBytes( bd );

			final ByteArrayInputStream di = new ByteArrayInputStream( bd );
			final NBTTagCompound combinedTag = CompressedStreamTools.read( new DataInputStream( di ) );
			if ( combinedTag.hasKey( AE2_NBT_KEYS.COMBINED_TAG.toString() ) )
				d.setTag( VANILLA_NBT_KEYS.COMPOUND.toString(), combinedTag.getCompoundTag( AE2_NBT_KEYS.COMBINED_TAG.toString() ) );
			if ( combinedTag.hasKey( AE2_NBT_KEYS.COMBINED_CAPS.toString() ) )
				d.setTag( VANILLA_NBT_KEYS.CAPS.toString(), combinedTag.getCompoundTag( AE2_NBT_KEYS.COMBINED_CAPS.toString() ) );
		}

		// long priority = getPacketValue( PriorityType, data );
		final long stackSize = getPacketValue( stackType, data );
		final long countRequestable = getPacketValue( countReqType, data );

		final ItemStack itemstack = new ItemStack( d );
		if (itemstack.isEmpty()) {
			return null;
		}

		final AEItemStack item = AEItemStack.create( itemstack );
		// item.priority = (int) priority;
		item.setStackSize( stackSize );
		item.setCountRequestable( countRequestable );
		item.setCraftable( isCraftable );
		return item;
	}

	@Override
	public void add( final IAEItemStack option )
	{
		if( option == null )
		{
			return;
		}

		// if ( priority < ((AEItemStack) option).priority )
		// priority = ((AEItemStack) option).priority;

		this.incStackSize( option.getStackSize() );
		this.setCountRequestable( this.getCountRequestable() + option.getCountRequestable() );
		this.setCraftable( this.isCraftable() || option.isCraftable() );
	}

	@Override
	public void writeToNBT( final NBTTagCompound i )
	{
		/*
		 * Mojang Fucked this over ; GC Optimization - Ugly Yes, but it saves a lot in the memory department.
		 */

		/*
		 * NBTBase id = i.getTag( "id" ); NBTBase Count = i.getTag( "Count" ); NBTBase Cnt = i.getTag( "Cnt" ); NBTBase
		 * Req = i.getTag( "Req" ); NBTBase Craft = i.getTag( "Craft" ); NBTBase Damage = i.getTag( "Damage" );
		 */

		/*
		 * if ( id != null && id instanceof NBTTagShort ) ((NBTTagShort) id).data = (short) this.def.item.itemID; else
		 */
		// i.setShort( "id", (short) Item.REGISTRY.getIDForObject( this.getDefinition().getItem() ) );
		ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject( this.getItem() );
		i.setString( VANILLA_NBT_KEYS.ITEM_ID.toString(), resourcelocation == null ? "minecraft:air" : resourcelocation.toString() );

		/*
		 * if ( Count != null && Count instanceof NBTTagByte ) ((NBTTagByte) Count).data = (byte) 0; else
		 */
		i.setByte( VANILLA_NBT_KEYS.STACKSIZE.toString(), (byte) 1 );

		/*
		 * if ( Cnt != null && Cnt instanceof NBTTagLong ) ((NBTTagLong) Cnt).data = this.stackSize; else
		 */
		i.setLong( AE2_NBT_KEYS.STACKSIZE.toString(), this.getStackSize() );

		/*
		 * if ( Req != null && Req instanceof NBTTagLong ) ((NBTTagLong) Req).data = this.stackSize; else
		 */
		i.setLong( AE2_NBT_KEYS.REQUESTABLE.toString(), this.getCountRequestable() );

		/*
		 * if ( Craft != null && Craft instanceof NBTTagByte ) ((NBTTagByte) Craft).data = (byte) (this.isCraftable() ?
		 * 1 : 0); else
		 */
		i.setBoolean( AE2_NBT_KEYS.CRAFTABLE.toString(), this.isCraftable() );

		/*
		 * if ( Damage != null && Damage instanceof NBTTagShort ) ((NBTTagShort) Damage).data = (short)
		 * this.def.damageValue; else
		 */
		i.setShort( VANILLA_NBT_KEYS.DAMAGE.toString(), (short) this.getDefinition().getDamageValue() );

		if( this.getDefinition().getTagCompound() != null )
		{
			i.setTag( VANILLA_NBT_KEYS.COMPOUND.toString(), this.getDefinition().getTagCompound() );
		}
		else
		{
			i.removeTag( VANILLA_NBT_KEYS.COMPOUND.toString() );
		}

		if ( this.getDefinition().getCapsTag() != null )
		{
			i.setTag( VANILLA_NBT_KEYS.CAPS.toString(), this.getDefinition().getCapsTag() );
		}
		else
		{
			i.removeTag( VANILLA_NBT_KEYS.CAPS.toString() );
		}
	}

	@Override
	public boolean fuzzyComparison( final Object st, final FuzzyMode mode )
	{
		if( st instanceof IAEItemStack )
		{
			final IAEItemStack o = (IAEItemStack) st;

			if( this.sameOre( o ) )
			{
				return true;
			}

			if( o.getItem() == this.getItem() )
			{
				if( this.getDefinition().getItem().isDamageable() )
				{
					final ItemStack a = this.getDisplayItemStack();
					final ItemStack b = o.getDisplayItemStack();

					try
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							final Item ai = a.getItem();
							final Item bi = b.getItem();

							return ( ai.getDurabilityForDisplay( a ) < 0.001f ) == ( bi.getDurabilityForDisplay( b ) < 0.001f );
						}
						else
						{
							final Item ai = a.getItem();
							final Item bi = b.getItem();

							final float percentDamageOfA = 1.0f - (float) ai.getDurabilityForDisplay( a );
							final float percentDamageOfB = 1.0f - (float) bi.getDurabilityForDisplay( b );

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
					catch( final Throwable e )
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							return ( a.getItemDamage() > 1 ) == ( b.getItemDamage() > 1 );
						}
						else
						{
							final float percentDamageOfA = (float) a.getItemDamage() / (float) a.getMaxDamage();
							final float percentDamageOfB = (float) b.getItemDamage() / (float) b.getMaxDamage();

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
				}

				return this.getItemDamage() == o.getItemDamage();
			}
		}

		if( st instanceof ItemStack )
		{
			final ItemStack o = (ItemStack) st;

			OreHelper.INSTANCE.sameOre( this, o );

			if( o.getItem() == this.getItem() )
			{
				if( this.getDefinition().getItem().isDamageable() )
				{
					final ItemStack a = this.getDisplayItemStack();

					try
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							final Item ai = a.getItem();
							final Item bi = o.getItem();

							return ( ai.getDurabilityForDisplay( a ) < 0.001f ) == ( bi.getDurabilityForDisplay( o ) < 0.001f );
						}
						else
						{
							final Item ai = a.getItem();
							final Item bi = o.getItem();

							final float percentDamageOfA = 1.0f - (float) ai.getDurabilityForDisplay( a );
							final float percentDamageOfB = 1.0f - (float) bi.getDurabilityForDisplay( o );

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
					catch( final Throwable e )
					{
						if( mode == FuzzyMode.IGNORE_ALL )
						{
							return true;
						}
						else if( mode == FuzzyMode.PERCENT_99 )
						{
							return ( a.getItemDamage() > 1 ) == ( o.getItemDamage() > 1 );
						}
						else
						{
							final float percentDamageOfA = (float) a.getItemDamage() / (float) a.getMaxDamage();
							final float percentDamageOfB = (float) o.getItemDamage() / (float) o.getMaxDamage();

							return ( percentDamageOfA > mode.breakPoint ) == ( percentDamageOfB > mode.breakPoint );
						}
					}
				}

				return this.getItemDamage() == o.getItemDamage();
			}
		}

		return false;
	}

	@Override
	public IAEItemStack copy()
	{
		return new AEItemStack( this );
	}

	@Override
	public IAEItemStack empty()
	{
		final IAEItemStack dup = this.copy();
		dup.reset();
		return dup;
	}

	@Override
	public IAETagCompound getTagCompound()
	{
		return this.getDefinition().getTagCompound();
	}

	@Override
	public boolean isItem()
	{
		return true;
	}

	@Override
	public boolean isFluid()
	{
		return false;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public ItemStack getItemStack()
	{
		final ItemStack is = new ItemStack( this.getDefinition().getItem(), (int) Math.min( Integer.MAX_VALUE, this.getStackSize() ), this.getDefinition().getDamageValue(), this.getDefinition().getCapsTag() );
		if( this.getDefinition().getTagCompound() != null )
		{
			is.setTagCompound( this.getDefinition().getTagCompound().getNBTTagCompoundCopy() );
		}

		return is;
	}

	public ItemStack getDisplayItemStack()
	{
		final ItemStack is = new ItemStack( this.getDefinition().getItem(), (int) Math.max(1, Math.min( Integer.MAX_VALUE, this.getStackSize() )), this.getDefinition().getDamageValue(), this.getDefinition().getCapsTag() );
		if( this.getDefinition().getTagCompound() != null )
		{
			is.setTagCompound( this.getDefinition().getTagCompound().getNBTTagCompoundCopy() );
		}

		return is;
	}

	@Override
	public Item getItem()
	{
		return this.getDefinition().getItem();
	}

	@Override
	public int getItemDamage()
	{
		return this.getDefinition().getDamageValue();
	}

	@Override
	public boolean sameOre( final IAEItemStack is )
	{
		return OreHelper.INSTANCE.sameOre( this, is );
	}

	@Override
	public boolean isSameType( final IAEItemStack otherStack )
	{
		if( otherStack == null )
		{
			return false;
		}

		return this.getDefinition().equals( ( (AEItemStack) otherStack ).getDefinition() );
	}

	@Override
	public boolean isSameType( final ItemStack otherStack )
	{
		if (otherStack.isEmpty()) {
			return false;
		}

		return this.getDefinition().isItem( otherStack );
	}

	@Override
	public int hashCode()
	{
		return this.getDefinition().getMyHash();
	}

	@Override
	public boolean equals( final Object ia )
	{
		if( ia instanceof AEItemStack )
		{
			return ( (AEItemStack) ia ).getDefinition().equals( this.def );// && def.tagCompound == ((AEItemStack)
																			// ia).def.tagCompound;
		}
		else if( ia instanceof ItemStack )
		{
			final ItemStack is = (ItemStack) ia;

			if( is.getItem() == this.getDefinition().getItem() && is.getItemDamage() == this.getDefinition().getDamageValue() )
			{
				final NBTTagCompound ta = this.getDefinition().getTagCompound();
				final NBTTagCompound tb = is.getTagCompound();
				if( ta == tb )
				{
					return true;
				}

				if( ( ta == null && tb == null ) || ( ta != null && ta.hasNoTags() && tb == null ) || ( tb != null && tb.hasNoTags() && ta == null ) || ( ta != null && ta.hasNoTags() && tb != null && tb.hasNoTags() ) )
				{
					return true;
				}

				if( ( ta == null && tb != null ) || ( ta != null && tb == null ) )
				{
					return false;
				}

				if( AESharedNBT.isShared( tb ) )
				{
					return ta == tb;
				}

				return Platform.itemComparisons().isNbtTagEqual( ta, tb ) && this.getItemStack().areCapsCompatible( (ItemStack)ia );
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		return this.getStackSize() + "x" + this.getItem().getUnlocalizedName() + "@" + this.getItemDamage();
	}

	//TODO integrate caps tag compare
	@Override
	public int compareTo( final AEItemStack b )
	{
		final int id = this.getDefinition().getItemID() - b.getDefinition().getItemID();
		if( id != 0 )
		{
			return id;
		}

		final int damageValue = this.getDefinition().getDamageValue() - b.getDefinition().getDamageValue();
		if( damageValue != 0 )
		{
			return damageValue;
		}

		final int displayDamage = this.getDefinition().getDisplayDamage() - b.getDefinition().getDisplayDamage();
		if( displayDamage != 0 )
		{
			return displayDamage;
		}

		return ( this.getDefinition().getTagCompound() == b.getDefinition().getTagCompound() && this.getDefinition().getCapsTag() == b.getDefinition().getCapsTag() ) ? 0 : this.compareNBT( b.getDefinition() );
	}

	private int compareNBT( final AEItemDef b )
	{
		final int nbt = this.compare( ( this.getDefinition().getTagCompound() == null ? 0 : this.getDefinition().getTagCompound().getHash() ), ( b.getTagCompound() == null ? 0 : b.getTagCompound().getHash() ) );
		if( nbt == 0 )
		{
			return this.compare( System.identityHashCode( this.getDefinition().getTagCompound() ), System.identityHashCode( b.getTagCompound() ) );
		}
		return nbt;
	}

	private int compare( final int l, final int m )
	{
		return l < m ? -1 : ( l > m ? 1 : 0 );
	}

	@SideOnly( Side.CLIENT )
	public List getToolTip()
	{
		if( this.getDefinition().getTooltip() != null )
		{
			return this.getDefinition().getTooltip();
		}

		return this.getDefinition().setTooltip( Platform.getTooltip( this.getDisplayItemStack() ) );
	}

	@SideOnly( Side.CLIENT )
	public String getDisplayName()
	{
		if( this.getDefinition().getDisplayName() == null )
		{
			this.getDefinition().setDisplayName( Platform.getItemDisplayName( this.getDisplayItemStack() ) );
		}

		return this.getDefinition().getDisplayName();
	}
	
	@SideOnly( Side.CLIENT )
	public String getModID()
	{
		if( this.getDefinition().getUniqueID() != null )
		{
			return this.getModName( this.getDefinition().getUniqueID() );
		}

		return this.getModName( this.getDefinition().setUniqueID( Item.REGISTRY.getNameForObject( this.getDefinition().getItem() ) ) );
	}

	private String getModName( final ResourceLocation uniqueIdentifier )
	{
		if( uniqueIdentifier == null )
		{
			return "** Null";
		}

		return uniqueIdentifier.getResourceDomain() == null ? "** Null" : uniqueIdentifier.getResourceDomain();
	}

	IAEItemStack getLow( final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		final AEItemStack bottom = new AEItemStack( this );
		final AEItemDef newDef = bottom.setDefinition( bottom.getDefinition().copy() );

		if( ignoreMeta )
		{
			newDef.setDisplayDamage( newDef.setDamageValue( 0 ) );
			newDef.reHash();
			return bottom;
		}

		if( newDef.getItem().isDamageable() )
		{
			if( fuzzy == FuzzyMode.IGNORE_ALL )
			{
				newDef.setDisplayDamage( 0 );
			}
			else if( fuzzy == FuzzyMode.PERCENT_99 )
			{
				if( this.getDefinition().getDamageValue() == 0 )
				{
					newDef.setDisplayDamage( 0 );
				}
				else
				{
					newDef.setDisplayDamage( 1 );
				}
			}
			else
			{
				final int breakpoint = fuzzy.calculateBreakPoint( this.getDefinition().getMaxDamage() );
				newDef.setDisplayDamage( breakpoint <= this.getDefinition().getDisplayDamage() ? breakpoint : 0 );
			}

			newDef.setDamageValue( newDef.getDisplayDamage() );
		}

		newDef.setTagCompound( AEItemDef.LOW_TAG );
		newDef.reHash();
		return bottom;
	}

	IAEItemStack getHigh( final FuzzyMode fuzzy, final boolean ignoreMeta )
	{
		final AEItemStack top = new AEItemStack( this );
		final AEItemDef newDef = top.setDefinition( top.getDefinition().copy() );

		if( ignoreMeta )
		{
			newDef.setDisplayDamage( newDef.setDamageValue( Integer.MAX_VALUE ) );
			newDef.reHash();
			return top;
		}

		if( newDef.getItem().isDamageable() )
		{
			if( fuzzy == FuzzyMode.IGNORE_ALL )
			{
				newDef.setDisplayDamage( this.getDefinition().getMaxDamage() + 1 );
			}
			else if( fuzzy == FuzzyMode.PERCENT_99 )
			{
				if( this.getDefinition().getDamageValue() == 0 )
				{
					newDef.setDisplayDamage( 0 );
				}
				else
				{
					newDef.setDisplayDamage( this.getDefinition().getMaxDamage() + 1 );
				}
			}
			else
			{
				final int breakpoint = fuzzy.calculateBreakPoint( this.getDefinition().getMaxDamage() );
				newDef.setDisplayDamage( this.getDefinition().getDisplayDamage() < breakpoint ? breakpoint - 1 : this.getDefinition().getMaxDamage() + 1 );
			}

			newDef.setDamageValue( newDef.getDisplayDamage() );
		}

		newDef.setTagCompound( AEItemDef.HIGH_TAG );
		newDef.reHash();
		return top;
	}

	public boolean isOre()
	{
		return this.getDefinition().getIsOre() != null;
	}

	@Override
	void writeIdentity( final ByteBuf i ) throws IOException
	{
		i.writeShort( Item.REGISTRY.getIDForObject( this.getDefinition().getItem() ) );
		i.writeShort( this.getItemDamage() );
	}

	@Override
	void readNBT( final ByteBuf i ) throws IOException
	{
		if( this.hasTagCompound() )
		{
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final DataOutputStream data = new DataOutputStream( bytes );

			NBTTagCompound combinedNBT = new NBTTagCompound();
			if ( this.getTagCompound() != null )
			{
				combinedNBT.setTag( AE2_NBT_KEYS.COMBINED_TAG.toString(), this.getDefinition().getTagCompound() );
			}
			if ( this.getDefinition().getCapsTag() != null )
			{
				combinedNBT.setTag( AE2_NBT_KEYS.COMBINED_CAPS.toString(), this.getDefinition().getCapsTag() );
			}

			CompressedStreamTools.write( combinedNBT, data );

			final byte[] tagBytes = bytes.toByteArray();
			final int size = tagBytes.length;

			i.writeInt( size );
			i.writeBytes( tagBytes );
		}
	}

	@Override
	public boolean hasTagCompound()
	{
		return this.getDefinition().getTagCompound() != null || this.getDefinition().getCapsTag() != null;
	}

	AEItemDef getDefinition()
	{
		return this.def;
	}

	private AEItemDef setDefinition( final AEItemDef def )
	{
		this.def = def;
		return def;
	}
}
