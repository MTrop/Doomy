/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.struct.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * A utility class that holds several helpful Reflection
 * functions and methods, mostly for passive error-handling.
 * @author Matthew Tropiano
 */
public final class ReflectUtils
{
	/** Array of numerically-typed classes. */
	public static final Class<?>[] NUMERIC_TYPES =
	{
		Byte.class,
		Byte.TYPE,
		Short.class,
		Short.TYPE,
		Integer.class,
		Integer.TYPE,
		Float.class,
		Float.TYPE,
		Long.class,
		Long.TYPE,
		Double.class,
		Double.TYPE,
		Number.class
	};

	/** 
	 * Hash of numerically-typed classes. 
	 */
	public static final Set<Class<?>> NUMERIC_TYPES_SET = Collections.unmodifiableSet(new HashSet<Class<?>>()
	{
		private static final long serialVersionUID = -998955236861170734L;
		{
			add(Byte.class);
			add(Byte.TYPE);
			add(Short.class);
			add(Short.TYPE);
			add(Integer.class);
			add(Integer.TYPE);
			add(Float.class);
			add(Float.TYPE);
			add(Long.class);
			add(Long.TYPE);
			add(Double.class);
			add(Double.TYPE);
			add(Number.class);
		}
	});

	/** 
	 * Hash of primitive types to promoted/boxed classes. 
	 */
	public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_CLASS_MAP = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>()
	{
		private static final long serialVersionUID = -2547995516963695295L;
		{
			put(Void.TYPE, Void.class);
			put(Boolean.TYPE, Boolean.class);
			put(Byte.TYPE, Byte.class);
			put(Short.TYPE, Short.class);
			put(Character.TYPE, Character.class);
			put(Integer.TYPE, Integer.class);
			put(Float.TYPE, Float.class);
			put(Long.TYPE, Long.class);
			put(Double.TYPE, Double.class);
		}
	});

	/** 
	 * Hash of primitive types.
	 */
	public static final Set<Class<?>> PRIMITIVE_TYPES = Collections.unmodifiableSet(new HashSet<Class<?>>()
	{
		private static final long serialVersionUID = -1424251090783630151L;
		{
			add(Void.TYPE);
			add(Boolean.TYPE);
			add(Byte.TYPE);
			add(Short.TYPE);
			add(Character.TYPE);
			add(Integer.TYPE);
			add(Float.TYPE);
			add(Long.TYPE);
			add(Double.TYPE);
		}
	});

	/**
	 * Promotes a primitive class type to an autoboxed object type,
	 * such as <code>int</code> to {@link Integer} (Integer.TYPE to Integer.class).
	 * If the provided type is NOT a primitive type or it is an array type, this returns the provided type. 
	 * @param clazz the input class type.
	 * @return the promoted type, or the same type if not promotable.
	 */
	public static Class<?> toAutoboxedType(Class<?> clazz)
	{
		if (PRIMITIVE_TYPES.contains(clazz))
			return PRIMITIVE_TO_CLASS_MAP.get(clazz);
		return clazz;
	}

	/**
	 * Tests if an object is actually an array type.
	 * @param object the object to test.
	 * @return true if so, false if not. 
	 */
	public static boolean isArray(Object object)
	{
		return object.getClass().isArray();
	}
	
	/**
	 * Gets how many dimensions that this array, represented by the provided type, has.
	 * @param arrayType the type to inspect.
	 * @return the number of array dimensions, or 0 if not an array.
	 */
	public static int getArrayDimensions(Class<?> arrayType)
	{
		if (!arrayType.isArray())
			return 0;
			
		String cname = arrayType.getName();
		
		int dims = 0;
		while (dims < cname.length() && cname.charAt(dims) == '[')
			dims++;
		
		if (dims == cname.length())
			return 0;
		
		return dims;
	}
	
	/**
	 * Gets how many array dimensions that an object (presumably an array) has.
	 * @param array the object to inspect.
	 * @return the number of array dimensions, or 0 if not an array.
	 */
	public static int getArrayDimensions(Object array)
	{
		if (!isArray(array))
			return 0;
			
		return getArrayDimensions(array.getClass());
	}
	
	/**
	 * Gets the class type of this array type, if this is an array type.
	 * @param arrayType the type to inspect.
	 * @return this array's type, or null if the provided type is not an array,
	 * or if the found class is not on the classpath.
	 */
	public static Class<?> getArrayType(Class<?> arrayType)
	{
		String cname = arrayType.getName();

		int typeIndex = getArrayDimensions(arrayType);
		if (typeIndex == 0)
			return null;
		
		char t = cname.charAt(typeIndex);
		if (t == 'L') // is object.
		{
			String classtypename = cname.substring(typeIndex + 1, cname.length() - 1);
			try {
				return Class.forName(classtypename);
			} catch (ClassNotFoundException e){
				return null;
			}
		}
		else switch (t)
		{
			case 'Z': return Boolean.TYPE; 
			case 'B': return Byte.TYPE; 
			case 'S': return Short.TYPE; 
			case 'I': return Integer.TYPE; 
			case 'J': return Long.TYPE; 
			case 'F': return Float.TYPE; 
			case 'D': return Double.TYPE; 
			case 'C': return Character.TYPE; 
		}
		
		return null;
	}
	
	/**
	 * Gets the class type of this array, if this is an array.
	 * @param object the object to inspect.
	 * @return this array's type, or null if the provided object is not an array, or if the found class is not on the classpath.
	 */
	public static Class<?> getArrayType(Object object)
	{
		if (!isArray(object))
			return null;
		
		return getArrayType(object.getClass());
	}
	
	/**
	 * Checks if a method is a "setter" method.
	 * This checks its name, if it returns a void value, takes one argument, and if it is <b>public</b>.
	 * @param method the method to inspect.
	 * @return true if so, false if not.
	 */
	public static boolean isSetter(Method method)
	{
		Class<?> rettype = method.getReturnType();
		return isSetterName(method.getName()) 
			&& method.getParameterTypes().length == 1
			&& (rettype == Void.TYPE || rettype == Void.class) 
			&& (method.getModifiers() & Modifier.PUBLIC) != 0;
	}

	/**
	 * Checks if a method is a "setter" method.
	 * This checks its name, if it returns a void value or the source class type (chain setters), takes one argument, and if it is <b>public</b>.
	 * @param method the method to inspect.
	 * @param sourceType the source type.
	 * @return true if so, false if not.
	 */
	public static boolean isSetter(Method method, Class<?> sourceType)
	{
		Class<?> rettype = method.getReturnType();
		return isSetterName(method.getName()) 
			&& method.getParameterTypes().length == 1
			&& (rettype == Void.TYPE || rettype == Void.class || rettype == sourceType) 
			&& (method.getModifiers() & Modifier.PUBLIC) != 0;
	}

	/**
	 * Checks if a method name describes a "setter" method. 
	 * @param methodName the name of the method.
	 * @return true if so, false if not.
	 */
	public static boolean isSetterName(String methodName)
	{
		if (methodName.startsWith("set"))
		{
			if (methodName.length() < 4)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(3));
		}
		return false;
	}

	/**
	 * Returns the "setter name" for a field.
	 * <p>
	 * For example, the field name "color" will return "setColor" 
	 * (note the change in camel case).
	 * @param name the field name.
	 * @return the setter name.
	 * @throws StringIndexOutOfBoundsException if name is the empty string.
	 * @throws NullPointerException if name is null.
	 */
	public static String getSetterName(String name)
	{
		return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Checks if a method is a "getter" method.
	 * This checks its name, if it returns a non-void value, takes no arguments, and if it is <b>public</b>.
	 * @param method the method to inspect.
	 * @return true if so, false if not.
	 */
	public static boolean isGetter(Method method)
	{
		return isGetterName(method.getName()) 
			&& method.getParameterTypes().length == 0
			&& !(method.getReturnType() == Void.TYPE || method.getReturnType() == Void.class) 
			&& (method.getModifiers() & Modifier.PUBLIC) != 0;
	}

	/**
	 * Checks if a method name describes a "getter" method (also detects "is" methods). 
	 * @param methodName the name of the method.
	 * @return true if so, false if not.
	 */
	public static boolean isGetterName(String methodName)
	{
		if (methodName.startsWith("is"))
		{
			if (methodName.length() < 3)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(2));
		}
		else if (methodName.startsWith("get"))
		{
			if (methodName.length() < 4)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(3));
		}
		return false;
	}

	/**
	 * Returns the "getter name" for a field.
	 * <p>
	 * For example, the field name "color" will return "getColor" 
	 * (note the change in camel case).
	 * @param name the field name.
	 * @return the getter name.
	 * @throws StringIndexOutOfBoundsException if name is the empty string.
	 * @throws NullPointerException if name is null.
	 */
	public static String getGetterName(String name)
	{
		return "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	// truncator method
	private static String truncateMethodName(String methodName, boolean is)
	{
		return is 
			? Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3)
			: Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
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
	
	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#newInstance()}, but wraps the call
	 * in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param constructor the constructor to call.
	 * @param params the constructor parameters.
	 * @return a new instance of an object created via the provided constructor.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T construct(Constructor<T> constructor, Object ... params)
	{
		Object out = null;
		try {
			out = (T)constructor.newInstance(params);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		return (T)out;
	}
	
	/**
	 * Returns the field name for a getter/setter method.
	 * If the method name is not a getter or setter name, then this will return <code>methodName</code>
	 * <p>
	 * For example, the field name "setColor" will return "color" and "isHidden" returns "hidden". 
	 * (note the change in camel case).
	 * @param methodName the name of the method.
	 * @return the modified method name.
	 */
	public static String getFieldName(String methodName)
	{
		if (isGetterName(methodName))
		{
			if (methodName.startsWith("is"))
				return truncateMethodName(methodName, true);
			else if (methodName.startsWith("get"))
				return truncateMethodName(methodName, false);
		}
		else if (isSetterName(methodName))
			return truncateMethodName(methodName, false);
		
		return methodName;
	}

	/**
	 * Sets the value of a field on an object.
	 * @param instance the object instance to set the field on.
	 * @param field the field to set.
	 * @param value the value to set.
	 * @throws NullPointerException if the field or object provided is null.
	 * @throws ClassCastException if the value could not be cast to the proper type.
	 * @throws RuntimeException if anything goes wrong (bad field name, 
	 * bad target, bad argument, or can't access the field).
	 * @see Field#set(Object, Object)
	 */
	public static void setFieldValue(Object instance, Field field, Object value)
	{
		try {
			field.set(instance, value);
		} catch (ClassCastException ex) {
			throw ex;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the value of a field on an object.
	 * @param instance the object instance to get the field value of.
	 * @param field the field to get the value of.
	 * @return the current value of the field.
	 * @throws NullPointerException if the field or object provided is null.
	 * @throws RuntimeException if anything goes wrong (bad target, bad argument, 
	 * or can't access the field).
	 * @see Field#set(Object, Object)
	 */
	public static Object getFieldValue(Object instance, Field field)
	{
		try {
			return field.get(instance);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Blindly invokes a method, only throwing a {@link RuntimeException} if
	 * something goes wrong. Here for the convenience of not making a billion
	 * try/catch clauses for a method invocation.
	 * @param method the method to invoke.
	 * @param instance the object instance that is the method target.
	 * @param params the parameters to pass to the method.
	 * @return the return value from the method invocation. If void, this is null.
	 * @throws ClassCastException if one of the parameters could not be cast to the proper type.
	 * @throws RuntimeException if anything goes wrong (bad target, bad argument, or can't access the method).
	 * @see Method#invoke(Object, Object...)
	 */
	public static Object invokeBlind(Method method, Object instance, Object ... params)
	{
		Object out = null;
		try {
			out = method.invoke(instance, params);
		} catch (ClassCastException ex) {
			throw ex;
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		return out;
	}
	
	/**
	 * Returns if a field can contain a type of class in the provided list of classes.
	 * @param field the field to inspect.
	 * @param classes the list of classes to check.
	 * @return true if so, false if not.
	 */
	public static boolean fieldCanContain(Field field, Class<?> ... classes)
	{
		Class<?> ret = field.getType();
		for (Class<?> c : classes)
		{
			if (ret.isAssignableFrom(c))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if a method signature has parameters that match
	 * the input list of classes. The match must be complete.
	 * <p>A method signature <code>addString(int x, String name)</code> would match:
	 * <p><code>matchParameterTypes(method, Integer.TYPE, String.class)</code>
	 * <p>A method signature <code>find(Reader reader, Number n)</code> would also match:
	 * <p><code>matchParameterTypes(method, InputReader.class, Long.class)</code>
	 * @param method the method to inspect.
	 * @param classes the types to check.
	 * @return true if a complete match, both in length and type, occurs. false if not.
	 */
	public static boolean matchParameterTypes(Method method, Class<?> ... classes)
	{
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length != classes.length)
			return false;
		
		for (int i = 0; i < paramTypes.length; i++)
		{
			if (!paramTypes[i].isAssignableFrom(classes[i]))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns if a method can return a type of class in the provided list of classes.
	 * @param method the method to inspect.
	 * @param classes the list of classes to check.
	 * @return true if so, false if not.
	 */
	public static boolean methodCanReturn(Method method, Class<?> ... classes)
	{
		Class<?> ret = method.getReturnType();
		for (Class<?> c : classes)
		{
			if (ret.isAssignableFrom(c))
				return true;
		}
		return false;
	}

	/**
	 * Gets the package path for a particular class (for classpath resources).
	 * @param cls the class for which to get to get the path.
	 * @return the equivalent path for finding a class.
	 */
	public static String getPackagePathForClass(Class<?> cls)
	{
		return cls.getPackage().getName().replaceAll("\\.", "/");		
	}

	/**
	 * Adds a list of files to the JVM Classpath during runtime.
	 * The files are added to the current thread's class loader.
	 * Directories in the list of files are exploded down to actual
	 * files, so that no directories remain.
	 * @param files	the list of files to add.
	 */
	public static void addFilesToClassPath(File ... files)
	{
		addURLsToClassPath(FileUtils.getURLsForFiles(files));
	}

	/**
	 * Adds a list of URLs to the JVM Classpath during runtime.
	 * The URLs are added to the current thread's class loader.
	 * @param urls	the list of files to add.
	 */
	public static void addURLsToClassPath(URL ... urls)
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URLClassLoader ucl = new URLClassLoader(urls,cl);
		Thread.currentThread().setContextClassLoader(ucl);
	}

	/**
	 * Adds a list of files/directories to the library path of the JVM.
	 * This utilizes a fix originally authored by Antony Miguel.
	 * @param libs the files to add. 
	 * @throws SecurityException if the JVM does not have access to the native library path field.
	 * @throws RuntimeException if the JVM cannot find the native library path field.
	 */
	public static void addLibrariesToPath(File ... libs)
	{
		for (File f : libs)
			addLibraryToPath(f);
	}

	/**
	 * Adds a file/directory to the library path of the JVM.
	 * This utilizes a fix originally authored by Antony Miguel.
	 * @param lib the file to add. 
	 * @throws SecurityException if the JVM does not have access to the native library path field.
	 * @throws RuntimeException if the JVM cannot find the native library path field.
	 */
	public static void addLibraryToPath(File lib)
	{
		String libPath = lib.getPath();
		try {
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[])field.get(null);
			for (int i = 0; i < paths.length; i++)
			{
				if (libPath.equals(paths[i]))
					return;
			}
			String[] tmp = new String[paths.length+1];
			System.arraycopy(paths,0,tmp,0,paths.length);
			tmp[paths.length] = libPath;
			field.set(null,tmp);
		} catch (IllegalAccessException e) {
			throw new SecurityException("Failed to get permissions to set library path");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Failed to get field handle to set library path");
		}
		System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + libPath);
	}

	/**
	 * Tests if a class exists on the classpath. Does not attempt to initialize it, 
	 * and uses the current thread's classloader via {@link Thread#getContextClassLoader()}
	 * Just for convenience - will swallow {@link ClassNotFoundException} and {@link NoClassDefFoundError}.
	 * @param className the fully qualified class name (e.g. <code>java.lang.String</code>).
	 * @return true if the class exists, false if not.
	 */
	public static boolean classExistsForName(String className)
	{
		return classExistsForName(className, false, Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * Tests if a class exists on the classpath. Does not attempt to initialize it.
	 * Just for convenience - will swallow {@link ClassNotFoundException} and {@link NoClassDefFoundError}.
	 * @param className the fully qualified class name (e.g. <code>java.lang.String</code>).
	 * @param classLoader the class loader to use.
	 * @return true if the class exists, false if not.
	 */
	public static boolean classExistsForName(String className, ClassLoader classLoader)
	{
		return classExistsForName(className, false, classLoader);
	}
	
	/**
	 * Tests if a class exists on the classpath.
	 * Just for convenience - will swallow {@link ClassNotFoundException} and {@link NoClassDefFoundError}.
	 * @param className the fully qualified class name (e.g. <code>java.lang.String</code>).
	 * @param initialize if true, class will also be initialized, if found.
	 * @param classLoader the class loader to use.
	 * @return true if the class exists, false if not.
	 */
	public static boolean classExistsForName(String className, boolean initialize, ClassLoader classLoader)
	{
		try {
			Class.forName(className, initialize, classLoader);
		} catch (ClassNotFoundException e) {
			return false;
		} catch (NoClassDefFoundError e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns the fully-qualified names of all classes beginning with
	 * a certain string. This uses {@link Thread#getContextClassLoader()} on the current thread to find them.
	 * None of the classes are "forName"-ed into PermGen space.
	 * <p>This scan can be expensive, as this searches the contents of the entire classpath.
	 * @param prefix the String to use for lookup. Can be null.
	 * @return the list of class names.
	 * @throws RuntimeException if a JAR file could not be read for some reason.
	 */
	public static String[] getClasses(String prefix)
	{
		return joinArrays(getClasses(prefix, Thread.currentThread().getContextClassLoader()), getClassesFromClasspath(prefix));
	}
	
	/**
	 * Returns the fully-qualified names of all classes beginning with
	 * a certain string. None of the classes are "forName"-ed into PermGen/Metaspace.
	 * <p>This scan can be expensive, as this searches the contents of the entire {@link ClassLoader}.
	 * @param prefix the String to use for lookup. Can be null.
	 * @param classLoader the ClassLoader to look into.
	 * @return the list of class names.
	 * @throws RuntimeException if a JAR file could not be read for some reason.
	 */
	public static String[] getClasses(String prefix, ClassLoader classLoader)
	{
		if (prefix == null)
			prefix = "";
		
		List<String> outList = new ArrayList<String>(128);
		
		while (classLoader != null)
		{
			if (classLoader instanceof URLClassLoader)
				scanURLClassLoader(prefix, (URLClassLoader)classLoader, outList);
			
			classLoader = classLoader.getParent();
		}
		
		String[] out = new String[outList.size()];
		outList.toArray(out);
		return out;
	}

	/**
	 * Returns the fully-qualified names of all classes beginning with
	 * a certain string. None of the classes are "forName"-ed into PermGen/Metaspace.
	 * <p>This scan can be expensive, as this searches the contents of the entire {@link ClassLoader}.
	 * @param prefix the String to use for lookup. Can be null.
	 * @return the list of class names.
	 * @throws RuntimeException if a JAR file could not be read for some reason.
	 */
	public static String[] getClassesFromClasspath(String prefix)
	{
		if (prefix == null)
			prefix = "";
		
		List<String> outList = new ArrayList<String>(128);

		String classpath = System.getProperty("java.class.path");
		String[] files = classpath.split("(\\"+File.pathSeparator+")");
		
		for (String fileName : files)
		{
			File f = new File(fileName);
			if (!f.exists())
				continue;
			
			if (f.isDirectory())
				scanDirectory(prefix, outList, fileName, f);
			else if (f.getName().toLowerCase().endsWith(".jar"))
				scanJARFile(prefix, outList, f);
			else if (f.getName().toLowerCase().endsWith(".jmod"))
				scanJMODFile(prefix, outList, f);
		}
		
		String[] out = new String[outList.size()];
		outList.toArray(out);
		return out;
	}

	/**
	 * Concatenates a set of arrays together, such that the contents of each
	 * array are joined into one array. Null arrays are skipped.
	 * @param <T> the object type stored in the arrays.
	 * @param arrays the list of arrays.
	 * @return a new array with all objects in each provided array added 
	 * to the resultant one in the order in which they appear.
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	private static <T> T[] joinArrays(T[] ...  arrays)
	{
		int totalLen = 0;
		for (T[] a : arrays)
			if (a != null)
				totalLen += a.length;
		
		Class<?> type = getArrayType(arrays);
		T[] out = (T[])Array.newInstance(type, totalLen);
		
		int offs = 0;
		for (T[] a : arrays)
		{
			System.arraycopy(a, 0, out, offs, a.length);
			offs += a.length;
		}
		
		return out;
	}

	/**
	 * Decodes a URL-encoded string.
	 * @param inString the input string.
	 * @return the unescaped string.
	 */
	private static String urlUnescape(String inString)
	{
		StringBuilder sb = new StringBuilder();
		char[] chars = new char[2];
		int x = 0;
		
		final int STATE_START = 0;
		final int STATE_DECODE = 1;
		int state = STATE_START;
		
		for (int i = 0; i < inString.length(); i++)
		{
			char c = inString.charAt(i);
			
			switch (state)
			{
				case STATE_START:
					if (c == '%')
					{
						x = 0;
						state = STATE_DECODE;
					}
					else
						sb.append(c);
					break;
				case STATE_DECODE:
					chars[x++] = c;
					if (x == 2)
					{
						int v = 0;
						try {
							v = Integer.parseInt(new String(chars), 16);
							sb.append((char)(v & 0x0ff));
					} catch (NumberFormatException e) {
							sb.append('%').append(chars[0]).append(chars[1]);
						}
						state = STATE_START;
					}
					break;
			}
		}
		
		if (state == STATE_DECODE)
		{
			sb.append('%');
			for (int n = 0; n < x; n++)
				sb.append(chars[n]);
		}
		
		return sb.toString();
	}

	// Scans a URL classloader.
	private static void scanURLClassLoader(String prefix, URLClassLoader classLoader, List<String> outList)
	{
		for (URL url : classLoader.getURLs())
		{
			if (url.getProtocol().equals("file"))
			{
				String startingPath = urlUnescape(url.getPath().substring(1));
				File file = new File(startingPath);
				if (file.isDirectory())
					scanDirectory(prefix, outList, startingPath, file);
				else if (file.getName().endsWith(".jar"))
					scanJARFile(prefix, outList, file);
			}
		}
	}

	// Scans a directory for classes.
	private static void scanDirectory(String prefix, List<String> outList, String startingPath, File file)
	{
		for (File f : FileUtils.explodeFiles(file))
		{
			String path = f.getPath();
			int classExtIndex = path.endsWith(".class") ? path.indexOf(".class") : -1;
			if (classExtIndex >= 0 && !path.contains("$") && !path.endsWith("package-info.class") && !path.endsWith("module-info.class"))
			{
				String className = path.substring(startingPath.length()+1, classExtIndex).replaceAll("[\\/\\\\]", ".");
				if (className.startsWith(prefix))
					outList.add(className);
			}
		}
	}

	// Scans a JAR file
	private static void scanJARFile(String prefix, List<String> outList, File file)
	{
		try (ZipFile jarFile = new ZipFile(file))
		{
			Enumeration<? extends ZipEntry> zipEntries = jarFile.entries();
			while (zipEntries.hasMoreElements())
			{
				ZipEntry ze = zipEntries.nextElement();
				String path = ze.getName();
				int classExtIndex = path.indexOf(".class");
				if (classExtIndex >= 0 && !path.contains("$") && !path.endsWith("package-info.class") && !path.endsWith("module-info.class"))
				{
					String className = path.substring(0, classExtIndex).replaceAll("[\\/\\\\]", ".");
					if (className.startsWith(prefix))
						outList.add(className);
				}
			}
			
		} 
		catch (ZipException e) 
		{
			throw new RuntimeException(e);
		} 
		catch (IOException e) 
		{
			throw new RuntimeException(e);
		}
	}
	
	// Scans a JMOD file
	private static void scanJMODFile(String prefix, List<String> outList, File file)
	{
		try (ZipFile jmodFile = new ZipFile(file)) 
		{
			Enumeration<? extends ZipEntry> zipEntries = jmodFile.entries();
			while (zipEntries.hasMoreElements())
			{
				ZipEntry ze = zipEntries.nextElement();
				String path = ze.getName();
				int classExtIndex = path.indexOf(".class");
				if (classExtIndex >= 0 && !path.contains("$") && !path.endsWith("package-info.class") && !path.endsWith("module-info.class"))
				{
					String className = path.substring(0, classExtIndex).replaceAll("[\\/\\\\]", ".");
					if (className.startsWith(prefix))
						outList.add(className);
				}
			}
			
		} 
		catch (ZipException e) 
		{
			throw new RuntimeException(e);
		} 
		catch (IOException e) 
		{
			throw new RuntimeException(e);
		}
	}	
}
