/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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
public class AlignmentDownload extends HttpServlet {
    private final Logger log = Logger.getLogger("AlignmentDownload");

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
        File alignment_root = new File(root.getParentFile(), "phylogeny/alignments");
        if (!(alignment_root.exists() && alignment_root.isDirectory())) {
            throw new ServletException("No alignment directory: "+alignment_root.getAbsolutePath());
        }
        
        log.log(Level.INFO, "Locating alignments in {0}", new Object[] {alignment_root.getAbsolutePath()});
        Map<String,String> params = ServiceCore.splitQuery(request.getQueryString());
        if (!params.containsKey("msa")) {
            throw new ServletException("msa parameter missing, but required!");
        }
        log.log(Level.INFO, "Looking for msa: "+params.get("msa"));
        File alignment = new File(alignment_root, params.get("msa")+".muscle.fasta");
        if (!alignment.exists() || !alignment.isFile() || !alignment.canRead()) {
            throw new ServletException("Cannot read "+alignment.getAbsolutePath());
        }
        
        log.log(Level.INFO, "Sending alignment file: {0}", new Object[] { alignment.getAbsolutePath() });
        response.setContentType("text/plain;charset=UTF-8");
        try (OutputStream out = response.getOutputStream()) {
           Files.copy(alignment.toPath(), out);
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
