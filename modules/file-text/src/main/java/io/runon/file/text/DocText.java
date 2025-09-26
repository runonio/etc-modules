package io.runon.file.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.runon.commons.utils.FileUtil;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author macle
 */
public class DocText {

    public static String getSimpleText(String filePath){
        File file = new File(filePath);
        String extension = FileUtil.getExtension(file).toLowerCase();

        return getSimpleText(filePath, extension);
    }

    public static String getSimpleText(String filePath, String extension){

        if(extension.equals("doc")){
            POIFSFileSystem fs = null;
            HWPFDocument doc = null;
            WordExtractor we = null;
            try{
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                doc = new HWPFDocument(fs);
                we = new WordExtractor(doc);

                return we.getText();
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
                try{we.close();}catch (Exception ignore){}
                try{doc.close();}catch (Exception ignore){}
                try{fs.close();}catch (Exception ignore){}
            }

        }else{
            FileInputStream fs =null;
            OPCPackage d = null;
            XWPFWordExtractor xw= null;
            try{
                fs = new FileInputStream(filePath);
                d = OPCPackage.open(fs);
                xw = new XWPFWordExtractor(d);

                return xw.getText();
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
                try{xw.close();}catch (Exception ignore){}
                try{d.close();}catch (Exception ignore){}
                try{fs.close();}catch (Exception ignore){}
            }
        }
    }

    public static JsonArray getPageArray(String filePath, String extension){
        JsonArray pageArray =new JsonArray();
        if(extension.equals("doc")){
            POIFSFileSystem fs = null;
            HWPFDocument doc = null;

            try{
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                doc = new HWPFDocument(fs);
                Range range = doc.getRange();

                StringBuilder sb = new StringBuilder();

                int pageIndex = 0;
                int size = range.numParagraphs();
                for (int i = 0; i < size; i++) {
                    Paragraph paragraph = range.getParagraph(i);
                    String text = paragraph.text();
                    if(text != null && !text.isEmpty()){
                        sb.append("\n").append(text);
                    }

                    if (paragraph.pageBreakBefore()) {
                        if(sb.length() > 0) {
                            JsonObject pageObj = new JsonObject();
                            pageObj.addProperty("index", pageIndex);
                            pageObj.addProperty("text", sb.substring(1));
                            pageArray.add(pageObj);
                            sb.setLength(0);
                        }

                        pageIndex++;
                    }

                }
                if(sb.length() > 0) {
                    JsonObject pageObj = new JsonObject();
                    pageObj.addProperty("index", pageIndex);
                    pageObj.addProperty("text", sb.substring(1));
                    pageArray.add(pageObj);
                    sb.setLength(0);
                }

                return pageArray;
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
                try{doc.close();}catch (Exception ignore){}
                try{fs.close();}catch (Exception ignore){}
            }

        }else{
            FileInputStream fs =null;
            XWPFDocument document = null;
            try{
                fs = new FileInputStream(filePath);
                document =  new XWPFDocument(fs);

                int pageIndex =0 ;
                StringBuilder sb = new StringBuilder();

                for (XWPFParagraph paragraph : document.getParagraphs()) {

                    String text = paragraph.getText();

                    if(text != null && !text.isEmpty()){

                        sb.append("\n").append(text);

                    }

                    if (paragraph.isPageBreak()) {
                        if(sb.length() > 0) {
                            JsonObject pageObj = new JsonObject();
                            pageObj.addProperty("index", pageIndex);
                            pageObj.addProperty("text", sb.substring(1));
                            pageArray.add(pageObj);
                            sb.setLength(0);
                        }

                        pageIndex++;
                    }

                }

                if(sb.length() > 0) {
                    JsonObject pageObj = new JsonObject();
                    pageObj.addProperty("index", pageIndex);
                    pageObj.addProperty("text", sb.substring(1));
                    pageArray.add(pageObj);
                    sb.setLength(0);
                }


                return pageArray;
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
                try{document.close();}catch (Exception ignore){}
                try{fs.close();}catch (Exception ignore){}
            }
        }
    }


}
