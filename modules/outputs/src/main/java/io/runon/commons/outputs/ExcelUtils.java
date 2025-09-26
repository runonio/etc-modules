package io.runon.commons.outputs;

import io.runon.commons.exception.IORuntimeException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author macle
 */
public class ExcelUtils {

    public static Workbook createWorkBook(String outPath){
        if (outPath.endsWith(".xls")) {
            return new HSSFWorkbook();
        }else{
            return new XSSFWorkbook();
        }
    }


    public static void write(Workbook workbook, String outPath){
        try(FileOutputStream fos = new FileOutputStream(outPath)){
            workbook.write(fos);
        }catch (IOException e){
            throw new IORuntimeException(e);
        }
    }
}
