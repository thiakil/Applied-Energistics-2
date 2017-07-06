package appeng.client.render.model;


import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;

import appeng.core.AppEng;


/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the selected color of the applicator.
 */
public class ColorApplicatorModel extends BaseModel
{

	private static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "item/color_applicator_colored" );

	private static final ResourceLocation TEXTURE_DARK = new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_dark" );
	private static final ResourceLocation TEXTURE_MEDIUM = new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_medium" );
	private static final ResourceLocation TEXTURE_BRIGHT = new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_bright" );

	public ColorApplicatorModel()
	{
		super( MODEL_BASE );
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return ImmutableList.of(
				TEXTURE_DARK,
				TEXTURE_MEDIUM,
				TEXTURE_BRIGHT
		);
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		IBakedModel baseModel = getBakedBaseModel( state, format, bakedTextureGetter );

		TextureAtlasSprite texDark = bakedTextureGetter.apply( TEXTURE_DARK );
		TextureAtlasSprite texMedium = bakedTextureGetter.apply( TEXTURE_MEDIUM );
		TextureAtlasSprite texBright = bakedTextureGetter.apply( TEXTURE_BRIGHT );

		return new ColorApplicatorBakedModel( baseModel, texDark, texMedium, texBright );
	}

}
