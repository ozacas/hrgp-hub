/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
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

    /**
    * Gets image dimensions for given file according to http://stackoverflow.com/questions/672916/how-to-get-image-height-and-width-using-java
    * @param imgFile image file
    * @return dimensions of image
    * @throws IOException if the file is not a known image
    */
    public static Dimension getImageDimension(final File imgFile) throws IOException {
      int pos = imgFile.getName().lastIndexOf(".");
      if (pos == -1)
        throw new IOException("No extension for file: " + imgFile.getAbsolutePath());
      String suffix = imgFile.getName().substring(pos + 1);
      Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
      if (iter.hasNext()) {
        ImageReader reader = iter.next();
        try {
          ImageInputStream stream = new FileImageInputStream(imgFile);
          reader.setInput(stream);
          int width = reader.getWidth(reader.getMinIndex());
          int height = reader.getHeight(reader.getMinIndex());
          return new Dimension(width, height);
        } finally {
          reader.dispose();
        }
      }

  throw new IOException("Not a known image file: " + imgFile.getAbsolutePath());
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
            sb.append("<div id=\"owl-example\" class=\"owl-carousel\">");
            for (File f : image_files) {
                sb.append("<div>");
                String title = StringEscapeUtils.escapeHtml4(f.getName());
                String uri = "services/FileDownload?file="+BootstrapDownloadTableVisitor.encodeFile(f);
                try {
                    Dimension d = getImageDimension(f);
                    sb.append("<a href=\"")
                            .append(uri+"&width="+d.width+"&height="+d.height)
                            //.append(uri)
                            .append("\" rel=\"prettyPhoto\" title=\"")
                            .append(title)
                            .append("\">");
                        sb.append("<img class=\"carousel\" alt=\"").append(title)
                            .append("\" src=\"")
                          .append(uri)
                          .append("\">");
                    sb.append("</a>");
                } catch (IOException ioe) {
                    // ioe is thrown if the image dimensions cant be determined, so we just ignore...
                }

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
