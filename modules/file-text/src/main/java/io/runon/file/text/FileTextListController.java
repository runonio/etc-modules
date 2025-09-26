package io.runon.file.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.runon.commons.config.Config;
import io.runon.commons.utils.ExceptionUtil;
import io.runon.commons.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * @author macle
 */
@Slf4j
@RestController
public class FileTextListController {

    @RequestMapping(value = "/text/list/file" , method = RequestMethod.POST, produces= MediaType.MULTIPART_FORM_DATA_VALUE)
    public String fileText(@RequestPart(value="file") MultipartFile file) {
        FileOutputStream outStream = null;
        InputStream inputStream = null;
        try {
            String originalName = file.getOriginalFilename();
            String extension = FileUtil.getExtension(originalName).toLowerCase();


            String homeDir = Config.getConfig("home.dir");
            String tempDirPath = homeDir + "/temp/";

            String tempFileName = System.currentTimeMillis() + "_" + FileTextController.getTempNum() + "." + extension;
            String fileFullPath = tempDirPath + tempFileName;

            outStream = new FileOutputStream(fileFullPath);
            inputStream = file.getInputStream();
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            try {
                outStream.getFD().sync();
            } catch (Exception ignore) {
            }
            try {
                outStream.close();
            } catch (Exception ignore) {
            }
            try {
                inputStream.close();
            } catch (Exception ignore) {
            }


            JsonArray pageArray  = FileText.getPageArray(homeDir, fileFullPath, true);

            JsonObject response = new JsonObject();
            response.addProperty("code", "1");
            response.add("outs", pageArray);

            return FileTextController.gson.toJson(response);

        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("code", "-1");
            response.addProperty("error_message", ExceptionUtil.getStackTrace(e));
            return FileTextController.gson.toJson(response);
        } finally {
            try {
                outStream.close();
            } catch (Exception ignore) {
            }
            try {
                inputStream.close();
            } catch (Exception ignore) {
            }
        }
    }

    @RequestMapping(value = "/text/list/filejson", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public String fileJson(@RequestBody final String jsonValue){
        FileOutputStream fos = null;
        try {
            JSONObject object = new JSONObject(jsonValue);
            String originalName = object.getString("file_name");
            String extension = FileUtil.getExtension(originalName).toLowerCase();

            String homeDir = Config.getConfig("home.dir");
            String tempDirPath = homeDir +"/temp/";

            String tempFileName = System.currentTimeMillis()+"_"+FileTextController.getTempNum() + "." + extension;
            String fileFullPath = tempDirPath + tempFileName ;

            String byteDataEncode = object.getString("file_bytes");

            byte[] bytes = Base64.getDecoder().decode(byteDataEncode);

            fos = new FileOutputStream(fileFullPath);
            fos.write(bytes);
            try{fos.getFD().sync();}catch (Exception ignore){}
            try{fos.close();}catch (Exception ignore){}


            JsonArray pageArray  = FileText.getPageArray(homeDir, fileFullPath, true);

            JsonObject response = new JsonObject();
            response.addProperty("code", "1");
            response.add("outs", pageArray);

            return FileTextController.gson.toJson(response);

        }catch (Exception e){
            JsonObject response = new JsonObject();
            response.addProperty("code", "-1");
            response.addProperty("error_message", ExceptionUtil.getStackTrace(e));
            return FileTextController.gson.toJson(response);
        }finally {
            try{fos.close();}catch (Exception ignore){}
        }
    }
}
