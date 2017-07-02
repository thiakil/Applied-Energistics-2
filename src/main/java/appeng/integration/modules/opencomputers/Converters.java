package appeng.integration.modules.opencomputers;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;

/**
 * Created by Thiakil on 2/07/2017.
 *
 * Currently a bit of a hack because OC currently does not convert results of an AbstractValue return
 */
public class Converters
{
	private static Method convertRecursivey;

	static
	{
		try
		{
			convertRecursivey = Class.forName( "li.cil.oc.server.driver.Registry" ).getMethod( "convertRecursively", Object.class, IdentityHashMap.class, boolean.class);
		}
		catch( NoSuchMethodException|ClassNotFoundException e )
		{
			//e.printStackTrace();
		}
	}

	public static Object convert(Object value)
	{
		if ( convertRecursivey == null )
			return value;
		try
		{
			return convertRecursivey.invoke( null, value, new IdentityHashMap<>(), false );
		}
		catch( IllegalAccessException | InvocationTargetException e )
		{
			return value;
		}
	}
}
