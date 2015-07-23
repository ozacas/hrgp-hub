/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author acassin
 */
public class auto1KPDataComplete extends HttpServlet {
    private final static int MAX_REPORTED_HITS = 32;
    
    private final Logger l = Logger.getLogger("autocomplete-1kp");

    private final JsonBuilderFactory factory = Json.createBuilderFactory(new HashMap<String,String>());

    private void addHits(final ResultSet rs, final JsonArrayBuilder bldr) throws SQLException {
         int idx = 1;
        while (rs.next()) {
            String sample_id = rs.getString("sample_id");
            String sample_name=rs.getString("species_name").replaceAll("\\s+", "_");
            
            String lbl = sample_id+"_"+sample_name;
            // prevent autocomplete list from ever having more than max_hits in it...
            bldr.add(factory.createObjectBuilder()
                    .add("id", String.valueOf(idx++))
                    .add("label", lbl)
                    .add("name", sample_name));
        }
    }
    
    private void addOrderHits(String order, final JsonArrayBuilder bldr, final Connection conn) throws SQLException {
        if (order.startsWith("order:")) {
            order = order.substring("order:".length());
        }
        if (order.length() < 2) {
            return;
        }
        String t     = "%"+order+"%";
        String query = "select sample_id,species_name from SAMPLEANNOTATION WHERE taxonomic_family like ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, t);
        ResultSet rs = stmt.executeQuery();
        addHits(rs, bldr);
    }
    
    private void addFamilyHits(String family, final JsonArrayBuilder bldr, final Connection conn) throws SQLException {
        if (family.startsWith("family:")) {
            family = family.substring("family:".length());
        }
        if (family.length() < 2) {
            return;
        }
        String t     = "%"+family+"%";
        String query = "select sample_id,species_name from SAMPLEANNOTATION WHERE taxonomic_family like ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, t);
        ResultSet rs = stmt.executeQuery();
        addHits(rs, bldr);
    }
    
    private void addSampleHits(final String term, final JsonArrayBuilder bldr, final Connection conn) throws SQLException {
        if (term.length() < 3) {
            return;
        }
        
        String t = "%"+term+"%";
        String query = "select sample_id,species_name FROM SAMPLEANNOTATION WHERE (sample_id like ? OR species_name like ?)";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, t);
        stmt.setString(2, t);
        ResultSet rs = stmt.executeQuery();
        addHits(rs, bldr);
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
        Map<String,String> params = ServiceCore.splitQuery(q);
        
        if (!params.containsKey("str")) {
            throw new ServletException("No search terms!");
        }
             
      /* JsonArray value = factory.createArrayBuilder()
     .add(factory.createObjectBuilder()
         .add("id", "1")
         .add("label", "test1")
         .add("value", "test1"))
     .add(factory.createObjectBuilder()
         .add("id", "2")
         .add("label", "test2")
         .add("value", "test2"))
     .build();*/
        
       /* issue mysql query to identify relevant hits:
        *  1. try for a library id match first
        *  2. then try for a species name match
        */
       JsonArrayBuilder bldr = factory.createArrayBuilder();

       try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/seqdb_onekp", "root", "Ethgabitc!");

            for (String term : params.get("str").split("\\s+")) {
                if (term.startsWith("order:")) {
                    addOrderHits(term, bldr, conn);
                } else if (term.startsWith("family:")) {
                    addFamilyHits(term, bldr, conn);
                } else {
                    addSampleHits(term, bldr, conn);
                }
            }
            JsonArray ret = bldr.build();
            if (ret.size() > MAX_REPORTED_HITS) {
               bldr = factory.createArrayBuilder();
               for (JsonValue jv : ret.subList(0, MAX_REPORTED_HITS)) {
                   bldr.add(jv);
               }
               ret = bldr.build();
            }
           
            response.setContentType("application/json");
            try (PrintWriter out = response.getWriter()) {
                JsonWriter w = Json.createWriter(out);
                w.write(ret);
           }
       } catch (Exception se) {
          l.log(Level.SEVERE, "Failed to access OneKP sample list!", se);
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
        throw new ServletException("POST not supported!");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "enables autocompletion based on partial user input when obtaining 1KP data via the search form";
    }// </editor-fold>

}
