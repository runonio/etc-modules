import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.runon.commons.http.HttpApis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
/**
 * @author macle
 */
public class Test {
    public static void main(String[] args) throws IOException {
        String filePath = "E:\\알바몬_(넥서스,위고).ppt";
        File file = new File(filePath);
        byte[] bytes = Files.readAllBytes(file.toPath());
        String encodeByte = Base64.getEncoder().encodeToString(bytes);

        JsonObject obj = new JsonObject();
        obj.addProperty("file_name", file.getName());
        obj.addProperty("file_bytes", encodeByte);
        String result = HttpApis.POST_JSON.getMessage("http://dev.runon.io:31335/text/list/filejson", new Gson().toJson(obj));

        System.out.println(result);
    }
}
