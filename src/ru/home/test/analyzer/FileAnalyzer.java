package ru.home.test.analyzer;

import ru.home.test.Configuration;
import ru.home.test.model.FilesInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Обход нерекурсивно
 */
public class FileAnalyzer extends AbstractFileAnalyzer{

    private final FilesInfo filesInfo = new FilesInfo();

    public FileAnalyzer(Configuration configuration) {
        super(configuration);
    }

    private void searchNotRecursive(final List<Call> callList, String path, int level) {
        final File rootDir = new File(getConfiguration().getPath());
        final Queue<File> fileTree = new PriorityQueue<>();
        Collections.addAll(fileTree, rootDir.listFiles());

        while (!fileTree.isEmpty())
        {
            final File currentFile = fileTree.remove();
            if(currentFile.isDirectory()){
                Collections.addAll(fileTree, currentFile.listFiles());
            } else if (isInWAndNotBLists(currentFile.getName())) {
                callList.add(new Call(currentFile, filesInfo));
            }
        }
    }

    @Override
    public void search() throws InterruptedException {
        final List<Call> callList = new ArrayList<>();
        searchNotRecursive(callList, getConfiguration().getPath(), 0);
        final ExecutorService service = Executors.newFixedThreadPool(getConfiguration().getThreads());
        service.invokeAll(callList, 10, TimeUnit.SECONDS);
        service.shutdown();
        try {
            if (service.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println(this.filesInfo.getAllStats());
                outputToFile();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
