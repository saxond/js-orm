package org.daubin.js.database;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class Annotations {
	private Annotations() {
	}

	/**
	 * Returns an implementation of an annotation using key/values from a map.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T generateAnnotationProxy(Class<T> annotationClass,
			Map<String, Object> map) {
		InvocationHandler handler = (proxy, method, args) -> {
			Object value = map.get(method.getName());
			if (null == value) {
				Method annotationMethod = annotationClass.getMethod(method.getName(), method.getParameterTypes());
				return annotationMethod.getDefaultValue();
			}
			return value;
		};
		return (T) Proxy.newProxyInstance(Columns.class.getClassLoader(), new Class<?>[]{ annotationClass }, handler);
	}
}
