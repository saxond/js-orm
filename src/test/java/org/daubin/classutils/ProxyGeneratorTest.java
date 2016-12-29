package org.daubin.classutils;

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class ProxyGeneratorTest {

	@Test
	public void multipleInterface() throws Exception {
		TestFactory factory = ProxyGenerator.createMultipleInterfaceFactory(TestFactory.class);
		
		Object instance = factory.newInstance(l -> l.toString(), () -> 666);
		Assert.assertEquals("5", ((Function<Long, String>)instance).apply(5l));
		Assert.assertEquals(Integer.valueOf(666), ((Supplier<Integer>)instance).get());
	}
	
	@Test
	public void extension() throws Exception {
		Function<Function<Long,String>, TestExtension> extensionFactory = ProxyGenerator.createInterfaceExtensionFactory(TestExtension.class);
		TestExtension extension = extensionFactory.apply(l -> "test" + l);
		
		Assert.assertEquals("test6", extension.apply(6l));
		Assert.assertEquals("testing", extension.test());
		
		Assert.assertEquals("test77after", extension.andThen(s -> s + "after").apply(77l));
	}
	
	public interface TestExtension extends Function<Long,String> {
		default String test() {
			return "testing";
		}
	}
	
	@FunctionalInterface
	public interface TestFactory {
		Object newInstance(Function<Long, String> f, Supplier<Integer> t);
	}
}
