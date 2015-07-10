/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author acassin
 */
public class TreeDownload extends HttpServlet {
    
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
        Map<String,String> params = ServiceCore.splitQuery(request.getQueryString());
        
        if (!(params.containsKey("hclass") && params.containsKey("torder"))) {
            throw new ServletException("HRGP class and taxonomic order expected!");
        }
        String hclass = params.get("hclass");
        String torder = params.get("torder");
        boolean want_file = params.containsKey("file") && "1".equals(params.get("file"));
        
        if (want_file) {
            response.setHeader("Content-Disposition", "attachment; filename=\""+suggestedFilename(params)+"\"");
        }
        if (!hclass.matches("^class\\d+$") || !torder.matches("^\\w+$")) {
            throw new ServletException("Invalid HRGP class and/or taxonomic order");
        }
        File root = new File(ServiceCore.ROOT);
        File phylogeny_root = new File(root, "phylogeny");
        if (!phylogeny_root.isDirectory()) {
            throw new ServletException("No phylogeny directory: "+phylogeny_root.getAbsolutePath());
        }
        File class_root = new File(phylogeny_root, hclass);
        if (!class_root.isDirectory()) {
            throw new ServletException("No folder: "+class_root.getAbsolutePath());
        }
        File phyloxml = new File(class_root, torder.toLowerCase()+".phyloxml");
        if (!phyloxml.isFile() || !phyloxml.canRead()) {
            throw new ServletException("Cannot read tree: "+phyloxml.getAbsolutePath());
        }
        try (OutputStream out = response.getOutputStream()) {
            response.setContentType("application/xml");
            Files.copy(phyloxml.toPath(), out);
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

    private void throwIllegal(final String s) throws ServletException {
        throw new ServletException("Illegal tree specification: Order-Class expected! "+s);
    }

    private String suggestedFilename(Map<String, String> params) {
        assert(params != null);
        
        String hrgp_class = params.get("hclass");
        String taxonomic_order = params.get("torder");
        if (hrgp_class != null && taxonomic_order != null) {
            return taxonomic_order + "." + hrgp_class +".phyloxml";
        }
        return "unknown.phyloxml";
    }

}
