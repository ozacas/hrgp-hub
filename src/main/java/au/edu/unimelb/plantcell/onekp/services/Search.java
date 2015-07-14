/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
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

/**
 *
 * @author acassin
 */
public class Search extends HttpServlet {
    private final static Logger log = Logger.getLogger("SearchServlet");
    
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
        File root = new File(ServiceCore.ROOT);
        if (!root.exists() || !root.isDirectory()) {
            throw new ServletException("No HRGP data folder: "+root.getAbsolutePath());
        }
        final Map<String,String> params = ServiceCore.splitQuery(request.getQueryString());
        final List<String> keywords = new ArrayList<>();
        final List<File> matching_folders = new ArrayList<>();
        if (!params.containsKey("kw")) {
            throw new ServletException("No keywords!");
        }
        keywords.addAll(Arrays.asList(params.get("kw").toLowerCase().split("\\s+")));
        
        log.log(Level.INFO, "Found {0} keywords to search for.", new Object[] {keywords.size()});
        
        SubfolderTableVisitor tv = new SubfolderTableVisitor(root);
        FileFilter ff = new FileFilter() {

            @Override
            public boolean accept(final File pathname) {
                if (pathname.isDirectory()) {
                    // add to wanted hit list of folders?
                    String name = pathname.getName().toLowerCase();
                    for (String keyword : keywords) {
                        if (name.contains(keyword)) {
                            matching_folders.add(pathname);
                        }
                    }
                    return true;
                }
                if (!pathname.canRead() || !pathname.isFile()) {
                    return false;
                }
                
                // keyword match on filename?
                String name = pathname.getName().toLowerCase();
                for (String keyword : keywords) {
                    if (name.contains(keyword)) {
                        return true;
                    }
                }
                
                return matching_folders.contains(pathname.getParentFile());
            }
            
        };
        ServiceCore.visitFiles(root, ff, tv);
        String results = tv.toString();
        
        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain;charset=UTF-8");
            if (results.length() < 1) {
                out.println("<p>No hits.</p>");
            } else {
                out.println(results);
            }
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
