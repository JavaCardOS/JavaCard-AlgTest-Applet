/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algtestprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author rk
 */
public class JCinfohtml {

    public static final String TABLE_HEAD = "<table cellspacing='0'> <!-- cellspacing='0' is important, must stay -->\n\t<tr><th style=\"width: 330px;\">Name of function</th><th><b>Operation average (ms/op)</b></th><th>Operation minimum (ms/op)</th><th>Operation maximum (ms/op)</th><th>Data length (bytes)</th><th>Prepare average (ms/op)</th><th>Prepare minimum (ms/op)</th><th>Prepare maximum (ms/op)</th><th>Iterations & Invocations</th></tr><!-- Table Header -->\n";
    public static final List<String> category = Arrays.asList("MESSAGE DIGEST", "RANDOM GENERATOR", "CIPHER", "SIGNATURE", "CHECKSUM", "AESKey", "DESKey", "KoreanSEEDKey", "DSAPrivateKey", "DSAPublicKey", "ECF2MPublicKey", "ECF2MPrivateKey", "ECFPPublicKey", "HMACKey", "RSAPrivateKey", "RSAPublicKey", "RSAPrivateCRTKey", "KEY PAIR", "UTIL", "SWALGS");
    public static final String topFunctionsFile = "top.txt";

    public static List<String> initalize(String input, StringBuilder cardName) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
            if (lines.get(lines.size() - 1).startsWith("#")) {
                lines.remove(lines.size() - 1);
            }
            if (lines.get(lines.size() - 1).equals("")) {
                lines.remove(lines.size() - 1);
            }
            if (lines.get(lines.size() - 1).contains("javax.smartcardio.CardException")) {
                lines.remove(lines.size() - 1);
            }

            String cardNameKey = "Card name;";
            if (line.contains(cardNameKey)) {
                String[] info = line.split(";");
                if ((info.length > 1) && (cardName != null)) {
                    cardName.setLength(0);
                    cardName.append(info[1].trim());
                }
            }
        }
        reader.close();
        return lines;
    }

    public static Integer parse(List<String> lines, FileOutputStream file, Integer lp) throws IOException {
        int tp = 0;

        while (lp < lines.size()) {
            for (String cat : category) {
                if ((cat.equals(lines.get(lp))) || (lines.get(lp).contains("END"))) {
                    lp--;
                    return lp;
                }
            }

            lp = parseOne(lines, file, lp, tp);
            tp++;
        }
        return lp;
    }

    public static HashMap<String, String> detailsBasic(List<String> lines, FileOutputStream file) throws IOException {
        String toFile;
        String[] info;

        // Transform lines into hashmap
        HashMap<String, String> infoMap = new HashMap<>();
        for (int i = 0; i < 150; i++) {
            info = lines.get(i).split(";");
            if (info.length > 1) {
                infoMap.put(info[0], info[1]);
            }
        }

        toFile = "";
        toFile += "<div class=\"pageColumnDetails\">\n";
        toFile += "<h3>Test details</h3>\n";
        toFile += "<p>Execution date/time: <strong>" + infoMap.get("Execution date/time") + "</strong></p>\n";
        toFile += "<p>AlgTestJClient version: <strong>" + infoMap.get("AlgTestJClient version") + "</strong></p>\n";
        toFile += "<p>AlgTest applet version: <strong>" + infoMap.get("AlgTest applet version") + "</strong></p>\n";
        toFile += "<p>Used reader: <strong>" + infoMap.get("Used reader") + "</strong></p>\n";
        toFile += "<p><strong>Card ATR: " + infoMap.get("Card ATR") + "</strong></p>\n";
        toFile += "<p><u><a href=\"https://smartcard-atr.appspot.com/parse?ATR=" + infoMap.get("Card ATR").replaceAll(" ", "") + "\" target=\"_blank\">Smart card ATR parsing link</a></u></p>\n</br>\n";

        toFile += "<p>JavaCard version: <strong>" + infoMap.get("JCSystem.getVersion()[Major.Minor]") + "</strong></p>\n";
        toFile += "<p>MEMORY_TYPE_PERSISTENT: <strong>" + infoMap.get("JCSystem.MEMORY_TYPE_PERSISTENT") + "</strong></p>\n";
        toFile += "<p>MEMORY_TYPE_TRANSIENT_RESET: <strong>" + infoMap.get("JCSystem.MEMORY_TYPE_TRANSIENT_RESET") + "</strong></p>\n";
        toFile += "<p>MEMORY_TYPE_TRANSIENT_DESELECT: <strong>" + infoMap.get("JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT") + "</strong></p>\n";
        toFile += "\n<h3>How it works</h3>\n";        
        toFile += "<p><strong>If you will spot any discrepancies in the presented data </strong></br>(e.g. strange error, too fast or too slow operation time etc.), "
                + "</br>please open an issue (click at New issue) at <u><a target=\"_blank\" href=\"https://github.com/crocs-muni/JCAlgTest/issues\">GitHub</a></u>. <strong>Thank you!</strong></p>\n";
        toFile += "<p>You can find information about testing on <u><a target=\"_blank\" href=\"https://github.com/crocs-muni/JCAlgTest/wiki\">GitHub wiki</a></u>.</p>\n";
        file.write(toFile.getBytes());

        return infoMap;
    }

    public static void details(List<String> lines, FileOutputStream file) throws IOException {
        HashMap<String, String> infoMap = detailsBasic(lines, file);
        String toFile = "";        
        toFile += "<p>The <b>Operation avg/min/max</b> is the exact number<br> of how many milliseconds the function takes.</p>\n\n";
        toFile += "<p><b>ILLEGAL_VALUE</b>, <b>NO_SUCH_ALGORITHM</b><br> or <b>UNKONWN_ERROR</b> means that the function is <u>not supported.</u></p>\n\n";
        toFile += "</br></div>\n";

        if (infoMap.containsKey("CPLC")) {
            toFile += "<div class=\"pageColumnCPLC\">\n";
            toFile += "<h3>CPLC info</h3>\n";
            toFile += "<p>IC Fabricator: <strong>" + infoMap.get("CPLC.ICFabricator") + "</strong></p>\n";
            toFile += "<p>IC Type: <strong>" + infoMap.get("CPLC.ICType") + "</strong></p>\n";
            toFile += "<p>OS ID: <strong>" + infoMap.get("CPLC.OperatingSystemID") + "</strong></p>\n";
            toFile += "<p>OS Release Date: <strong>" + infoMap.get("CPLC.OperatingSystemReleaseDate") + "</strong></p>\n";
            toFile += "<p>OS Release Level: <strong>" + infoMap.get("CPLC.OperatingSystemReleaseLevel") + "</strong></p>\n";
            toFile += "<p>IC Fabrication Date ((Y DDD) date in that year): <strong>" + infoMap.get("CPLC.ICFabricationDate") + "</strong></p>\n";
            toFile += "<p>IC Serial Number: <strong>" + infoMap.get("CPLC.ICSerialNumber") + "</strong></p>\n";
            toFile += "<p>IC Batch Identifier: <strong>" + infoMap.get("CPLC.ICBatchIdentifier") + "</strong></p>\n";
            toFile += "<p>IC Module Fabricator: <strong>" + infoMap.get("CPLC.ICModuleFabricator") + "</strong></p>\n";
            toFile += "<p>IC Module Packaging Date: <strong>" + infoMap.get("CPLC.ICModulePackagingDate") + "</strong></p>\n";
            toFile += "<p>IC Manufacturer: <strong>" + infoMap.get("CPLC.ICCManufacturer") + "</strong></p>\n";
            toFile += "<p>IC Embedding Date: <strong>" + infoMap.get("CPLC.ICEmbeddingDate") + "</strong></p>\n";
            toFile += "<p>IC Pre Personalizer: <strong>" + infoMap.get("CPLC.ICPrePersonalizer") + "</strong></p>\n";
            toFile += "<p>IC Pre Personalization Equipment Date: <strong>" + infoMap.get("CPLC.ICPrePersonalizationEquipmentDate") + "</strong></p>\n";
            toFile += "<p>IC Pre Personalization Equipment ID: <strong>" + infoMap.get("CPLC.ICPrePersonalizationEquipmentID") + "</strong></p>\n";
            toFile += "<p>IC Personalizer: <strong>" + infoMap.get("CPLC.ICPersonalizer") + "</strong></p>\n";
            toFile += "<p>IC Personalization Date: <strong>" + infoMap.get("CPLC.ICPersonalizationDate") + "</strong></p>\n";
            toFile += "<p>IC Personalization Equipment ID: <strong>" + infoMap.get("CPLC.ICPersonalizationEquipmentID") + "</strong></p>\n";
            toFile += "</div>\n";
        }

        toFile += "</div>\n";
        file.write(toFile.getBytes());
    }

    public static void quickLinks(List<String> lines, FileOutputStream file) throws IOException {
        String toFile;
        toFile = "<div class=\"pageColumnQuickLinks\">\n";
        toFile += "<h3>Quick links</h3>\n<ul style=\"list-style-type: circle;\">\n";
        if (0 != loadTopFunctions(new ArrayList<String>(), null)) {
            toFile += "\t<li>" + "<a href=\"#TOP\">TOP FUNCTIONS</a>" + "</li>\n";
        }

        List<String> usedCategories = new ArrayList<>();
        for (int i = 10; i < lines.size(); i++) {
            if ((category.contains(lines.get(i))) && !((lines.get(i + 1)).contains("END") || (lines.get(i + 2)).contains("END"))) {
                usedCategories.add(lines.get(i));
            }
        }

        for (String cat : usedCategories) {
            toFile += "\t<li>" + "<a href=\"#" + cat.replaceAll(" ", "_") + "\">" + cat + "</a>" + "</li>\n";
        }

        toFile += "</ul>\n</div>\n";
        file.write(toFile.getBytes());
    }
    
    public static void beginLite(FileOutputStream file, String headline) throws IOException {
        String toFile = "";
        toFile += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
        toFile += "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
        toFile += "<head>\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n";
        toFile += "<title>JCAlgTest Performance test</title>\n";
        toFile += "<link rel=\"stylesheet\" type=\"text/css\" href=\"./source/style.css\">\n";
        toFile += "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js\"></script>\n";
        toFile += "<script>\n" + "\tjQuery(document).ready(function(){\n\tvar offset = 220;var duration = 500;\n"
                + "\t\tjQuery(window).scroll(function(){\n\tif (jQuery(this).scrollTop()>offset){jQuery('.back-to-top').fadeIn(duration);\n"
                + "\t}else{jQuery('.back-to-top').fadeOut(duration);}});\n"
                + "\t\tjQuery('.back-to-top').click(function(event){event.preventDefault();\n"
                + "\t\tjQuery('html, body').animate({scrollTop: 0}, duration);\n"
                + "\treturn false;})});\n</script>\n";
        toFile += "</head>\n";
        toFile += "<body>\n</br>\n<div class=\"main\">\n";
        toFile += "<div style=\"margin:15px 20px 10px 20px;\">\n";
        toFile += "\t<a target=\"_blank\" href=\"https://github.com/crocs-muni/JCAlgTest\"><img  src=\"data:image/png;base64,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\" alt=\"logo\" border=\"0\" align=\"left\"></a>\n</div>\n";
        toFile += "<div style=\"margin:10px 20px 10px 150px;\">\n" + "\t<h1>" + headline + "</h1>\n";
        toFile += "\t<p style=\"margin-left:20px;\"><strong>This file was generated by AlgTest utility. See <u><a target=\"_blank\" href=\"http://www.fi.muni.cz/~xsvenda/jcsupport.html\">this website</a></u>  for more results, source codes and other details. </strong><u><a href=\"http://www.fi.muni.cz/~xsvenda/jcsupport.html#LIST\">List of all measured cards.</a></u></p>\n";
        toFile += "</div>\n </br>\n";
        file.write(toFile.getBytes());
    }

    public static void begin(FileOutputStream file, String headline) throws IOException {
        String toFile = "";
        toFile += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
        toFile += "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
        toFile += "<head>\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n";
        toFile += "<title>JCAlgTest Performance test</title>\n";
        toFile += "<link rel=\"stylesheet\" type=\"text/css\" href=\"./source/style.css\">\n";
        toFile += "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js\"></script>\n";
        toFile += "<script type=\"text/javascript\" src=\"./source/jquery-1.10.1.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"./source/jquery.tablesorter.js\"></script>\n"
                + "<script type=\"text/javascript\" id=\"js\">\n"
                + "\t$.tablesorter.addParser({\n"
                + "\t\tid: 'error',\n"
                + "\t\tis: function(s) {\n"
                + "\t\t\treturn true;\n"
                + "\t\t},\n"
                + "\tformat: function(s) {\n"
                + "\t\treturn s.toLowerCase().replace(/-/,99999);\n"
                + "\t},\n\t\ttype: 'numeric'\n\t});\n\n"
                + "\t$(function() {\n"
                + "\t$(\"#sortable_sym\").tablesorter({\n"
                + "\t\theaders: { \n"
                + "\t\t\t1:{sorter:'error'}, 2:{sorter:'error'}, 3:{sorter:'error'}, 4:{sorter:'error'}, 5:{sorter:'error'}, "
                + "6:{sorter:'error'}, 7:{sorter:'error'}, 8:{sorter:'error'}, 9:{sorter:'error'}, 10:{sorter:'error'}, \n"
                + "\t\t\t11:{sorter:'error'}, 12:{sorter:'error'}, 13:{sorter:'error'}, 14:{sorter:'error'}, 15:{sorter:'error'}, "
                + "16:{sorter:'error'}, 17:{sorter:'error'}, 18:{sorter:'error'}, 19:{sorter:'error'}, 20:{sorter:'error'}, \n"
                + "\t\t\t21:{sorter:'error'}, 22:{sorter:'error'}, 23:{sorter:'error'}, 24:{sorter:'error'}, 25:{sorter:'error'}, "
                + "26:{sorter:'error'}, 27:{sorter:'error'}, 28:{sorter:'error'}, 29:{sorter:'error'}, 30:{sorter:'error'} \n"
                + "\t\t\t}\t\t});\n\t});\n"
                + "\t$(function() {\n"
                + "\t$(\"#sortable_asym\").tablesorter({\n"
                + "\t\theaders: { \n"
                + "\t\t\t1:{sorter:'error'}, 2:{sorter:'error'}, 3:{sorter:'error'}, 4:{sorter:'error'}, 5:{sorter:'error'}, "
                + "6:{sorter:'error'}, 7:{sorter:'error'}, 8:{sorter:'error'}, 9:{sorter:'error'}, 10:{sorter:'error'}, \n"
                + "\t\t\t11:{sorter:'error'}, 12:{sorter:'error'}, 13:{sorter:'error'}, 14:{sorter:'error'}, 15:{sorter:'error'}, "
                + "16:{sorter:'error'}, 17:{sorter:'error'}, 18:{sorter:'error'}, 19:{sorter:'error'}, 20:{sorter:'error'}, \n"
                + "\t\t\t21:{sorter:'error'}, 22:{sorter:'error'}, 23:{sorter:'error'}, 24:{sorter:'error'}, 25:{sorter:'error'}, "
                + "26:{sorter:'error'}, 27:{sorter:'error'}, 28:{sorter:'error'}, 29:{sorter:'error'}, 30:{sorter:'error'} \n"
                + "\t\t\t}\t\t});\n\t});\n"
                + "</script>\n";

        toFile += "<script type=\"text/javascript\" src=\"./source/jquery.fancybox.js?v=2.1.5\"></script>\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"./source/jquery.fancybox.css?v=2.1.5\" media=\"screen\" />\n"
                + "<script type=\"text/javascript\">\n"
                + "\t$(document).ready(function() {\n"
                + "\t\t$('.fancybox').fancybox();\n"
                + "\t});\n"
                + "</script>";

        toFile += "<script>\n" + "\tjQuery(document).ready(function(){\n\tvar offset = 220;var duration = 500;\n"
                + "\t\tjQuery(window).scroll(function(){\n\tif (jQuery(this).scrollTop()>offset){jQuery('.back-to-top').fadeIn(duration);\n"
                + "\t}else{jQuery('.back-to-top').fadeOut(duration);}});\n"
                + "\t\tjQuery('.back-to-top').click(function(event){event.preventDefault();\n"
                + "\t\tjQuery('html, body').animate({scrollTop: 0}, duration);\n"
                + "\treturn false;})});\n</script>\n";
        toFile += "</head>\n";
        toFile += "<body>\n</br>\n<div class=\"main\">\n";
        toFile += "<div style=\"margin:15px 20px 10px 20px;\">\n";
        toFile += "\t<a target=\"_blank\" href=\"https://github.com/crocs-muni/JCAlgTest\"><img  src=\"data:image/png;base64,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\" alt=\"logo\" border=\"0\" align=\"left\"></a>\n</div>\n";
        toFile += "<div style=\"margin:10px 20px 10px 150px;\">\n" + "\t<h1>" + headline + "</h1>\n";
        toFile += "\t<p style=\"margin-left:20px;\"><strong>This file was generated by AlgTest utility. See <u><a target=\"_blank\" href=\"http://www.fi.muni.cz/~xsvenda/jcsupport.html\">this website</a></u> for more results, source codes and other details. </strong><u><a href=\"http://www.fi.muni.cz/~xsvenda/jcsupport.html#LIST\">List of all measured cards.</a></u></p>\n";
        toFile += "</div>\n </br>\n";

        file.write(toFile.getBytes());
    }

    public static void tableGenerator(List<String> lines, FileOutputStream file, Integer lp) throws IOException {
        String toFile = "";
        while (lp < lines.size() - 2) {
            lp++;
            for (String cat : category) {
                if (lines.get(lp).equals(cat) && !((lines.get(lp + 1)).contains("END") || (lines.get(lp + 2)).contains("END"))) {
                    toFile += "<h3 id=\"" + cat.replaceAll(" ", "_") + "\">" + cat + "</h3>\n";     //test category name
                    lp++;
                    if (lines.get(lp).contains("data")) {
                        toFile += "<p>" + lines.get(lp) + "</p>\n";           // info about length of data
                        lp++;
                    }

                    toFile += TABLE_HEAD;                                   // head of table 
                    file.write(toFile.getBytes());
                    toFile = "";
                    lp = parse(lines, file, lp);                                 //parsing info of separate tests
                    toFile += "</table>\n</br>\n";                          //end of table
                    file.write(toFile.getBytes());
                    toFile = "";
                }
            }
        }
    }

    public static int loadTopFunctions(List<String> topNames, List<String> topAcronyms) throws IOException {
        List<String> topNames_sym = new ArrayList<>();
        List<String> topAcronyms_sym = new ArrayList<>();
        List<String> topNames_asym = new ArrayList<>();
        List<String> topAcronyms_asym = new ArrayList<>();
        loadTopFunctions(topNames_sym, topAcronyms_sym, topNames_asym, topAcronyms_asym);
        topNames.addAll(topNames_sym);
        topNames.addAll(topNames_asym);
        if (topAcronyms != null) {
            topAcronyms.addAll(topAcronyms_sym);
            topAcronyms.addAll(topAcronyms_asym);
        }
        return topNames.size();
    }

    public static void loadTopFunctions(List<String> topNames_sym, List<String> topAcronyms_sym, List<String> topNames_asym, List<String> topAcronyms_asym) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(topFunctionsFile));
        } catch (IOException e) {
            System.out.println("Top Functions file not found");
        }

        String[] lineArray;
        String line;

        if (reader != null) {
            while ((line = reader.readLine()) != null) {
                if (!(line.trim().isEmpty())) {
                    lineArray = line.split(";");

                    if (lineArray.length > 2) {
                        if (lineArray[2].trim().equalsIgnoreCase("SYM")) {
                            topNames_sym.add(lineArray[0]);
                            if (topAcronyms_sym != null) {
                                topAcronyms_sym.add(lineArray[1]);
                            }
                        } else if (lineArray[2].trim().equalsIgnoreCase("ASYM")) {
                            topNames_asym.add(lineArray[0]);
                            if (topAcronyms_asym != null) {
                                topAcronyms_asym.add(lineArray[1]);
                            }
                        } else {
                            // if no indication of type of lagorith was provided, put it into topNames_sym list
                            System.out.println("ERROR: Unknown type of algorithm detected when parsting top.txt: " + lineArray[2]);
                        }
                    } else {
                        // if no indication of type of algorithm was provided, put it into topNames_sym list
                        topNames_sym.add(lineArray[0]);
                        if (topAcronyms_sym != null) {
                            topAcronyms_sym.add(lineArray[1]);
                        }
                    }
                }
            }
            reader.close();
        }
    }

    public static List<String> listFilesForFolder(final File folder) {
        List<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.getName().contains(".csv")) {
                    files.add(folder + "//" + fileEntry.getName());
                }
            }
        }
        return files;
    }

    public static void generateSortableTable(String tableID, List<String> topAcronyms, List<String> topNames, List<String> files, FileOutputStream file) throws IOException {
        Integer lp = 0;
        String result = "<table id=\"" + tableID + "\" class=\"tablesorter\" cellspacing='0'>\n";
        result += "\t<thead><tr>\n\t<th style=\"min-width:300px;\">CARD/FUNCTION (ms/op)</th>";

        for (String topAcronym : topAcronyms) {
            result += "<th>" + topAcronym + "</th>";
        }

        result += "</tr>\n</thead>\n<tbody>\n";
        file.write(result.getBytes());
        result = "";

        for (String filename : files) {
            StringBuilder cardName = new StringBuilder();
            List<String> lines = initalize(filename, cardName);
            if (cardName.toString().isEmpty()) {
                // If card name is not filled, use whole file name
                cardName.append(filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf(".")));
            }
            result += "<tr><td><strong>" + cardName + "</strong></td>";
            file.write(result.getBytes());
            result = "";
            for (String topName : topNames) {
                boolean bTopNameFound = false;
                while (lp < lines.size() - 4) {
                    if (lines.get(lp).contains(topName)) {
                        bTopNameFound = true;
                        lp = parseOneSortable(lines, file, lp);
                    } else {
                        lp++;
                    }
                }
                // In case given algorithm (topname) is not present in measured file, put -
                if (!bTopNameFound) {
                    result = "<td>-</td>";
                    file.write(result.getBytes());
                    result = "";
                }
                lp = 0;
            }
            result += "</tr>\n";
        }

        result += "</tbody>\n</table>\n";
        file.write(result.getBytes());
    }

    public static String generateLegendHeader(List<String> topNames, List<String> topAcronyms) throws IOException {
        String header = "";
        //header = "Used notation:<br>\n";
        header += "<ul style=\"list-style-type:circle; font-size:14px; line-height:120%;\">\n";
        for (int i = 0; i < topNames.size(); i++) {
            header += "\t<li><strong>" + topAcronyms.get(i) + "</strong> = " + topNames.get(i) + "</li>\n";
        }
        header += "</ul>\n";
        return header;
    }

    public static void sortableGenerator(String dir, FileOutputStream file, Integer lp) throws IOException {
        List<String> topNames_sym = new ArrayList<>();
        List<String> topAcronyms_sym = new ArrayList<>();
        List<String> topNames_asym = new ArrayList<>();
        List<String> topAcronyms_asym = new ArrayList<>();
        loadTopFunctions(topNames_sym, topAcronyms_sym, topNames_asym, topAcronyms_asym);
        List<String> files = listFilesForFolder(new File(dir));
        lp = 0;
        file.write("</div>".getBytes());
        //
        // Sortable table for symmetric algorithms
        //
        file.write(generateLegendHeader(topNames_sym, topAcronyms_sym).getBytes());
        generateSortableTable("sortable_sym", topAcronyms_sym, topNames_sym, files, file);
        //
        // Sortable table for asymmetric algorithms
        file.write(generateLegendHeader(topNames_asym, topAcronyms_asym).getBytes());
        generateSortableTable("sortable_asym", topAcronyms_asym, topNames_asym, files, file);
    }

    public static void topFunction(List<String> lines, FileOutputStream file, Integer lp) throws IOException {
        String toFile;
        Integer tp = 0;
        List<String> topNames = new ArrayList<>();
        loadTopFunctions(topNames, null);

        if (topNames.size() != 0) {
            toFile = "<h3 id=\"TOP\">TOP FUNCTIONS</h3>\n";                              //name of table
            toFile += "<p>In the table below you can find results of performance testing for frequently used functions.</p>";
            toFile += TABLE_HEAD;
            file.write(toFile.getBytes());
            toFile = "";

            for (String top : topNames) {
                while (lp < lines.size() - 4) {
                    if (lines.get(lp).contains(top)) {
                        lp = parseOne(lines, file, lp, tp);
                    } else {
                        lp++;
                    }
                }
                lp = 0;
            }

            toFile += "</table>\n</br>\n";                                  //end of table
            file.write(toFile.getBytes());
            lp = 0;
        }
    }

    public static Integer parseOne(List<String> lines, FileOutputStream file, Integer lp, Integer tp) throws IOException {
        String toFile = "";
        String[] prepare;
        String[] operation;
        String[] other;

        if (lines.get(lp + 1).equals("ALREADY_MEASURED")) {
            lp += 2;
            return lp;
        } else {
            toFile += ((tp % 2) == 0) ? "\t<tr>" : "\t<tr class='even'>";
            prepare = lines.get(lp).trim().split(";");
            toFile += "<td><b>"+prepare[1]+"</b></td>";       //classic name without reference to chart
            //toFile += "<td><a class=\"fancybox fancybox.iframe\" href=\"./graphs/" + prepare[1] + ".html\" style=\"font-size:12px;\">" + prepare[1] + "</a></td>";
            lp += 2;
        }

        if ((lines.get(lp).contains("baseline")) && (lines.get(lp + 3).contains("avg op:"))) {
            lp++;
            prepare = lines.get(lp).trim().split(";");
            lp += 2;
            operation = lines.get(lp).trim().split(";");
            lp++;
            other = lines.get(lp).trim().split(";"); 

            toFile += "<td style=\"font-size: 110%; font-weight: bold;\">" + Float.valueOf(operation[2].replace(",", ".")) + "</td>";
            toFile += "<td>" + Float.valueOf(operation[4].replace(",", ".")) + "</td>";
            toFile += "<td>" + Float.valueOf(operation[6].replace(",", ".")) + "</td>";
            toFile += "<td>" + Integer.parseInt(other[2]) + "</td>";
            toFile += "<td>" + Float.valueOf(prepare[2].replace(",", ".")) + "</td>";
            toFile += "<td>" + Float.valueOf(prepare[4].replace(",", ".")) + "</td>";
            toFile += "<td>" + Float.valueOf(prepare[6].replace(",", ".")) + "</td>";
                       
            toFile += "<td>" + Integer.parseInt(other[4]) + "/" + Integer.parseInt(other[6]) + "</td>";
        } else {
            if (lines.get(lp).contains("baseline") && !(lines.get(lp).contains("error"))) {
                lp++;
                prepare = lines.get(lp).trim().split(";");
                lp++;
                toFile += "<td colspan=\"3\">" + lines.get(lp) + "</td><td> </td>";
                toFile += "<td>" + Float.valueOf(prepare[2].replace(",", ".")) + "</td>";
                toFile += "<td>" + Float.valueOf(prepare[4].replace(",", ".")) + "</td>";
                toFile += "<td>" + Float.valueOf(prepare[6].replace(",", ".")) + "</td>";

            } else if (lines.get(lp).contains("error")) {
                prepare = lines.get(lp).trim().split(";");
                toFile += "<td colspan=\"3\">" + prepare[prepare.length - 1] + "</td>";
            } else {
                toFile += "<td colspan=\"3\">" + lines.get(lp) + "</td>";
            }
        }

        toFile += "</tr>\n";
        file.write(toFile.getBytes());
        lp++;
        return lp;
    }

    public static Integer parseOneSortable(List<String> lines, FileOutputStream file, Integer lp) throws IOException {
        String[] operation;
        String result = "";

        String title = "";
        String value = "-";
        if (lines.get(lp + 1).equals("ALREADY_MEASURED")) {
            lp += 2;
            return lp;
        } else {
            lp += 2;
        }

        if ((lines.get(lp).contains("baseline")) && (lines.get(lp + 3).contains("avg op:"))) {
            lp += 3;
            operation = lines.get(lp).trim().split(";");

            value = (Float.valueOf(operation[2].replace(",", "."))).toString();
            title += "min: " + Float.valueOf(operation[4].replace(",", ".")) + "; max: ";
            title += Float.valueOf(operation[6].replace(",", "."));
            lp++;
        } else {
            if (lines.get(lp).contains("baseline") && !(lines.get(lp).contains("error"))) {
                lp += 2;
                value = "-";
            } else if (lines.get(lp).contains("error")) {
                value = "-";
            } else {
                value = "-";
            }
        }

        result += "<td title=\"" + title + "\">" + value;
        result += "</td>";
        lp++;
        file.write(result.getBytes());
        return lp;
    }

    public static Integer parseOneForGraph(List<String> lines, StringBuilder toFile, Integer lp) throws IOException {
        String[] operation;
        Float min = -1.0F;
        Float max = -1.0F;
        Float avg = -1.0F;
        String length = "-999";

        if (lines.get(lp + 1).equals("ALREADY_MEASURED")) {
            lp += 2;
            return lp;
        } else {
            lp += 5;
        }

        if ((lines.get(lp).contains("avg op:"))) {
            operation = lines.get(lp).trim().split(";");
            avg = Float.valueOf(operation[2].replace(",", "."));
            min = Float.valueOf(operation[4].replace(",", "."));
            max = Float.valueOf(operation[6].replace(",", "."));
            lp++;
            length = lines.get(lp).trim().split(";")[2];
        }

        if ((min == -1.0) || (max == -1.0) || (avg == -1.0) || (length.equals("-1"))) {
            //toFile.append("\t\t[0, 0, 0, 0, '0', false],\n");
        } else {
            toFile.append(("\t\t[" + length + ", " + avg.toString() + ", " + min.toString() + ", " + max.toString() + ", '" + length + "', false],\n"));
        }

        lp++;
        return lp;
    }

    public static Integer parseOneGraph(List<String> lines, String dir, String name, Integer lp) throws FileNotFoundException, IOException {
        StringBuilder toFile = new StringBuilder();
        String methodName = lines.get(lp).split(";")[1];

        toFile.append("<!DOCTYPE html>\n"
                + "<html>\n<head>\n"
                + "\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "\t<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n"
                + "\t<script type=\"text/javascript\">\n"
                + "\tgoogle.setOnLoadCallback(drawFancyVisualization);\n"
                + "\tfunction drawFancyVisualization() {\n"
                + "\t\tvar data = new google.visualization.DataTable();\n"
                + "\t\tdata.addColumn('number', 'length of data (bytes)');\n"
                + "\t\tdata.addColumn('number', 'Time (ms)');\n"
                + "\t\tdata.addColumn({type:'number', role:'interval'});\n"
                + "\t\tdata.addColumn({type:'number', role:'interval'});\n"
                + "\t\tdata.addColumn({type:'string', role:'annotation'});\n"
                + "\t\tdata.addColumn({type:'boolean',role:'certainty'});\n"
                + "\t\tdata.addRows([");

        while (lp < lines.size() - 4) {
            if (lines.get(lp).contains(methodName)) {
                lp = parseOneForGraph(lines, toFile, lp);
            } else {
                if (lines.get(lp).contains("method name:")) {
                    break;
                } else {
                    lp++;
                }
            }
        }

        toFile.append("]);\n\n"
                + "\tvar options = {\n"
                + "\t\ttitle: '" + methodName + "',\n"
                + "\t\ttitleTextStyle: {fontSize: 15},\n"
                + "\t\thAxis: {title: 'length of data (bytes)', viewWindow: {min: 0, max: "+ (toFile.toString().contains("512") ? "530" : "265") +"} },\n"
                + "\t\tvAxis: {title: 'duration of operation (ms)' },\n"
                + "\t\tlegend:'none',};\n\n"
                + "\tvar chart = new google.visualization.LineChart(document.getElementById('" + methodName.trim() + "'));\n"
                + "\tchart.draw(data, options);\n"
                + "\t}\n\n\n\t</script>\n</head>\n<body>\n"
                + "\t<script type=\"text/javascript\" src=\"https://www.google.com/jsapi?autoload={'modules':[{'name':'visualization','version':'1.1','packages':['corechart']}]}\"></script>\n"
                + "\t<div id=\"" + methodName.trim() + "\" style=\"width: 790px; height: 500px;\"></div>\n"
                + "</body>\n</html>");

        if (!(toFile.toString().contains("data.addRows([]);"))) {
            FileOutputStream file = new FileOutputStream(dir + "\\" + methodName + ".html");
            file.write(toFile.toString().getBytes());
            file.close();
        }

        return lp;
    }

    public static void parseGraphsPage(List<String> lines, FileOutputStream file) throws FileNotFoundException, IOException {
        List<String> topFunctions = new ArrayList<>();
        List<String> usedFunctions = new ArrayList<>();
        loadTopFunctions(topFunctions, null);
        StringBuilder toFile = new StringBuilder();
        StringBuilder chart = new StringBuilder();
        //end of test details (1st div), end of beginning (2nd div)
        toFile.append("</br></div>\n</div>\n\t<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n");
        Integer lp = 0;
        String methodName = "";

        // Generating charts of functions that are present in top functions
        while (lp < lines.size() - 20) {
            if (lines.get(lp).contains("method name:")) {
                methodName = lines.get(lp).split(";")[1];
                if (methodName.startsWith(" ")) {
                    methodName = methodName.substring(1);
                }
                // delete UTIL condition to generate UTIL algs charts
                if ((topFunctions.contains(methodName)) || (topFunctions.size() == 0)) {
                    chart.append("\t<script type=\"text/javascript\">\n"
                            + "\tgoogle.setOnLoadCallback(drawFancyVisualization);\n"
                            + "\tfunction drawFancyVisualization() {\n"
                            + "\t\tvar data = new google.visualization.DataTable();\n"
                            + "\t\tdata.addColumn('number', 'length of data (bytes)');\n"
                            + "\t\tdata.addColumn('number', 'Time (ms)');\n"
                            + "\t\tdata.addColumn({type:'number', role:'interval'});\n"
                            + "\t\tdata.addColumn({type:'number', role:'interval'});\n"
                            + "\t\tdata.addColumn({type:'string', role:'annotation'});\n"
                            + "\t\tdata.addColumn({type:'boolean',role:'certainty'});\n"
                            + "\t\tdata.addRows([");

                    while (lp < lines.size() - 6) {
                        if (lines.get(lp).contains(methodName)) {
                            lp = parseOneForGraph(lines, chart, lp);
                        } else if (lines.get(lp).contains("method name:")) {
                            break;
                        } else {
                            lp++;
                        }
                    }

                    chart.append("]);\n\n"
                            + "\tvar options = {\n"
                            + "\t\ttitle: '" + methodName + "',\n"
                            + "\t\ttitleTextStyle: {fontSize: 15},\n"
                            + "\t\thAxis: {title: 'length of data (bytes)', viewWindow: {min: 0, max: "+ (chart.toString().contains("512") ? "530" : "265") +"} },\n"
                            + "\t\tvAxis: {title: 'duration of operation (ms)' },\n"
                            + "\t\tlegend:'none',};\n\n"
                            + "\tvar chart = new google.visualization.LineChart(document.getElementById('" + methodName.replaceAll(" ", "_") + "'));\n"
                            + "\tchart.draw(data, options);\n"
                            + "\t}\n\t</script>\n");

                    if (!(chart.toString().contains("data.addRows([]);"))) {
                        usedFunctions.add(methodName);
                        toFile.append(chart.toString());
                    }
                    chart.delete(0, chart.length() - 1);
                } else {
                    lp++;
                }
            } else {
                lp++;
            }
        }

        BigDecimal sec = new BigDecimal(2 + usedFunctions.size()*0.15);
        sec = sec.setScale(2, BigDecimal.ROUND_HALF_UP);
        for (String usedFunction : usedFunctions) {
            toFile.append("\t<div id=\"" + usedFunction.replaceAll(" ", "_") + "\" style=\" min-height:400px; max-height:1000px; min-width:600px; width:49%; height:60%; float:left;\">"
                    + "</br><h3 style=\"text-align: center;\">" + usedFunction + "</br></br></h3><p style=\"text-align: center;\"><strong>GRAPH IS LOADING. </br></br> THIS MAY TAKE <u>"+ sec +"</u> SECONDS DEPENDING ON THE NUMBER OF GRAPHS.</strong></p>"
                    + "</div>\n");
        }

        toFile.append("\t<script type=\"text/javascript\" src=\"https://www.google.com/jsapi?autoload={'modules':[{'name':'visualization','version':'1.1','packages':['corechart']}]}\"></script>\n");

        //quick links to generated charts at the beginning of html file
        String toFileBegin;
        toFileBegin = "<div class=\"pageColumnQuickLinks\" style=\"max-width:50%;\">\n";        
        toFileBegin += "<h3>Quick links | number of graphs: "+usedFunctions.size()+" | est. load time: "+ sec +" s</h3>\n<ul style=\"list-style-type: circle;\">\n";
        for (String usedFunction : usedFunctions) {
            toFileBegin += "\t<li>" + "<a href=\"#" + usedFunction.replaceAll(" ", "_") + "\">" + usedFunction + "</a>" + "</li>\n";
        }
        toFileBegin += "</ul>\n</div>\n";
        file.write(toFileBegin.getBytes());             //quick links written

        //test details generated at the beginning of html file
        detailsBasic(lines, file);                      //details written
        file.write(toFile.toString().getBytes());       //charts written
    }

    public static void parseGraphs(List<String> lines, String dir, String name) throws FileNotFoundException, IOException {
        Integer lp = 15;

        while (lp < lines.size() - 4) {
            if (lines.get(lp).contains("method name:")) {
                lp = parseOneGraph(lines, dir, name, lp);
            } else {
                lp++;
            }
        }
    }

    public static void endOfHtml(FileOutputStream file) throws FileNotFoundException, IOException {
        String toFile = "<script>/* <![CDATA[ */(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)\n"
                + "[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');ga('create','UA-1931909-2','auto');ga('send','pageview');/* ]]> */</script>\n\n";
        toFile += "</br></p>\n<a href=\"#\" class=\"back-to-top\">Back to Top</a>\n</body>\n" + "</html>";
        toFile += "<div style=\"margin:10px 20px 10px 20px;\">\n" +
                  "<center>\n" +
                  "<p><a rel=\"license\" href=\"http://creativecommons.org/licenses/by/4.0/\"><img alt=\"Creative Commons License\" style=\"border-width:0\" src=\"https://i.creativecommons.org/l/by/4.0/88x31.png\" /></a><br /><span xmlns:dct=\"http://purl.org/dc/terms/\" href=\"http://purl.org/dc/dcmitype/Dataset\" property=\"dct:title\" rel=\"dct:type\">JCAlgTest</span> by <span xmlns:cc=\"http://creativecommons.org/ns#\" property=\"cc:attributionName\">CRoCS MU</span> is licensed <br>under a <a rel=\"license\" href=\"http://creativecommons.org/licenses/by/4.0/\">Creative Commons Attribution 4.0 International License</a>.</p>\n" +
                  "</center>\n</div>";
        file.write(toFile.getBytes());
        file.close();
    }

    public static void run(String input, String name) throws FileNotFoundException, IOException {
        Integer linePosition = 0;        
        StringBuilder cardName = new StringBuilder();
        String cardNameFile = name;
        
        List<String> lines = initalize(input, cardName);                            //load lines to from file to List
        if (!(cardName.toString().equals("")) && !(cardName.toString().equals(" ")))
            cardNameFile = cardName.toString().replaceAll(" ", "");
  
        cardNameFile = cardNameFile.replaceAll("_", "");
        FileOutputStream file = new FileOutputStream(cardNameFile + ".html");
        begin(file, "Test results card: " + cardName.toString());                     //logo + headline
        quickLinks(lines, file);
        details(lines, file);                                                       //test details + CPLC info
        topFunction(lines, file, linePosition);
        tableGenerator(lines, file, linePosition);                                  //all tables generator
        endOfHtml(file);
        System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
    }

    public static void runSortable(String dir) throws FileNotFoundException, IOException {
        Integer linePosition = 0;
        FileOutputStream file = new FileOutputStream(dir + "//" + "sortable.html");
        begin(file, "Card performance - comparative table");
        sortableGenerator(dir, file, linePosition);
        endOfHtml(file);
        System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
    }

    public static void runGraphs(String input) throws IOException {
        StringBuilder cardName = new StringBuilder();
        List<String> lines = initalize(input, cardName);
        String resultsDir = new File(input).getAbsolutePath();
        resultsDir = resultsDir.substring(0, resultsDir.lastIndexOf("\\")) + "\\graphs";
        File dir = new File(resultsDir);
        dir.mkdirs();
        parseGraphs(lines, (dir.getAbsolutePath()), cardName.toString());
        System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
    }

    public static void runGraphsOnePage(String input) throws IOException {
        StringBuilder cardName = new StringBuilder();
        String cardNameFile = "graphspage";
        List<String> lines = initalize(input, cardName);
        String resultsDir = new File(input).getAbsolutePath();
        resultsDir = resultsDir.substring(0, resultsDir.lastIndexOf("\\"));
        if (!(cardName.toString().equals("")) && !(cardName.toString().equals(" "))){
            cardNameFile = cardName.toString().replaceAll(" ", "");
            cardNameFile = cardNameFile.replaceAll("_", "");
        } 
        FileOutputStream file = new FileOutputStream(resultsDir + "\\" + cardNameFile + ".html");
        beginLite(file, "Graphs results card: " + cardName.toString());
        parseGraphsPage(lines, file);
        endOfHtml(file);
        System.out.println("Make sure that CSS file & JS files (\"Source\" folder) is present in output folder.");
    }
}
