/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services;

import au.edu.unimelb.plantcell.hrgp.interfaces.FileVisitor;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author acassin
 */
public class ImageCarousel extends HttpServlet {

    /**
     * Returns the HTML for the carousel of the images (.png's only) 
     * within the specified folder (and all subfolders).
     * 
     * @param folder_to_search
     * @return 
     */
    public String visitImageFiles(final File folder_to_search, String carousel_id) {
        assert(folder_to_search != null && folder_to_search.isDirectory());
        
        ImageCarouselVisitor tv = new ImageCarouselVisitor(folder_to_search, carousel_id);
        ServiceCore.visitFiles(folder_to_search, new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                if (!pathname.canRead() || !pathname.isFile()) {
                    return false;
                }
                String name = pathname.getName().toLowerCase();
                return (name.endsWith(".png"));
            }
            
        }, tv);
        
        return tv.toString();
    }
    
    public String visitImageFilesNoSubfolders(File folder_to_search, String carousel_id) {
         assert(folder_to_search != null && folder_to_search.isDirectory());
        
        ImageCarouselVisitor tv = new ImageCarouselVisitor(folder_to_search, carousel_id);
        ServiceCore.visitFiles(folder_to_search, new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return false;
                }
                if (!pathname.canRead() || !pathname.isFile()) {
                    return false;
                }
                String name = pathname.getName().toLowerCase();
                return (name.endsWith(".png"));
            }
            
        }, tv);
        
        return tv.toString();
    }
    
    public String makeCarouselFromFiles(final List<File> files, final String carousel_id) {
        if (files == null || files.isEmpty()) {
            return "";
        }
        FileVisitor tv = new ImageCarouselVisitor(files.get(0).getParentFile(), carousel_id);
        
        ServiceCore.visitFiles(files, new FileFilter() {

            @Override
            public boolean accept(File pathname) {
               return true;
            }
            
        }, tv);
        return tv.toString();
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
        String q = request.getQueryString();
        if (q == null || q.length() < 1 || q.length() > 100) {
            throw new ServletException("No class of downloads specified!");
        }
        if (q.startsWith("category=")) {
            q = q.substring("category=".length());
        }
        
        File root = ServiceCore.find_root(q);
        String results = visitImageFiles(root, "carousel1");
        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain");
            out.println(results);
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
