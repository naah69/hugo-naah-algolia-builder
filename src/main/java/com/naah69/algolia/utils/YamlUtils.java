package com.naah69.algolia.utils;

import org.yaml.snakeyaml.Yaml;

import java.util.Map;


/**
 * YamlUtils
 *
 * @author naah
 * @date 2018-09-01 下午11:54
 * @desc
 */
public class YamlUtils {


    /**
     * Yaml转Map
     *
     * @param yamlString yaml文本
     * @return
     */

    public static Map<String, Object> convertToMap(String yamlString) {
        if (yamlString.startsWith("---\n")) {
            yamlString = yamlString.replaceAll("---\n", "");
        }
        if (yamlString.contains("!ruby/hash")) {
            yamlString = yamlString.replaceAll("(?i)!ruby/.*\n", "\n");
        }
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);
        return map;
    }


}