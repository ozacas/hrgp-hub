/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.interfaces;

/**
 * 
 * @author acassin
 */
public interface HitPredicateFilter {
    
    public boolean accept(final String repeat, final String maab_class);
}
