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


import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.function.Function;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.client.render.model.BlockBaseModel;
import appeng.core.AppEng;


class SpatialPylonModel extends BlockBaseModel
{
	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Arrays.stream( SpatialPylonTextureType.values() ).map( SpatialPylonModel::getTexturePath ).collect( Collectors.toList() );
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return getTextures();
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		Map<SpatialPylonTextureType, TextureAtlasSprite> textures = new EnumMap<>( SpatialPylonTextureType.class );

		for( SpatialPylonTextureType type : SpatialPylonTextureType.values() )
		{
			ResourceLocation loc = getTexturePath( type );
			textures.put( type, bakedTextureGetter.apply( loc ) );
		}

		//Grab the default Block transforms
		ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms = PerspectiveMapWrapper.getTransforms( state );

		return new SpatialPylonBakedModel( transforms, format, textures );
	}

	private static ResourceLocation getTexturePath( SpatialPylonTextureType type )
	{
		return new ResourceLocation( AppEng.MOD_ID, "blocks/spatial_pylon/" + type.name().toLowerCase() );
	}
}
