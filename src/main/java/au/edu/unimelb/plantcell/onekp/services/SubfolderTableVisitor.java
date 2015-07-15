/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author acassin
 */
public class SubfolderTableVisitor implements FileVisitor {
    private final static Logger log = Logger.getLogger("SubfolderTableVisitor");
    
    private final HashMap<File,List<File>> dir2files = new HashMap<>();
    private String prefix;
    private int n_carousels;
    private int n_large_tables;
    
    public SubfolderTableVisitor() {
        this("");
    }
    
    public SubfolderTableVisitor(File f) {
        this(f.getAbsolutePath());
    }
    
    public SubfolderTableVisitor(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public void beforeVisit() {
        dir2files.clear();
        n_carousels = 1;
        n_large_tables = 1;
    }
    
    @Override
    public void afterVisit() {
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ArrayList<File> image_files = new ArrayList<>();
        
        if (dir2files.keySet().isEmpty()) {
            sb.append("<p>No files available for download.</p>");
            return sb.toString();
        }
        
        log.log(Level.INFO, "Found {0} folders to scan for suitable downloads", new Object[] { dir2files.keySet().size() });
        
        for (File folder : dir2files.keySet()) {
            List<File> files = dir2files.get(folder);
            if (files.isEmpty()) {
                log.log(Level.INFO, "Found no files for {0}... ignoring", new Object[] { folder.getAbsolutePath() });
                continue;
            }
            log.log(Level.INFO, "Processing folder {0}", new Object[] { folder.getAbsolutePath() });
                 
            ArrayList<File> other_files = new ArrayList<>();
            separateImagesFromOtherFiles(files, image_files, other_files);
            
            if (other_files.size() < 1) {
                log.log(Level.INFO, "Zero other files but found {0} image files in {1}", 
                                        new Object[] { image_files.size(), folder.getAbsolutePath()});
                continue;
            }
            log.log(Level.INFO, "File sizes: {0} {1} {2}", new Object[] { files.size(), image_files.size(), other_files.size() });
            
            String suffix = folder.getAbsolutePath();
            if (suffix.startsWith(prefix)) {
                suffix = suffix.substring(prefix.length());
                while (suffix.startsWith("/")) {
                    suffix = suffix.substring(1);
                }
            }
            String heading = StringEscapeUtils.escapeHtml4(suffix);
            if (heading.length() > 0) {
                 sb.append("<h3>");
                 sb.append(heading); 
                 sb.append("</h3>");
            } else {
                 if (dir2files.keySet().size() > 1) {
                    sb.append("<h3>/</h3>");
                 }
            }
           
            boolean make_collapsed_table = (other_files.size() > 20);
            if (make_collapsed_table) {
                 String table_id = "table" + n_large_tables++;
                 sb.append("<button type=\"button\" class=\"btn btn-info\" data-toggle=\"collapse\" data-target=\"#"+table_id+"\">Show large table</button>");
                 sb.append("<div id=\""+table_id+"\" class=\"collapse\">");
            }
            sb.append("<table class=\"table table-condensed\">");
            sb.append("<tr>");
            sb.append("<th>Name</th>");
            sb.append("<th>Size</th>");
            sb.append("<th>Last modified</th>");
            sb.append("</tr>");
            
            for (File f : other_files) {
                sb.append("<tr>");
                sb.append("<td>");
                suffix = f.getName();
                if (suffix.startsWith(prefix)) {
                    suffix = suffix.substring(prefix.length());
                    while (suffix.startsWith("/")) {
                        suffix = suffix.substring(1);
                    }
                }
                sb.append("<a href=\"services/FileDownload?file=");
                String encoded = Base64.getEncoder().encodeToString(f.getAbsolutePath().getBytes());
                sb.append(encoded);
                sb.append("\">");
                sb.append(StringEscapeUtils.escapeHtml4(suffix));
                sb.append("</a>");
                sb.append("</td>");
                sb.append("<td>");
                sb.append(StringEscapeUtils.escapeHtml4(BootstrapDownloadTableVisitor.readableFileSize(f.length())));
                sb.append("</td>");
                
                // last modified
                long yourmilliseconds = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");    
                Date resultdate = new Date(f.lastModified());
                sb.append("<td>");
                sb.append(StringEscapeUtils.escapeHtml4(sdf.format(resultdate)));
                sb.append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            if (make_collapsed_table) {
                sb.append("</div>");
            }
        }
   
        log.log(Level.INFO, "Resulting HTML has length: {0}", new Object[] { sb.length() });
        return sb.toString();
    }
    
    public String debuggingToString() {
        StringBuilder sb = new StringBuilder();
        Set<File> dirs = dir2files.keySet();
        sb.append("<pre>\n");
        for (File dir : dirs) {
            sb.append(dir.getAbsolutePath());
            sb.append(": ");
            sb.append(dir2files.get(dir).size());
            sb.append("\n");
            for (File f : dir2files.get(dir)) {
                sb.append(f.getName());
                sb.append(" ");
            }
            sb.append("\n");
        }
        sb.append("</pre>");
        return sb.toString();
    }
    
    @Override
    public void process(final File f) {
        assert(f != null);
        File dir = f.getParentFile();
        if (!dir2files.containsKey(dir)) {
            dir2files.put(dir, new ArrayList<File>());
        }
        List<File> l = dir2files.get(dir);
        assert(l != null);
        l.add(f);
    }

    private void separateImagesFromOtherFiles(List<File> files, ArrayList<File> image_files, ArrayList<File> other_files) {
        for (File f : files) {
            if (f.getName().toLowerCase().endsWith(".png")) {
                image_files.add(f);
            } else {
                other_files.add(f);
            }
        }
        Collections.sort(image_files);
        Collections.sort(other_files);
    }

    private void addBootstrapImageCarousel(StringBuilder sb, ArrayList<File> image_files) {
        if (image_files.size() < 1) {
            return;
        }
        String id = "myCarousel" + n_carousels++;
        sb.append("<div id=\"");
        sb.append(id);
        sb.append("\" class=\"carousel slide\" data-ride=\"carousel\">");
  
        sb.append("<ol class=\"carousel-indicators\">");
        String active = "active";
        for (int i=0; i<image_files.size(); i++) {
            sb.append("<li data-target=\"#");
            sb.append(id);
            sb.append("\" data-slide-to=\"");
            sb.append(i);
            sb.append("\"");
            if (active.length() > 0) {
                sb.append(" class=\"active\"");
            }
            
            sb.append("></li>");
            active = "";
        }
        sb.append("</ol>");
        
        sb.append("<div class=\"carousel-inner\" role=\"listbox\">");
        active = " active";
        for (File f : image_files) {
            sb.append("<div class=\"item");
            sb.append(active);
            sb.append("\">");
                sb.append("<img class=\"maabCarouselImg\" ");
                sb.append(" src=\"services/FileDownload?file=")
                  .append(BootstrapDownloadTableVisitor.encodeFile(f))
                  .append("\" alt=\"")
                  .append(StringEscapeUtils.escapeHtml4(f.getName()))
                        .append("\">");
            sb.append("</div>");
            active = "";
        }
        sb.append("</div>");

        sb.append("<a class=\"left carousel-control\" href=\"#");
        sb.append(id);
        sb.append("\" role=\"button\" data-slide=\"prev\">");
        sb.append("<span class=\"glyphicon glyphicon-chevron-left\" aria-hidden=\"true\"></span>");
        sb.append("<span class=\"sr-only\">Previous</span></a>");
        sb.append("<a class=\"right carousel-control\" href=\"#");
        sb.append(id);
        sb.append("\" role=\"button\" data-slide=\"next\">");
        sb.append("<span class=\"glyphicon glyphicon-chevron-right\" aria-hidden=\"true\"></span>");
        sb.append("<span class=\"sr-only\">Next</span>");
        sb.append("</a>");
        sb.append("</div>");
    }
}
