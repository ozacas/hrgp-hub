/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.onekp.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
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
        
       JsonBuilderFactory factory = Json.createBuilderFactory(new HashMap<String,String>());
       JsonArray value = factory.createArrayBuilder()
     .add(factory.createObjectBuilder()
         .add("id", "1")
         .add("label", "test1")
         .add("value", "test1"))
     .add(factory.createObjectBuilder()
         .add("id", "2")
         .add("label", "test2")
         .add("value", "test2"))
     .build();
 
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            JsonWriter w = Json.createWriter(out);
            w.write(value);
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
