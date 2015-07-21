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
import java.util.Base64;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

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
        Map<String,String> params = ServiceCore.splitQuery(fl);
        if (!params.containsKey("file")) {
            throw new ServletException("No file to download!");
        }
        boolean as_attachment = params.containsKey("attachment") && params.get("attachment").equals("1");
        
        String decoded = new String(Base64.getDecoder().decode(params.get("file")), "UTF-8");
        if (!decoded.startsWith(ServiceCore.ROOT) || decoded.contains("..")) {
            throw new ServletException("Bogus file: "+decoded);
        }
        File f = new File(decoded);
        if (!(f.exists() && f.isFile())) {
            throw new ServletException("Must be plain file: "+f.getAbsolutePath());
        }
        
        try (OutputStream out = response.getOutputStream()) {
           response.setContentType(guessContentTypeFromFile(f));
           if (as_attachment) {
                String safe_filename = StringEscapeUtils.escapeHtml4(f.getName().replaceAll("\\s+", "_"));
                response.setHeader("Content-disposition", " attachment; filename="+safe_filename);
           }
           
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
