package export;

import org.apache.commons.io.FilenameUtils;
import parser.JavaMaskParser;
import storage.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class HtmlExport
{
    public static final String BEGIN = """
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                <link rel="stylesheet" href="https://bootswatch.com/5/flatly/bootstrap.css" crossorigin="anonymous">
                <link rel="stylesheet" href="styles.css">
               \s
                <style type="text/css" media="all">
                  pre\s
                  {
                    margin: 0;
                    white-space: pre-wrap;       /* Since CSS 2.1 */
                    white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */
                    white-space: -pre-wrap;      /* Opera 4-6 */
                    white-space: -o-pre-wrap;    /* Opera 7 */
                    word-wrap: break-word;       /* Internet Explorer 5.5+ */
                  }
                  
                  .main
                  {
                    margin:20px;
                  }
                </style>
               \s
                <title>Test Report</title>
              </head>
              <body>
                <!-- Navigation bar -->
                <nav class="navbar navbar-expand-lg bg-primary" data-bs-theme="dark">
                  <div class="container">
                    <a class="navbar-brand" href="#">
                      <img src="https://freesvg.org/img/accessories-text-editor.png" width="28"
                       alt="Logo"\s
                       class="d-inline-block align-text-top" />\s
                       Test Report
                    </a>
                  </div>
                </nav>
               \s
                <!-- Tabs -->
                <ul class="nav nav-tabs bg-body-tertiary" role="tablist">
                  <li class="nav-item" role="presentation">
                    <a class="nav-link active" data-bs-toggle="tab" href="#home" aria-selected="true" role="tab">Strategy 1</a>
                  </li>
                </ul>
                <div id="myTabContent" class="tab-content">
                  <div class="tab-pane fade active show" id="home" role="tabpanel">
                    <div class="main">
            """;

    public static final String END = """
                        </div>
                    </div>
                </div>
               \s
                <!-- Navigation bar -->
              </body>
            </html>
            """;

    private Path pathToExport;

    public void setPathToExport(Path pathToExport) {
        this.pathToExport = pathToExport;
    }

    public Path getPathToExport() {
        return pathToExport;
    }

    public void export(ArrayList<FileInfo> files)
    {
        // 1. Export every file infos
        for (FileInfo fileInfo: files)
        {
            String toExport = BEGIN;
            String name = fileInfo.fileName; // A.java

            toExport += """
                    <br />
                    <h1 class="text-center">"""
                    + name
                    + """
                    </h1>
                        \s
                        <br />
                        <h2 class="">Code</h2>

                        <table class="table table-sm table-responsive table-hover">
                            <thead>
                            </thead>
                            \s
                            <tbody>
                    """;
            LinkedHashMap<Integer, ArrayList<String>> mutationsInfo = new LinkedHashMap<>();
            // Table containing the codeInLines
            try
            {
                String code = Files.readString(Path.of(fileInfo.pathName));
                String[] codeInLines = code.split("\\r?\\n", -1);

                // Get all the wrong lines and good lines

                // <line number, is line killed>
                HashMap<Integer, Boolean> isLineKilled = new HashMap<>();

                // Browse every prediction
                for(AbstractInfo ab: fileInfo.getSpecificChildren(AbstractInfo.Info.PREDICTION_INFO))
                {
                    PredictionInfo pr = (PredictionInfo) ab;

                    // Get the lines
                    for(int i = ((MutationInfo)pr.parent).position.beginLine; i <= ((MutationInfo)pr.parent).position.endLine; i++)
                    {
                        // The mutation has been executed
                        if(pr.metrics.get("killed") != null
                                || pr.metrics.get("survived") != null) {
                            // Line already counted
                            if (isLineKilled.get(i) != null) {
                                if (pr.metrics.get("killed").equals("false"))
                                    isLineKilled.put(i, false);
                            } else {
                                if (pr.metrics.get("killed").equals("true"))
                                    isLineKilled.put(i, true);
                                else isLineKilled.put(i, false);
                            }

                            // If the line is not registered in the map yet
                            if(mutationsInfo.get(i) == null) {
                                mutationsInfo.put(i, new ArrayList<>());
                            }

                            mutationsInfo.get(i).add("Replaced <b>“" + code.substring(((MutationInfo)pr.parent).position.beginIndex, ((MutationInfo)pr.parent).position.endIndex+1) + "”</b> with <b>“" + pr.tokenPredicted + "”</b> --> "
                                    + (pr.metrics.get("killed").equals("true") ? "KILLED":"SURVIVED"));

                        }
                    }

                }

                System.out.println(mutationsInfo);

                int lineNumber = 1;
                for(String line: codeInLines)
                {
                    String htmlClass = "";
                    if(isLineKilled.get(lineNumber) != null) {
                        if (isLineKilled.get(lineNumber).equals(true))
                            htmlClass = "table-success";
                        else htmlClass = "table-danger";
                    }

                    toExport += """
                            <tr class=\""""
                            + htmlClass + """
                              \">
                              <td>"""
                            + lineNumber + """
                              </td>
                              <td></td>
                              <td><pre>"""
                            + line + """
                            </pre></td>
                            </tr>
                            """;
                    lineNumber++;
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            toExport += """
                        </tbody>
                    </table>
                   \s
                    <br />
                    <h2 class="">Mutations</h2>
                    <table class="table table-sm table-responsive table-hover">
                    <thead>
                    </thead>
                    \s
                    <tbody>
                    """;

            for(Map.Entry<Integer, ArrayList<String>> entry: mutationsInfo.entrySet())
            {
                for(String info: entry.getValue()) {
                    String htmlClass = "";
                    if(info.contains("KILLED")) htmlClass = "table-success";
                    else if(info.contains("SURVIVED")) htmlClass = "table-danger";
                    toExport += """
                            <tr class=\""""
                            + htmlClass + """
                            \">
                            <td>"""
                            + entry.getKey() + """
                            </td>
                            <td></td>
                            <td><pre>"""
                            + info + """
                            </pre></td>
                            </tr>
                            """;
                }
            }

            toExport += "</tbody>\n" +
                    "                    </table>\n" + END;

            try
            {
                //Creation of folder
                Files.createDirectories(Path.of(this.pathToExport.toAbsolutePath().toString(), "files"));

                File newFile = Path.of(this.pathToExport.toAbsolutePath().toString(), "files", FilenameUtils.removeExtension(name)+".html").toFile();
                String newFileName = newFile.getAbsolutePath();
                FileWriter newFile1 = new FileWriter(newFileName);
                newFile1.write(toExport);
                newFile1.close();
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
