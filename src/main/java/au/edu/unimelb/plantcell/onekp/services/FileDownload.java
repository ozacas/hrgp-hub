/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author acassin
 */
public class FileDownload extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        String fl = request.getQueryString();
        if (fl.length() > 10*1024 || fl.length() < 1) {
            throw new ServletException("No file to download!");
        }
        if (fl.startsWith("file=")) {
            fl = fl.substring("file=".length());
        }
        String decoded = new String(Base64.decode(fl), "UTF-8");
        if (!decoded.startsWith(ServiceCore.ROOT) || decoded.contains("..")) {
            throw new ServletException("Bogus file: "+decoded);
        }
        File f = new File(decoded);
        if (!(f.exists() && f.isFile())) {
            throw new ServletException("Must be plain file: "+f.getAbsolutePath());
        }
        
        try (OutputStream out = response.getOutputStream()) {
           response.setContentType(guessContentTypeFromFile(f));
           
           Files.copy(f.toPath(), out);
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

    private String guessContentTypeFromFile(File f) {
        if (f == null || !f.canRead()) {
            return "application/octet-stream";
        }
        try {
            return Files.probeContentType(f.toPath());
        } catch (IOException ioe) {
            return "application/octet-stream";
        }
    }

}
