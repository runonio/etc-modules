
package io.runon.poi.excel.example;

import io.runon.poi.excel.ExcelGet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;

/**
 * ExcelGet 예제
 *
 * @author macle
 */
public class ExcelGetExample {


    private ExcelGet excelGet;
    private Row row;

    /**
     * 엑셀 파일 읽기
     * @param excelFilePath string excel file path
     */
    public void load(String excelFilePath){

        try {
            excelGet = new ExcelGet();


            File file = new File(excelFilePath);

            Workbook workbook = WorkbookFactory.create(file);
            excelGet.setWorkbook(file);

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = excelGet.getRowCount(sheet);

            for (int i = 0; i < rowCount ; i++) {
                row = sheet.getRow(i);

                int columnCount = excelGet.getColumnCount(row);
                for (int j = 0; j <columnCount ; j++) {
                    System.out.println(getCellValue(j));
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * cell value string 형태로 얻기
     * @param cellNum int cell num first 0
     * @return string cell value
     */
    private String getCellValue(int cellNum){
        return excelGet.getCellValue(row, cellNum);
    }

    public static void main(String[] args) {
        ExcelGetExample excelGetExample = new ExcelGetExample();
        excelGetExample.load("excel file path");
    }
}
