package ru.home.test.analyzer;

import ru.home.test.Configuration;
import ru.home.test.model.FileInfo;
import ru.home.test.model.FilesInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public abstract class AbstractFileAnalyzer {
    public static final String COMMENT_MASK = "(^\\/\\/.*$)||(^#.*$)";
    public static final String FILE_EXT_MASK = "^.*\\.%s$";

    private final Configuration configuration;
    private final FilesInfo filesInfo = new FilesInfo();

    public AbstractFileAnalyzer(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public FilesInfo getFilesInfo() {
        return filesInfo;
    }

    public abstract void search() throws InterruptedException;

    /**
     * Функция для проверки наличия в черном и белом списках расширения файла по маске
     * @param name Имя файла
     * @return true или false
     */
    public boolean isInWAndNotBLists(String name) {
        boolean result = true;
        if (!configuration.getInclideExtList().isEmpty()) {
            result = configuration.getInclideExtList().stream().anyMatch(i -> name.matches(String.format(FILE_EXT_MASK, i)));
        }
        if (!configuration.getExclideExtList().isEmpty()) {
            result &= configuration.getExclideExtList().stream().noneMatch(e -> name.matches(String.format(FILE_EXT_MASK, e)));
        }
        return result;
    }

    /**
     * Функция для поиска внутри файла имени, размера, количества строк/непустых строк/строк с комментариями, информация кладётся в {@link FilesInfo}
     * @param file - файл внутри директории
     * @param filesInfo
     */
    protected void loadFile(File file, FilesInfo filesInfo)
    {
        final FileInfo fileInfo = new FileInfo();
        fileInfo.setName(file.getName());
        fileInfo.setSize((int) file.length());

        try (FileReader fileReader = new FileReader(file);
             BufferedReader br = new BufferedReader(fileReader))
        {
            String line;
            do{
                line = br.readLine();
                fileInfo.incrementCountString();
                if (line != null && !line.isEmpty()) {
                    //Строка уже не пустая
                    fileInfo.incrementCountNotEmptyStrings();
                    //Проверка на соответствие маске комментария в начале строки
                    if (line.matches(COMMENT_MASK)) {
                        fileInfo.incrementCountCommentStrings();
                    }
                }
            }
            while (line != null);
            filesInfo.addFileInfo(fileInfo);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Класс для создания очереди и выполнения в ExecutorService
     */
    protected class Call implements Callable<Void> {
        final FilesInfo filesInfo;
        final File file;

        public Call(File file, FilesInfo filesInfo) {
            this.filesInfo = filesInfo;
            this.file = file;
        }

        @Override
        public Void call()  {
            loadFile(file, filesInfo);
            //System.out.println("Thread " + Thread.currentThread().getName() + " " + filesInfo.getFileStatByName(file.getName()));
            return null;
        }
    }

    public void outputToFile() {
        if (!configuration.getOutputExtList().isEmpty()) {
            for (String ext : configuration.getOutputExtList()) {
                switch (ext) {
                    case Configuration.JSON:
                        saveToFile(filesInfo.getJsonStr(), ext);
                        break;
                    case Configuration.PLAIN:
                        saveToFile(filesInfo.getPlainStr(), ext);
                        break;
                    case Configuration.XML:
                        saveToFile(filesInfo.getXmlStr(), ext);
                        break;
                    default:
                        System.out.println("Unknown ext");
                }
            }
        }
    }

    private void saveToFile(String str, String ext) {
        FileOutputStream fop = null;
        final File file;
        try {
//            String pathToJar = null;
//            try {
//                pathToJar = AbstractFileAnalyzer.class
//                        .getProtectionDomain()
//                        .getCodeSource()
//                        .getLocation()
//                        .toURI()
//                        .getPath();
//               // String path = pathToJar.split("^(.*)(\\/.+\\.jar)$")[0];
//                System.out.println(pathToJar);
//
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
            file = new File(configuration.getPath() + "stats." + ext);
            if (!file.exists()) {
                file.createNewFile();
            }
            fop = new FileOutputStream(file);

            final byte[] contentInBytes = str.getBytes(StandardCharsets.UTF_8);
            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
