import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import io.runon.commons.config.Config;
import io.runon.file.text.FileText;

/**
 * @author macle
 */
public class TextListOut {
    public static void main(String[] args) {
        Config.getConfig("");

        String filePath = "E:\\알바몬_(넥서스,위고).ppt";

        JsonArray array = FileText.getPageArray("E:\\",filePath,false);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        System.out.println(gson.toJson(array));

    }
}
