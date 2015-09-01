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
        RepeatSearchFilter rsf = new RepeatSearchFilter()
                                    .filterRepeatsByMAABclass(params.get("classfilter"))
                                    .filterRepeatsByMotif(params.get("motif"));
        
        try (PrintWriter pw = response.getWriter(); 
                Connection conn = ServiceCore.getTRALDatabaseConnection()) {
            if (params.get("what").equals("repeats")) {
                pw.println("<h3>Repeats identified by TRAL for "+StringEscapeUtils.escapeHtml4(params.get("q"))+"</h3>");
                AddRepeatHitsAsHtml(conn, pw, params.get("q"), rsf);
            } else {
                pw.println("<h3>Repeats which have aligned sequences from "+StringEscapeUtils.escapeHtml4(params.get("q"))+"</h3>");
                AddMatchingRepeatsFromAlignedSequencesHit(conn, pw, params.get("q"), rsf);
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
    private void AddRepeatHitsAsHtml(final Connection conn, final PrintWriter pw, 
            final String query, final RepeatSearchFilter rsf) throws SQLException {
        Map<String,Integer> model2hit_count = findAlignedSequenceCountPerModel(conn, query);
        
        pw.println("<div id=\"prp_repeats_found\">");
        String t = "%"+query+"%";
        String q = "select * FROM significant_repeats WHERE (split_0 like ? OR Species like ? or tOrder like ? or Clade like ? ) ORDER BY Clade";
            
        int n=0;
        try (PreparedStatement stmt = conn.prepareStatement(q)) {
             stmt.setString(1, t);
             stmt.setString(2, t);
             stmt.setString(3, t);
             stmt.setString(4, t);
             ResultSet rs = stmt.executeQuery();
             
             pw.println("<table class=\"table-condensed collapsible\">");
             pw.println("<tr><th>cphmm model</th><th>Clade/Order</th><th>Tissue</th><th>Found in MAAB class</th><th>Repeat</th></tr>");
             
             while (rs.next()) {
                    String species = rs.getString("Species");
                    String clade   =rs.getString("Clade");
                    String order   = rs.getString("tOrder");
                    String sample_id = rs.getString("split_0");
                    String repeat  = rs.getString("Schaper2014__reported_repeat_alignment");
                    String model_id= rs.getString("uid");
                    String tt      = rs.getString("Tissue_Type");
                    String maab_class = rs.getString("repeat_from_class");
                    String mid = model_id.replaceAll("/", "_");

                    // remove repeat index from model ID, since it is not stored in the model2hit_count hashtable
                    if (mid.matches("^.*Length_\\d+_\\d+_\\d$")) {
                        mid = mid.substring(0, mid.length()-2);
                    }
                    
                    // match model against precomputed hash table
                    boolean has_hits = model2hit_count.containsKey(mid) 
                            && (model2hit_count.get(mid) > 0);
                    if (!rsf.accept(repeat, maab_class)) {
                        log.log(Level.INFO, "Rejected {0} as it failed user-specified filter settings", new Object[] { mid });
                        continue;
                    } else {
                        dump_record(pw, species+"("+sample_id+")", clade, order, 
                            tt, repeat, mid, has_hits, maab_class);
                        n++;
                    }
             }
             pw.println("</table>");
        } catch (Exception e) {
            pw.println("<div class=\"alert\">Unable to read database: "+e.getMessage()+"</div>");
            // fallthru to close div
        }
        pw.println("<p>Found "+n+" repeats. Search complete.</p>");

        pw.println("</div>");
    }

    /**
     * Must spew out the cells in the following order:
     * <tr><th>cpHMM model</th>
     * <th>Clade/Order/Species</th>
     * <th>Tissue</th>
     * <th>Found in MAAB class</th>
     * <th>Repeat</th></tr>
     * and ensure the output is suitably formatted and escaped for browsers etc.

     * @param pw
     * @param species_and_sample_id
     * @param clade
     * @param order
     * @param tissue_type
     * @param repeat
     * @param model_id
     * @param has_hits
     * @param maab_class 
     */
    private void dump_record(final PrintWriter pw, final String species_and_sample_id, final String clade,
            final String order, String tissue_type, final String repeat, 
            final String model_id, boolean has_hits, final String maab_class) {
        if (tissue_type == null) {
            tissue_type = "";
        }
        pw.println("<tr>");
        pw.print("<td class=\"small\">");
        log.log(Level.INFO, "Looking for model: {0} with hits: {1}", new Object[] { model_id, has_hits });
        if (has_hits) {
            pw.print("<a href=\"services/RepeatAlignmentsFor?model=");
            pw.print(Base64.getEncoder().encodeToString(model_id.getBytes()));
            pw.print("\">");
        } 
        pw.print(StringEscapeUtils.escapeHtml4(model_id));
        if (has_hits) {
            pw.print("</a>");
        }
        pw.print("</td>");

        pw.print("<td class=\"small\">");
        pw.print(StringEscapeUtils.escapeHtml4(clade+"/"+order+"/"+species_and_sample_id));
        pw.print("</td>");

        pw.print("<td>");
        pw.print(StringEscapeUtils.escapeHtml4(tissue_type));
        pw.print("</td>");
        
        pw.print("<td>");
        pw.print(StringEscapeUtils.escapeHtml4(maab_class));
        pw.println("</td>");
        
        pw.print("<td class=\"small\">");
        pw.print("<pre>");
        pw.print(StringEscapeUtils.escapeHtml4(repeat));
        pw.print("</pre></td>");

        pw.println("</tr>");
    }
    
    private Map<String, Integer> findAlignedSequenceCountPerModel(final Connection conn, final String query) throws SQLException {
        String sql = "select count(distinct(aligned_sequence_id)),cphmm_model from significant_repeat_alignments group by cphmm_model";
        
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

    private void AddMatchingRepeatsFromAlignedSequencesHit(final Connection conn, 
        final PrintWriter pw, final String q, final RepeatSearchFilter rsf) throws SQLException {
        // these queries are designed to find aligned sequences and trace it back to the repeats to report
        // to do this, we need to:
        // 1. identify relevant samples based on the users query terms
        // 2. find relevant models based on aligned sequences which match the relevant samples
        // 3. report all repeats based on step 2
        String term = "%"+q.trim()+"%";
        log.log(Level.INFO, "Aligned sequence repeat search for {0}", new Object[] { term });

        String sql = 
                "select distinct(cphmm_model) from significant_repeat_alignments "+
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
        
        sql = "select * from significant_repeats where uid like ?";
        PreparedStatement stmt2 = conn.prepareStatement(sql);
        boolean first = true;
        int n = 0;
        
        while (rs.next()) {
            if (first) {
                first = false;
                pw.println("<table class=\"table-condensed collapsible\">");
                pw.println("<tr><th>cphmm model</th><th>Clade/Order</th><th>Tissue</th><th>Found in MAAB class</th><th>Repeat</th></tr>");
            }
            String model_id = rs.getString(1);
            
            log.log(Level.INFO, "Finding hits to model {0}", new Object[] { model_id });
            stmt2.setString(1, model_id+"%");
            ResultSet rs2 = stmt2.executeQuery();
            
            boolean expecting_hit = true;
            while (rs2.next()) {
                String species = rs2.getString("Species");
                String clade   = rs2.getString("Clade");
                String order   = rs2.getString("tOrder");
                String sample_id=rs2.getString("split_0");
                String repeat  = rs2.getString("Schaper2014__reported_repeat_alignment");
                String tt      = rs2.getString("Tissue_Type");
                String maab_class = rs2.getString("repeat_from_class");
                
                if (rsf.accept(repeat, maab_class)) {
                    dump_record(pw, species+"("+sample_id+")", clade, order, tt, repeat, model_id, true, maab_class);
                    n++;
                }
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
            pw.println("<p>Found "+n+" repeats. Search complete.</p>");
        }
    }

}
