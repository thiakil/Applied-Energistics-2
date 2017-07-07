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
import java.util.List;

import java.util.function.Function;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;


/**
 * The parent model for the compass baked model. Declares the dependencies for the base and pointer submodels mostly.
 */
public class SkyCompassModel extends BaseModel
{
	private static final ResourceLocation MODEL_BASE = new ResourceLocation( "appliedenergistics2:block/sky_compass_base" );

	private static final ResourceLocation MODEL_POINTER = new ResourceLocation( "appliedenergistics2:block/sky_compass_pointer" );

	private static final List<ResourceLocation> DEPENDENCIES = ImmutableList.of( MODEL_BASE, MODEL_POINTER );

	private IModel pointerModel;

	public SkyCompassModel()
	{
		super( MODEL_BASE );
	}

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return DEPENDENCIES;
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Collections.emptyList();
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		IBakedModel bakedBase = getBakedBaseModel( state, format, bakedTextureGetter );
		IBakedModel bakedPointer = getPointerModel().bake( state, format, bakedTextureGetter );
		return new SkyCompassBakedModel( bakedBase, bakedPointer );
	}

	private IModel getPointerModel()
	{
		if ( pointerModel == null )
		{
			try
			{
				pointerModel = ModelLoaderRegistry.getModel( MODEL_POINTER );
			}
			catch( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
		return pointerModel;
	}
}
