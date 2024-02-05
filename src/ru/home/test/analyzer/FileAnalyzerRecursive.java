package ru.home.test.analyzer;

import ru.home.test.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Обход рекурсивно
 */
public class FileAnalyzerRecursive extends AbstractFileAnalyzer
{
    public FileAnalyzerRecursive(Configuration configuration) {
        super(configuration);
    }

    /**
     * Функция для поиска файлов и директорий рекурсивно для добавления в очередь из callList
     * @param callList - лист очереди для исполнения в {@link ExecutorService}
     * @param path - путь до файла
     * @param level - текущая глубина обхода
     */
    private void searchRecursive(final List<Call> callList, String path, int level) {
        final File directory = new File(path);
        final File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() ) {
                    //Проверяем на глубину рекурсии
                    if (!getConfiguration().isMaxDepthInit() || level < getConfiguration().getMaxDepth()) {
                        //Вызываем рекурсию
                        searchRecursive(callList, file.getPath(), level + 1);
                    }
                } else {
                    if (isInWAndNotBLists(file.getName())) {
                        callList.add(new Call(file, getFilesInfo()));
                    }
                }
            }
        }
    }

    @Override
    public void search() throws InterruptedException {
        final List<Call> callList = new ArrayList<>();
        searchRecursive(callList, getConfiguration().getPath(), 0);
        final ExecutorService service = Executors.newFixedThreadPool(getConfiguration().getThreads());
        service.invokeAll(callList, 10, TimeUnit.SECONDS);
        service.shutdown();
        try {
            if (service.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println(getFilesInfo().getPlainStr());
                outputToFile();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
