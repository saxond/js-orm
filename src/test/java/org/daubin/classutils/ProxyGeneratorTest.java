package org.daubin.classutils;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

public class ProxyGeneratorTest {
	
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
}
