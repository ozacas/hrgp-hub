/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Base64;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author acassin
 */
public class BootstrapDownloadTableVisitor implements FileVisitor {
    private final StringBuilder sb = new StringBuilder();
    private final String prefix;
    private int n_files = 0;
    
    public BootstrapDownloadTableVisitor() {
        this("");
    }
    
    public BootstrapDownloadTableVisitor(final File root) {
        this(root.getAbsolutePath());
    }
    
    public BootstrapDownloadTableVisitor(String prefix_to_strip) {
        prefix = prefix_to_strip;
    }
    
    @Override
    public void beforeVisit() {
        sb.setLength(0);
        sb.append("<table class=\"table table-striped\">");
        sb.append("<tr>");
        sb.append("<th>Name</th>");
        sb.append("<th>Size (MB)</th>");
        sb.append("<th>Preview</th>");
        sb.append("</tr>");
        n_files = 0;
    }
    
    @Override
    public void afterVisit() {
        sb.append("</table>");
    }
    
    @Override
    public String toString() {
        if (n_files == 0) {
            return "No files are available for download at this time.";
        }
        return sb.toString();
    }
    
    // from http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    public static String encodeFile(final File f) {
        assert(f != null);
        return Base64.getEncoder().encodeToString(f.getAbsolutePath().getBytes());
    }
    
    @Override
    public void process(final File f) {
       
        sb.append("<tr>");
        sb.append("<td>");
        String suffix = f.getAbsolutePath();
        if (suffix.startsWith(prefix)) {
            suffix = suffix.substring(prefix.length());
            while (suffix.startsWith("/")) {
                suffix = suffix.substring(1);
            }
        }
        sb.append("<a href=\"services/FileDownload?file=");
        sb.append(encodeFile(f));
        sb.append("\">");
        sb.append(StringEscapeUtils.escapeHtml4(suffix));
        sb.append("</a>");
        sb.append("</td>");
        sb.append("<td>");
        sb.append(StringEscapeUtils.escapeHtml4(readableFileSize(f.length())));
        sb.append("</td>");
        sb.append("<td></td>");
        sb.append("</tr>");
        n_files++;
    }
}
