package org.eclipse.recommenders.livedoc.utils;

import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.commons.io.filefilter.FileFileFilter.FILE;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;

public class ZipUtils {

    public static File unzipToSeparateFolder(File archive, File output) throws IOException{
        output = new File(output.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(archive.getName()));
        Files.createParentDirs(output);
        unzip(archive, output);
        return output;
    }
    
    public static void unzip(File archive, File outputDir) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            
            for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if (!entry.isDirectory()) unzipEntry(zipfile, entry, outputDir);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {
        
        File outputFile = new File(outputDir, entry.getName());
        Files.createParentDirs(outputFile);

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }
    
    

    public static void zip(File directory, File out) throws IOException {
        ZipOutputStream zos = null;
        try {
            OutputSupplier<FileOutputStream> s = Files.newOutputStreamSupplier(out);
            zos = new ZipOutputStream(s.getOutput());
            for (File f : FileUtils.listFiles(directory, FILE, DIRECTORY)) {
                String path = removeStart(f.getPath(), directory.getAbsolutePath() + File.separator);
                ZipEntry e = new ZipEntry(path);
                zos.putNextEntry(e);
                byte[] data = Files.toByteArray(f);
                zos.write(data);
                zos.closeEntry();
            }
        } finally {
            Closeables.close(zos, false);
        }
    }
}
