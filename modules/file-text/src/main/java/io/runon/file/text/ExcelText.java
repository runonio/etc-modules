package io.runon.file.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.runon.commons.utils.string.Change;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author macle
 */
public class ExcelText {
    private ExcelGet excelGet;
    private Row row;


    public String getSimpleText(String excelFilePath) throws IOException, InvalidFormatException {

        excelGet = new ExcelGet();

        StringBuilder sb = new StringBuilder();

        File file = new File(excelFilePath);

        Workbook workbook = WorkbookFactory.create(file);
        excelGet.setWorkbook(file);

        int sheetSheet = workbook.getNumberOfSheets();
        for (int sheetIndex = 0; sheetIndex <sheetSheet ; sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            int rowCount = excelGet.getRowCount(sheet);

            for (int rowIndex = 0; rowIndex < rowCount ; rowIndex++) {
                row = sheet.getRow(rowIndex);
                sb.append("\n");

                int columnCount = excelGet.getColumnCount(row);
                StringBuilder rowBuilder = new StringBuilder();
                for (int columnIndex = 0; columnIndex <columnCount ; columnIndex++) {

                    String value = getCellValue(columnIndex);
                    if(value == null){
                        continue;
                    }
                    rowBuilder.append(" ").append(value);
                }

                if(rowBuilder.length() > 0){
                    String rowText = rowBuilder.toString().trim();
                    rowText = Change.spaceContinue(rowText).trim();
                    sb.append(rowText);
                }

            }
        }

        if(sb.length() == 0){
            return "";
        }

        return sb.substring(1);
    }

    public JsonArray getPageArray(String excelFilePath) throws IOException, InvalidFormatException {
        excelGet = new ExcelGet();

        JsonArray pageArray =new JsonArray();

        File file = new File(excelFilePath);

        Workbook workbook = WorkbookFactory.create(file);
        excelGet.setWorkbook(file);

        int sheetSheet = workbook.getNumberOfSheets();
        for (int sheetIndex = 0; sheetIndex <sheetSheet ; sheetIndex++) {
            JsonObject sheetObj = new JsonObject();

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            int rowCount = excelGet.getRowCount(sheet);

            StringBuilder sb = new StringBuilder();

            for (int rowIndex = 0; rowIndex < rowCount ; rowIndex++) {
                row = sheet.getRow(rowIndex);
                sb.append("\n");

                int columnCount = excelGet.getColumnCount(row);
                StringBuilder rowBuilder = new StringBuilder();
                for (int columnIndex = 0; columnIndex <columnCount ; columnIndex++) {

                    String value = getCellValue(columnIndex);
                    if(value == null){
                        continue;
                    }
                    rowBuilder.append(" ").append(value);
                }

                if(rowBuilder.length() > 0){
                    String rowText = rowBuilder.toString().trim();
                    rowText = Change.spaceContinue(rowText).trim();
                    sb.append(rowText);
                }
            }

            if(sb.length() > 0){
                sheetObj.addProperty("index", sheetIndex);
                sheetObj.addProperty("name",sheet.getSheetName());
                sheetObj.addProperty("text", sb.substring(1));

                pageArray.add(sheetObj);
            }
        }

        return pageArray;
    }


    /**
     * cell value string 형태로 얻기
     * @param cellNum int cell num first 0
     * @return string cell value
     */
    private String getCellValue(int cellNum){
        return excelGet.getCellValue(row, cellNum);
    }


    public static void main(String[] args) throws Exception{
        String text = new ExcelText().getSimpleText("D:\\업무\\DB약어표준(행정용).xls");
        System.out.println(text);
    }
}
