package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Validate;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import tk.thebrightstuff.blindtale.speech.SpeechResource;
import tk.thebrightstuff.blindtale.utils.Log;

/**
 * Created by niluje on 17/06/15.
 *
 */
@Root
public class Tale implements Serializable, SpeechResource {

    private final static String DESCRIPTOR = "descriptor.xml";
    private final static String SAVE_FILE = "save.txt";

    private File folder;

    public File getTaleFolder(){ return folder; }

    public File getSaveFile() {
        if(!new File(folder, "save").exists())
            new File(folder, "save").mkdir();
        return new File(new File(folder, "save"), SAVE_FILE);
    }

    private final Map<String,Scene> sceneMap = new HashMap<>();
    public Scene getScene() { return sceneMap.get(getEntryScene()); }
    public Scene getScene(String id) { return sceneMap.get(id); }

    private final Map<String, Set<String>> keywords = new HashMap<>();
    @Override
    public Map<String, Set<String>> getKeywords() { return keywords; }


    @Validate
    public void validate() throws Exception {
        for(Scene s : scenes)
            if(! sceneMap.containsKey(s.getId()))
                sceneMap.put(s.getId(), s);
            else
                throw new Exception("Duplicate scene id: "+s.getId());

        if(! sceneMap.containsKey(entryScene))
            throw new Exception("Entry scene could not be found: "+entryScene);

        for(Scene s : scenes) {
            keywords.put(s.getId(), new HashSet<String>());
            for (Action a : s.getActionList()){
                keywords.get(s.getId()).addAll(Arrays.asList(a.getSeparatedKeys()));
                if(a.getNextScene()!=null){
                    if(sceneMap.containsKey(a.getNextScene()))
                        a.nextSceneObj = sceneMap.get(a.getNextScene());
                    else
                        throw new Exception("Action points to unknown scene: "+a.getNextScene());
                }
            }
            for (Dialog d : s.getDialogList()){
                keywords.put(s.getId()+"-"+d.getId(), new HashSet<String>());
                for(Line l : d.getLineList()){
                    keywords.get(s.getId()+"-"+d.getId()).addAll(Arrays.asList(l.getSeparatedKeys()));
                    if(l.getNextScene()!=null){
                        if(sceneMap.containsKey(l.getNextScene()))
                            l.nextSceneObj = sceneMap.get(l.getNextScene());
                        else
                            throw new Exception("Dialog line points to unknown scene: "+l.getNextScene());
                    }
                }
            }
            s.tale = this;
        }

        if(getLang().toString().equals(Locale.FRENCH.toString()))
            setLang(Locale.FRENCH);
        else
            throw new Exception("Language not supported: "+getLang().toString());
    }



    @Element
    private String title;
    @Element
    private String entryScene;
    @Element
    private Locale lang;
    @Element
    private Voices voices;
    @ElementList(entry="scene")
    private List<Scene> scenes = new ArrayList<>();
    @ElementList(entry="credit")
    private List<Credit> credits = new ArrayList<>();


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEntryScene() {
        return entryScene;
    }

    public void setEntryScene(String entryScene) {
        this.entryScene = entryScene;
    }

    @Override
    public Locale getLang() {
        return lang;
    }

    public void setLang(Locale lang) {
        this.lang = lang;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public List<Credit> getCredits() {
        return credits;
    }

    public void setCredits(List<Credit> credits) {
        this.credits = credits;
    }

    public Voices getVoices() {
        return voices;
    }

    public void setVoices(Voices voices) {
        this.voices = voices;
    }


    public static class Voices {

        @Attribute(name="default")
        private String defaultVoiceId;
        @ElementList(entry="voice", inline=true)
        private List<Voice> voices = new ArrayList<>();

        private final Map<String,Voice> voiceMap = new HashMap<>();
        public Voice getVoice(String id){ return id!=null && voiceMap.containsKey(id)? voiceMap.get(id) : getDefaultVoice(); }
        public Voice getDefaultVoice(){ return voiceMap.get(getDefaultVoiceId());}

        public List<Voice> getVoices() {
            return voices;
        }

        public void setVoices(List<Voice> voices) {
            this.voices = voices;
        }

        public String getDefaultVoiceId() {
            return defaultVoiceId;
        }

        public void setDefaultVoiceId(String defaultVoice) {
            this.defaultVoiceId = defaultVoice;
        }

        @Validate
        public void validate() throws Exception {
            for(Voice v : getVoices())
                if(!voiceMap.containsKey(v.getId()))
                    voiceMap.put(v.getId(), v);
                else
                    throw new Exception("Duplicate voice: "+v.getId());

            if(!voiceMap.containsKey(defaultVoiceId))
                throw new Exception("Default voice id does not exist: "+defaultVoiceId);
        }
    }



    public String toString() { return getTitle(); }


    /**
     * Static method to read all tales from internal storage
     * @param rootDir Directory in which tales are stored
     * @return array of tales
     */
    public static Tale[] getAvailableTales(File rootDir, Log log) {
        log.info("Tale", "Scanning app folder to look for tales... "+rootDir.getAbsolutePath());
        File[] dirs = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().matches("app_\\w+");
            }
        });
        TaleParser parser = new TaleParser();
        List<Tale> tales = new ArrayList<>();
        for(File dir : dirs){
            log.info("Tale", "Found Tale folder: " + dir.getAbsolutePath());
            File descr = new File(dir, DESCRIPTOR);
            if(descr.exists())
                try {
                    Tale tale = parser.parse(descr);
                    tale.folder = dir;
                    tales.add(tale);
                    log.info("Tale", "Reading descriptor :" + descr.getAbsolutePath());
                } catch (Exception e) {
                    log.error("Tale", "Error reading the descriptor file: "+descr.getAbsolutePath(), e);
                }
            else{
                log.error("Tale", "Could not find a descriptor in :"+dir.getAbsolutePath(), null);
            }
        }
        return tales.toArray(new Tale[tales.size()]);
    }


}
