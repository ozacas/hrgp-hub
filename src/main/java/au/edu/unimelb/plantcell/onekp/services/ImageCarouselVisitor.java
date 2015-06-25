/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author acassin
 */
class ImageCarouselVisitor implements FileVisitor {
    private final ArrayList<File> image_files = new ArrayList<>();
    
    public ImageCarouselVisitor(File root) {
    }

    @Override
    public void beforeVisit() {
        image_files.clear();
    }

    @Override
    public void afterVisit() {
    }

    @Override
    /**
     * Generates HTML fragment: 
     * 
     * NB: assumes owl jquery plugin has been correctly loaded/initialised
     * and CSS styles applied - see owlgraphic.com for details
     * 
     * @param sb
     * @param image_files 
     */
    public String toString() {  
        StringBuilder sb = new StringBuilder();
        if (image_files.size() > 0) {
            sb.append("<h3>Image summary</h3>");

            sb.append("<div id=\"owl-example\" class=\"owl-carousel\">");
            for (File f : image_files) {
                sb.append("<div>");
                sb.append("<img class=\"carousel\" alt=\"").append(StringEscapeUtils.escapeHtml4(f.getName()))
                        .append("\" src=\"services/FileDownload?file=")
                      .append(BootstrapDownloadTableVisitor.encodeFile(f))
                      .append("\">");
                sb.append("</div>");
            }
            sb.append("</div>");
        }
        return sb.toString();
    }
    
    @Override
    public void process(File f) {
        image_files.add(f);
    }
    
}
