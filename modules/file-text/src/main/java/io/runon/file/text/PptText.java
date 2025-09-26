package io.runon.file.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.runon.commons.utils.FileUtil;
import io.runon.commons.utils.string.Strings;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author macle
 */
public class PptText {

    public static String getSimpleText(String filePath){
        File file = new File(filePath);
        String extension = FileUtil.getExtension(file).toLowerCase();

        return getSimpleText(filePath, extension);
    }

    public static String getSimpleText(String filePath, String extension){

        if(extension.equals("ppt")){
            POIFSFileSystem fs = null;
            PowerPointExtractor extractor = null;
            try{
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                extractor = new PowerPointExtractor(fs);

                return extractor.getText();
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
                try{extractor.close();}catch (Exception ignore){}
                try{fs.close();}catch (Exception ignore){}
            }

        }else{
            FileInputStream fs =null;
            OPCPackage d = null;
            XSLFPowerPointExtractor xp = null;
            try{
                fs = new FileInputStream(filePath);
                d = OPCPackage.open(fs);
                xp = new XSLFPowerPointExtractor (d);

                return xp.getText();
            }catch (Exception e){
                throw new RuntimeException(e);
            }finally {
                try{xp.close();}catch (Exception ignore){}
                try{d.close();}catch (Exception ignore){}
                try{fs.close();}catch (Exception ignore){}
            }
        }
    }
    public static JsonArray getPageArray(String filePath, String extension){

        SlideShow ppt = null;
        FileInputStream is = null;
        JsonArray pageArray =new JsonArray();
        try{
            is = new FileInputStream(filePath);

            if(extension.equals("ppt")){
                ppt = new HSLFSlideShow(is);


                HSLFSlideShow ppth = (HSLFSlideShow)ppt;
                List<HSLFSlide> list = ppth.getSlides();
                for (int i = 0; i <list.size() ; i++) {
                    HSLFSlide slide = list.get(i);

                    List<String> textList = new ArrayList<>();
                    List<HSLFShape> shapes = slide.getShapes();
                    for(HSLFShape shape : shapes){
                        addText(textList, shape);
                    }

                    String text = Strings.toString(textList, "\n");
                    if(!text.trim().isEmpty()){
                        JsonObject pageObj = new JsonObject();
                        pageObj.addProperty("index", i);
                        pageObj.addProperty("text", text);
                        pageArray.add(pageObj);
                    }
                }


            }else{
                ppt = new XMLSlideShow(is);
                XMLSlideShow pptx = (XMLSlideShow)ppt;
                List<XSLFSlide> list =  pptx.getSlides();
                for (int i = 0; i <list.size() ; i++) {
                    XSLFSlide slide = list.get(i);


                    List<String> textList = new ArrayList<>();

                    List<XSLFShape> shapes = slide.getShapes();
                    for(XSLFShape shape : shapes){
                        addText(textList, shape);
                    }

                    String text = Strings.toString(textList, "\n");
                    if(!text.trim().isEmpty()){
                        JsonObject pageObj = new JsonObject();
                        pageObj.addProperty("index", i);
                        pageObj.addProperty("text", text);
                        pageArray.add(pageObj);
                    }

                }

            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            try{ppt.close();}catch (Exception ignore){}
            try{is.close();}catch (Exception ignore){}
        }


        return pageArray;
    }

    public static void addText(List<String> textList, XSLFShape shape){
        if (shape instanceof XSLFGroupShape) {
            XSLFGroupShape gShapes = (XSLFGroupShape) shape;
            for(XSLFShape sh : gShapes){
                addText(textList, sh);
            }
        }else if (shape instanceof XSLFTextShape) {
            XSLFTextShape textShape = (XSLFTextShape) shape;
            String text =textShape.getText();
            if(text != null && !text.isEmpty()){
                textList.add(text);
            }
        }
    }

    public static void addText(List<String> textList, HSLFShape shape){
        if (shape instanceof HSLFGroupShape) {
            HSLFGroupShape gShapes = (HSLFGroupShape) shape;
            for(HSLFShape sh : gShapes){
                addText(textList, sh);
            }
        }else if (shape instanceof HSLFTextShape) {
            HSLFTextShape textShape = (HSLFTextShape) shape;
            String text =textShape.getText();
            if(text != null && !text.isEmpty()){
                textList.add(text);
            }
        }
    }




    public static void main(String[] args) {
        String text =getSimpleText("D:\\업무\\알바몬_(넥서스,위고).pptx");
        System.out.println(text);
    }
}
