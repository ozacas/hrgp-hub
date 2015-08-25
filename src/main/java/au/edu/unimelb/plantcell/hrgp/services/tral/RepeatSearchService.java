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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Performs a search for repeats involving cpHMM models and their constituent hits. Provides 
 * a navigable interface to the large-scale TRAL hits which would otherwise be difficult to navigate.
 * 
 * @author acassin
 */
public class RepeatSearchService extends HttpServlet {
     private static final Logger log = Logger.getLogger("RepeatSearchService");

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
        if (!params.containsKey("q")) {
            throw new ServletException("No query keywords!");
        }
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter pw = response.getWriter(); 
                Connection conn = ServiceCore.getTRALDatabaseConnection()) {
            pw.println("<h3>PRPs repeats identified by TRAL for "+StringEscapeUtils.escapeHtml4(params.get("q"))+"</h3>");
            AddRepeatHitsAsHtml(conn, pw, params.get("q"));
            
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

    /**
     * Reads various tables from {@code conn} and constructs a html div fragment, representing
     * the hits from the database connection. The HTML is output to {@code pw}. The HTML
     * fragment requires bootstrap to work correctly.
     * 
     * @param conn must not be null
     * @param pw   must not be null
     */
    private void AddRepeatHitsAsHtml(final Connection conn, final PrintWriter pw, final String query) {
        pw.println("<div id=\"prp_repeats_found\">");
        String t = "%"+query+"%";
        String q = "select * FROM prp_significant_repeats WHERE (split_0 like ? OR Species like ? or tOrder like ? or Clade like ? )";
            
        try (PreparedStatement stmt = conn.prepareStatement(q)) {
             stmt.setString(1, t);
             stmt.setString(2, t);
             stmt.setString(3, t);
             stmt.setString(4, t);
             ResultSet rs = stmt.executeQuery();
             
             pw.println("<table class=\"table-condensed collapsible\">");
             pw.println("<tr><th>Species</th><th>Clade</th><th>Order</th><th>1kp Sample ID</th><th>Repeat</th><th>Tissue</th><th>Model ID</th></tr>");
             
             while (rs.next()) {
                    String species = rs.getString("Species");
                    String clade   =rs.getString("Clade");
                    String order   = rs.getString("tOrder");
                    String sample_id = rs.getString("split_0");
                    String repeat  = rs.getString("Schaper2014__reported_repeat_alignment");
                    String model_id= rs.getString("uid");
                    String tt      = rs.getString("Tissue_Type");
                    pw.println("<tr>");
                    pw.print("<td>");
                    pw.print(StringEscapeUtils.escapeHtml4(species));
                    pw.print("</td>");

                    pw.print("<td>");
                    pw.print(StringEscapeUtils.escapeHtml4(clade));
                    pw.print("</td>");

                    pw.print("<td>");
                    pw.print(StringEscapeUtils.escapeHtml4(order));
                    pw.print("</td>");

                    pw.print("<td>");
                    pw.print(StringEscapeUtils.escapeHtml4(sample_id));
                    pw.print("</td>");

                    pw.print("<td><pre>");
                    pw.print(StringEscapeUtils.escapeHtml4(repeat));
                    pw.print("</pre></a></td>");
                    
                    pw.print("<td>");
                        pw.print(tt);
                    pw.print("</td>");
                    
                    pw.print("<td><a href=\"services/RepeatAlignmentsFor?model=");
                        pw.print(Base64.getEncoder().encodeToString(model_id.getBytes()));
                    pw.print("\">");
                    pw.print(StringEscapeUtils.escapeHtml4(model_id));
                    pw.print("</a></td>");
                    
                    pw.println("</tr>");
             }
             pw.println("</table>");
        } catch (Exception e) {
            pw.println("<div class=\"alert\">Unable to read database: "+e.getMessage()+"</div>");
            // fallthru to close div
        }
        pw.println("</div>");
    }

}
