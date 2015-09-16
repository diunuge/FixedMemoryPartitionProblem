/*
 * Fixed Memory Partition Probem
 */
package fixedmemorypartition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diunuge
 */
public class FixedMemoryPartition {
    
    private static final int MAX_MEM_PARTITIONS = 5;
    private static final int MAX_PROGRAM_CONFIG_PAIRS = 5;
    
    private String inFile;
    
    private int noOfMemoryPartitions;
    private Integer[] memPartitionSize;
    
    private int noOfPrograms;
    private Integer[][] completionTime;
    
    private Integer[] CombTurnaroundTime;
    private Integer noOfCombinations;
    
    public FixedMemoryPartition(String inputFile){
        inFile = inputFile;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        FixedMemoryPartition myFMP = new FixedMemoryPartition("in2.txt");
        
        myFMP.run();
    }
    
    public void run(){
        readInput();
        //showInput();
        calcTurnAroundTime();
        int optimalCombinationIndex = getOptimalCombinationIndex();
        showAllocation(optimalCombinationIndex);
    }
    
    private void readInput(){
        BufferedReader br=null;
        try {
            br = new BufferedReader(new FileReader(inFile));
            
            String line = br.readLine();
            String buf[] = line.split(" ");
            noOfMemoryPartitions = Integer.parseInt(buf[0]);
            noOfPrograms = Integer.parseInt(buf[1]);
            
            noOfCombinations = power(noOfMemoryPartitions, noOfPrograms);
            CombTurnaroundTime = new Integer[noOfCombinations];
            
            line = br.readLine();
            buf = line.split(" ");
            
            memPartitionSize = new Integer[noOfMemoryPartitions];
            for(int i=0; i<noOfMemoryPartitions; i++){
                memPartitionSize[i] = Integer.parseInt(buf[i]);
            }
            
            completionTime = new Integer[noOfPrograms][noOfMemoryPartitions];
            
            for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
                int noOfPaires;
                int[] memSize = new int[MAX_PROGRAM_CONFIG_PAIRS];
                int[] executionTime = new int[MAX_PROGRAM_CONFIG_PAIRS];
                
                line = br.readLine();
                buf = line.split(" ");
                noOfPaires = Integer.parseInt(buf[0]);
                
                
                for(int pairIndex=0; pairIndex<noOfPaires; pairIndex++){
                    memSize[pairIndex] = Integer.parseInt(buf[pairIndex*2+1]);
                    executionTime[pairIndex] = Integer.parseInt(buf[pairIndex*2+2]);
                }
                
                //Fill the matix
                int lowerMemBoundry = 0;
                    
                    //Initialize
                for(int partitionIndex=0; partitionIndex<noOfMemoryPartitions; partitionIndex++){
                    completionTime[programIndex][partitionIndex]=-1;
                }
                
                    //Find the minimum program time for specific memory slot
                for(int partitionIndex=0; partitionIndex<noOfMemoryPartitions; partitionIndex++){
                    
                    for(int pairIndex=0; pairIndex<noOfPaires; pairIndex++){
                        if(lowerMemBoundry<memSize[pairIndex] && memSize[pairIndex]<=memPartitionSize[partitionIndex]){
                            //Program can be assigned to this partition
                            if(completionTime[programIndex][partitionIndex]>executionTime[pairIndex] || completionTime[programIndex][partitionIndex]==-1) //Current value is bigger
                                completionTime[programIndex][partitionIndex] = executionTime[pairIndex];
                        }
                    }
                    lowerMemBoundry = memPartitionSize[partitionIndex];
                }
                    //Fill null locations
                for(int partitionIndex=1; partitionIndex<noOfMemoryPartitions; partitionIndex++){
                    
                    if(completionTime[programIndex][partitionIndex]==-1) //Current value is bigger
                        completionTime[programIndex][partitionIndex] = completionTime[programIndex][partitionIndex-1];
                }
                
                //showCompletionTimeMat();
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FixedMemoryPartition.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            Logger.getLogger(FixedMemoryPartition.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(FixedMemoryPartition.class.getName()).log(Level.SEVERE, null, ex);
            }
        }       
    }
    
    private void calcTurnAroundTime(){
        for(int combinationIndex=0; combinationIndex<noOfCombinations; combinationIndex++){
            if(isValidCombination(getCombination(combinationIndex))){
                CombTurnaroundTime[combinationIndex] = getCombinationTurnaroundTime(combinationIndex);
            }
            else{ // Impossible combination
                CombTurnaroundTime[combinationIndex] = -1;
            }
        }
    }
    
    private int getOptimalCombinationIndex(){
        int optimalCombinationIndex=0;
        
        int minTurnaroundTime = 1000000;
        //Find the minimum allocation combination time
        for(int combinationIndex=0; combinationIndex<noOfCombinations; combinationIndex++){
            if(CombTurnaroundTime[combinationIndex]!=-1){
                if(CombTurnaroundTime[combinationIndex]<minTurnaroundTime)
                    minTurnaroundTime=CombTurnaroundTime[combinationIndex];
            }
            else{ // Impossible combination
                continue;
            }
        }
        //Find the combination
        for(int combinationIndex=0; combinationIndex<noOfCombinations; combinationIndex++){
            if(CombTurnaroundTime[combinationIndex]==minTurnaroundTime){
                optimalCombinationIndex = combinationIndex;
                break;
            }
        }
        
        return optimalCombinationIndex;
    }
    
    private void showAllocation(int combinationIndex){
        Integer[] combination = getCombination(combinationIndex);
        
        int totalTurnaroundTime = 0;
        
        int[] timeSpent = new int[noOfMemoryPartitions];
        for(int memPartitionIndex=0; memPartitionIndex<noOfMemoryPartitions; memPartitionIndex++)
            timeSpent[memPartitionIndex] = 0;
        
        ArrayList<ProgramData> allocation = new ArrayList();
        
        for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
            allocation.add(new ProgramData(programIndex, combination[programIndex], completionTime[programIndex][combination[programIndex]]));
        }
        
        Collections.sort(allocation, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                ProgramData a = (ProgramData)o1;
                ProgramData b = (ProgramData)o2;
                return a.completionTime - b.completionTime;
            }

        });
        
        for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
            ProgramData program = allocation.get(programIndex);
            int partition = program.allocatedPartition;
            int completionTime = program.completionTime;
            System.out.println("Program "+(program.programIndex+1)+" runs in region "+(partition+1)+
                    " from "+timeSpent[partition]+" to "+(timeSpent[partition]+completionTime));
            
            timeSpent[partition]+=completionTime;     
            totalTurnaroundTime+=timeSpent[partition];
        }
        System.out.println("Average turnaround time = "+totalTurnaroundTime/(float)noOfPrograms);
    }
    
    private boolean isValidCombination(Integer[] combination){
        
        for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
            int assignedPartition = combination[programIndex];
            if(completionTime[programIndex][assignedPartition]==-1)
                return false;
        }
        return true;
    }
    
    private int getCombinationTurnaroundTime(int combinationIndex){
        
        int turnaroundTime=0;        
        Integer[] combination = getCombination(combinationIndex);
        
        for(int memPartitionIndex=0; memPartitionIndex<noOfMemoryPartitions; memPartitionIndex++){
            
            //Get program set allocated to this partition
            ArrayList<Integer> allocatedProgramTime = new ArrayList<Integer>();
            for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
                if(combination[programIndex]==memPartitionIndex)
                    allocatedProgramTime.add(completionTime[programIndex][memPartitionIndex]);
            }
            Collections.sort(allocatedProgramTime);
            
            int timeSpent=0;
            for(int i=0; i<allocatedProgramTime.size(); i++){
                int programTime = allocatedProgramTime.get(i);
                turnaroundTime+=timeSpent+programTime;
                timeSpent+=programTime;
            }
            
        }
        return turnaroundTime;
    }
    
    private void showCompletionTimeMat(){
        for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
            for(int partitionIndex=0; partitionIndex<noOfMemoryPartitions; partitionIndex++){
                System.out.print(completionTime[programIndex][partitionIndex]+"\t");
            }
            System.out.println("");
        }
    }
    
    private void showInput(){
        System.out.println("# of Memory Partitions\t:"+noOfMemoryPartitions+"\n# of Programs\t\t:"+noOfPrograms+"\n");
        
        showCompletionTimeMat();
    }
    
    private Integer[] getCombination(int combinationIndex){
        Integer[] combination = new Integer[noOfPrograms];
        
        int number = combinationIndex;
        for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
            combination[noOfPrograms-programIndex-1] = number%noOfMemoryPartitions;
            number/=noOfMemoryPartitions;
        }
        return combination;
    }
    
    private int getCombinationIndex(int[] combination){
        int combinationIndex = 0;
        for(int programIndex=0; programIndex<noOfPrograms; programIndex++){
            combinationIndex+=power(noOfMemoryPartitions, (noOfPrograms-programIndex-1))*combination[programIndex];
        }
        return combinationIndex;
    }
    
    private int power(int base, int power){
        if(power==0)
            return 1;
        return base*power(base, power-1);
    }
}
