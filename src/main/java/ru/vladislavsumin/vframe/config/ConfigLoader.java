package ru.vladislavsumin.vframe.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vladislavsumin.vframe.VFrameRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Load field with annotation {@link LoadFromConfig}
 * Should work without initialized VFrame
 *
 * @author Sumin Vladislav
 * @version 1.4
 * @see LoadFromConfig
 */
@SuppressWarnings("unused")
public class ConfigLoader {
    private static Logger log = LogManager.getLogger();

    private static final String CONFIG_NAME = "%s.properties";
    private static final String CONFIG_FOLDER = "config/";

    private final Map<String, Container> preferencesMap = new HashMap<>();

    public static void load(Object o) throws VFrameRuntimeException {
        new ConfigLoader(o.getClass(), o);
    }

    public static void load(Class<?> clazz) throws VFrameRuntimeException {
        new ConfigLoader(clazz, null);
    }

    private class Container {
        Properties properties;
        boolean updated = false;
    }

    private ConfigLoader(Class<?> clazz, Object o) {
        createConfigFolder();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            initField(field, o, clazz);
        }

        saveProperty();
    }

    private void initField(Field field, Object o, Class<?> clazz) {
        LoadFromConfig annotation = field.getAnnotation(LoadFromConfig.class);
        if (annotation == null) return;
        if (o == null && !Modifier.isStatic(field.getModifiers())) return;

        final Container container = getContainer(annotation.filename());

        String propertyName;
        if (annotation.key().equals("")) propertyName = field.getName();
        else propertyName = annotation.key();

        String propertyValue = container.properties.getProperty(propertyName);
        if (propertyValue == null) {
            propertyValue = annotation.defaultValue();
            container.properties.setProperty(propertyName, propertyValue);
            container.updated = true;
        }

        field.setAccessible(true);
        final Class<?> fieldType = field.getType();
        try {
            // String
            if (fieldType.equals(String.class)) {
                field.set(o, propertyValue);
            }
            // Int
            else if (fieldType.equals(Integer.TYPE)) {
                field.set(o, Integer.parseInt(propertyValue));
            }
            // Boolean
            else if (fieldType.equals(Boolean.TYPE)) {
                field.set(o, Boolean.parseBoolean(propertyValue));
            }
            // Unsupported type.
            else {
                final String err = String.format("VFrame: Field %s in class %s have unsupported type %s",
                        field.getName(), clazz.getName(), fieldType.getName());
                log.error(err);
                throw new VFrameRuntimeException(err);
            }
        } catch (IllegalAccessException e) {
            log.error("VFrame: ConfigLoader internal error", e);
            throw new VFrameRuntimeException(e);
        } catch (NumberFormatException e) {
            log.error("VFrame: Value {} is not a type {}", propertyValue, fieldType.getName());
            throw new VFrameRuntimeException(e);
        }
    }

    private Container getContainer(String name) {
        if (preferencesMap.containsKey(name))
            return preferencesMap.get(name);

        Container container = new Container();
        container.properties = new Properties();

        try {
            final String filename = String.format(CONFIG_FOLDER + CONFIG_NAME, name);
            final File file = new File(filename);
            if (!file.exists() && !file.createNewFile()) {
                String err = "VFrame can not create file " + filename;
                log.fatal(err);
                throw new VFrameRuntimeException(err);
            }
            container.properties.load(new FileInputStream(file));
        } catch (IOException e) {
            log.fatal("VFrame: ConfigLoader internal error", e);
            throw new VFrameRuntimeException(e);
        }

        preferencesMap.put(name, container);
        return container;
    }

    private void saveProperty() {
        for (String name : preferencesMap.keySet()) {
            Container container = preferencesMap.get(name);
            if (container.updated) try {
                container.properties.store(new FileOutputStream(
                        String.format(CONFIG_FOLDER + CONFIG_NAME, name)), "");
            } catch (IOException e) {
                log.error("VFrame: ConfigLoader can not save config file", e);
                throw new VFrameRuntimeException(e);
            }
        }
    }

    private void createConfigFolder() {
        final File file = new File(CONFIG_FOLDER);
        if (!file.exists() && !file.mkdir()) {
            final String err = "VFrame cannot create config folder.";
            log.fatal(err);
            throw new VFrameRuntimeException(err);
        }
    }
}
