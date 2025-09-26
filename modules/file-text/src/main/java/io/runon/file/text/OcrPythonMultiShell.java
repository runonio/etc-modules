package io.runon.file.text;

import com.google.gson.JsonArray;
import io.runon.commons.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

/**
 * @author macle
 */
@Slf4j
public class OcrPythonMultiShell {
    private final String [] filePaths;
    private final String ocrHome;

    private  boolean isDelete = true;
    public OcrPythonMultiShell(String ocrHome, String [] filePaths){
        this.ocrHome = ocrHome;
        this.filePaths = filePaths;
    }
    public OcrPythonMultiShell(String ocrHome, String [] filePaths, boolean isDelete){
        this.ocrHome = ocrHome;
        this.filePaths = filePaths;
        this.isDelete = isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    private final Object lock = new Object();

    final JsonArray array = new JsonArray();
    String errorMessage = null;

    String [] results;


    public void runToWait(){

        for (int i = 0; i <filePaths.length ; i++) {
            final int index = i;
            results = new String[filePaths.length];

            new Thread(() -> {
                OcrText ocrText;
                try{
                    ocrText = OcrPythonShell.analysis(ocrHome, filePaths[index], isDelete);
                    ocrText.setIndex(index);
                    complete(ocrText);
                }catch (Exception e){
                    synchronized (lock){

                        errorMessage = ExceptionUtil.getStackTrace(e);
                        completeCount++;
                    }
                }
            }).start();
        }


        for(;;){
            if(completeCount >= filePaths.length){
                break;
            }
            try{Thread.sleep(500);}catch (Exception ignore){}
        }

        for (String result : results) {
            if (result != null) {
                JSONArray ocrArray = new JSONArray(result);
                for (int ij = 0; ij < ocrArray.length(); ij++) {
                    array.add(ocrArray.getString(ij));
                }
            }
        }
    }


    private int completeCount = 0;
    public void complete(OcrText ocrText){
        synchronized (lock){
            try{
                if(ocrText.getType() == OcrText.Type.SUCCESS){
                    results[ocrText.index] = ocrText.getText();


                }else{
                    errorMessage = ocrText.getText();
                }

            }catch (Exception e){
                errorMessage = ExceptionUtil.getStackTrace(e);
            }
            completeCount++;

        }


    }

    public JsonArray getArray() {
        return array;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
