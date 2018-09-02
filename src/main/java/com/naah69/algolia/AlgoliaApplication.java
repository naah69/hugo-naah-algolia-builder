package com.naah69.algolia;

import com.algolia.search.exceptions.AlgoliaException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.naah69.algolia.utils.AlgoliaUtils;
import com.naah69.algolia.utils.FileUtils;
import com.naah69.algolia.utils.YamlUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class AlgoliaApplication {

    private static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("demo-pool-%d").build();
    private static ExecutorService fixedThreadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());


    public static void main(String[] args) throws AlgoliaException, IOException, InterruptedException {

        printAuthorInfo();
        existsConfigFile();
        System.out.println("==================== update algolia index start ====================");
        long startTime = System.currentTimeMillis();
        execCompile();
        existsAlgoliaCompileResultJson();
        System.out.println("compile success: " + (System.currentTimeMillis() - startTime) / 1000 + " sec");
        startTime = System.currentTimeMillis();
        List<File> mdList = getMarkDownList();
        List<String> disableList = getDisableWordList();
        List<Algolia> algoliaList = getAlgoliasList();

        Map<String, String> participlesMap = new ConcurrentHashMap<>(algoliaList.size());

        CountDownLatch latch = new CountDownLatch(algoliaList.size());

        for (File file : mdList) {

            fixedThreadPool.execute(() -> {
                String article = FileUtils.readFileByLines(file);
                try {
                    String articleParam = article.substring(0, article.indexOf("---", 4));
                    article = article.substring(article.indexOf("---", 4) + 3, article.length());
                    Map<String, Object> articleYaml = YamlUtils.convertToMap(articleParam);
                    String title = articleYaml.get("title").toString();
                    String participles = getParticiples(disableList, article);
                    participlesMap.put(title, participles);
                    System.out.println("generate success: " + file.getAbsolutePath());
                } catch (Exception e) {
                    System.out.println("generate error: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
                finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        for (Algolia algolia : algoliaList) {
            String participles = participlesMap.get(algolia.getTitle());
            algolia.setContent(algolia.getContent() + " " + participles);
        }

        boolean result = AlgoliaUtils.updateAlgolia(algoliaList);
        if (result) {
            writeAlgoliaJson(algoliaList);
            System.out.println("update success: " + (System.currentTimeMillis() - startTime) / 1000 + " sec");
            System.out.println("==================== update algolia index end ====================");
            System.exit(0);
        }


    }

    /**
     * 打印作者信息
     */
    private static void printAuthorInfo() {
        System.out.println("     ___           ___           ___           ___");
        System.out.println("    /\\__\\         /\\  \\         /\\  \\         /\\__\\");
        System.out.println("   /::|  |       /::\\  \\       /::\\  \\       /:/  /");
        System.out.println("  /:|:|  |      /:/\\:\\  \\     /:/\\:\\  \\     /:/__/");
        System.out.println(" /:/|:|  |__   /::\\~\\:\\  \\   /::\\~\\:\\  \\   /::\\  \\ ___");
        System.out.println("/:/ |:| /\\__\\ /:/\\:\\ \\:\\__\\ /:/\\:\\ \\:\\__\\ /:/\\:\\  /\\__\\");
        System.out.println("\\/__|:|/:/  / \\/__\\:\\/:/  / \\/__\\:\\/:/  / \\/__\\:\\/:/  /");
        System.out.println("    |:/:/  /       \\::/  /       \\::/  /       \\::/  /");
        System.out.println("    |::/  /        /:/  /        /:/  /        /:/  /");
        System.out.println("    /:/  /        /:/  /        /:/  /        /:/  /");
        System.out.println("    \\/__/         \\/__/         \\/__/         \\/__/");

        System.out.println("================ Welcome to use naah-algolia-builder ===============");
        System.out.println();
        System.out.println("==================== Blog: http://www.naah69.com ===================");
        System.out.println();
    }

    /**
     * 判断配置文件是否存在
     */
    private static void existsConfigFile(){
        System.out.println("====================== check config file start =====================");

        boolean result=true;

        result=result&&FileUtils.existsFile(FilePath.PARENT_DIR_PATH);
        result=result&&FileUtils.existsFile(FilePath.ALGOLIA_CONFIG_YAML_PATH);
        result=result&&FileUtils.existsFile(FilePath.COMPLIE_EXEC_PATH);
        result=result&&FileUtils.existsFile(FilePath.CONENT_DIR_PATH);

        if(result){
            System.out.println("check success: all file found");
        }else{
            System.out.println("check error: please check these files that are not found");
        }
        System.out.println("====================== check config file end =======================\n");
        if (!result){
            System.exit(1);
        }
    }

    /**
     * 判断编译结果是否存在
     */
    private static  void existsAlgoliaCompileResultJson(){
        File algoliaConfigJson=new File(FilePath.ALGOLIA_COMPLIE_JSON_PATH);
        boolean algoliaConfigJsonExistsResult = algoliaConfigJson.exists();
        if(!algoliaConfigJsonExistsResult){
            System.out.println("compile error: "+algoliaConfigJson.getAbsolutePath()+" not found");
            System.exit(1);
        }

    }

    /**
     * 执行编译脚本
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private static void execCompile() throws IOException, InterruptedException {
        Process process = null;
        process = Runtime.getRuntime().exec(FilePath.COMPLIE_EXEC_PATH);
        process.waitFor();
    }

    /**
     * 向文件写入algolia.json
     *
     * @param algoliaList Algolias列表
     */
    private static void writeAlgoliaJson(List<Algolia> algoliaList) {
        FileUtils.writeToFile(FilePath.ALGOLIA_COMPLIE_JSON_PATH, JSON.toJSONString(algoliaList));
    }

    /**
     * 获取Algolias列表
     *
     * @return Algolias列表
     */
    private static List<Algolia> getAlgoliasList() {
        String algoliaJSON = FileUtils.readFileByLines(new File(FilePath.ALGOLIA_COMPLIE_JSON_PATH));
        return JSON.parseObject(algoliaJSON, new TypeReference<List<Algolia>>() {
        });
    }

    /**
     * 获取分词
     *
     * @param disableList 停用词列表
     * @param str         文本
     * @return 分词文本（空格间隔）
     */
    private static String getParticiples(List<String> disableList, String str) {
        List<Term> termList = IndexTokenizer.segment(str);
        HashSet<String> wordSet = new HashSet<>();
        for (Term term : termList) {
            wordSet.add(term.word);
        }
        wordSet.removeAll(disableList);
        StringBuilder sb = new StringBuilder();
        for (String word : wordSet) {
            sb.append(word).append(" ");
        }
        return sb.toString().replaceAll("\n", "");
    }

    /**
     * 获取停用表
     *
     * @return 停用表
     */
    private static List<String> getDisableWordList() {
        String disable = "";
        String classPath = AlgoliaApplication.class.getResource("AlgoliaApplication.class").toString();
        if (classPath.startsWith("jar")) {
            disable = FileUtils.readFileByInputStream(AlgoliaApplication.class.getClassLoader().getResourceAsStream("disable_word.txt"));
        } else {
            disable = FileUtils.readFileByLines(new File(AlgoliaApplication.class.getResource("/").getPath() + "disable_word.txt"));
        }
        String[] split = disable.split(",");
        return Arrays.asList(split);
    }

    /**
     * 获取markdown列表
     *
     * @return markdown列表
     */
    private static List<File> getMarkDownList() {
        List<File> mdList = FileUtils.getAllFiles(FilePath.CONENT_DIR_PATH);
        for (Iterator<File> fileIterator = mdList.iterator(); fileIterator.hasNext(); ) {
            File next = fileIterator.next();
            if (!next.getAbsolutePath().endsWith(".md")) {
                fileIterator.remove();
            }
        }
        return mdList;
    }
}
