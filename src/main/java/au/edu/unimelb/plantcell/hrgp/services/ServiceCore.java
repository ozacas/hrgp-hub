/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services;

import au.edu.unimelb.plantcell.hrgp.interfaces.FileVisitor;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 * @author acassin
 */
public class ServiceCore {
    public final static String ROOT = "/1kp/hrgp/classical-AGP";
    
    public static final Map<String, String> splitQuery(final String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), 
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
    
    /**
     * Returns true if a file of the given extension should be available to the user
     * as a file download, false otherwise.
     * @param name name of the file including extension eg. .docx (should be lowercased by caller) and must not be null
     * @return true if the filename is an acceptable download, false otherwise
     */
    public final static boolean acceptableFileExtensions(final String name) {
        assert(name != null && name.length() > 0);
        return (name.endsWith(".png") || name.endsWith(".xls") || name.endsWith(".table") ||
                        name.endsWith(".eps") || name.endsWith(".zip") || name.endsWith(".fasta") ||
                        name.endsWith(".csv") || name.endsWith(".gz") || name.endsWith(".jpg") ||
                        name.endsWith(".fa")  || name.endsWith(".docx") || name.endsWith(".pdf") ||
                        name.endsWith(".xlsx")|| name.endsWith(".pptx"));
    }
    
    /**
     * 
     * @param q
     * @return
     * @throws ServletException 
     */
    public static final File find_root(String q) throws ServletException {
         File root = new File(ROOT);
                        
        // compute root folder based on q
        if (q.equals("maab_raw_input")) {
            root = new File(root, "raw-input-data");
        } else if (q.equals("maab_input_redundancy_removal")) {
            root = new File(root, "per-sample-input-clustering");
        } else if (q.equals("maab_contaminant_rm")) {
            root = new File(root, "contaminant-removal");
        } else if (q.equals("maab_class")) {
            root = new File(root, "maab-classification");
        } else if (q.equals("maab_known_hyp")) {
            root = new File(root, "hits-to-known-hyp-peptides/May2014");
        } else if (q.equals("maab_figures")) {
            root = new File(root, "publication-figures");
        } else if (q.equals("maab_supptables")) {
            root = new File(root, "supplementary-tables");
        } else if (q.equals("maab_chimeric_hrgp")) {
            root = new File(root, "putative-chimeric-hrgp");
        } else if (q.equals("maab_clustering")) {
            root = new File(root, "clustering-analyses");
        } else if (q.equals("maab_saved_results")) {
            root = new File(root, "final-saved-results");
        } else if (q.equals("non-chimeric-paper")) {
            root = new File(root, "publications/non-chimeric");
        } else if (q.equals("agpep-paper")) {
            root = new File(root, "publications/ag-peptide");
        } else {
            throw new ServletException("Unknown class of downloads: "+q);
        }
        
        return root;
    }
    
    private static void doVisit(final File folder, final FileFilter filter, final FileVisitor visitor) {
         // want subfolders too?
        File[] folders = folder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return (pathname.isDirectory());
            }
            
        });
        for (File f: folders) {
            if (filter.accept(f)) {
                doVisit(f, filter, visitor);
            }
        }
        File[] plain_files = folder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return (pathname.canRead() && pathname.isFile() && filter.accept(pathname));
            }
            
        });
        for (File f : plain_files) {
            visitor.process(f);
        }
    }
    
    public static final void visitFiles(final File folder, final FileFilter filter, final FileVisitor visitor) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return;
        }
        if (filter == null || visitor == null) {
            return;
        }
        visitor.beforeVisit();
        doVisit(folder, filter, visitor);
        visitor.afterVisit();
    }
    
    public static final void visitFiles(List<File> files, final FileFilter filter, final FileVisitor visitor) {
        if (filter == null || visitor == null) {
            return;
        }
        visitor.beforeVisit();
        for (File f : files) {
            if (filter.accept(f)) {
                visitor.process(f);
            }
        }
        visitor.afterVisit();
    }
    
    /**
     * Converts an absolute path on the hrgp site eg. /1kp/hrgp/classical-AGP/publication-figures/...
     * to one relative to the root.
     * 
     * @param path should be a path located under {@code ServiceCore.ROOT}. Must not be null
     * @return relative path
     */
    public static final String relativize(final File path) {
        assert(path != null);
        String relative = new File(ROOT).toURI().relativize(new File(path.getAbsolutePath()).toURI()).getPath();
        
        return relative;
    }
}