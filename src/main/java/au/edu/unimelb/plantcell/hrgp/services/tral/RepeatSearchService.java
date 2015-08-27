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
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            if (params.get("what").equals("repeats")) {
                pw.println("<h3>PRPs repeats identified by TRAL for "+StringEscapeUtils.escapeHtml4(params.get("q"))+"</h3>");
                AddRepeatHitsAsHtml(conn, pw, params.get("q"));
            } else {
                pw.println("<h3>PRP repeats which have aligned sequences from "+StringEscapeUtils.escapeHtml4(params.get("q"))+"</h3>");
                AddMatchingRepeatsFromAlignedSequencesHit(conn, pw, params.get("q"));
            }
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
    private void AddRepeatHitsAsHtml(final Connection conn, final PrintWriter pw, final String query) throws SQLException {
        Map<String,Integer> model2hit_count = findAlignedSequenceCountPerModel(conn, query);
        
        pw.println("<div id=\"prp_repeats_found\">");
        String t = "%"+query+"%";
        String q = "select * FROM prp_significant_repeats WHERE (split_0 like ? OR Species like ? or tOrder like ? or Clade like ? ) ORDER BY Clade";
            
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
                    String mid = model_id.replaceAll("/", "_");

                    boolean has_hits = model2hit_count.containsKey(mid) 
                            && (model2hit_count.get(mid) > 0);
                    dump_record(pw, species, clade, order, sample_id, repeat, tt, mid, has_hits);
             }
             pw.println("</table>");
        } catch (Exception e) {
            pw.println("<div class=\"alert\">Unable to read database: "+e.getMessage()+"</div>");
            // fallthru to close div
        }
        pw.println("</div>");
    }

    private void dump_record(final PrintWriter pw, final String species, final String clade,
            final String order, final String sample_id, final String repeat, String tissue_type,
            final String model_id, boolean has_hits) {
        if (tissue_type == null) {
            tissue_type = "";
        }
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
            pw.print(tissue_type);
        pw.print("</td>");

        log.log(Level.INFO, "Looking for model: {0} with hits: {1}", new Object[] { model_id, has_hits });
        
        if (has_hits) {
             pw.print("<td><a href=\"services/RepeatAlignmentsFor?model=");
             pw.print(Base64.getEncoder().encodeToString(model_id.getBytes()));
            pw.print("\">");
        } else {
             pw.print("<td>No hits at better than evalue 1e-5 for ");
        }
        pw.print(StringEscapeUtils.escapeHtml4(model_id));
        if (has_hits) {
            pw.print("</a>");
        }
        pw.print("</td>");

        pw.println("</tr>");
    }
    
    private Map<String, Integer> findAlignedSequenceCountPerModel(final Connection conn, final String query) throws SQLException {
        String sql = "select count(distinct(aligned_sequence_id)),cphmm_model from prp_significant_repeat_alignments group by cphmm_model";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        Map<String,Integer> ret = new HashMap<>();
        while (rs.next()) {
            Integer count = rs.getInt(1);
            String model  = rs.getString(2);
            ret.put(model, count);
        }
        
        return ret;
    }

    private void AddMatchingRepeatsFromAlignedSequencesHit(final Connection conn, final PrintWriter pw, final String q) throws SQLException {
        // these queries are designed to find aligned sequences and trace it back to the repeats to report
        // to do this, we need to:
        // 1. identify relevant samples based on the users query terms
        // 2. find relevant models based on aligned sequences which match the relevant samples
        // 3. report all repeats based on step 2
        String term = "%"+q.trim()+"%";
        log.log(Level.INFO, "Aligned sequence repeat search for {0}", new Object[] { term });

        String sql = 
                "select distinct(cphmm_model) from prp_significant_repeat_alignments "+
                "where aligned_sample_id in "+
                "(select `1kP_Sample` from sample_list "+
		"where (species like ? or "+
                " clade like ? or `1kP_Sample` like ? or "+
		" Taxonomic_Order like ? or Family like ?));";
               
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, term);
        stmt.setString(2, term);
        stmt.setString(3, term);
        stmt.setString(4, term);
        stmt.setString(5, term);
        ResultSet rs = stmt.executeQuery();
        
        sql = "select * from prp_significant_repeats where uid = ?";
        PreparedStatement stmt2 = conn.prepareStatement(sql);
        Pattern p = Pattern.compile("^([A-Z]{4}_Locus_\\d+_Transcript_\\d+)_(.*)$");
        boolean first = true;
        
        while (rs.next()) {
            if (first) {
                first = false;
                pw.println("<table class=\"table-condensed collapsible\">");
                pw.println("<tr><th>Species</th><th>Clade</th><th>Order</th><th>1kp Sample ID</th><th>Repeat</th><th>Tissue</th><th>Model ID</th></tr>");
            }
            String model_id = rs.getString(1);
           
            Matcher m = p.matcher(model_id);
            if (!m.matches()) {
                throw new SQLException("Bad model id: "+model_id);
            }
            
            // correct the model id and run query to find the repeat details to report
            model_id = m.group(1) + "/" + m.group(2);
            log.log(Level.INFO, "Finding hits to model {0}", new Object[] { model_id });
            stmt2.setString(1, model_id);
            ResultSet rs2 = stmt2.executeQuery();
            
            boolean expecting_hit = true;
            while (rs2.next()) {
                String species = rs2.getString("Species");
                String clade   = rs2.getString("Clade");
                String order   = rs2.getString("tOrder");
                String sample_id=rs2.getString("split_0");
                String repeat  = rs2.getString("Schaper2014__reported_repeat_alignment");
                String tt      = rs2.getString("Tissue_Type");
                    
                dump_record(pw, species, clade, order, sample_id, repeat, tt, model_id, true);
                expecting_hit = false;
            }
            rs2.close();
            if (expecting_hit) {
                log.log(Level.SEVERE, "Did not get hit for {0}", new Object[] { model_id });
            }
        }
        rs.close();
        
        if (first) {
            pw.println("<p>No alignments contained relevant samples.</p>");
        } else {
            pw.println("</table>");
        }
    }

}
