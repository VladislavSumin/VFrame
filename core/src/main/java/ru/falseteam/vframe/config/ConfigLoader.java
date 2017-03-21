package ru.falseteam.vframe.config;

import ru.falseteam.vframe.VFrameRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

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
    private static Logger log = Logger.getLogger(ConfigLoader.class.getName());

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
                final String err = String.format("VFrame: field %s in class %s have unsupported type %s",
                        field.getName(), clazz.getName(), fieldType.getName());
                throw new VFrameRuntimeException(err);
            }
        } catch (IllegalAccessException e) {
            throw new VFrameRuntimeException("VFrame: ConfigLoader internal error", e);
        } catch (NumberFormatException e) {
            String err = String.format("VFrame: Value %s is not a type %s", propertyValue, fieldType.getName());
            throw new VFrameRuntimeException(err, e);
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
                throw new VFrameRuntimeException("VFrame can not create file " + filename);
            }
            container.properties.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new VFrameRuntimeException("VFrame: ConfigLoader internal error", e);
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
                throw new VFrameRuntimeException("VFrame: ConfigLoader can not save config file", e);
            }
        }
    }

    private void createConfigFolder() {
        final File file = new File(CONFIG_FOLDER);
        if (!file.exists() && !file.mkdir()) {
            throw new VFrameRuntimeException("VFrame: ConfigLoader cannot create config folder");
        }
    }
}
