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

package appeng.client.render.spatial;


import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;

import appeng.block.spatial.BlockSpatialPylon;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.tile.spatial.TileSpatialPylon;


/**
 * The baked model that will be used for rendering the spatial pylon.
 */
class SpatialPylonBakedModel implements IPerspectiveAwareModel
{

	private final Map<SpatialPylonTextureType, TextureAtlasSprite> textures;

	private final VertexFormat format;

	private final IPerspectiveAwareModel.MapWrapper perspective;

	SpatialPylonBakedModel( ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, VertexFormat format, Map<SpatialPylonTextureType, TextureAtlasSprite> textures )
	{
		this.textures = ImmutableMap.copyOf( textures );
		this.format = format;
		perspective = new IPerspectiveAwareModel.MapWrapper( this, transforms );
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{
		int flags = getFlags( state );

		CubeBuilder builder = new CubeBuilder( format );

		if( flags != 0 )
		{
			EnumFacing ori = null;
			int displayAxis = flags & TileSpatialPylon.DISPLAY_Z;
			if( displayAxis == TileSpatialPylon.DISPLAY_X )
			{
				ori = EnumFacing.EAST;

				if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					builder.setUvRotation( EnumFacing.SOUTH, 1 );
					builder.setUvRotation( EnumFacing.NORTH, 1 );
					builder.setUvRotation( EnumFacing.UP, 2 );
					builder.setUvRotation( EnumFacing.DOWN, 2 );
				}
				else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					builder.setUvRotation( EnumFacing.SOUTH, 2 );
					builder.setUvRotation( EnumFacing.NORTH, 2 );
					builder.setUvRotation( EnumFacing.UP, 1 );
					builder.setUvRotation( EnumFacing.DOWN, 1 );
				}
				else
				{
					builder.setUvRotation( EnumFacing.SOUTH, 1 );
					builder.setUvRotation( EnumFacing.NORTH, 1 );
					builder.setUvRotation( EnumFacing.UP, 1 );
					builder.setUvRotation( EnumFacing.DOWN, 1 );
				}
			}

			else if( displayAxis == TileSpatialPylon.DISPLAY_Y )
			{
				ori = EnumFacing.UP;
				if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					builder.setUvRotation( EnumFacing.NORTH, 3 );
					builder.setUvRotation( EnumFacing.SOUTH, 3 );
					builder.setUvRotation( EnumFacing.EAST, 3 );
					builder.setUvRotation( EnumFacing.WEST, 3 );
				}
			}

			else if( displayAxis == TileSpatialPylon.DISPLAY_Z )
			{
				ori = EnumFacing.NORTH;
				if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					builder.setUvRotation( EnumFacing.EAST, 2 );
					builder.setUvRotation( EnumFacing.WEST, 1 );
				}
				else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					builder.setUvRotation( EnumFacing.EAST, 1 );
					builder.setUvRotation( EnumFacing.WEST, 2 );
					builder.setUvRotation( EnumFacing.UP, 3 );
					builder.setUvRotation( EnumFacing.DOWN, 3 );
				}
				else
				{
					builder.setUvRotation( EnumFacing.EAST, 1 );
					builder.setUvRotation( EnumFacing.WEST, 2 );
				}
			}

			builder.setTextures( textures.get( getTextureTypeFromSideOutside( flags, ori, EnumFacing.UP ) ), textures.get( getTextureTypeFromSideOutside( flags, ori, EnumFacing.DOWN ) ), textures.get( getTextureTypeFromSideOutside( flags, ori, EnumFacing.NORTH ) ), textures.get( getTextureTypeFromSideOutside( flags, ori, EnumFacing.SOUTH ) ), textures.get( getTextureTypeFromSideOutside( flags, ori, EnumFacing.EAST ) ), textures.get( getTextureTypeFromSideOutside( flags, ori, EnumFacing.WEST ) ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );

			if( ( flags & TileSpatialPylon.DISPLAY_POWERED_ENABLED ) == TileSpatialPylon.DISPLAY_POWERED_ENABLED )
			{
				builder.setRenderFullBright( true );
			}

			builder.setTextures( textures.get( getTextureTypeFromSideInside( flags, ori, EnumFacing.UP ) ), textures.get( getTextureTypeFromSideInside( flags, ori, EnumFacing.DOWN ) ), textures.get( getTextureTypeFromSideInside( flags, ori, EnumFacing.NORTH ) ), textures.get( getTextureTypeFromSideInside( flags, ori, EnumFacing.SOUTH ) ), textures.get( getTextureTypeFromSideInside( flags, ori, EnumFacing.EAST ) ), textures.get( getTextureTypeFromSideInside( flags, ori, EnumFacing.WEST ) ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );
		}
		else
		{
			builder.setTexture( textures.get( SpatialPylonTextureType.BASE ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );

			builder.setTexture( textures.get( SpatialPylonTextureType.DIM ) );
			builder.addCube( 0, 0, 0, 16, 16, 16 );
		}

		return builder.getOutput();
	}

	private int getFlags( IBlockState state )
	{
		if( !( state instanceof IExtendedBlockState ) )
		{
			return 0;
		}

		IExtendedBlockState extState = (IExtendedBlockState) state;

		return extState.getValue( BlockSpatialPylon.STATE );
	}

	private static SpatialPylonTextureType getTextureTypeFromSideOutside( int flags, EnumFacing ori, EnumFacing dir )
	{
		if( ori == dir || ori.getOpposite() == dir )
		{
			return SpatialPylonTextureType.BASE;
		}

		if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_MIDDLE )
		{
			return SpatialPylonTextureType.BASE_SPANNED;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
		{
			return SpatialPylonTextureType.BASE_END;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
		{
			return SpatialPylonTextureType.BASE_END;
		}

		return SpatialPylonTextureType.BASE;
	}

	private static SpatialPylonTextureType getTextureTypeFromSideInside( int flags, EnumFacing ori, EnumFacing dir )
	{
		final boolean good = ( flags & TileSpatialPylon.DISPLAY_ENABLED ) == TileSpatialPylon.DISPLAY_ENABLED;

		if( ori == dir || ori.getOpposite() == dir )
		{
			return good ? SpatialPylonTextureType.DIM : SpatialPylonTextureType.RED;
		}

		if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_MIDDLE )
		{
			return good ? SpatialPylonTextureType.DIM_SPANNED : SpatialPylonTextureType.RED_SPANNED;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
		{
			return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
		}
		else if( ( flags & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
		{
			return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
		}

		return SpatialPylonTextureType.BASE;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.textures.get( SpatialPylonTextureType.DIM );
	}

	@Override
	@Deprecated
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective( ItemCameraTransforms.TransformType cameraTransformType )
	{
		return this.perspective.handlePerspective( cameraTransformType );
	}
}
