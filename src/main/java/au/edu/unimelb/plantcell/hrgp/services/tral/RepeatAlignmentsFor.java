/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services.tral;

import au.edu.unimelb.plantcell.hrgp.services.ServiceCore;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author acassin
 */
public class RepeatAlignmentsFor extends HttpServlet {

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
        if (!params.containsKey("model")) {
            throw new ServletException("No cpHMM model ID!");
        }
        
        response.setContentType("text/html;charset=UTF-8");
        InputStream in = getServletContext().getResourceAsStream("/alignment.html");
        if (in == null) {
            throw new ServletException("Cannot load alignment.html from webapp: missing?");
        }
        String html = IOUtils.toString(in);
        IOUtils.closeQuietly(in);
        
        URL u = new URL(request.getRequestURL().toString());

        String alignment_url = u.getPath().replaceAll("RepeatAlignmentsFor", "DBAlignmentDownload")+
                                "?model="+params.get("model");
        html = html.replaceAll("@@ALIGNMENT_URL@@", alignment_url);
        html = html.replaceAll("@@TITLE@@", new String(Base64.getDecoder().decode(params.get("model"))));
        html = html.replaceAll("@@MESSAGE@@", 
                "This alignment represents all hits to the repeat found in the RD001 dataset at better than evalue 1e-5. The idea of this data is to identify possibly novel PRPs using repeats identified by TRAL as seed models where SignalP was not able to predict due to missing/incorrect N-terminal sequence."
        );
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
