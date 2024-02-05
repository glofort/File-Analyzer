package ru.home.test.model;

/**
 * Содержание файла
 */
public class FileInfo {
    //Имя файла
    private String name;
    //Размер в байтах
    private int size;
    //Количество строк
    private int countStrings;
    //Количество строк с хотя бы одним печатным символом
    private int countNotEmptyStrings;
    //Количество строк с однострочным комментарием
    private int countCommentStrings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCountStrings() {
        return countStrings;
    }

    public int getCountNotEmptyStrings() {
        return countNotEmptyStrings;
    }

    public int getCountCommentStrings() {
        return countCommentStrings;
    }

    public synchronized void incrementCountString() {
        this.countStrings += 1;
    }

    public synchronized void incrementCountNotEmptyStrings() {
        this.countNotEmptyStrings += 1;
    }

    public synchronized void incrementCountCommentStrings() {
        this.countCommentStrings += 1;
    }
}
