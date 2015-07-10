/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author acassin
 */
public class AssemblyDownload extends HttpServlet {
    private final static Logger l = Logger.getLogger("AssemblyDownload");
    
    /**
     * Add all the relevant files for the specified search term.
     * At the moment, only XXXX:"species name" search terms are supported, but 
     * eventually this method will support orders/families and add all the relevant files
     * to the {@code ret} parameter
     * 
     * @param t
     * @param ret 
     */
    private void addTermFiles(String t, final ArrayList<File> ret, final File data_directory) {
        if (t.matches("^[A-Za-z]{4}_.*$") || t.matches("^[A-Za-z]{4}")) {
            final String sample_id = t.substring(0, 4).toUpperCase();
            FileFilter ff = new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName().toUpperCase();     // must be uppercase to match sample_id
                    return (pathname.isFile() && pathname.canRead() && 
                            (name.endsWith(".FASTA") || name.endsWith(".FA")) &&
                            name.contains(sample_id));
                }
            };
            File[] matching_files = data_directory.listFiles(ff);
            if (matching_files != null) {
                l.log(Level.INFO, "Found {0} matching files for sample ID {1}", 
                        new Object[]{matching_files.length, sample_id});
                ret.addAll(Arrays.asList(matching_files));
            }
        }
    }
    
    /**
     * Responsible for examining the query string and determining a filename suitable for
     * users to save to. Should be conservative in the characters used and the length. Use of a suitable 
     * extension is strongly recommended.
     * 
     * @param q
     * @return 
     */
    private String suggestedFilename(final String q) throws UnsupportedEncodingException {
        Map<String,String> params = ServiceCore.splitQuery(q);
        String[] terms = params.get("q").split("\\s+");
        String type = params.get("type");
        for (String t : terms) {
            if (t.matches("^[A-Za-z]{4}_.*$") || t.matches("^[A-Za-z]{4}$")) {
                String s = t.substring(0,4).toUpperCase();
                return s+"."+type+".fasta";
            }
        }
        
        return "unknown.fasta";
    }
    
    /**
     * 
     * @param dataset
     * @param is_protein
     * @return 
     */
    private File getDatasetPath(final String dataset, final boolean is_protein) {
        File root = new File("/1kp/4website");
        File ret  = null;
        
        // test for all the legitimate values and then fallback to k25 if none match...
        if (dataset == null || dataset.startsWith("OneKP: k25 contigs")) {
            ret = new File(root, "k25");
        } else if (dataset.equals("OneKP: k=25 scaffolds")) {
            ret = new File(root, "k25s");
        } else if (dataset.equals("Oases k=39 scaffolds")) {
            ret = new File(root, "k39");
        } else if (dataset.equals("Oases k=49 scaffolds")) {
            ret = new File(root, "k49");
        } else if (dataset.equals("Oases k=59 scaffolds")) {
            ret = new File(root, "k59");
        } else if (dataset.equals("Oases k=69 scaffolds")) {
            ret = new File(root, "k69");
        } else {
            ret = new File(root, "k25");
        }
        
        if (is_protein) {
            ret = new File(ret, "proteomes");
        } else {
            ret = new File(ret, "transcriptomes");
        }
        
        l.log(Level.INFO, "Dataset root is {0}", ret.getAbsolutePath());
        return ret;
    }
    
    private List<File> queryAsFiles(final String q) throws UnsupportedEncodingException {
        ArrayList<File> ret = new ArrayList<>();
        Map<String,String> param2value = ServiceCore.splitQuery(q);
        boolean is_protein = false;
        if (param2value.containsKey("type") && "protein".equals(param2value.get("type"))) {
            is_protein = true;
        }
        File dataset_root = getDatasetPath(param2value.get("assembly-method"), is_protein);
        
        String[] terms =  param2value.get("q").split("\\s+");
        for (String t : terms) {
            addTermFiles(t, ret, dataset_root);
        }
        
        return ret;
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        String q = request.getQueryString();
        l.info(q);
        String disp = "attachment; filename=\""+StringEscapeUtils.escapeHtml4(suggestedFilename(q))+"\"";
        l.log(Level.INFO, "Computed disposition: {0}", disp);
        List<File> files = queryAsFiles(q);
        l.log(Level.INFO, "Found {0} files for query.", files.size());
        response.setHeader("Content-Disposition", disp);
        try (OutputStream out = response.getOutputStream()) {
           for (File f : files) {
               Files.copy(f.toPath(), out);
           }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>



}
