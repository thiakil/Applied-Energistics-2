package appeng.client.render.model;


import java.util.Collection;
import java.util.Collections;

import java.util.function.Function;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;

import appeng.core.AppEng;


/**
 * Model wrapper for the biometric card item model, which combines a base card layer with a "visual hash" of the player name
 */
public class BiometricCardModel extends BaseModel
{

	private static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "item/biometric_card" );
	private static final ResourceLocation TEXTURE = new ResourceLocation( AppEng.MOD_ID, "items/biometric_card_hash" );

	public BiometricCardModel()
	{
		super( MODEL_BASE );
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Collections.singletonList( TEXTURE );
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		TextureAtlasSprite texture = bakedTextureGetter.apply( TEXTURE );

		IBakedModel baseModel = getBakedBaseModel( state, format, bakedTextureGetter );

		return new BiometricCardBakedModel( format, baseModel, texture );
	}
}
