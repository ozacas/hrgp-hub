/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.services.tral;

import au.edu.unimelb.plantcell.hrgp.interfaces.HitPredicateFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implements the {@code accept()} method for filtering hits identified from the
 * tral database. Provides a fluent-style api for configuring the filter.
 * 
 * @author acassin
 */
public class RepeatSearchFilter implements HitPredicateFilter {
    private Pattern motif_filter;
    private String class_filter;
    
    public RepeatSearchFilter() {
    }
    
    public RepeatSearchFilter filterRepeatsByMotif(final String required_motif) throws IOException,PatternSyntaxException {
        if (required_motif == null || required_motif.matches("^\\s*$")) {
            motif_filter = null;
            return this;
        }
            
        if (!required_motif.matches("^[A-Za-z]+$")) {
            throw new IOException("Illegal user motif: "+required_motif);
        }
        String pat   = required_motif.replaceAll("[Xx]", ".");
        motif_filter = Pattern.compile(pat);
        return this;
    }
    
    public RepeatSearchFilter filterRepeatsByMAABclass(final String required_class) throws IOException {
        if (required_class == null || required_class.matches("^\\s+$") || required_class.equals("all")) {
            class_filter = null;
        } else if (required_class.equals("class24")) {
            class_filter = "class 24 -";
        } else if (required_class.equals("class2")) {
            class_filter = "class 2 -";
        } else if (required_class.equals("class3")) {
            class_filter = "class 3 -";
        } else {
            throw new IOException("Unknown MAAB class: "+required_class);
        }
        return this;
    }
    
    @Override
    public boolean accept(final String repeat, final String maab_class) {
        boolean ret = true;
        if (motif_filter != null) {
            Matcher m = motif_filter.matcher(repeat);
            if (!m.find()) {
                ret= false;
            }
            // else fallthru to class filter
        }
        if (class_filter != null) {
            if (!maab_class.startsWith(class_filter)) {
                ret = false;
            }
        }
        return ret;
    }
}
