/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.edu.unimelb.plantcell.hrgp.interfaces;

import java.io.File;

/**
 * Used by the visitor pattern during a walk an instance of this is called
 * via the appropriate hooks below.
 * 
 * @author acassin
 */
public interface FileVisitor {
    /**
     * Called before any file is processed
     */
    public void beforeVisit();
    
    /**
     * Called after all files are processed
     */
    public void afterVisit();
    
    /**
     * Called for each file which is acceptable to the traversal semantics
     * @param f 
     */
    public void process(final File f);
}
