package testing;

import core.Language;
import core.Mutator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.junit.runner.Result;
import storage.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaTesting extends AbstractTesting
{
    public JavaTesting()
    {
        this.language = Language.JAVA;
    }

    public static void main(String[] args) {
        //DiscoverySelector selector = DiscoverySelectors.selectPackage("tests");
    }

    @Override
    public void test(FileInfo file)
    {
        try
        {
            // 1. Only get the test classes where all tests passed

            // List of all java test classes
            List<Path> candidates = Files.walk(Mutator.getInstance().getTestsPath())
                .filter(p -> p.toString().endsWith(".java"))
                .toList();

            Process proc = new ProcessBuilder("mvn",
                    "-f", "\""+Mutator.getInstance().getProjectPath()+"\"",
                    "test")
                    .start();
            proc.waitFor();

            Path pathToResults = Path.of(Mutator.getInstance().getProjectPath().toString(), "target", "surefire-reports");

            // List of all xml reports
            List<Path> resultsXml = Files.walk(pathToResults)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .toList();

            // The list of all successful test classes
            List<Path> goodTestClasses = new ArrayList<>();

            for (Path candidate: candidates)
            {
                System.out.println("Candidate: " + candidate);
                for(Path xmlReport: resultsXml)
                {
                    System.out.println("    XmlReport: " + xmlReport);
                    if(xmlReport.toString().contains(FilenameUtils.removeExtension(candidate.toFile().getName())))
                    {
                        // Parse the xml file to see if any errors
                        try
                        {
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            Document document = builder.parse(xmlReport.toFile());

                            NodeList nodeList = document.getElementsByTagName("failure");
                            System.out.println(nodeList.getLength());
                            if(nodeList.getLength() == 0) // No failure, we can add it
                            {
                                goodTestClasses.add(candidate);
                                //break;
                            }
                        }
                        catch (Exception e) {}
                    }
                }
            }

            System.out.println(goodTestClasses);

            // 2. For every test class, test with the mutated file
            File folder = new File("/Users/schumi/eclipse-workspace/Mutation Tool/output/Caesar-java/0/compiled-classes");
            if (!folder.exists() || !folder.isDirectory()) {
                throw new IllegalArgumentException("Path is not a directory!");
            }
            URL[] urls = {folder.toURI().toURL()};
            try (URLClassLoader classLoader = new URLClassLoader(urls)) {
                // Scan for .class files
                List<File> classFiles = Files.walk(folder.toPath())
                        .filter(f -> f.toString().endsWith(".class"))
                        .map(Path::toFile)
                        .toList();

                List<Class<?>> testClasses = new ArrayList<>();
                // Iterate over class files
                for (File classFile : classFiles) {
                    String className = getClassNameFromFile(folder, classFile);
                    Class<?> clazz = classLoader.loadClass(className);
                    for(Path potentialGoodTestClass: goodTestClasses)
                    {
                        String onlyName = FilenameUtils.removeExtension(potentialGoodTestClass.toFile().getName());
                        if(className.contains(onlyName))
                        {
                            testClasses.add(clazz);
                            break;
                        }
                    }
                }
                Class<?>[] testClassesArray = new Class<?>[testClasses.size()];
                for(int i = 0; i < testClassesArray.length; i++)
                    testClassesArray[i] = testClasses.get(i);

                Result result = JUnitCore.runClasses(testClassesArray);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void test(ArrayList<FileInfo> fileInfos)
    {
        try
        {
            // 1. Only get the test classes where all tests passed

            // List of all java test classes
            List<Path> candidates = Files.walk(Mutator.getInstance().getTestsPath())
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();

            Process proc = new ProcessBuilder("mvn",
                    "-f", "\"" + Mutator.getInstance().getProjectPath() + "\"",
                    "test")
                    .start();
            proc.waitFor();

            Path pathToResults = Path.of(Mutator.getInstance().getProjectPath().toString(), "target", "surefire-reports");

            // List of all xml reports
            List<Path> resultsXml = Files.walk(pathToResults)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .toList();

            // The list of all successful test classes
            List<Path> goodTestClasses = new ArrayList<>();

            for (Path candidate : candidates) {
                System.out.println("Candidate: " + candidate);
                for (Path xmlReport : resultsXml) {
                    System.out.println("    XmlReport: " + xmlReport);
                    if (xmlReport.toString().contains(FilenameUtils.removeExtension(candidate.toFile().getName()))) {
                        // Parse the xml file to see if any errors
                        try {
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder = factory.newDocumentBuilder();
                            Document document = builder.parse(xmlReport.toFile());

                            NodeList nodeList = document.getElementsByTagName("failure");
                            System.out.println(nodeList.getLength());
                            if (nodeList.getLength() == 0) // No failure, we can add it
                            {
                                goodTestClasses.add(candidate);
                                //break;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }

            for (FileInfo fileInfo : fileInfos) {
                for (ClassInfo cl : fileInfo.classes) {
                    for (MethodInfo me : cl.methods) {
                        for (StatementInfo st : me.statements) {
                            for (MaskingInfo ma : st.maskingInfos) {
                                for (PredictionInfo pr : ma.predictions) {
                                    try {
                                        // 2. For every test class, test with the mutated file
                                        File folder = Path.of(pr.pathToOutput, "compiled-classes").toAbsolutePath().toFile();
                                        if (!folder.exists() || !folder.isDirectory()) {
                                            throw new IllegalArgumentException("Path is not a directory!");
                                        }
                                        URL[] urls = {folder.toURI().toURL()};
                                        try (URLClassLoader classLoader = new URLClassLoader(urls)) {
                                            // Scan for .class files
                                            List<File> classFiles = Files.walk(folder.toPath())
                                                    .filter(f -> f.toString().endsWith(".class"))
                                                    .map(Path::toFile)
                                                    .toList();

                                            List<Class<?>> testClasses = new ArrayList<>();
                                            // Iterate over class files
                                            for (File classFile : classFiles) {
                                                String className = getClassNameFromFile(folder, classFile);
                                                Class<?> clazz = classLoader.loadClass(className);
                                                for(Path potentialGoodTestClass: goodTestClasses)
                                                {
                                                    String onlyName = FilenameUtils.removeExtension(potentialGoodTestClass.toFile().getName());
                                                    if(className.contains(onlyName))
                                                    {
                                                        testClasses.add(clazz);
                                                        break;
                                                    }
                                                }
                                            }
                                            Class<?>[] testClassesArray = new Class<?>[testClasses.size()];
                                            for(int i = 0; i < testClassesArray.length; i++)
                                                testClassesArray[i] = testClasses.get(i);

                                            Result result = JUnitCore.runClasses(testClassesArray);
                                            if(result.wasSuccessful())
                                            {
                                                pr.metrics.put("killed", "true");
                                                pr.metrics.put("survived", "false");

                                                System.out.println(folder + " worked :)");
                                            }
                                            else
                                            {
                                                pr.metrics.put("killed", "false");
                                                pr.metrics.put("survived", "true");
                                                // Put the list of every failed tests

                                                System.out.println(folder + " failed :(");
                                                for (Failure failure : result.getFailures()) {
                                                    System.out.println("\t\tProblem in "
                                                            + failure.getDescription().getClassName()
                                                            + " (" +failure.getTestHeader()+ ") "
                                                            + ": " + failure.getMessage());
                                                }
                                            }
                                        }

                                    } catch (Exception e) {}
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {

        }
    }

    private static String getClassNameFromFile(File folder, File classFile) {
        String relativePath = folder.toURI().relativize(classFile.toURI()).getPath();
        return relativePath.replace(File.separatorChar, '.').replace(".class", "");
    }
}
