package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.ConfigurationInstantiatorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;



@Service("configurationProviderService")
public class ConfigurationProviderService {

    private static final String ARDOCO = "edu.kit.kastel.mcse.ardoco";

    private static final Logger logger = LogManager.getLogger(ResultService.class);

    /**
     * Retrieves the default configuration for all configurable classes in the ArDoCo project.
     *
     * @return a map containing the configuration keys and their default values
     * @throws InvocationTargetException if an error occurs during instantiation
     * @throws InstantiationException if an error occurs during instantiation
     * @throws IllegalAccessException if an error occurs during access to fields
     */
    public Map<String, String> getDefaultConfiguration() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, String> configs = new TreeMap<>();
        var reflectAccess = new Reflections(ARDOCO);
        var classesThatMayBeConfigured = reflectAccess.getSubTypesOf(AbstractConfigurable.class)
                .stream()
                .filter(c -> c.getPackageName().startsWith(ARDOCO))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(c -> !c.getPackageName().contains("tests"))
                .toList();
        for (var clazz : classesThatMayBeConfigured) {
            processConfigurationOfClass(configs, clazz);
        }

        System.out.println("-".repeat(50));
        System.out.println("Current Default Configuration");
        System.out.println(configs.entrySet()
                .stream()
                .map(e -> e.getKey() + AbstractConfigurable.KEY_VALUE_CONNECTOR + e.getValue())
                .collect(Collectors.joining("\n")));
        System.out.println("-".repeat(50));
        return configs;
    }


    protected void processConfigurationOfClass(Map<String, String> configs, Class<? extends AbstractConfigurable> clazz) throws InvocationTargetException,
            InstantiationException, IllegalAccessException {
        var object = ConfigurationInstantiatorUtils.createObject(clazz);
        List<Field> fields = new ArrayList<>();
        findImportantFields(object.getClass(), fields);
        fillConfigs(object, fields, configs);
    }

    private void fillConfigs(AbstractConfigurable object, List<Field> fields, Map<String, String> configs) throws IllegalAccessException {
        for (Field f : fields) {
            f.setAccessible(true);
            var key = AbstractConfigurable.getKeyOfField(object, f.getDeclaringClass(), f);
            var rawValue = f.get(object);
            var value = getValue(rawValue);
            if (configs.containsKey(key)) {
                logger.warn("Found duplicate entry in map: " + key);
            }
            configs.put(key, value);
        }
    }

    private String getValue(Object rawValue) {
        if (rawValue instanceof Integer i) {
            return Integer.toString(i);
        }
        if (rawValue instanceof Double d) {
            return String.format(Locale.ENGLISH, "%f", d);
        }
        if (rawValue instanceof Boolean b) {
            return String.valueOf(b);
        }
        if (rawValue instanceof List<?> s && s.stream().allMatch(it -> it instanceof String)) {
            return s.stream().map(Object::toString).collect(Collectors.joining(AbstractConfigurable.LIST_SEPARATOR));
        }
        if (rawValue instanceof Enum<?> e) {
            return e.name();
        }

        throw new IllegalArgumentException("RawValue has no type that may be transformed to an Configuration" + rawValue + "[" + rawValue.getClass() + "]");
    }

    private void findImportantFields(Class<?> clazz, List<Field> fields) {
        if (clazz == Object.class || clazz == AbstractConfigurable.class) {
            return;
        }

        for (var field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Configurable.class)) {
                fields.add(field);
            }
        }
        findImportantFields(clazz.getSuperclass(), fields);
    }


}


