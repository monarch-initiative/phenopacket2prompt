package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.biodownload.BioDownloader;
import org.monarchinitiative.biodownload.FileDownloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Download a number of files needed for the analysis. We download by default to a subdirectory called
 * {@code data}, which is created if necessary. We download the files {@code hp.obo}, {@code phenotype.hpoa},
 * {@code Homo_sapiencs_gene_info.gz}, and {@code mim2gene_medgen}.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */

@CommandLine.Command(name = "download",
        mixinStandardHelpOptions = true,
        description = "Download files for phenopacket2prompt")
public class DownloadCommand implements Callable<Integer>{
    private static final Logger logger = LoggerFactory.getLogger(DownloadCommand.class);
    @CommandLine.Option(names={"-d","--data"}, description ="directory to download data (default: ${DEFAULT-VALUE})" )
    public Path datadir = Path.of("data");

    @CommandLine.Option(names={"-w","--overwrite"}, description = "overwrite previously downloaded files (default: ${DEFAULT-VALUE})")
    public boolean overwrite = false;

    @Override
    public Integer call() throws FileDownloadException, MalformedURLException {
        logger.info(String.format("Download analysis to %s", datadir));
        URL hpoInternational = new URL("https://github.com/obophenotype/human-phenotype-ontology/releases/latest/download/hp-international.obo");
        BioDownloader downloader = BioDownloader.builder(datadir)
                .overwrite(overwrite)
                .hpoJson()
                .custom("hp-international.obo", hpoInternational)
                .build();
        downloader.download();
        return 0;
    }

}
