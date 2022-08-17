package net.mtrop.doomy.gui;

import static net.mtrop.doomy.gui.swing.struct.ContainerFactory.frame;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;

import net.mtrop.doomy.struct.ObjectUtils;

public final class PanelTest 
{
	public static void main(String[] args) 
	{
		if (args.length < 1 || ObjectUtils.isEmpty(args[0]))
		{
			System.err.println("ERROR: Need panel.");
			return;
		}

		DoomyGUIMain.setLAF();

		Object obj = null;
		try {
			obj = create(Class.forName(args[0]));
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			return;
		}

		if (!(obj instanceof Container))
		{
			System.err.println("Not a Container: " + obj.getClass().getName());
			return;
		}
		
		Container container = (Container)obj;
		
		ObjectUtils.apply(frame("Test", container), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
		
	}

	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#getDeclaredConstructor(Class...)} with no arguments 
	 * and {@link Class#newInstance()}, but wraps the call in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param clazz the class type to instantiate.
	 * @return a new instance of an object.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	public static <T> T create(Class<T> clazz)
	{
		Object out = null;
		try {
			out = clazz.getDeclaredConstructor().newInstance();
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		
		return clazz.cast(out);
	}
	
}
