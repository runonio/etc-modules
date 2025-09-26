package io.runon.file.text;

import com.argo.hwp.HwpTextExtractor;
import io.runon.commons.config.Config;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
/**
 * @author macle
 */
public class HwpText {
    public static void main(String[] args) throws IOException {
        Config.getConfig("");

        File hwp = new File("D:\\업무\\문서\\업무보고\\(지란지교소프트)20211221_작업계획서_설치.hwp");
        Writer writer = new StringWriter();
        HwpTextExtractor.extract(hwp, writer);
        String text = writer.toString();
        System.out.println(text);

    }
}
