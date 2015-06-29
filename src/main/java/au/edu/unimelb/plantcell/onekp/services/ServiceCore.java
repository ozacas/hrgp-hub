/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
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
    
    public static final File find_root(String q) throws ServletException {
         File root = new File(ROOT);
                        
        // compute root folder based on q
        if (q.equals("maab_raw_input")) {
            root = new File(root, "raw-input-data");
        } else if (q.equals("maab_input_redundancy_removal")) {
            root = new File(root, "clustering-performance");
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
        } else {
            throw new ServletException("Unknown class of downloads: "+q);
        }
        
        return root;
    }
    
    private static final void doVisit(final File folder, final FileFilter filter, final FileVisitor visitor) {
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
}
