package com.brunomoraesz;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AutoInjector {

    // Map of factory functions for creating instances of a certain type
    private final Map<Class<?>, Supplier<?>> factoryMap = new HashMap<>();

    // Map of singleton instances for a certain type
    private final Map<Class<?>, Object> singletonMap = new HashMap<>();

    // Map of lazy singleton factory functions for creating singleton instances of a certain type
    private final Map<Class<?>, Supplier<?>> lazySingletonMap = new HashMap<>();

    // Map of specific instances for a certain type
    private final Map<Class<?>, Object> instanceMap = new HashMap<>();


    /**
     * Registers a factory function for creating instances of a certain type.
     *
     * @param supplier The factory function.
     */
    public <T> void add(Supplier<T> supplier) {
        Class<T> type = getType(supplier);
        factoryMap.put(type, supplier);
    }

    /**
     * Registers a singleton instance for a certain type.
     *
     * @param supplier The factory function used to create the singleton instance.
     */
    public <T> void addSingleton(Supplier<T> supplier) {
        Class<T> type = getType(supplier);
        singletonMap.put(type, supplier.get());
    }

    /**
     * Registers a lazy singleton factory function for creating singleton instances of a certain type.
     *
     * @param supplier The lazy singleton factory function.
     */
    public <T> void addLazySingleton(Supplier<T> supplier) {
        Class<T> type = getType(supplier);
        lazySingletonMap.put(type, supplier);
    }

    /**
     * Registers a specific instance for a certain type.
     *
     * @param instance The instance to register.
     */
    public <T> void instance(T instance) {
        instanceMap.put(instance.getClass(), instance);
    }

    /**
     * Gets an instance of a certain type.
     *
     * @param type             The type of the instance to get.
     * @param createIfNotFound If true, a new instance will be created using reflection if an
     *                         instance of the given type is not found in the injector's maps.
     * @return The instance, or null if not found and createIfNotFound is false.
     */
    public <T> T get(Class<T> type, boolean createIfNotFound) {

        // Check if the instance is in the instanceMap
        T instance = (T) instanceMap.get(type);
        if (instance != null) {
            return instance;
        }

        // Check if the instance is in the factoryMap
        Supplier<T> supplier = (Supplier<T>) factoryMap.get(type);
        if (supplier != null) {
            return supplier.get();
        }

        // Check if the instance is in the singletonMap
        T singleton = (T) singletonMap.get(type);
        if (singleton != null) {
            return singleton;
        }

        // Check if the instance is in the lazySingletonMap
        Supplier<T> lazySingletonSupplier = (Supplier<T>) lazySingletonMap.get(type);
        if (lazySingletonSupplier != null) {
            T lazySingleton = lazySingletonSupplier.get();
            singletonMap.put(type, lazySingleton);
            return lazySingleton;
        }

        // If createIfNotFound is true, try to create the instance using reflection
        if (createIfNotFound) {
            try {
                // Use reflection to get the constructor for the given type
                Constructor<T> constructor = type.getConstructor();

                // Get the parameter types for the constructor
                Class<?>[] paramTypes = constructor.getParameterTypes();

                // Create an array to store the constructor arguments
                Object[] args = new Object[paramTypes.length];

                // Recursively call get() for each required dependency
                for (int i = 0; i < paramTypes.length; i++) {
                    args[i] = get(paramTypes[i], true);
                    if (args[i] == null) {
                        // One or more dependencies were not found, so return null
                        return null;
                    }
                }

                // All dependencies were found, so create a new instance using the constructor
                instance = constructor.newInstance(args);

                // Add the instance to the instanceMap
                instanceMap.put(type, instance);

                return instance;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                // The type does not have a no-arg constructor, so return null
                return null;
            } // An error occurred while trying to create the instance, so return null

        }

        return null;
    }

    /**
     * Gets an instance of a certain type.
     *
     * @param type The type of the instance to get.
     * @return The instance, or null if not found.
     */
    public <T> T get(Class<T> type) {
        return get(type, false);
    }

    /**
     * Gets an instance of a certain type using the default factory function.
     *
     * @return The instance.
     * @throws IllegalArgumentException If the default factory function is not registered.
     */
    public <T> T get() {
        return get(getType(null), true);
    }

    /**
     * Request an instance by [Type]
     */
    public <T> T call(Class<T> type) {
        return get(type);
    }

    /**
     * Request an instance by [Type]
     */
    public <T> boolean isAdded() {
        return isAdded(getType(null));
    }

    /**
     * Checks if the instance record exists.
     */
    public boolean isAdded(Class<?> type) {
        return factoryMap.containsKey(type) || singletonMap.containsKey(type) || lazySingletonMap.containsKey(type) || instanceMap.containsKey(type);
    }

    /**
     * Helper function to get the type of an instance from a factory function.
     */
    private <T> Class<T> getType(Supplier<T> supplier) {
        if (supplier == null) return null;
        return (Class<T>) supplier.get().getClass();
    }

    /**
     * Helper function to get the type of an instance from an object.
     */
    private <T> Class<T> getType(T instance) {
        return (Class<T>) instance.getClass();
    }

    /**
     * Inherit all instance and transform records.
     */
    public void addInjector(AutoInjector injector) {
        factoryMap.putAll(injector.factoryMap);
        singletonMap.putAll(injector.singletonMap);
        lazySingletonMap.putAll(injector.lazySingletonMap);
        instanceMap.putAll(injector.instanceMap);
    }

    /**
     * Checks if the instance registration is as singleton.
     */
    public <T> boolean isInstantiateSingleton() {
        return isInstantiateSingleton(getType(null));
    }

    /**
     * Checks if the instance registration is as singleton.
     */
    public boolean isInstantiateSingleton(Class<?> type) {
        return singletonMap.containsKey(type) || lazySingletonMap.containsKey(type);
    }

    /**
     * Unregisters an instance by type.
     */
    public <T> void remove() {
        remove(getType(null));
    }

    /**
     * Unregisters an instance by type.
     */
    public void remove(Class<?> type) {
        factoryMap.remove(type);
        singletonMap.remove(type);
        lazySingletonMap.remove(type);
        instanceMap.remove(type);
    }

    /**
     * Removes the singleton instance.
     * This does not remove it from the registry tree.
     */
    public <T> void disposeSingleton() {
        disposeSingleton(getType(null));
    }

    /**
     * Removes the singleton instance.
     * This does not remove it from the registry tree.
     */
    public void disposeSingleton(Class<?> type) {
        singletonMap.remove(type);
    }

    /**
     * Replaces an instance record with a concrete instance.
     * This function should only be used for unit testing.
     * Any other use is discouraged.
     */
    public <T> void replaceInstance(T instance) {
        instanceMap.put(instance.getClass(), instance);
    }

}
