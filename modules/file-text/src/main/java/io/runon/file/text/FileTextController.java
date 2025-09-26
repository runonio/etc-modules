package io.runon.file.text;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.runon.commons.config.Config;
import io.runon.commons.utils.ExceptionUtil;
import io.runon.commons.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author macle
 */
@Slf4j
@RestController
public class FileTextController {

    public static final Object TEMP_NUM_LOCK = new JSONObject();

    private static int TEMP_NUM =0;

    public static int getTempNum(){
        synchronized (TEMP_NUM_LOCK){
            TEMP_NUM++;
            if(TEMP_NUM < 1 || TEMP_NUM > 999999999){
                TEMP_NUM = 1;
            }
            return TEMP_NUM;
        }
    }

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @RequestMapping(value = "/text/file" , method = RequestMethod.POST, produces= MediaType.MULTIPART_FORM_DATA_VALUE)
    public String fileText(@RequestPart(value="file") MultipartFile file){
        FileOutputStream outStream = null;
        InputStream inputStream = null;
        try{
            String originalName = file.getOriginalFilename();
            String extension = FileUtil.getExtension(originalName).toLowerCase();


            String homeDir = Config.getConfig("home.dir");
            String tempDirPath = homeDir +"/temp/";

            String tempFileName = System.currentTimeMillis()+"_"+getTempNum() + "." + extension;
            String fileFullPath = tempDirPath + tempFileName ;

            outStream = new FileOutputStream(fileFullPath);
            inputStream = file.getInputStream();
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            try{outStream.getFD().sync();}catch (Exception ignore){}
            try{outStream.close();}catch (Exception ignore){}
            try{inputStream.close();}catch (Exception ignore){}



            String text = FileText.getTextSimple(homeDir, fileFullPath, true);

            JsonObject response = new JsonObject();
            response.addProperty("code", "1");
            response.addProperty("text", text);

            return gson.toJson(response);

        }catch (Exception e){
            JsonObject response = new JsonObject();
            response.addProperty("code", "-1");
            response.addProperty("error_message", ExceptionUtil.getStackTrace(e));
            return gson.toJson(response);
        }finally {
            try{outStream.close();}catch (Exception ignore){}
            try{inputStream.close();}catch (Exception ignore){}
        }
    }
    @RequestMapping(value = "/text/filejson", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public String fileJson(@RequestBody final String jsonValue){
        FileOutputStream fos = null;
        try {
            JSONObject object = new JSONObject(jsonValue);
            String originalName = object.getString("file_name");
            String extension = FileUtil.getExtension(originalName).toLowerCase();

            String homeDir = Config.getConfig("home.dir");
            String tempDirPath = homeDir +"/temp/";

            String tempFileName = System.currentTimeMillis()+"_"+getTempNum() + "." + extension;
            String fileFullPath = tempDirPath + tempFileName ;

            String byteDataEncode = object.getString("file_bytes");

            byte[] bytes = Base64.getDecoder().decode(byteDataEncode);

            fos = new FileOutputStream(fileFullPath);
            fos.write(bytes);
            try{fos.getFD().sync();}catch (Exception ignore){}
            try{fos.close();}catch (Exception ignore){}


            String text = FileText.getTextSimple(homeDir, fileFullPath, true);

            JsonObject response = new JsonObject();
            response.addProperty("code", "1");
            response.addProperty("text", text);

            return gson.toJson(response);

        }catch (Exception e){
            JsonObject response = new JsonObject();
            response.addProperty("code", "-1");
            response.addProperty("error_message", ExceptionUtil.getStackTrace(e));
            return gson.toJson(response);
        }finally {
            try{fos.close();}catch (Exception ignore){}
        }
    }


    @RequestMapping(value = "/text/ocrjson" , method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public String ocrJson(@RequestBody final String jsonValue){
        FileOutputStream fos = null;
        try {
            JSONObject object = new JSONObject(jsonValue);

            String fileName = object.getString("file_name");
            String byteDataEncode = object.getString("file_bytes");

            byte[] bytes = Base64.getDecoder().decode(byteDataEncode);

            String ocrHome = Config.getConfig("home.dir");
            String tempDirPath = ocrHome + "/temp/";

            String tempFileName = System.currentTimeMillis() + "_" + getTempNum();

            String tempFullPath = tempDirPath + tempFileName;

            fos = new FileOutputStream(tempFullPath);
            fos.write(bytes);
            try{fos.getFD().sync();}catch (Exception ignore){}
            try{fos.close();}catch (Exception ignore){}

            if(fileName != null && fileName.endsWith(".pdf")){
                return getPdfOcr(ocrHome, new File(tempFullPath));
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            OcrText ocrText = OcrPythonShell.analysis(ocrHome, tempFullPath, true);
            JsonObject response = new JsonObject();

            if(ocrText.getType() == OcrText.Type.SUCCESS){
                response.addProperty("code", "1");
                response.add("ocr_text", gson.fromJson(ocrText.getText(), JsonArray.class));
            }else{
                response.addProperty("code", "-2");
                response.addProperty("error_message", ocrText.getText());
            }

            return gson.toJson(response);

        }catch (Exception e){
            JSONObject response = new JSONObject();
            response.put("code", "-1");
            response.put("message", ExceptionUtil.getStackTrace(e));
            return response.toString();
        }finally {
            try{fos.close();}catch (Exception ignore){}
        }
    }

    @RequestMapping(value = "/text/ocr" , method = RequestMethod.POST, produces= MediaType.MULTIPART_FORM_DATA_VALUE)
    public String ocr(@RequestPart(value="file") MultipartFile file){
        FileOutputStream outStream = null;
        InputStream inputStream = null;
        try{

            String homeDir = Config.getConfig("home.dir");
            String tempDirPath = homeDir +"/temp/";

            String tempFileName = System.currentTimeMillis()+"_"+getTempNum();

            String tempFullPath = tempDirPath + tempFileName;

            outStream = new FileOutputStream(tempFullPath);
            inputStream = file.getInputStream();
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            try{outStream.getFD().sync();}catch (Exception ignore){}
            try{outStream.close();}catch (Exception ignore){}
            try{inputStream.close();}catch (Exception ignore){}
            String originalName = file.getOriginalFilename();

            if(originalName != null && originalName.endsWith(".pdf")){
                return getPdfOcr(homeDir, new File(tempFullPath));
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            OcrText ocrText = OcrPythonShell.analysis(homeDir, tempFullPath, true);
            JsonObject response = new JsonObject();

            if(ocrText.getType() == OcrText.Type.SUCCESS){
                response.addProperty("code", "1");
                response.add("ocr_text", gson.fromJson(ocrText.getText(), JsonArray.class));
            }else{
                response.addProperty("code", "-2");
                response.addProperty("error_message", ocrText.getText());
            }

            return gson.toJson(response);

        }catch (Exception e){
            JSONObject response = new JSONObject();
            response.put("code", "-1");
            response.put("error_message", ExceptionUtil.getStackTrace(e));
            return response.toString();

        }finally {
            try{outStream.close();}catch (Exception ignore){}
            try{inputStream.close();}catch (Exception ignore){}
        }
    }


    public String getPdfOcr(String homeDir, File pdfFile) throws IOException {

        PDDocument document = PDDocument.load(pdfFile);
        PDFTextStripper stripper = new PDFTextStripper();

        String tempDirPath = homeDir +"/temp/";
        List<String> pathList = new ArrayList<>();
        String text;
        try {
            text = stripper.getText(document);
            int pageCount = document.getNumberOfPages();//pdf의 페이지 수
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for(int i=0;i<pageCount;i++)
            {
                String tempFileName = System.currentTimeMillis() + "_" + getTempNum();
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
            document.close();
        }

        OcrPythonMultiShell ocrPythonMultiShell = new OcrPythonMultiShell(homeDir, pathList.toArray(new String[0]), true);
        ocrPythonMultiShell.runToWait();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject response = new JsonObject();
        if(ocrPythonMultiShell.getErrorMessage() == null){
            response.addProperty("code", "1");
        }else{
            response.addProperty("code", "-2");
            response.addProperty("error_mesage", ocrPythonMultiShell.getErrorMessage());
        }

        response.addProperty("pdf_text", text);
        response.add("ocr_text", ocrPythonMultiShell.getArray());

        try {
            pdfFile.delete();
        }catch (Exception ignore){}


        return gson.toJson(response);
    }

}
