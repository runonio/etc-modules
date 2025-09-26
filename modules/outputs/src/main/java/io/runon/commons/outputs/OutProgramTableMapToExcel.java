package io.runon.commons.outputs;

import io.runon.commons.utils.ExceptionUtil;
import io.runon.commons.utils.FileUtil;
import io.runon.commons.utils.string.Change;
import io.runon.commons.utils.string.Check;
import io.runon.commons.utils.string.Remove;
import io.runon.collect.crawling.core.http.CrawlingScript;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.*;

/**
 * @author macle
 */
@Slf4j
public class OutProgramTableMapToExcel {

    private final String dirSeparator;

    private final String serviceFileEnd = "Service.java";

    private final int serviceFileEndLength = serviceFileEnd.length();

    public OutProgramTableMapToExcel(){

        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win")){
            dirSeparator = "\\";
        }else{
            dirSeparator = "/";
        }
    }

    private int rowIndex = 2;

    private int rowNum = 1;

    private Sheet sheet;

    private Row row;

    private String javaDirPath;
    private String resDirPath;
    public void out(String srcPath, String outPath) {

        javaDirPath = srcPath+dirSeparator +"main" + dirSeparator + "java";

        List<File> fileList = FileUtil.getFileList(javaDirPath);
        List<File> serviceFileList = new ArrayList<>();

        for(File file : fileList){
            if(file.isDirectory()){
                continue;
            }
            if(!file.isFile()){
                continue;
            }

            if(file.getName().endsWith("Service.java") && isFile(getDaoPath(file))){
                serviceFileList.add(file);
            }
        }

        fileList.clear();

        if(serviceFileList.isEmpty()){
            log.error("service file size 0");
            return ;
        }

        File [] files = serviceFileList.toArray(new File[0]);
        serviceFileList.clear();

        Arrays.sort(files,  Comparator.comparing(File::getAbsolutePath));

        resDirPath = srcPath+dirSeparator +"main" + dirSeparator + "resources";

        List<File> xmlList = FileUtil.getFileList(resDirPath,"xml");

        Map<String, File > mapperMap = new HashMap<>();
        for(File xml : xmlList){
            String fileText = FileUtil.getFileContents(xml, "UTF-8");
            Document dom = Jsoup.parse(fileText);
            Elements itemElements = dom.getElementsByTag("mapper");
            if(itemElements == null || itemElements.size() == 0){
                continue;
            }
            Element element = itemElements.get(0);
            mapperMap.put(element.attribute("namespace").getValue().replace(".",dirSeparator), xml);

        }


        File dirFile = new File(javaDirPath);
        String removePath = dirFile.getParentFile().getAbsolutePath();
        int removeLength = removePath.length()+1;

        try ( Workbook workbook = ExcelUtils.createWorkBook(outPath)){

            sheet = workbook.createSheet("program");

            row = sheet.createRow(1);

            Cell cell = row.createCell(1);
            cell.setCellValue("번호");

            cell = row.createCell(2);
            cell.setCellValue("유형");

            cell = row.createCell(3);
            cell.setCellValue("서비스");

            cell = row.createCell(4);
            cell.setCellValue("Controller");

            cell = row.createCell(5);
            cell.setCellValue("Dao");

            cell = row.createCell(6);
            cell.setCellValue("프로그램");


            cell = row.createCell(7);
            cell.setCellValue("테이블");

            cell = row.createCell(8);
            cell.setCellValue("scr dir");

            cell = row.createCell(9);
            cell.setCellValue("mapper xml");

            rowIndex = 2;

            rowNum = 1;

            for(File file : files){

                File daoFile = new File(getDaoPath(file));
                String mapperKey = daoFile.getAbsolutePath().substring(javaDirPath.length()+1);
                mapperKey = mapperKey.substring(0, mapperKey.length()-5);

                File xmlFile = mapperMap.get(mapperKey);
                if(xmlFile == null){
                    newRow(file, null );
                }else{
                    boolean isRow = false;

                    String fileText = FileUtil.getFileContents(xmlFile, "UTF-8");
                    Document dom = Jsoup.parse(fileText);
                    Elements itemElements  = dom.getElementsByTag("insert");
                    LinkedHashSet<String> tableNames = new LinkedHashSet<>();
                    for (Element element : itemElements) {
                        tableNames.add(getInsertTable(element));
                    }

                    if(tableNames.size() > 0){
                        isRow = true;
                        newRow(file, xmlFile );
                        StringBuilder sb = new StringBuilder();
                        for (String name: tableNames){
                            sb.append("\n").append(name);
                        }

                        cell = row.createCell(6);
                        cell.setCellValue("insert");

                        cell = row.createCell(7);
                        CellStyle cs = workbook.createCellStyle();
                        cs.setWrapText(true);
                        cell.setCellStyle(cs);
                        cell.setCellValue(sb.substring(1));
                    }

                    tableNames.clear();
                    itemElements  = dom.getElementsByTag("select");
                    for (Element element : itemElements) {
                        tableNames.addAll(getSelectTable(element));
                    }


                    if(tableNames.size() > 0){
                        isRow = true;
                        newRow(file, xmlFile );
                        StringBuilder sb = new StringBuilder();
                        for (String name: tableNames){
                            sb.append("\n").append(name);
                        }

                        cell = row.createCell(6);
                        cell.setCellValue("select");

                        cell = row.createCell(7);
                        CellStyle cs = workbook.createCellStyle();
                        cs.setWrapText(true);
                        cell.setCellStyle(cs);

                        cell.setCellValue(sb.substring(1));
                    }


                    tableNames.clear();
                    itemElements  = dom.getElementsByTag("update");
                    for (Element element : itemElements) {
                        tableNames.add(getUpdateTable(element));
                    }

                    if(tableNames.size() > 0){
                        isRow = true;
                        newRow(file, xmlFile );
                        StringBuilder sb = new StringBuilder();
                        for (String name: tableNames){
                            sb.append("\n").append(name);
                        }

                        cell = row.createCell(6);
                        cell.setCellValue("update");

                        cell = row.createCell(7);
                        CellStyle cs = workbook.createCellStyle();
                        cs.setWrapText(true);
                        cell.setCellStyle(cs);
                        cell.setCellValue(sb.substring(1));
                    }

                    tableNames.clear();
                    itemElements  = dom.getElementsByTag("delete");
                    for (Element element : itemElements) {
                        tableNames.addAll(getSelectTable(element));
                    }

                    if(tableNames.size() > 0){
                        isRow = true;
                        newRow(file, xmlFile );
                        StringBuilder sb = new StringBuilder();
                        for (String name: tableNames){
                            sb.append("\n").append(name);
                        }

                        cell = row.createCell(6);
                        cell.setCellValue("delete");

                        cell = row.createCell(7);
                        //개행문자처리
                        CellStyle cs = workbook.createCellStyle();
                        cs.setWrapText(true);
                        cell.setCellStyle(cs);
                        cell.setCellValue(sb.substring(1));
                    }

                    if(!isRow){
                        newRow(file, xmlFile );
                    }
                }

            }

            for (int i = 0; i <10 ; i++) {
                sheet.autoSizeColumn(i);
            }

            ExcelUtils.write(workbook, outPath);

        }catch (Exception e){
            log.error(ExceptionUtil.getStackTrace(e));
        }
    }


    public static String getInsertTable(Element element){

        String sql = element.toString();

        sql = sql.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");// *. --> *?
        sql = Change.spaceContinue(sql);

        String tableName= new CrawlingScript(sql.toUpperCase()).getValue("INTO "," ");
        if(tableName.endsWith("(")){
            tableName = tableName.substring(0, tableName.length()-1);
        }

        return tableName;
    }

    public static List<String> getSelectTable(Element element){

        List<String> tableNames = new ArrayList<>();

        String sql = element.toString();

        String initSql = sql;

        sql = sql.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");// *. --> *?

        sql = Change.spaceContinue(sql);

        CrawlingScript crawlingScript = new CrawlingScript(sql.toUpperCase()+" ");

        String tableName = crawlingScript.getValueNext("FROM "," ");
        if(tableName == null){
            tableName = crawlingScript.getValueNext(" DELETE "," ");
        }
        if(tableName == null){
            tableName = crawlingScript.getValueNext(" UPDATE "," ");
        }



        for (;;) {
            if(tableName == null){
                break;
            }

            if(tableName.startsWith("(")){
                tableName = crawlingScript.getValueNext("FROM "," ");
            }else if(Check.isEng(tableName) && !"".equals(tableName)){
                tableNames.add(tableName);
                tableName = crawlingScript.getValueNext("FROM "," ");


            } else{
                break;
            }
        }

        //오류체크
//        for(String tableNameCheck : tableNames) {
//
//            if (tableNameCheck == null || (!tableNameCheck.startsWith("TB_") && !tableNameCheck.startsWith("SYS_") && !tableNameCheck.startsWith("DUAL") && !tableNameCheck.startsWith("FT_") && !tableNameCheck.startsWith("ALL_") && !tableNameCheck.startsWith("COLS"))) {
//                System.out.println();
//                System.out.println();
//                System.out.println(tableNameCheck + ", " + initSql);
//            }
//        }

        return tableNames;
    }

    public static String getUpdateTable(Element element){

        String sql = element.toString();

        sql = sql.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");// *. --> *?
        sql = Remove.htmlTag(sql);
        sql = Change.spaceContinue(sql);


        return new CrawlingScript(sql.toUpperCase()).getValue("UPDATE "," ");
    }

    public void newRow(File serviceFile,  File mapperXml){


        row = sheet.createRow(rowIndex);

        Cell cell = row.createCell(1);
        cell.setCellValue(rowNum++);

        cell = row.createCell(2);
        cell.setCellValue(getTypeName(serviceFile.getName()));

        cell = row.createCell(3);
        cell.setCellValue(serviceFile.getName().substring(0, serviceFile.getName().length()-5));

        if(isFile(getControllerPath(serviceFile))) {
            cell = row.createCell(4);
            File controllerFile = new File(getControllerPath(serviceFile));
            cell.setCellValue(controllerFile.getName().substring(0, controllerFile.getName().length()-5));
        }


        cell = row.createCell(5);
        File daoFile = new File(getDaoPath(serviceFile));
        cell.setCellValue(daoFile.getName().substring(0, daoFile.getName().length()-5));


        cell = row.createCell(8);
        cell.setCellValue(serviceFile.getParentFile().getParentFile().getAbsolutePath().substring(javaDirPath.length()+1).replace("\\","/"));

        if(mapperXml != null){
            cell = row.createCell(9);
            cell.setCellValue(mapperXml.getAbsolutePath().substring(resDirPath.length() +1).replace("\\","/"));
        }

        rowIndex++;
    }

    public boolean isFile(String path){
        if(path == null){
            return false;
        }

        return new File(path).isFile();
    }

    public  String getDaoPath(File serviceFile){

        try{
            return serviceFile.getParentFile().getParentFile()+dirSeparator+"dao" + dirSeparator+getTypeName(serviceFile.getName()) + "Dao.java";
        }catch (Exception e){
            return null;
        }
    }

    public  String getControllerPath(File serviceFile){

        try{
            return serviceFile.getParentFile().getParentFile()+dirSeparator+"controller" + dirSeparator+getTypeName(serviceFile.getName()) + "Controller.java";
        }catch (Exception e){
            return null;
        }
    }

    public String getTypeName(String serviceFileName){
        return serviceFileName.substring(0, serviceFileName.length() - serviceFileEndLength);
    }



    public static void main(String[] args) {

        OutProgramTableMapToExcel outProgramTableMapToExcel = new OutProgramTableMapToExcel();
        outProgramTableMapToExcel.out("C:\\project\\mr-voc-master\\src","D:\\program.xlsx");

    }
}
