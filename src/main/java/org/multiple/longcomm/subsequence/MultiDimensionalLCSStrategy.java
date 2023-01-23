package org.multiple.longcomm.subsequence;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;


public class MultiDimensionalLCSStrategy {
    
    Boolean VERBOSE = false;
    Boolean DEBUG = false;
    Boolean TIMED_STATUS = false;
    Long NEXT_INTERVAL = 0L;
    Integer SECONDS_CONST_15 = 0;

    MultiDimensionalArray<Integer> scoreKeeper = null;
    MultiDimensionalArray<List<List<Integer>>> incomingDirectionKeeper = null;

    public MultiDimensionalLCSStrategy(Boolean verbose, Boolean debug, Boolean timed_status, long next_interval, int seconds_const_15) {
        this.VERBOSE = verbose;
        this.DEBUG = debug;
        this.TIMED_STATUS = timed_status;
        this.NEXT_INTERVAL = next_interval;
        this.SECONDS_CONST_15 = seconds_const_15;

    }

    public int getFinalScore() {
        return scoreKeeper.get(scoreKeeper.lastIndex());
    }

    public Map<List<Integer>, List<List<Integer>>> scoreSpace(List<String[]> nucs, Scoring scoring) throws Exception, IllegalStateException {
        List<Integer> dimensions = new ArrayList<Integer>();
        for(String[] nuc : nucs) {
            dimensions.add(nuc.length);
        }
        scoreKeeper = new MultiDimensionalArray<Integer>(0, dimensions);
        incomingDirectionKeeper = new MultiDimensionalArray<List<List<Integer>>>(new ArrayList<List<Integer>>(), dimensions);

        buildWorkingStateMDA(nucs, scoring, scoreKeeper, incomingDirectionKeeper);

        calculateScoresMDA(nucs, scoring, scoreKeeper, incomingDirectionKeeper);

        Map<List<Integer>, List<List<Integer>>> existingPaths = new HashMap<List<Integer>, List<List<Integer>>>();

        try {
            walkBackwardsMDA(incomingDirectionKeeper, nucs, existingPaths);
        } catch (Exception e) {
            System.out.print(e);
            throw e;
        }

        return existingPaths;
    }

    public void buildWorkingStateMDA(List<String[]> nucs, Scoring scoring, MultiDimensionalArray<Integer> scoreKeeper, MultiDimensionalArray<List<List<Integer>>> incomingDirectionKeeper) {

        for(List<Integer> idx : incomingDirectionKeeper.reverseIndexIterator) {
            System.out.println(idx.toString());
            int nzCount = 0;
            for(int d : idx) {
                if (d != 0) {
                    nzCount++;
                }
            }

            if (nzCount < idx.size()) {
                int maxDistance = 0;
                for(int d: idx) {
                    if ((d != 0) && (d > maxDistance)) {
                        maxDistance = d;
                    }
                }
                scoreKeeper.put(idx, (scoring.indelPenalty * maxDistance));
            }


            int nzFound = 0;
            List<List<Integer>> dirAvailable = new ArrayList<List<Integer>>();
            // note: not Math.pow(2, nzCount) - 1, the total list is 2^n-1, so , have to do -2 for the cycle control
            for(int i = 0; i < Math.pow(2, nzCount) -1; i++) {
                dirAvailable.add(new ArrayList<Integer>());
            }

            for(int d : idx) {
                if (d == 0) {
                    for(List<Integer> lb : dirAvailable) {
                        lb.add(0);
                    }
                } else {
                    int cycle = (int)Math.round(Math.pow(2, (nzCount-nzFound)));
                    int halfcycle = cycle / 2;
                    int cLoc = 1; // note: not Zero, start one-up on the cylce
                    for(int i = 0; i < dirAvailable.size(); i++) {
                        Integer dv = (cLoc < halfcycle) ? d : d - 1;
                        dirAvailable.get(i).add(dv);
                        cLoc = (cLoc + 1) % cycle;
                    }
                    nzFound++;
                }
            }

            List<List<Integer>> copyToDirAvailable = new ArrayList<List<Integer>>();
            for(List<Integer> da : dirAvailable) {
                copyToDirAvailable.add(da);
            }

            incomingDirectionKeeper.put(idx, copyToDirAvailable);
        }

        if (VERBOSE) {
            System.out.println("prep");
            printSpaceMDA(scoreKeeper, incomingDirectionKeeper);    
        }
    }


    public void calculateScoresMDA(List<String[]> nucs, Scoring scoring, MultiDimensionalArray<Integer> scoreKeeper, MultiDimensionalArray<List<List<Integer>>> incomingDirectionKeeper) {
        for(List<Integer> idx : incomingDirectionKeeper.forwardIndexIterator) {
            int zCount = 0;
            List<Integer> perfectAlignmentIdx = new ArrayList<Integer>();
            for(int d : idx) {
                if (d == 0) {
                    zCount++;
                }
                perfectAlignmentIdx.add(d-1);
            }
            if (zCount > 0)
                continue;
            
            List<List<Integer>> contributingIdx = new ArrayList<List<Integer>>();
            for(int i = 0; i < Math.pow(2, idx.size()) -1; i++) {
                contributingIdx.add(new ArrayList<Integer>());
            }
    

            for(int i = 0; i < idx.size(); i++) {
                int cycle = (int)Math.round(Math.pow(2, idx.size() - i));
                int halfcycle = cycle / 2;
                int cLoc = 1; // note: not Zero, start one-up on the cylce
                for(int j = 0; j < contributingIdx.size(); j++) {
                    Integer dv = (cLoc < halfcycle) ? idx.get(i) : idx.get(i) - 1;
                    contributingIdx.get(j).add(dv);
                    cLoc = (cLoc + 1) % cycle;
                }
            }

            int matchAdvantage = Integer.MIN_VALUE;
            List<Integer> possibleScores = new ArrayList<Integer>();
            for(List<Integer> cidx : contributingIdx) {
                if (cidx.equals(perfectAlignmentIdx)) {
                    List<String> proteinsToCompare = new ArrayList<String>();
                    for(int i = 0; i < cidx.size(); i++) {
                        proteinsToCompare.add(nucs.get(i)[cidx.get(i)]);
                    }
                    matchAdvantage = scoring.getMatchVal(proteinsToCompare);
                    possibleScores.add(scoreKeeper.get(cidx) + matchAdvantage);
                } else {
                    possibleScores.add(scoreKeeper.get(cidx) + scoring.indelPenalty);
                }
            }
            int score = Collections.max(possibleScores);

            List<List<Integer>> incommingDirectionsIdx = new ArrayList<>();
            for(List<Integer> cidx : contributingIdx) {
                int idxScore = cidx.equals(perfectAlignmentIdx) ? scoreKeeper.get(cidx) + matchAdvantage : scoreKeeper.get(cidx) + scoring.indelPenalty;
                if (score == idxScore) {
                    incommingDirectionsIdx.add(cidx);
                }
            }
            scoreKeeper.put(idx, score);
            incomingDirectionKeeper.put(idx, incommingDirectionsIdx);

        }

        if (VERBOSE) {
            System.out.println("complete");
            printSpaceMDA(scoreKeeper, incomingDirectionKeeper);
        }
    }

    public void walkBackwardsMDA(MultiDimensionalArray<List<List<Integer>>> incomingDirectionKeeper, List<String[]> nucs, Map<List<Integer>, List<List<Integer>>> existingPaths) throws Exception, IllegalStateException {

        Stack<List<Object>> loopStack = new Stack<List<Object>>();

        List<Object> newStackFrame = new ArrayList<Object>();        
        List<Integer> lastNode = incomingDirectionKeeper.lastIndex();
        newStackFrame.add(lastNode);
        newStackFrame.add(Integer.valueOf(0));
        
        loopStack.push(newStackFrame);

        while (!loopStack.empty()) {
            List<Object> currentStackFrame = loopStack.pop();
            List<Integer> currentNode = (List<Integer>) currentStackFrame.get(0);
            Integer tabs = (Integer) currentStackFrame.get(1);

            for(int idx : currentNode) {
                if (idx < 0) {
                    throw new Exception("  " + new String(new char[tabs]).replace("\0", " ") + "invalid value: " + currentNode.toString());    
                }
            }

            if (DEBUG) {
                System.out.println("  " + new String(new char[tabs]).replace("\0"," ") + "cn: " + currentNode.toString());
            }
    
            if (existingPaths.containsKey(currentNode)) {
                List<List<Integer>> followNode = (List<List<Integer>>) currentStackFrame.get(2);
                if (!existingPaths.get(followNode).contains(currentNode)) {
                    existingPaths.get(followNode).add(currentNode);
                }
                continue;
            }

            List<Integer> firstNode = incomingDirectionKeeper.firstIndex();
            if (currentNode.equals(firstNode)) {
                if (!existingPaths.containsKey(currentNode)) {
                    existingPaths.put(currentNode, new ArrayList<>());
                }

                try {
                    List<List<Integer>> followNode = (List<List<Integer>>) currentStackFrame.get(2);
                    existingPaths.get(followNode).add(currentNode);
                } catch (IndexOutOfBoundsException e) {
                    int ij = 0;
                    // no-op, this can happen at times
                }
                continue;
            }

            if (TIMED_STATUS && (NEXT_INTERVAL < System.currentTimeMillis())) {
                System.out.println(Long.toString(System.currentTimeMillis()) + " " + Long.toString(System.currentTimeMillis() + NEXT_INTERVAL));
                System.out.println("timed alert - walk_backwards - node: " + currentNode.toString());
                NEXT_INTERVAL = System.currentTimeMillis() + SECONDS_CONST_15;
            }


            for(List<Integer> prevNode : incomingDirectionKeeper.get(currentNode)) {
                List<Object> nextStackFrame = new ArrayList<Object>();

                nextStackFrame.add(prevNode);
                tabs += 1;
                nextStackFrame.add(tabs);
                nextStackFrame.add(currentNode);
                loopStack.push(nextStackFrame);
            }

            if (!existingPaths.containsKey(currentNode)) {
                existingPaths.put(currentNode, new ArrayList<List<Integer>>());
            }

            try {
                List<List<Integer>> followNode = (List<List<Integer>>) currentStackFrame.get(2);
                existingPaths.get(followNode).add(currentNode);
            } catch (IndexOutOfBoundsException e) {
                int ij = 0;
                // no-op, this can happen at times
            }

            if (DEBUG) {
                System.out.println(new String(new char[tabs]).replace("\0", " ") + "findPath - existingPaths");            
                for(Map.Entry<List<Integer>, List<List<Integer>>> kvp : existingPaths.entrySet()) {
                    System.out.println(new String(new char[tabs]).replace("\0", " ") + kvp.getKey().toString());
                    for(List<Integer> n : kvp.getValue()){
                        System.out.println(new String(new char[tabs+1]).replace("\0", " ") + n.toString());
                    }
                }
                System.out.println();
            }
    
        }
        return;
    }

    public void printSpaceMDA(MultiDimensionalArray<Integer> scoreKeeper, MultiDimensionalArray<List<List<Integer>>> incomingDirectionKeeper) {

        System.out.println("scores");
        System.out.println(scoreKeeper.toString());
        System.out.println();
        System.out.println("directions");
        System.out.println(incomingDirectionKeeper.toString());
        System.out.println();

    }


}
