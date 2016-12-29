package org.daubin.classutils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class ProxyGenerator {
	
	public static <T, E extends T> Function<T, E> createInterfaceExtensionFactory(final Class<E> extensionClass) throws IllegalAccessException {
		final Function<InvocationHandler, E> proxyFactory = createProxyFactory(extensionClass, new Class[] { extensionClass });

		// build a map of the default methods on the extension to their method handlers
		Builder<Method, MethodHandle> builder = ImmutableMap.builder();
		Lookup lookup = getLookupFactory().apply(extensionClass);
		for (Method m : extensionClass.getDeclaredMethods()) {
			if (m.isDefault()) {
				builder.put(m, lookup.unreflectSpecial(m, extensionClass));
			} else {
				throw new RuntimeException("All methods on " + extensionClass.getName() + " must be default implementations");
			}
		}
		ImmutableMap<Method, MethodHandle> methodMap = builder.build();

		return original -> {
			InvocationHandler handler = (object, method, params) -> {
				if (method.getDeclaringClass().isInstance(original)) {
					return method.invoke(original, params);
				} else {
					MethodHandle methodHandle = methodMap.get(method);
					if (null == methodHandle) {
						throw new RuntimeException("No handler for method " + method);
					} else {
						return methodHandle.bindTo(object).invokeWithArguments(params);
					}
				}
			};
			return proxyFactory.apply(handler);
		};
	}

	@SuppressWarnings("unchecked")
	private static <T> Function<InvocationHandler, T> createProxyFactory(
			final Class<T> proxyClassType, Class<?>[] interfaces) {
		// create a proxy class and cache its constructor
		Class<?> proxyClass = Proxy.getProxyClass(proxyClassType.getClassLoader(), interfaces);
		
		Constructor<T> constructor;
		try {
			constructor = (Constructor<T>) proxyClass.getConstructor(InvocationHandler.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new InternalError(e);
		}
		return handler -> {
			try {
				return constructor.newInstance(handler);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		};
	}

	private static Function<Class<?>, Lookup> getLookupFactory() {
        Constructor<MethodHandles.Lookup> methodHandleConstuctor;
		try {
			methodHandleConstuctor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}

		return clazz -> {
			final boolean accessible = methodHandleConstuctor.isAccessible();
			methodHandleConstuctor.setAccessible(true);
	        try {
				return methodHandleConstuctor.newInstance(clazz, MethodHandles.Lookup.PRIVATE);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			} finally {
				methodHandleConstuctor.setAccessible(accessible);
			}
		};
	}
}
