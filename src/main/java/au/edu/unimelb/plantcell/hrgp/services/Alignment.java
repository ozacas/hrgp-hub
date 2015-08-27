/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * This servlet returns the requested alignment as a FASTA file, so that the
 * webpage can display it. Requests are to be of the form:
 * 
 * ...Alignment?torder=brassicales&hclass=class2
 * 
 * for the brassicales extensin alignment (corresponding to the phyloxml)
 * 
 * @author acassin
 */
public class Alignment extends HttpServlet {
    private static final Logger log = Logger.getLogger("Alignment");

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
            throw new ServletException("HRGP class and taxonomic order required!");
        }
        if (!params.get("hclass").matches("^class\\d+$") || 
                !params.get("torder").matches("^[a-z]+$")) {
            throw new ServletException("Invalid arguments hclass and/or torder");
        }
        
        URL u = new URL(request.getRequestURL().toString());
        String alignment_url = u.getPath().replaceAll("Alignment", "AlignmentDownload")+
                                "?msa="+
                                StringEscapeUtils.escapeHtml4(params.get("hclass")) + "/" + 
                                StringEscapeUtils.escapeHtml4(params.get("torder"));
        if (alignment_url == null) {
            throw new ServletException("Unable to compute URL for alignment download!");
        }
        log.log(Level.INFO, "Alignment to be downloaded from: {0}", new Object[] { alignment_url });
        
        InputStream in = getServletContext().getResourceAsStream("/alignment.html");
        if (in == null) {
            throw new ServletException("Cannot load alignment.html from webapp: missing?");
        }
        String html = IOUtils.toString(in);
        IOUtils.closeQuietly(in);
        html = html.replaceAll("@@ALIGNMENT_URL@@", alignment_url);
        String title = params.get("torder") + " - " + params.get("hclass");
        html = html.replaceAll("@@TITLE@@", title);
        html = html.replaceAll("@@MESSAGE@@", "");
        
        /*
         * We write out alignment.html, customised to the file being aligned... 
         */
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
           out.println(html);
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
