/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileFilter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author acassin
 */
public class AvailableDownloads extends HttpServlet {
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processGET(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String q = request.getQueryString();
        if (q == null || q.length() < 1 || q.length() > 100) {
            throw new ServletException("No class of downloads specified!");
        }
        if (q.startsWith("category=")) {
            q = q.substring("category=".length());
        }
       
        File root = ServiceCore.find_root(q);
        SubfolderTableVisitor tv = new SubfolderTableVisitor(root);
        ServiceCore.visitFiles(root, new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                if (!pathname.canRead() || !pathname.isFile()) {
                    return false;
                }
                String name = pathname.getName().toLowerCase();
                return (name.endsWith(".png") || name.endsWith(".xls") || 
                        name.endsWith(".eps") || name.endsWith(".zip") || name.endsWith(".fasta"));
            }
            
        }, tv);
        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain");
            
            out.println(tv.toString());
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
        processGET(request, response);
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
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Returns a bootstrap-compatible HTML table with available downloads for a given GET query";
    }// </editor-fold>

}
