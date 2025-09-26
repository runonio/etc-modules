package io.runon.file.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.runon.commons.utils.GsonUtils;
import io.runon.commons.utils.string.Strings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author macle
 */
public class PdfText {


    public static String getPdfSimple(String filePath) {
        PDDocument document = null;
        try{
            document = PDDocument.load(new File(filePath));
            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            try{document.close();}catch (Exception ignore){}
        }
    }


    public static String getPdfOcrSimple(String homeDir, File pdfFile, boolean isDelete) {

        PDDocument document = null;
        List<String> pathList = null;
        String text = null;
        try {

            document = PDDocument.load(pdfFile);
            PDFTextStripper stripper = new PDFTextStripper();

            String tempDirPath = homeDir +"/temp/";
            pathList = new ArrayList<>();
            text = stripper.getText(document);
            int pageCount = document.getNumberOfPages();//pdf의 페이지 수
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for(int i=0;i<pageCount;i++) {

                String tempFileName = System.currentTimeMillis() + "_" + FileTextController.getTempNum();
                String tempFullPath = tempDirPath + tempFileName + ".jpg";
                try {
                    BufferedImage imageObj = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);//pdf파일의 페이지를돌면서 이미지 파일 변환
                    File outputfile = new File(tempFullPath);//파일이름 변경(.pdf->.jpg)
                    ImageIO.write(imageObj, "jpg", outputfile);//변환한 파일 업로드
                    pathList.add(tempFullPath);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            try{document.close();}catch (Exception ignore){}

        }

        OcrPythonMultiShell ocrPythonMultiShell = new OcrPythonMultiShell(homeDir, pathList.toArray(new String[0]), isDelete);
        ocrPythonMultiShell.runToWait();

        StringBuilder sb = new StringBuilder();

        if(ocrPythonMultiShell.getErrorMessage() == null){
            if(text !=null) {
                sb.append(text);
                sb.append("\n");
            }
            sb.append(Strings.toString(GsonUtils.getString(ocrPythonMultiShell.getArray()), " "));

        }else{
            throw new RuntimeException(ocrPythonMultiShell.getErrorMessage());
        }

        try {
            if(isDelete)
                pdfFile.delete();
        }catch (Exception ignore){}

        return sb.toString();
    }


    public static JsonArray getPageArray(File pdfFile, boolean isDelete){
        PDDocument document = null;

        JsonArray pageArray =new JsonArray();
        try {

            document = PDDocument.load(pdfFile);
            PDFTextStripper stripper = new PDFTextStripper();

            int pageCount = document.getNumberOfPages();//pdf의 페이지 수

            for(int i=0;i<pageCount;i++) {

                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(document).trim();
                if(text.isEmpty()){
                    continue;
                }

                JsonObject pageObj = new JsonObject();
                pageObj.addProperty("index", i);
                pageObj.addProperty("text", text);

                pageArray.add(pageObj);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            try{document.close();}catch (Exception ignore){}

        }
        try {
            if(isDelete)
                pdfFile.delete();
        }catch (Exception ignore){}
        return pageArray;
    }

}
