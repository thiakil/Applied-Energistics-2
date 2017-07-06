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

package appeng.client.render.model;


import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;


/**
 * Model class for the connected texture glass model. Uses standard block model as base, to get the default states
 */
public class GlassModel extends BlockBaseModel
{

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return ImmutableSet.<ResourceLocation>builder()
				.add( GlassBakedModel.TEXTURE_A, GlassBakedModel.TEXTURE_B, GlassBakedModel.TEXTURE_C, GlassBakedModel.TEXTURE_D )
				.add( GlassBakedModel.TEXTURES_FRAME )
				.build();
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		//Grab the default Block transforms
		ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms = IPerspectiveAwareModel.MapWrapper.getTransforms( state );
		return new GlassBakedModel( transforms, format, bakedTextureGetter );
	}

}
