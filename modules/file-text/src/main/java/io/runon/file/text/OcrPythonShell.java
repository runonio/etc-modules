package io.runon.file.text;

import io.runon.commons.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * api 서버 시작
 * @author macle
 */
@Slf4j
public class OcrPythonShell {


    public static OcrText analysis(String ocrHome, String filePullPath, boolean isDelete) throws InterruptedException, IOException {
        List<String> commandList =  new ArrayList<>();
        commandList.add(ocrHome +"/bin/python");
        commandList.add(ocrHome +"/ocr_out.py");
        commandList.add(filePullPath);


        AtomicBoolean isAnalysis = new AtomicBoolean(false);
        ProcessBuilder builder = new ProcessBuilder(commandList);

        final Process process = builder.start();

        final String [] message = new String[1];

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line ;

                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("ocr json text:")) {
                        message[0] = line.substring("ocr json text:".length()).trim();
                        break;
                    }
                }

            } catch (Exception e) {
                log.error(ExceptionUtil.getStackTrace(e));
            } finally {
                isAnalysis.set(true);
            }
        }).start();


        final StringBuilder errorBuilder = new StringBuilder();
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line ;

                while ((line = reader.readLine()) != null) {
                    errorBuilder.append(line).append("\n");
                }

            } catch (Exception e) {
                log.error(ExceptionUtil.getStackTrace(e));
            }

            isAnalysis.set(true);

        }).start();

        long watiSum = 0;

        while (!isAnalysis.get()) {
            watiSum += 500;
            Thread.sleep(500);

            if(watiSum > 5000 && errorBuilder.length() > 0){
                break;
            }
        }

        synchronized (process) {
            process.wait();
            process.destroy();
        }

        if(isDelete) {
            try {
                new File(filePullPath).delete();
            } catch (Exception ignore) {
            }
        }
        if(message[0] == null && errorBuilder.length() > 0){
            return new OcrText(OcrText.Type.ERROR, errorBuilder.toString());
        }
        return new OcrText(OcrText.Type.SUCCESS, message[0]);
    }




}
