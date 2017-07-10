package appeng.bootstrap.components;


import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import appeng.bootstrap.IBootstrapComponent;


/**
 * Created by Thiakil on 10/07/2017.
 */
public interface RegistryComponent extends IBootstrapComponent
{
	<T extends IForgeRegistryEntry<T>> void registryEvent( IForgeRegistry<T> registry, Class<T> clazz );
}
