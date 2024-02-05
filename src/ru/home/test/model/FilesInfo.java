package ru.home.test.model;

import ru.home.test.utils.Converter;
import ru.home.test.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FilesInfo {

    private static final String COUNT_FILES = "Количество файлов";
    private static final String FILE_SIZE = "Размер в байтах";
    private static final String COUNT_ALL_STR = "Количество строк всего";
    private static final String COUNT_NOT_EMPTY_STR = "Количество не пустых строк";
    private static final String COUNT_COMMENT_STR = "Количество строк с комментариями";


    private final List<FileInfo> fileInfos = new ArrayList<>();

    public synchronized void addFileInfo(FileInfo fileInfo) {
        fileInfos.add(fileInfo);
    }

    public synchronized List<FileInfo> getFileInfos() {
        return fileInfos;
    }

    public String getFileStatByName(String name) {
        final Optional<FileInfo> optionalFileInfo = fileInfos.stream().filter(f -> f.getName().equals(name)).findAny();
        final StringBuilder sb = new StringBuilder();
        if (optionalFileInfo.isPresent()) {
            final FileInfo f = optionalFileInfo.get();
            sb.append("Name: ").append(f.getName()).append(";")
                    .append(" size: ").append(f.getSize()).append(";")
                    .append(" all str: ").append(f.getCountStrings()).append(";")
                    .append(" not empty str: ").append(f.getCountNotEmptyStrings()).append(";")
                    .append(" comment str: ").append(f.getCountCommentStrings());
        } else {
            sb.append("file " + name + " is absend");
        }
        return sb.toString();
    }

    public String getFileStats() {
        return this.fileInfos.stream().map(f -> "Name: " + f.getName() +
                " size: " + f.getSize() + " all str: " + f.getCountStrings() + " not empty str: "
                + f.getCountNotEmptyStrings() + " comment str: " +f.getCountCommentStrings()).collect(Collectors.joining("\r\n"));

    }

    public String getAllStats() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Count files: ").append(getCountFiles()).append(";")
                .append(" size: ").append(getAllSize()).append(";")
                .append(" strings: ").append(getAllCountStr()).append(";")
                .append(" not empty str: ").append(getAllNotEmptyStr()).append(";")
                .append(" comment str: ").append(getAllCommentStr());
        return sb.toString();
    }

    public int getCountFiles() {
        return fileInfos.size();
    }

    public int getAllSize() {
        return fileInfos.stream().mapToInt(f -> f.getSize()).sum();
    }

    public int getAllCountStr() {
        return fileInfos.stream().mapToInt(f -> f.getCountStrings()).sum();
    }

    public int getAllNotEmptyStr() {
        return fileInfos.stream().mapToInt(f -> f.getCountNotEmptyStrings()).sum();
    }

    public int getAllCommentStr() {
        return fileInfos.stream().mapToInt(f -> f.getCountCommentStrings()).sum();
    }

    private List<Pair<String, String >> getAllData() {
        final List<Pair<String, String>> result = new ArrayList<>();
        result.add(new Pair<>(COUNT_FILES, String.valueOf(getCountFiles())));
        result.add(new Pair<>(FILE_SIZE, String.valueOf(getAllSize())));
        result.add(new Pair<>(COUNT_ALL_STR, String.valueOf(getAllCountStr())));
        result.add(new Pair<>(COUNT_NOT_EMPTY_STR, String.valueOf(getAllNotEmptyStr())));
        result.add(new Pair<>(COUNT_COMMENT_STR, String.valueOf(getAllNotEmptyStr())));
        return result;
    }

    public String getJsonStr() {
        return Converter.convertToJsonStr(getAllData());
    }

    public String getPlainStr() {
        return Converter.convertToPlainStr(getAllData());
    }

    public String getXmlStr() {
        return Converter.convertToXmlStr(getAllData());
    }

}
