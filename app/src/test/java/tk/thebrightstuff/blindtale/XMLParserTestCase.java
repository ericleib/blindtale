package tk.thebrightstuff.blindtale;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import tk.thebrightstuff.blindtale.tale.Tale;
import tk.thebrightstuff.blindtale.tale.TaleParser;

import static org.junit.Assert.assertTrue;

/**
 * Created by niluje on 07/07/15.
 *
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = "src/main/AndroidManifest.xml")
public class XMLParserTestCase {

    @Test
    public void testXMLParser() throws Exception {
        new TaleParser().parse(new File("/mnt/data/DEV/BlindTale/app/src/main/assets/labyrinth/descriptor.xml"));
    }

}
