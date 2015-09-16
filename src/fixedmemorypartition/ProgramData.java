/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fixedmemorypartition;

/**
 *
 * @author Diunuge
 */
public class ProgramData{
    final int programIndex;
    final int allocatedPartition;
    final int completionTime;
    
    public ProgramData(int programIndex, int allocatedPartition, int completionTime){
        this.programIndex = programIndex;
        this.allocatedPartition = allocatedPartition;
        this.completionTime = completionTime;
    }
}
