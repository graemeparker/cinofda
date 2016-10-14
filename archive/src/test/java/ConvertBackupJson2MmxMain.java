import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.model.v1.JsonBackupLogReader;
import net.byyd.archive.transform.MVELTransformer;
import net.byyd.archive.transform.OneItemLineFileSink;

/**
 * 
 */
public class ConvertBackupJson2MmxMain {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Need adserver-backuplog-file parameter");
        }
        JsonBackupLogReader reader = new JsonBackupLogReader();
        MVELTransformer<AdEvent> tr = new MVELTransformer<AdEvent>(AdEvent.VERSION);
        OneItemLineFileSink<AdEvent> sink = new OneItemLineFileSink<AdEvent>(tr, System.out);

        File inputFile = new File(args[0]);
        try (InputStream is = new FileInputStream(inputFile)) {
            reader.feed(is, sink);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
