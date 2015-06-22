package tk.thebrightstuff.blindtale;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by niluje on 17/06/15.
 */
public class Tale implements Serializable {

    private final static String DESCRIPTOR = "descriptor.xml";
    private final static String TAG = "Tale";

    private File folder;

    private String title;

    private Scene scene;

    public String toString() {
        return title;
    }

    public Scene getScene() {
        return scene;
    }

    public File getTaleFolder(){
        return folder;
    }

    /**
     * Parse the provided descriptor file using XmlPullParser
     * @param descriptor
     * @throws IOException
     */
    public void readFromXML(File descriptor) throws IOException {
        Log.v(TAG, "Reading descriptor :"+descriptor.getAbsolutePath());
        InputStream in = new FileInputStream(descriptor);
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "tale");
            String entryScene = null;
            Map<String,Scene> scenes = new HashMap<String,Scene>();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                switch (name) {
                    case "title": this.title = readTxtTag(parser, name); break;
                    case "entry-scene" : entryScene = readTxtTag(parser, name); break;
                    case "scenes": readScenesTag(parser, scenes, name); break;
                    default: throw new Exception("Unexpected tag in descriptor: "+name);
                }
            }

            if(scenes.containsKey(entryScene))
                this.scene = scenes.get(entryScene);
            else
                throw new Exception("Entry scene could not be found: "+entryScene);

            if(title==null)
                throw new Exception("Title could not be found in descriptor");

            for(Scene s : scenes.values()){
                for(Action a: s.actions){
                    if(scenes.containsKey(a.nextSceneId))
                        a.nextScene = scenes.get(a.nextSceneId);
                    else
                        throw new Exception("Action points to unknown scene: "+a.nextSceneId);
                }
            }

        }catch(Exception e){
            Log.e(TAG, "Could not properly read descriptor: "+descriptor.getAbsolutePath(), e);
        } finally {
            if(in!=null)
                in.close();
        }
    }

    private void readScenesTag(XmlPullParser parser, Map<String, Scene> scenes, String tag) throws Exception {
        parser.require(XmlPullParser.START_TAG, null, tag);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("scene")) {
                Scene scene = readSceneTag(parser, name);
                scenes.put(scene.id, scene);
            } else {
                throw new Exception("Unexpected tag: "+name);
            }
        }
    }

    private Scene readSceneTag(XmlPullParser parser, String tag) throws Exception {
        parser.require(XmlPullParser.START_TAG, null, tag);

        Scene scene = new Scene();

        scene.tale = this;
        scene.id = parser.getAttributeValue(null, "id");
        scene.soundPath = parser.getAttributeValue(null, "sound");

        if(scene.id==null || scene.soundPath==null)
            throw new Exception("Scene must have an id and a sound path");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("action")) {
                Action action = readActionTag(parser, name);
                scene.actions.add(action);
            } else {
                throw new Exception("Unexpected tag: "+name);
            }
        }

        return scene;
    }

    private Action readActionTag(XmlPullParser parser, String tag) throws Exception {
        parser.require(XmlPullParser.START_TAG, null, tag);

        Action action = new Action();

        String[] keys = parser.getAttributeValue(null, "keys").split(",");
        for(String key : keys)
            action.keys.add(key);

        if(action.keys.size()==0)
            throw new Exception("Action must have at least one key");

        action.nextSceneId = parser.getAttributeValue(null, "next-scene");

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, tag);

        return action;
    }

    private String readTxtTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String txt = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return txt;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    /**
     * Static method to read all tales from internal storage
     * @param rootDir
     * @return
     */
    public static Tale[] getAvailableTales(File rootDir){
        Log.v(TAG, "Scanning app folder to look for tales... "+rootDir.getAbsolutePath());
        File[] dirs = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().matches("app_\\w+");
            }
        });
        Tale[] tales = new Tale[dirs.length];
        for(int i=0; i<tales.length; i++){
            Log.v(TAG, "Found Tale folder: "+ dirs[i]);
            tales[i] = new Tale();
            tales[i].folder = dirs[i];
            File descr = new File(tales[i].folder, DESCRIPTOR);
            if(descr.exists())
                try {
                    tales[i].readFromXML(descr);
                } catch (IOException e) {
                    Log.e(TAG, "Error reading the descriptor file: "+descr.getAbsolutePath(), e);
                }
            else
                Log.e(TAG, "Could not find a descriptor in :"+tales[i].folder.getAbsolutePath());
        }
        return tales;
    }

}
