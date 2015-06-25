package tk.thebrightstuff.blindtale.speech;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by niluje on 25/06/15.
 *
 */
public class SphinxSpeechAdapter implements SpeechAdapter, RecognitionListener {

    private static final String SEARCH = "search";
    private SpeechRecognizer recognizer;
    private SpeechListener listener;
    private String lang;
    private File keywordFile;

    private String dict, data;

    @Override
    public void initialize(SpeechListener listener, String lang, Set<String> keywords) throws Exception {

        this.listener = listener;
        this.lang = lang.substring(0,2);
        this.keywordFile = writeKeywordFile(keywords);

        switch(this.lang){
            case "fr":
                dict = "frenchWords62K.dic";
                data = "lium_french_f2";
                break;

            case "en":
                dict = "cmudict-en-us.dict";
                data = "en-us-ptm";
                break;

            default: throw new Exception("Unsupported language: "+lang);
        }

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(SphinxSpeechAdapter.this.listener.getContext());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    SphinxSpeechAdapter.this.listener.onError(result.getMessage());
                } else {
                    startListening();
                    SphinxSpeechAdapter.this.listener.onReadyForSpeech();
                }
            }
        }.execute();
    }

    private File writeKeywordFile(Set<String> keywords) throws Exception {
        FileOutputStream os = listener.getContext().openFileOutput("sphinx-temp-file.txt", Context.MODE_PRIVATE);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        for(String keyword : keywords){
            bw.write(keyword+" /1.0/");
            bw.newLine();
        }
        bw.close();
        File tmpFile = new File(listener.getContext().getFilesDir(), "sphinx-temp-file.txt");
        if(!tmpFile.exists())
            throw new Exception("Temp file does not exist...");
        return tmpFile;
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


    private void setupRecognizer(File assetsDir) throws IOException {
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

        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeywordSearch(SEARCH, keywordFile);

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
