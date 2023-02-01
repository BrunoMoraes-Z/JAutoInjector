package com.brunomoraesz;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AutoInjectorTest {

    private AutoInjector autoInjector;

    @BeforeMethod
    public void setUp() {
        autoInjector = new AutoInjector();
    }

    @Test
    public void testAddSingleton() {
        autoInjector.addSingleton(MyType::new);
        MyType myType = autoInjector.get(MyType.class);
        Assert.assertNotNull(myType);
    }

    @Test
    public void testGet() {
        autoInjector.add(MyType::new);
        MyType myType = autoInjector.get(MyType.class);
        Assert.assertNotNull(myType);
    }

    @Test
    public void testRemove() {
        autoInjector.addSingleton(MyType::new);
        MyType myType = autoInjector.get(MyType.class);
        Assert.assertNotNull(myType);

        autoInjector.remove(MyType.class);

        myType = autoInjector.get(MyType.class);
        Assert.assertNull(myType);
    }

    @Test
    public void testReplaceInstance() {
        autoInjector.addSingleton(MyType::new);
        MyType myObject = autoInjector.get(MyType.class);
        Assert.assertNotNull(myObject);

        MyType newMyObject = new MyType();
        autoInjector.replaceInstance(newMyObject);
        MyType replacedMyObject = autoInjector.get(MyType.class);
        Assert.assertNotNull(replacedMyObject);

        Assert.assertEquals(replacedMyObject, newMyObject);
    }

    @Test
    public void testDisposeSingleton() {
        autoInjector.addSingleton(MyType::new);
        MyType myType = autoInjector.get(MyType.class);
        Assert.assertNotNull(myType);

        autoInjector.disposeSingleton(MyType.class);

        myType = autoInjector.get(MyType.class);
        Assert.assertNull(myType);
    }

    @Test
    public void testIsInstantiateSingleton() {
        autoInjector.addSingleton(MyType::new);
        boolean isSingleton = autoInjector.isInstantiateSingleton(MyType.class);
        Assert.assertTrue(isSingleton);
    }

    @Test
    void testInstance() {
        MyType myObject = new MyType();
        autoInjector.instance(myObject);

        Assert.assertNotNull(autoInjector.get(MyType.class));
        Assert.assertEquals(myObject, autoInjector.get(MyType.class));
    }

    @Test
    public void testAddLazySingleton() {
        autoInjector.addLazySingleton(MyType::new);

        MyType instance1 = autoInjector.get(MyType.class);
        MyType instance2 = autoInjector.get(MyType.class);

        // Test that the same instance is returned on subsequent calls to get
        Assert.assertSame(instance1, instance2);
    }

    @Test
    public void testCall() {
        autoInjector.addSingleton(MyType::new);

        // Test calling a registered instance
        MyType myObject = autoInjector.call(MyType.class);
        Assert.assertEquals(myObject.getValue(), "Hello World");

        // Test calling an unregistered instance
        try {
            autoInjector.call(AnotherType.class);
        } catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "Instance not found: AnotherType");
        }

        // Test calling a disposed instance
        autoInjector.disposeSingleton(MyType.class);
        try {
            autoInjector.call(MyType.class);
        } catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "Instance not found: MyType");
        }
    }

    @Test
    public void testIsAdded() {
        autoInjector.add(MyType::new);

        Assert.assertTrue(autoInjector.isAdded(MyType.class));
        Assert.assertFalse(autoInjector.isAdded(AnotherType.class));
    }

    @Test
    public void testAddInjector() {
        AutoInjector injector1 = new AutoInjector();
        AutoInjector injector2 = new AutoInjector();

        injector1.addSingleton(MyType::new);
        injector2.addSingleton(AnotherType::new);

        injector1.addInjector(injector2);

        Assert.assertTrue(injector1.isAdded(MyType.class));
        Assert.assertTrue(injector1.isAdded(AnotherType.class));
    }

}

class MyType {
    public String getValue() {
        return "Hello World";
    }
}

class AnotherType {
}
