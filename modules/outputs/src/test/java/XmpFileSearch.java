import io.runon.commons.utils.FileUtil;
import io.runon.commons.outputs.OutProgramTableMapToExcel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

/**
 * @author macle
 */
public class XmpFileSearch {
    public static void main(String[] args) {

        String text = FileUtil.getFileContents(new File("DspcBas_SqlMapper.xml"),"UTF-8");
        Document dom = Jsoup.parse(text);



        Elements itemElements = dom.getElementsByTag("delete");

        for (int i = 0; i <itemElements.size() ; i++) {
            Element element = itemElements.get(i);

            System.out.println(element);
            System.out.println(OutProgramTableMapToExcel.getSelectTable(element));


        }

//        itemElements = dom.getElementsByTag("mapper");
//        for (int i = 0; i <itemElements.size() ; i++) {
//            Element element = itemElements.get(i);
//            System.out.println(element.attribute("namespace").getValue());
//        }
//
//        System.out.println(itemElements.size());
    }
}
