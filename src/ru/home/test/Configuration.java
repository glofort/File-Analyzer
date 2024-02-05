package ru.home.test;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParsePosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 1. <path> - путь до каталога по которому надо выполнить сбор статистики
 * 2. --recursive - выполнять обход дерева рекурсивно
 * 3. --max-depth=<number> - глубина рекурсивного обхода
 * 4. --thread=<number> - количество потоков используемого для обхода
 * 5. --include-ext=<ext1,ext2,ext3,..> - обрабатывать файлы только с указанными расширениями
 * 6. --exclude-ext=<ext1,ext2,ext3,..> - не обрабатывать файлы с указанными расширениями
 * 7. --git-ignore - не обрабатывать файлы указанные в файле .gitignore
 * 8. --output=<plain,xml,json> - формат вывода статистики
 */
public class Configuration {

    public static final String JSON = "json";
    public static final String XML = "xml";
    public static final String PLAIN = "plain";

    private static final String RECURSIVE = "recursive";
    private static final String MAX_DEPTH = "max-depth";
    private static final String THREAD = "thread";
    private static final String INCLUDE_EXT = "include-ext";
    private static final String EXCLUDE_EXT = "exclude-ext";
//    private static final String GIT_IGNORE = "git-ignore";
    private static final String OUTPUT = "output";

    private static final String CMD_DELIMITER = "--";
    private static final String DELIMITER = ",";

    private static final String helpText = "Available commands: <path> --recursive --max-depth=<number> --thread=<number> " +
            "--include-ext=<ext1,ext2,ext3,..> --exclude-ext=<ext1,ext2,ext3,..> --git-ignore --output=<plain,xml,json>" +
            "\n1. \"<path>\" - путь до каталога по которому надо выполнить сбор статистики\n" +
            "2. --recursive - выполнять обход дерева рекурсивно\n" +
            "3. --max-depth=<number> - глубина рекурсивного обхода\n" +
            "4. --thread=<number> - количество потоков используемого для обхода\n" +
            "5. --include-ext=<ext1,ext2,ext3,..> - обрабатывать файлы только с указанными расширениями\n" +
            "6. --exclude-ext=<ext1,ext2,ext3,..> - не обрабатывать файлы с указанными расширениями\n" +
/*            "7. --git-ignore - не обрабатывать файлы указанные в файле .gitignore\n" +*/
            "8. --output=<plain,xml,json> - формат вывода статистики\n";

    private static final List<String> availableCommandList = Collections.unmodifiableList(Arrays.asList(RECURSIVE, MAX_DEPTH, THREAD, INCLUDE_EXT, EXCLUDE_EXT/*, GIT_IGNORE*/, OUTPUT));
    private static final List<String> availableCommandWithArgList = Collections.unmodifiableList(Arrays.asList(MAX_DEPTH, THREAD, INCLUDE_EXT, EXCLUDE_EXT, OUTPUT));
    private static final List<String> availableCommandWithDelimArgList = Collections.unmodifiableList(Arrays.asList(INCLUDE_EXT, EXCLUDE_EXT, OUTPUT));

    private static final List<String> availableExtOutput = Collections.unmodifiableList(Arrays.asList(JSON, XML, PLAIN));


    private String path;
    private boolean isRecursive = false;
    private int maxDepth = 0;
    private boolean isMaxDepthInit = false;
    private int threads = 1;
    private List<String> inclideExtList = new ArrayList<>();
    private List<String> exclideExtList = new ArrayList<>();
/*    private boolean isGitIgnore;*/
    private List<String> outputExtList = new ArrayList<>();

    private Validator validator = new Validator();

    public Configuration(String[] args) {
        final List<String> argList = Arrays.asList(args.clone());

        //Мап с командами, где ключ - команда, значение - значение после "="
        final Map<String, String> commands = new HashMap<>();

        for (String arg : argList) {
            if (arg.contains(CMD_DELIMITER)) {
                //Делим аргументы с "--" на часть с командой и часть со значением
                final String[] splittedArg = arg.split("=");
                //Удалим "--" из команды
                final String command = splittedArg[0].replace(CMD_DELIMITER, "");
                if (splittedArg.length > 1) {
                    //Для команд со значением
                    commands.put(command, splittedArg[1]);
                } else {
                    //Для команд без значений
                    commands.put(command, null);
                }
            }
        }
        validatePath(argList);
        validateArgs(commands);
        initArgs(commands);
    }

    private class Validator {

        private boolean pathIsNotPresent;
        private String pathIsNotValid;
        private String invalidCommands;
        private String commandHasNotContainValue;
        private String incorrectOutputExt;
        private String incorrectMaxDepth;
        private String incorrectThread;

        public String summary() {
            final StringBuilder sb = new StringBuilder();

            if (pathIsNotPresent) {
                sb.append("Path is not present.").append("\r\n");
            }
            if (pathIsNotValid != null && !pathIsNotValid.isEmpty()) {
                sb.append("Path is not valid:").append(pathIsNotValid).append("\r\n");
            }
            if (invalidCommands != null && !invalidCommands.isEmpty()) {
                sb.append("Invalid commands: ").append(invalidCommands).append("\r\n");
            }
            if (commandHasNotContainValue != null && !commandHasNotContainValue.isEmpty()) {
                sb.append("Command ").append(commandHasNotContainValue).append(" must contains value.").append("\r\n");
            }
            if (incorrectOutputExt != null && !incorrectOutputExt.isEmpty()) {
                sb.append("Incorrect output ext: ").append(incorrectOutputExt).append("\r\n");
            }
            if (incorrectMaxDepth != null && !incorrectMaxDepth.isEmpty()) {
                sb.append("Incorrect max depth: ").append(incorrectMaxDepth).append("\r\n");
            }
            if (incorrectThread != null && !incorrectThread.isEmpty()) {
                sb.append("Incorrect thread: ").append(incorrectThread).append("\r\n");
            }

            if (!sb.toString().isEmpty()) {
                sb.append(helpText);
            }

            return sb.toString();
        }

        public void setPathIsNotPresent(boolean pathIsNotPresent) {
            this.pathIsNotPresent = pathIsNotPresent;
        }


        public void setPathIsNotValid(String pathIsNotValid) {
            this.pathIsNotValid = pathIsNotValid;
        }

        public void setInvalidCommands(String invalidCommands) {
            this.invalidCommands = invalidCommands;
        }

        public void setCommandHasNotContainValue(String commandHasNotContainValue) {
            this.commandHasNotContainValue = commandHasNotContainValue;
        }

        public void setIncorrectOutputExt(String incorrectOutputExt) {
            this.incorrectOutputExt = incorrectOutputExt;
        }

        public void setIncorrectMaxDepth(String incorrectMaxDepth) {
            this.incorrectMaxDepth = incorrectMaxDepth;
        }

        public void setIncorrectThread(String incorrectThread) {
            this.incorrectThread = incorrectThread;
        }
    }


    private void validatePath(List<String> argList) {
        String tempPath = argList.isEmpty() ? null : argList.get(0);
        if (tempPath == null || tempPath.isEmpty() || tempPath.contains("--")) {
            //Проверка на наличие пути
            validator.setPathIsNotPresent(true);
        } else {
            //Проверка на существование директории
            final File f = new File(tempPath);
            if(!f.exists() || !f.isDirectory()) {
                validator.setPathIsNotValid(tempPath);
            } else {
                this.path = tempPath;
            }
        }
    }

    private void validateArgs(Map<String, String> commands) {
        //Проверка на корректность доступных комманд
        if (!availableCommandList.containsAll(commands.keySet())) {
            validator.setInvalidCommands(commands.keySet().stream().filter(c -> !availableCommandList.contains(c)).collect(Collectors.joining(", ")));
        }
        //Проверка на наличие аргументов для команд, которые должны содержать их
        final List<String> hasCommandsWithoutArg = commands.keySet().stream().filter(c -> availableCommandWithArgList.contains(c) && commands.get(c) == null).collect(Collectors.toList());
        if (!hasCommandsWithoutArg.isEmpty()) {
            validator.setCommandHasNotContainValue(hasCommandsWithoutArg.stream().collect(Collectors.joining(";")));
        }
    }

    public void initArgs(Map<String, String> commands) {
        final List<String> allCommands = new ArrayList<>(commands.keySet());
        for (String command : allCommands) {
            switch (command) {
                case RECURSIVE:
                    this.isRecursive = true;
                    break;
                case MAX_DEPTH:
                    if (!isNumeric(commands.get(command))) {
                        validator.setIncorrectMaxDepth(commands.get(command));
                    } else {
                        this.isMaxDepthInit = true;
                        this.maxDepth = Integer.valueOf(commands.get(command)) < 0 ? 0 : Integer.valueOf(commands.get(command));
                    }
                    break;
                case THREAD:
                    if (!isNumeric(commands.get(command))) {
                        validator.setIncorrectThread(commands.get(command));
                    } else {
                        this.threads = Integer.valueOf(commands.get(command)) < 1 ? 1 : Integer.valueOf(commands.get(command));
                    }
                    break;
                case INCLUDE_EXT:
                    final List<String> splitIncludeExt = Arrays.asList(commands.get(command).split(DELIMITER).clone());
                    this.inclideExtList.addAll(splitIncludeExt);
                    break;
                case EXCLUDE_EXT:
                    final List<String> splitExcludeExt = Arrays.asList(commands.get(command).split(DELIMITER).clone());
                    this.exclideExtList.addAll(splitExcludeExt);
                    break;
                case OUTPUT:
                    //Проверка на корректность форматов
                    final List<String> splitOutputExt = Arrays.asList(commands.get(command).split(DELIMITER).clone());
                    final List<String> checkExtOutput = new ArrayList<>();
                    checkExtOutput.addAll(splitOutputExt);
                    checkExtOutput.removeAll(availableExtOutput);
                    if (!checkExtOutput.isEmpty()) {
                        validator.setIncorrectOutputExt(checkExtOutput.stream().collect(Collectors.joining(";")));
                    } else {
                        this.outputExtList.addAll(splitOutputExt);
                    }
                    break;
                default:
            }
        }
    }

    public static boolean isNumeric(String str) {
        ParsePosition pos = new ParsePosition(0);
        NumberFormat.getInstance().parse(str, pos);
        return str.length() == pos.getIndex();
    }

    public String getPath() {
        return path;
    }

    public boolean isRecursive() {
        return isRecursive;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getThreads() {
        return threads;
    }

    public List<String> getInclideExtList() {
        return inclideExtList;
    }

    public List<String> getExclideExtList() {
        return exclideExtList;
    }

//    public boolean isGitIgnore() {
//        return isGitIgnore;
//    }

    public List<String> getOutputExtList() {
        return outputExtList;
    }

    public String validateText() {
        return this.validator.summary();
    }

    public boolean isMaxDepthInit() {
        return isMaxDepthInit;
    }

}
