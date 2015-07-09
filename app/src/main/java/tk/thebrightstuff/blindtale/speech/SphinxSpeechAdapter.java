package tk.thebrightstuff.blindtale.speech;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import tk.thebrightstuff.blindtale.utils.Callback;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by niluje on 25/06/15.
 *
 */
public class SphinxSpeechAdapter implements SpeechAdapter, RecognitionListener {

    private static final String SEARCH = "search";
    private static SpeechRecognizer recognizer;
    private static RecognitionListener recognitionListener;
    private static File keywordFile;

    public static void initialize(final Context context, final SpeechResource resource, final Callback<String> callback) {

        final String data, dict;
        if(resource.getLang().equals(Locale.FRENCH)){
            dict = "frenchWords62K.dic";
            data = "lium_french_f2";
        }else if(resource.getLang().equals(Locale.ENGLISH)){
            dict = "cmudict-en-us.dict";
            data = "en-us-ptm";
        }else {
            dict = null;
            data = null;
        }

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {

                    if(recognizer==null) {   // Avoid multiple initializations
                        keywordFile = writeKeywordFile(resource.getKeywords(), context);
                        Assets assets = new Assets(context);
                        File assetDir = assets.syncAssets();
                        setupRecognizer(assetDir, data, dict);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    callback.callback(null, result);
                } else {
                    callback.callback("Speech recognition ready", null);
                    //startListening();
                    //SphinxSpeechAdapter.this.listener.onReadyForSpeech();
                }
            }
        }.execute();
    }

    private static void setupRecognizer(File assetsDir, String data, String dict) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, data))
                .setDictionary(new File(assetsDir, dict))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                        //.setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();

        // Create keyword-activation search.
        recognizer.addKeywordSearch(SEARCH, keywordFile);

    }

    public static void cleanup(){
        if(recognizer!=null) {
            recognizer.shutdown();
            recognizer = null;
        }
    }


    private static File writeKeywordFile(Set<String> keywords, Context context) throws Exception {
        FileOutputStream os = context.openFileOutput("sphinx-temp-file.txt", Context.MODE_PRIVATE);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        for(String keyword : keywords){
            bw.write(keyword+" /1.0/");
            bw.newLine();
        }
        bw.close();
        File tmpFile = new File(context.getFilesDir(), "sphinx-temp-file.txt");
        if(!tmpFile.exists())
            throw new Exception("Temp file does not exist...");
        return tmpFile;
    }





    private SpeechListener listener;

    public SphinxSpeechAdapter(){
        if(recognitionListener!=null)
            recognizer.removeListener(recognitionListener);
        recognizer.addListener(this);
        recognitionListener = this; // Trick to make sure there's always only one recognition listener
    }

    @Override
    public void setSpeechListener(SpeechListener listener){
        this.listener = listener;
    }

    @Override
    public void destroy() {
        if(isAvailable()) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void startListening() {
        if(isAvailable()){
            recognizer.cancel();
            recognizer.startListening(SEARCH);
            listener.onReadyForSpeech();
        }
    }

    @Override
    public void stopListening() {
        if(isAvailable()){
            recognizer.cancel();
        }
    }

    @Override
    public boolean isAvailable() {
        return recognizer!=null;
    }



    @Override
    public void onBeginningOfSpeech() {
        listener.onBeginningOfSpeech();
    }

    @Override
    public void onEndOfSpeech() {
        listener.onEndOfSpeech();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if(hypothesis!=null){
            List<String> list = new ArrayList<>();
            list.add(hypothesis.getHypstr());
            listener.onPartialResults(list);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis!=null){
            List<String> list = new ArrayList<>();
            list.add(hypothesis.getHypstr());
            listener.onPartialResults(list);
        }
    }

    @Override
    public void onError(Exception e) {
        listener.onError(e.getLocalizedMessage());
        startListening();
    }

    @Override
    public void onTimeout() {
        startListening();
    }
}
