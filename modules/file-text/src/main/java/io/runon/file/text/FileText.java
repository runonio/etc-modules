package io.runon.file.text;

import com.argo.hwp.HwpTextExtractor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.runon.commons.utils.FileUtil;
import io.runon.commons.utils.GsonUtils;
import io.runon.commons.utils.string.Strings;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author macle
 */
public class FileText {

    @SuppressWarnings({"ResultOfMethodCallIgnored", "IfCanBeSwitch"})
    public static String getTextSimple(String homeDir , String filePath, boolean isDelete){

        if (!FileUtil.isFile(filePath)) {
            throw new RuntimeException("file no search: " + filePath);
        }

        File file = new File(filePath);
        String extension = FileUtil.getExtension(file).toLowerCase();

        if(extension.equals("txt") || extension.equals("csv")){
            String content =  FileUtil.getFileContents(filePath);
            if(isDelete){
                file.delete();
            }
            return content;
        }else if(extension.equals("xlsx") || extension.equals("xls") ){
           //엑셀처리
            try{
                String text =  new ExcelText().getSimpleText(filePath);
                if(isDelete){
                    file.delete();
                }
                return text;
            }catch (Exception e){
                throw new RuntimeException(e);
            }

        }else if(extension.equals("docx") || extension.equals("doc") ){
            //doc
            String text = DocText.getSimpleText(filePath, extension);

            if(isDelete){
                file.delete();
            }
            return text;
        }else if(extension.equals("pptx") || extension.equals("ppt") ){
            //power point 처리
            String text = PptText.getSimpleText(filePath, extension);
            if(isDelete){
                file.delete();
            }
            return text;
        }else if(extension.equals("hwp")){
            //hwp 처리
            try{
                File hwp = new File(filePath);
                Writer writer = new StringWriter();
                HwpTextExtractor.extract(hwp, writer);
                String text = writer.toString();
                if(isDelete){
                    file.delete();
                }

                return text;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        } else if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")|| extension.equals("bmp") || extension.equals("gif")){
            //이미지처리
            try {
                OcrText ocrText = OcrPythonShell.analysis(homeDir, filePath, isDelete);
                if (ocrText.getType() == OcrText.Type.SUCCESS) {

                    String [] array = GsonUtils.getString(GsonUtils.fromJsonArray(ocrText.getText()));

                    if(isDelete){
                        file.delete();
                    }

                    return Strings.toString(array, " ");

                } else {
                    throw new RuntimeException(ocrText.getText());
                }

            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }else if(extension.equals("pdf")){
//            String text =PdfText.getPdfOcrSimple(homeDir, new File(filePath), isDelete);
            String text = PdfText.getPdfSimple(filePath);
            if(isDelete){
                file.delete();
            }
            return text;
        } else{
            throw new RuntimeException("file no search: " + filePath);
        }
    }

    public static JsonArray getPageArray(String homeDir , String filePath, boolean isDelete){

        if (!FileUtil.isFile(filePath)) {
            throw new RuntimeException("file no search: " + filePath);
        }

        File file = new File(filePath);
        String extension = FileUtil.getExtension(file).toLowerCase();



        if(extension.equals("txt") || extension.equals("csv")){
            String content =  FileUtil.getFileContents(filePath);
            if(isDelete){
                file.delete();
            }
            JsonArray out = new JsonArray();
            JsonObject row = new JsonObject();
            row.addProperty("index",0);
            row.addProperty("text",content);
            out.add(row);
            return out;
        }else if(extension.equals("xlsx") || extension.equals("xls") ){
            //엑셀처리
            try{

                JsonArray pageArray = new ExcelText().getPageArray(filePath);
                if(isDelete){
                    file.delete();
                }
                return pageArray;
            }catch (Exception e){
                throw new RuntimeException(e);
            }

        }else if(extension.equals("docx") || extension.equals("doc") ){
            //doc
            JsonArray pageArray = DocText.getPageArray(filePath, extension);
            if(isDelete){
                file.delete();
            }
            return pageArray;
        }else if(extension.equals("pptx") || extension.equals("ppt") ){
            //power point 처리
            JsonArray pageArray = PptText.getPageArray(filePath, extension);
            if(isDelete){
                file.delete();
            }

            return pageArray;
        }else if(extension.equals("hwp")){
            //hwp 처리
            try{
                File hwp = new File(filePath);
                Writer writer = new StringWriter();
                HwpTextExtractor.extract(hwp, writer);
                String text = writer.toString();
                if(isDelete){
                    file.delete();
                }
                JsonArray out = new JsonArray();
                JsonObject row = new JsonObject();
                row.addProperty("index",0);
                row.addProperty("text",text);
                out.add(row);

                return out;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        } else if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")|| extension.equals("bmp") || extension.equals("gif")){
            //이미지처리
            try {
                OcrText ocrText = OcrPythonShell.analysis(homeDir, filePath, isDelete);
                if (ocrText.getType() == OcrText.Type.SUCCESS) {

                    String [] array = GsonUtils.getString(GsonUtils.fromJsonArray(ocrText.getText()));

                    if(isDelete){
                        file.delete();
                    }
//                    return Strings.toString(array, " ");
                    JsonArray out = new JsonArray();
                    JsonObject row = new JsonObject();
                    row.addProperty("index",0);
                    row.addProperty("text",Strings.toString(array, " "));
                    out.add(row);

                    return out;

                } else {
                    throw new RuntimeException(ocrText.getText());
                }

            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }else if(extension.equals("pdf")){
//            String text =PdfText.getPdfOcrSimple(homeDir, new File(filePath), isDelete);
            return   PdfText.getPageArray(new File(filePath), isDelete);
        }

        else{
            throw new RuntimeException("file no search: " + filePath);
        }
    }


}
