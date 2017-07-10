package appeng.coremod;


import java.util.Map;
import javax.annotation.Nullable;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;


/**
 * Coremod loading plugin, to separate from DummyModContainer, which some people want to transform
 */
@IFMLLoadingPlugin.MCVersion( "1.12" )
public class CoreModLoader implements IFMLLoadingPlugin
{
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { "appeng.coremod.transformer.ASMIntegration" };
	}

	@Override
	public String getModContainerClass()
	{
		return "appeng.coremod.AppEngCore";
	}

	@Nullable
	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData( final Map<String, Object> data )
	{

	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
