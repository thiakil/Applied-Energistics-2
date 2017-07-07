package appeng.client.render.crafting;


import java.util.Collection;
import java.util.Collections;

import java.util.function.Function;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.client.render.model.BaseModel;
import appeng.core.AppEng;


/**
 * Simple model for the encoded pattern built-in baked model.
 */
class ItemEncodedPatternModel extends BaseModel
{

	private static final ResourceLocation BASE_MODEL = new ResourceLocation( AppEng.MOD_ID, "item/encoded_pattern" );

	public ItemEncodedPatternModel()
	{
		super( BASE_MODEL );
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Collections.emptyList();
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms = PerspectiveMapWrapper.getTransforms(state);

		return new ItemEncodedPatternBakedModel( getBakedBaseModel( state, format, bakedTextureGetter ), transforms );
	}

}
