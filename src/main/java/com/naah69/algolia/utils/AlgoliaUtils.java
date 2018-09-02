package com.naah69.algolia.utils;

import com.algolia.search.APIClient;
import com.algolia.search.ApacheAPIClientBuilder;
import com.algolia.search.Index;
import com.algolia.search.exceptions.AlgoliaException;
import com.algolia.search.objects.tasks.sync.TaskSingleIndex;
import com.naah69.algolia.Algolia;
import com.naah69.algolia.FilePath;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * AlgoliaUtils
 *
 * @author naah
 * @date 2018-09-01 下午11:54
 * @desc
 */
public class AlgoliaUtils {

    private static APIClient client;
    private static Index<Algolia> index;
    private static final String appId;
    private static final String key;

    static {
        String configYaml = FileUtils.readFileByLines(new File(FilePath.ALGOLIA_CONFIG_YAML_PATH));
        Map<String, Object> configMap = YamlUtils.convertToMap(configYaml);
        Map<String, String> algoliaMap = (Map<String, String>) configMap.get("algolia");
        appId = algoliaMap.get("appID");
        key = algoliaMap.get("key");
    }

    /**
     * 更新 Algolia
     *
     * @param list Algolia列表
     * @return 结果
     * @throws AlgoliaException
     */
    public static boolean updateAlgolia(List<Algolia> list) throws AlgoliaException {
        try {
            client = new ApacheAPIClientBuilder(appId, key).build();
            index = client.initIndex("blog", Algolia.class);
            TaskSingleIndex result = index.saveObjects(list);
            if (result != null) {
                System.out.println("ObjectIDs: " + result.getObjectIDs());
                System.out.println("TaskID: " + result.getTaskID());
                System.out.println("IndexName: " + result.getIndexName());
                return true;
            } else {
                return false;
            }
        } catch (AlgoliaException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
        return false;
    }


}
