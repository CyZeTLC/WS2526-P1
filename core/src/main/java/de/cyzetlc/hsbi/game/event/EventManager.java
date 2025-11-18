package de.cyzetlc.hsbi.game.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventManager {
    private static final Logger logger = LoggerFactory.getLogger(EventManager.class.getName());

    private static final Map<Class<? extends Event>, ArrayList<EventData>> REGISTRY_MAP = new HashMap<>();

    /**
     * The function sorts a list of EventData objects based on their priority value.
     *
     * @param clazz The parameter "clazz" is a Class object that represents the type of Event. It is used to retrieve the
     * list of EventData objects associated with that specific Event type from the EventManager's registry map.
     */
    private static void sortListValue(final Class<? extends Event> clazz) {
        final ArrayList<EventData> flexableArray = new ArrayList<EventData>();

        for (final byte b : EventPriority.VALUE_ARRAY) {
            for (EventData methodData : EventManager.REGISTRY_MAP.get(clazz)) {
                if (methodData.priority == b) {
                    flexableArray.add(methodData);
                }
            }
        }
        EventManager.REGISTRY_MAP.put(clazz, flexableArray);
    }

    /**
     * The function checks if a given method is bad by checking if it has exactly one parameter and if it is annotated with
     * the EventHandler annotation.
     *
     * @param method The parameter `method` is of type `Method`.
     * @return The method is returning a boolean value.
     */
    private static boolean isMethodBad(final Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventHandler.class);
    }

    /**
     * The function checks if a given method is bad or if its first parameter type matches a specified class.
     *
     * @param method The "method" parameter is of type Method, which represents a method in Java. It is used to check if
     * the method is bad or not.
     * @param clazz The parameter `clazz` is a `Class` object that represents a class that extends the `Event` class.
     * @return The method is returning a boolean value.
     */
    private static boolean isMethodBad(final Method method, final Class<? extends Event> clazz) {
        return isMethodBad(method) || method.getParameterTypes()[0].equals(clazz);
    }

    /**
     * The function returns an ArrayList of EventData objects based on the provided class.
     *
     * @param clazz The parameter "clazz" is a Class object that represents the class of the Event. It is a generic type
     * parameter that extends the Event class.
     * @return An ArrayList of EventData objects is being returned.
     */
    public static ArrayList<EventData> get(final Class<? extends Event> clazz) {
        return REGISTRY_MAP.get(clazz);
    }

    /**
     * The function `cleanMap` removes entries from a map if they are empty or if the `removeOnlyEmptyValues` flag is set
     * to false.
     *
     * @param removeOnlyEmptyValues A boolean flag indicating whether to remove only empty values from the map. If set to
     * true, only entries with empty ArrayList values will be removed. If set to false, all entries will be removed
     * regardless of their ArrayList values.
     */
    public static void cleanMap(final boolean removeOnlyEmptyValues) {
        final Iterator<Map.Entry<Class<? extends Event>, ArrayList<EventData>>> iterator = EventManager.REGISTRY_MAP.entrySet().iterator();

        while (iterator.hasNext()) {
            if (!removeOnlyEmptyValues || iterator.next().getValue().isEmpty()) {
                iterator.remove();
            }
        }
    }

    /**
     * The function unregisters an object from a registry map based on its class type.
     *
     * @param o The parameter "o" represents the object that needs to be unregistered from the event registry. This object
     * is typically the listener or subscriber that was previously registered to receive events of a specific class.
     * @param clazz The "clazz" parameter is a Class object that represents the type of event for which the object is being
     * unregistered.
     */
    public static void unregister(final Object o, final Class<? extends Event> clazz) {
        if (REGISTRY_MAP.containsKey(clazz)) {
            for (final EventData methodData : REGISTRY_MAP.get(clazz)) {
                if (methodData.source.equals(o)) {
                    REGISTRY_MAP.get(clazz).remove(methodData);
                }
            }
        }
        cleanMap(true);
    }

    /**
     * The function unregisters an object from a registry map by removing all instances of the object from the map's
     * values.
     *
     * @param o The parameter "o" represents the object that you want to unregister from the registry.
     */
    public static void unregister(final Object o) {
        for (ArrayList<EventData> flexableArray : REGISTRY_MAP.values()) {
            for (int i = flexableArray.size() - 1; i >= 0; i--) {
                if (flexableArray.get(i).source.equals(o)) {
                    flexableArray.remove(i);
                }
            }
        }
        cleanMap(true);
    }

    /**
     * The function registers a method as an event handler and adds it to a registry map.
     *
     * @param method The method to be registered as an event handler.
     * @param o The parameter "o" is an object that represents the instance of the class that contains the method being
     * registered.
     */
    public static void register(final Method method, final Object o) {
        final Class<?> clazz = method.getParameterTypes()[0];
        final EventData methodData = new EventData(o, method, method.getAnnotation(EventHandler.class).priority());

        if (!methodData.target.isAccessible()) {
            methodData.target.setAccessible(true);
        }

        if (REGISTRY_MAP.containsKey(clazz)) {
            if (!REGISTRY_MAP.get(clazz).contains(methodData)) {
                REGISTRY_MAP.get(clazz).add(methodData);
                sortListValue((Class<? extends Event>) clazz);
            }
        } else {
            REGISTRY_MAP.put((Class<? extends Event>) clazz, new ArrayList<EventData>() {
                {
                    this.add(methodData);
                }
            });
        }
        logger.info("Registered new Listener ({})", o.getClass().getSimpleName());
    }

    /**
     * The function registers all methods of an object that are not considered bad for a given event class.
     *
     * @param o The parameter "o" is an object that you want to register for event handling. It could be any object that
     * has methods that handle events.
     * @param clazz The "clazz" parameter is a Class object that represents the type of event that the method should
     * handle. It is a generic type parameter that extends the Event class.
     */
    public static void register(final Object o, final Class<? extends Event> clazz) {
        for (final Method method : o.getClass().getMethods()) {
            if (!isMethodBad(method, clazz)) {
                register(method, o);
            }
        }
    }

    /**
     * The function registers all non-bad methods of an object.
     *
     * @param o The parameter "o" is an object that we want to register.
     */
    public static void register(Object o) {
        for (final Method method : o.getClass().getMethods()) {
            if (!isMethodBad(method)) {
                register(method, o);
            }
        }
    }

}
