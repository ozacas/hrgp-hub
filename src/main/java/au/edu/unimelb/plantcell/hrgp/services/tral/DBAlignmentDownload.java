/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services.tral;

import au.edu.unimelb.plantcell.hrgp.services.ServiceCore;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet looks up the database of repeats and constructs the appropriate FASTA
 * formatted alignment and returns it to the caller, based on the cpHMM model supplied
 * 
 * @author acassin
 */
public class DBAlignmentDownload extends HttpServlet {
    private final static Logger logger = Logger.getLogger("DBAlignmentDownload");
    
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
        String model = new String(Base64.getDecoder().decode(params.get("model")));
        model = model.replaceAll("/", "_");
        if (model.length() > 100 || !model.matches("^[A-Z]{4}_Locus_\\d+_Transcript_\\d+_\\d+_.*$")) {
            throw new ServletException("Illegal model ID: oases locus ID expected");
        }
        
        // FIXME BUG TODO.. validate model parameter
        response.setContentType("text/plain;charset=UTF-8");
        try (PrintWriter pw = response.getWriter(); 
                Connection conn = ServiceCore.getTRALDatabaseConnection()) {
           PreparedStatement stmt = conn.prepareStatement("select * from prp_significant_repeat_alignments where cphmm_model = ?");
           stmt.setString(1, model.replaceAll("/", "_"));
           logger.log(Level.INFO, "Searching for alignments for {0}", new Object[] { model });
           ResultSet rs = stmt.executeQuery();
           int n = 0;
           while (rs.next()) {
               String id = rs.getString("aligned_sequence_id");
               String seq = rs.getString("aligned_sequence");
               pw.print(">");
               pw.println(id);
               pw.println(seq);
               n++;
           }
           logger.log(Level.INFO, "Found {0} aligned sequences for {1}", new Object[] { n, model});
           stmt.close();
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
