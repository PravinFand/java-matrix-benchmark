package jmbench.tools.stability;

import com.thoughtworks.xstream.XStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * @author Peter Abeles
 */
public class UtilXmlSerialization {
    public static void serializeXml( Object o , String fileName ) {
        XStream xstream = new XStream();

        try {
            xstream.toXML(o,new FileOutputStream(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserializeXml( String fileName ) {
        XStream xstream = new XStream();

        try {
            return (T)xstream.fromXML(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
