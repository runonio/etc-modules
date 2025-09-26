package io.runon.commons.outputs;

import io.runon.commons.utils.ExceptionUtil;
import io.runon.commons.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * @author macle
 */
@Slf4j
public class OutDirFileInfoToExcel {

    public OutDirFileInfoToExcel(){

    }


    public void out(String dirPath, String outPath) {

        List<File> fileList = FileUtil.getFileList(dirPath);
        File [] files = fileList.toArray(new File[0]);
        fileList.clear();

        Arrays.sort(files,  Comparator.comparing(File::getAbsolutePath));

        File dirFile = new File(dirPath);
        String removePath = dirFile.getParentFile().getAbsolutePath();
        int removeLength = removePath.length()+1;

        try ( Workbook workbook = ExcelUtils.createWorkBook(outPath)){
            Sheet sheet = workbook.createSheet("files");

            Row row = sheet.createRow(1);

            Cell cell = row.createCell(1);
            cell.setCellValue("번호");

            cell = row.createCell(2);
            cell.setCellValue("dir");

            cell = row.createCell(3);
            cell.setCellValue("이름");

            cell = row.createCell(4);
            cell.setCellValue("확장자");


            int rowIndex = 2;

            int rowNum = 1;


            for(File file : files){
                if(file.isDirectory()){
                    continue;
                }
                if(!file.isFile()){
                    continue;
                }
                row = sheet.createRow(rowIndex);

                cell = row.createCell(1);
                cell.setCellValue(rowNum++);

                cell = row.createCell(2);
                cell.setCellValue(file.getParentFile().getAbsolutePath().substring(removeLength));

                cell = row.createCell(3);
                cell.setCellValue(file.getName());

                cell = row.createCell(4);
                cell.setCellValue(FileUtil.getExtension(file.getName()));

                rowIndex++;
            }

            for (int i = 0; i <5 ; i++) {
                sheet.autoSizeColumn(i);
            }

            ExcelUtils.write(workbook, outPath);

        }catch (Exception e){
            log.error(ExceptionUtil.getStackTrace(e));
        }

    }


    public static void main(String[] args){
        OutDirFileInfoToExcel outDirFileInfoToExcel = new OutDirFileInfoToExcel();
        outDirFileInfoToExcel.out("C:\\project\\mr-voc-master\\src","D:\\files.xlsx");

//        File f = new File("D:\\test.xls");
//        System.out.println(f.getAbsolutePath());

    }
}
