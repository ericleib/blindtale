# Blind Tale

A hand-free audiogame engine for Android

## Interactive story-telling

**Tales** are a collection of **Scenes**. A scene has various **Audio** segments that tell a story and various **Actions** that affect the story in various ways. Audio segments can consist of sound **files** (e.g. mp3, midi,...), or **text** (which is read using Android's **Text-To-Speech** capabilities). A scene can have any number of actions, which may trigger any number of events:
 * Play new audio segments.
 * Update a game variable (e.g. keep track of a player's choices, simulate object pickup, etc.)
 * Start a **Dialog**. Dialogs are *scenes within the scenes*, where players can choose between various **Lines**, (which work pretty much as actions do).
 * Transition to another scene.
Audio segments, as well as actions & dialogs can all be set to be **Conditional**, meaning they will only be played if a particular game variable verifies a given condition (e.g. The action *"Slay the dragon"* is available only if the player has performed beforehand the *"Pick-up the sword"* action).
Actions and Lines can be performed by clicking the corresponding button on the screen, or by *speaking* a keyword to the microphone.

## Voice recognition

Blind Tale makes use of [**PocketSphinx**](https://github.com/cmusphinx/pocketsphinx) for offline voice recognition. Currently, the French and English languages are supported.

## Architecture

Blind Tale is cleanly divided into three parts for enhanced modularity, interoperability and extendibility.
 * **Tale files**: These are what defines a tale. They consist at least of a **descriptor**, as well as any number of sound files. The descriptor is a xml file describing the story (Scenes, Audio segments, Actions, Dialogs, Conditions, Game variables). See below how to write a descriptor.
 * **Game Engine**: The engine (or *"Controller"*) is responsible for reading the descriptor, building the corresponding Java classes and wiring them up (= creating the *model*), and ultimately "run" the game, (chaining the scenes, actions, etc.) responding to user input.
 * **Tale View**: A tale view is in charge of interacting with the player (displaying buttons, playing sounds, listening to voice and clicks as ordered by the controller). A view can be implemented on any system supporting Java (a simplistic [Command-line view](https://github.com/ericleib/blindtale/blob/master/app/src/main/java/tk/thebrightstuff/blindtale/view/CliTaleView.java) is provided to give an idea). The [Android view](https://github.com/ericleib/blindtale/blob/master/app/src/main/java/tk/thebrightstuff/blindtale/view/SceneActivity.java) is an *Activity* class, which is in charge of providing the Controller with the handles for voice recognition, media player, text-to-speech, and for displaying a dynamic UI for each scene and dialog.

## Writing a descriptor

Descriptors come as XML files, providing the content of the game. An [example descriptor](https://github.com/ericleib/blindtale/blob/master/app/src/main/assets/labyrinth/descriptor.xml) is provided for inspiration. The syntax and wording are hopefully self-explanatory, but information can be provided on demand.
The [Simple XML framework](http://simple.sourceforge.net/) is used to build the model from the XML descriptor. This means that changing or extending the current model can be achieved pretty quickly just by tuning the existing classes in the [tale package](https://github.com/ericleib/blindtale/tree/master/app/src/main/java/tk/thebrightstuff/blindtale/tale). No need to put your hands in a dirty XML parser! However, avoid breaking the current *TaleView* interface which could possibly have deeper impacts (on the views and controller).

## Testing a descriptor

You can first test your tale with the Command-line view (without needing to run the app in Android, just running it locally on a JVM), with just a few lines of code:

    public static void main(String[] args) throws Exception {
        Tale tale = new TaleParser().parse(new File("/path/to/descriptor.xml"));
        TaleView view = new CliTaleView(false);
        Map<String,String> state = new HashMap<String,String>();
        new Controller(tale.getScene(), state, view).startScene();
    }

To test the tale on an Android device, the descriptor (and sound files) must be stored in the application's assets, and the application deployed to the phone. The tale will then be available in the dropdown menu when the application is launched.
**Note:** This solution is only temporary. In the future, new tales will be downloadable from the app itself, without needing to perform any technical operation, besides uploading the tale to a store.
