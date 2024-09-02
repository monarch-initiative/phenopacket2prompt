package org.monarchinitiative.phenopacket2prompt.output;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.io.*;

/**
 * Class to copy phenopackets from the input directory to an output directory so that we have all of the files
 * used for an experiment in one place.
 */
public class PpktCopy {

    private final File ppkt_out_dir;


    public PpktCopy(File outdirectory) {
        ppkt_out_dir = new File(outdirectory + File.separator + "original_phenopackets");
        createDir(ppkt_out_dir);
    }



    private void createDir(File path) {
        if (! path.exists() ) {
            boolean result = path.mkdir();
            if (! result) {
                throw new PhenolRuntimeException("Could not create output directory at " + path);
            }
        }
    }

    public void copyFile(File sourceLocation) {
        try {
            String fname = sourceLocation.getName();
            File outfile = new File(ppkt_out_dir + File.separator + fname);

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(outfile);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            throw new PhenolRuntimeException(e.getMessage());
        }
    }
}
