package appeng.integration.modules.opencomputers;


import java.util.Map;

import li.cil.oc.api.driver.Converter;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;


/**
 * Created by Thiakil on 27/05/2017.
 * Converts AE item & fluid stacks to their MC equivalent and tells OC to replace this value with the converted object.
 */
public class ConverterAEStack implements Converter
{
	@Override
	public void convert(Object value, Map<Object, Object> output) {
		if (value instanceof IAEItemStack ){
			output.put("oc:flatten", ((IAEItemStack) value).getItemStack());
		} else if (value instanceof IAEFluidStack ){
			output.put("oc:flatten", ((IAEFluidStack) value).getFluidStack() );
		}
	}
}
