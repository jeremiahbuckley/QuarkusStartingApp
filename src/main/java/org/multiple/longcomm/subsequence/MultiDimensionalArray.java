package org.multiple.longcomm.subsequence;

import java.security.InvalidKeyException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.lang.Iterable;
import java.util.NoSuchElementException;

import javax.naming.SizeLimitExceededException;

public class MultiDimensionalArray<T> {
    int dimensions = 0;
    List<Integer> dimensionSizes = new ArrayList<>();
    T t;
    int maxAllowedSize = 20000;
    int maxAllowedDimensions = 6;

    Map<List<Integer>, T> dimensionSpace = new HashMap<List<Integer>, T>();

    public IndexIterator<T> reverseIndexIterator = null;

    public class IndexIterator<T> implements Iterable<List<Integer>> {

        MultiDimensionalArray<T> mda = null;
        T t;

        public IndexIterator(T t, MultiDimensionalArray mda) {
            this.mda = mda;
        }

        @Override
        public Iterator<List<Integer>> iterator() {
            BackwardsIterator<T> myIterator = new BackwardsIterator<T>(this.t, this.mda);
            return myIterator;
        }
        public class BackwardsIterator<E> implements Iterator<List<Integer>> {

            List<Integer> idx = new ArrayList<>();
            List<Integer> min = new ArrayList<>();
            MultiDimensionalArray<E> mda = null;
            Boolean reachedZero = false;


            public BackwardsIterator(E e, MultiDimensionalArray<E> incommingmda) {
                this.mda = incommingmda;
                
                for(int i = 0; i < this.mda.dimensionSizes.size(); i++) {
                    idx.add(dimensionSizes.get(i));
                    min.add(0);
                }    

                System.out.println("hi " + idx.toString() + " " + min.toString());
            }

            @Override
            public boolean hasNext() {
                return !reachedZero;
            }

            @Override
            public List<Integer> next() throws NoSuchElementException {
                if (reachedZero) {
                    throw new NoSuchElementException();
                }

                List<Integer> next = new ArrayList<>();
                for(int i: idx) {
                    next.add(i);
                }
                if (idx.equals(min)) {
                    reachedZero = true;
                }
        
                Boolean iteratedOneDigit = false;
                for (int digit = dimensionSizes.size() - 1; digit >= 0; digit-- ){
                    if (!iteratedOneDigit) {
                        if (idx.get(digit) > 0) {
                            idx.set(digit, idx.get(digit)-1);
                            for(int remainder = digit+1; remainder < dimensionSizes.size(); remainder++) {
                                idx.set(remainder, dimensionSizes.get(remainder));
                            }    
                            iteratedOneDigit = true;
                        }
                    }
                }

                return next;
            }
        }
    }

    MultiDimensionalArray(T t, int[] sizes) throws SizeLimitExceededException {
        this.dimensions = sizes.length;
        this.dimensionSizes = new ArrayList<Integer>();
        int maxSizeCheck = 0;
        for (int val : sizes) {
            dimensionSizes.add(val);
            maxSizeCheck *= val;
        }
        if (dimensionSizes.size() > maxAllowedDimensions) {
            throw new SizeLimitExceededException("Required set dimensions is " + dimensionSizes.size() + ". Cannot handle sets of size larger than " + maxAllowedDimensions + ".");
        }
        if (maxSizeCheck > maxAllowedSize) {
            throw new SizeLimitExceededException("Required memory set is " + maxSizeCheck + ". Cannot handle sets of size larger than " + maxAllowedSize + ".");
        }
        this.t = t;

        List<List<Integer>> workingIdxSet = new ArrayList<List<Integer>>();
        for(int i = sizes.length-1; i >= 0; i--){

            List<List<Integer>> newWorkingSet = new ArrayList<List<Integer>>();
            for(int j = 0; j < sizes[i] + 1; j++) {
                if (workingIdxSet.size() > 0) {
                    for(List<Integer> oneWorkingIdx : workingIdxSet) {
                        List<Integer> alist = new ArrayList<Integer>();
                        alist.add(Integer.valueOf(j));
                        alist.addAll(oneWorkingIdx);
                        newWorkingSet.add(alist);
                    }
                } else {
                    List<Integer> aList = new ArrayList<Integer>();
                    aList.add(Integer.valueOf(j));
                    newWorkingSet.add(aList);
                }
            };
            workingIdxSet = newWorkingSet;                
        }

        for(List<Integer> idx : workingIdxSet) {
            dimensionSpace.put(idx, t);
        }
    }

    MultiDimensionalArray(T t, List<Integer> sizes) throws SizeLimitExceededException {
        this.dimensions = sizes.size();
        this.dimensionSizes = new ArrayList<Integer>();
        int maxSizeCheck = 0;
        this.reverseIndexIterator = new IndexIterator(t, this);

        for (int val : sizes) {
            dimensionSizes.add(val);
            maxSizeCheck *= val;
        }
        if (dimensionSizes.size() > maxAllowedDimensions) {
            throw new SizeLimitExceededException("Required set dimensions is " + dimensionSizes.size() + ". Cannot handle sets of size larger than " + maxAllowedDimensions + ".");
        }
        if (maxSizeCheck > maxAllowedSize) {
            throw new SizeLimitExceededException("Required memory set is " + maxSizeCheck + ". Cannot handle sets of size larger than " + maxAllowedSize + ".");
        }
        // System.out.println(dimensionSizes.toString());
        this.t = t;

        List<List<Integer>> workingIdxSet = new ArrayList<List<Integer>>();
        for(int i = sizes.size()-1; i >= 0; i--){

            List<List<Integer>> newWorkingSet = new ArrayList<List<Integer>>();
            for(int j = 0; j < sizes.get(i) + 1; j++) {
                if (workingIdxSet.size() > 0) {
                    for(List<Integer> oneWorkingIdx : workingIdxSet) {
                        List<Integer> alist = new ArrayList<Integer>();
                        alist.add(Integer.valueOf(j));
                        alist.addAll(oneWorkingIdx);
                        newWorkingSet.add(alist);
                    }
                } else {
                    List<Integer> aList = new ArrayList<Integer>();
                    aList.add(Integer.valueOf(j));
                    newWorkingSet.add(aList);
                }
            };
            workingIdxSet = newWorkingSet;                
        }

        for(List<Integer> idx : workingIdxSet) {
            dimensionSpace.put(idx, t);
        }
        // for(Map.Entry<List<Integer>, T> kvp : dimensionSpace.entrySet()) {
        //     System.out.println(kvp.getKey().toString() + " : " + kvp.getValue().toString());
        // }
    }

    public T getData(List<Integer> idx) throws KeyException {
        if (idx.size() != dimensions) {
            throw new KeyException("Incorrect dimensionality. Expected: " + dimensions + " got: " + idx.size());
        }

        return this.t;
    }

    public T getData(int[] idx) throws KeyException {
        if (idx.length != dimensions) {
            throw new KeyException("Incorrect dimensionality. Expected: " + dimensions + " got: " + idx.length);
        }

        return this.t;
    }

    public String toString() {
        String output = "";


        List<Integer> idx = new ArrayList<>();
        List<Integer> max = new ArrayList<>();
        String gridTab = new String(new char[dimensionSizes.size()-2]).replace("\0", " ") ;
        for(int i = 0; i < dimensionSizes.size()-2; i++) {
            idx.add(0);
            max.add(dimensionSizes.get(i)-1);
        }

        if (dimensionSizes.size() == 2)
        {
            String grid = "";
            for (int j = 0; j < dimensionSizes.get(dimensionSizes.size()-2) + 1; j++) {
                String gridline = "";
                for(int k = 0; k < dimensionSizes.get(dimensionSizes.size()-1) + 1; k++) {
                    List<Integer> midx = new ArrayList<Integer>();
                    midx.addAll(idx); midx.add(j); midx.add(k);
                    if (gridline.length() > 0) {
                        gridline += " ";
                    }
                    gridline = gridline + dimensionSpace.get(midx).toString();
                }
                gridline += "\n";
                grid += gridTab + gridline;
            }
            grid += "\n";

            // System.out.println(grid);
            output += grid;

        } else {

            List<Integer> idxprinted = new ArrayList<>();
            for(int idxprint = 0; idxprint < dimensionSizes.size() -2; idxprint++) {
                idxprinted.add(0);
            }
            while(!idx.equals(max)) {

                for(int idxprint = 0; idxprint < dimensionSizes.size()-2; idxprint++) {
                    if (idxprinted.get(idxprint) == idx.get(idxprint) ) {
                        // System.out.println(new String(new char[idxprint]).replace("\0", " ") + idx.get(idxprint).toString());
                        output += new String(new char[idxprint]).replace("\0", " ") + idx.get(idxprint).toString();
                        idxprinted.set(idxprint, idxprinted.get(idxprint) + 1);
                        for(int i2 =idxprint+1; i2 < dimensionSizes.size()-2; i2++) {
                            idxprinted.set(i2, 0);
                        }

                    }
                }

                // System.out.println(idx.toString() + " " + max.toString());

                String grid = "";
                for (int j = 0; j < dimensionSizes.get(dimensionSizes.size()-2) + 1; j++) {
                    String gridline = "";
                    for(int k = 0; k < dimensionSizes.get(dimensionSizes.size()-1) + 1; k++) {
                        List<Integer> midx = new ArrayList<Integer>();
                        midx.addAll(idx); midx.add(j); midx.add(k);
                        if (gridline.length() > 0) {
                            gridline += " ";
                        }
                        gridline = gridline + dimensionSpace.get(midx).toString();
                    }
                    gridline += "\n";
                    grid += gridTab + gridline;
                }
                grid += "\n";

                // System.out.println(grid);
                output += grid;

                Boolean incrementOne = false;
                for (int digit = dimensionSizes.size() - 3; digit >= 0; digit-- ){
                    if (!incrementOne) {
                        if (idx.get(digit) < dimensionSizes.get(digit)-1) {
                            idx.set(digit, idx.get(digit)+1);
                            for(int remainder = digit+1; remainder < dimensionSizes.size() - 3; remainder++) {
                                idx.set(remainder, 0);
                            }    
                            incrementOne = true;
                        }
                    }
                }
            }
        }

        return output;
    }

    public T get(List<Integer> idx) {
        return dimensionSpace.get(idx);
    }

    public void put(List<Integer> idx, T val) {
        dimensionSpace.put(idx, val);
    }

    static List<List<Boolean>> generatePossibleDirections(int size) throws InvalidKeyException {
        List<List<Boolean>> possibleDirections = new ArrayList<>();

        switch(size) {
            case 2: {
                possibleDirections.add(Arrays.asList(false, true));

                possibleDirections.add(Arrays.asList(true, false));
                possibleDirections.add(Arrays.asList(true, true));
                break;
            }
            case 3: {
                possibleDirections.add(Arrays.asList(false, false, true));
                possibleDirections.add(Arrays.asList(false, true, false));
                possibleDirections.add(Arrays.asList(false, true, true));

                possibleDirections.add(Arrays.asList(true, false, false));
                possibleDirections.add(Arrays.asList(true, false, true));
                possibleDirections.add(Arrays.asList(true, true, false));
                possibleDirections.add(Arrays.asList(true, true, true));
                break;
            }
            case 4: {
                possibleDirections.add(Arrays.asList(false, false, false, true));
                possibleDirections.add(Arrays.asList(false, false, true, false));
                possibleDirections.add(Arrays.asList(false, false, true, true));
                possibleDirections.add(Arrays.asList(false, true, false, false));
                possibleDirections.add(Arrays.asList(false, true, false, true));
                possibleDirections.add(Arrays.asList(false, true, true, false));
                possibleDirections.add(Arrays.asList(false, true, true, true));

                possibleDirections.add(Arrays.asList(true, false, false, false));
                possibleDirections.add(Arrays.asList(true, false, false, true));
                possibleDirections.add(Arrays.asList(true, false, true, false));
                possibleDirections.add(Arrays.asList(true, false, true, true));
                possibleDirections.add(Arrays.asList(true, true, false, false));
                possibleDirections.add(Arrays.asList(true, true, false, true));
                possibleDirections.add(Arrays.asList(true, true, true, false));
                possibleDirections.add(Arrays.asList(true, true, true, true));
                break;
            }
            case 5: {
                possibleDirections.add(Arrays.asList(false, false, false, false, true));
                possibleDirections.add(Arrays.asList(false, false, false, true, false));
                possibleDirections.add(Arrays.asList(false, false, false, true, true));
                possibleDirections.add(Arrays.asList(false, false, true, false, false));
                possibleDirections.add(Arrays.asList(false, false, true, false, true));
                possibleDirections.add(Arrays.asList(false, false, true, true, false));
                possibleDirections.add(Arrays.asList(false, false, true, true, true));
                possibleDirections.add(Arrays.asList(false, true, false, false, false));
                possibleDirections.add(Arrays.asList(false, true, false, false, true));
                possibleDirections.add(Arrays.asList(false, true, false, true, false));
                possibleDirections.add(Arrays.asList(false, true, false, true, true));
                possibleDirections.add(Arrays.asList(false, true, true, false, false));
                possibleDirections.add(Arrays.asList(false, true, true, false, true));
                possibleDirections.add(Arrays.asList(false, true, true, true, false));
                possibleDirections.add(Arrays.asList(false, true, true, true, true));

                possibleDirections.add(Arrays.asList(true, false, false, false, false));
                possibleDirections.add(Arrays.asList(true, false, false, false, true));
                possibleDirections.add(Arrays.asList(true, false, false, true, false));
                possibleDirections.add(Arrays.asList(true, false, false, true, true));
                possibleDirections.add(Arrays.asList(true, false, true, false, false));
                possibleDirections.add(Arrays.asList(true, false, true, false, true));
                possibleDirections.add(Arrays.asList(true, false, true, true, false));
                possibleDirections.add(Arrays.asList(true, false, true, true, true));
                possibleDirections.add(Arrays.asList(true, true, false, false, false));
                possibleDirections.add(Arrays.asList(true, true, false, false, true));
                possibleDirections.add(Arrays.asList(true, true, false, true, false));
                possibleDirections.add(Arrays.asList(true, true, false, true, true));
                possibleDirections.add(Arrays.asList(true, true, true, false, false));
                possibleDirections.add(Arrays.asList(true, true, true, false, true));
                possibleDirections.add(Arrays.asList(true, true, true, true, false));
                possibleDirections.add(Arrays.asList(true, true, true, true, true));
                break;
            }
            case 6: {
                possibleDirections.add(Arrays.asList(false, false, false, false, false, true));
                possibleDirections.add(Arrays.asList(false, false, false, false, true, false));
                possibleDirections.add(Arrays.asList(false, false, false, false, true, true));
                possibleDirections.add(Arrays.asList(false, false, false, true, false, false));
                possibleDirections.add(Arrays.asList(false, false, false, true, false, true));
                possibleDirections.add(Arrays.asList(false, false, false, true, true, false));
                possibleDirections.add(Arrays.asList(false, false, false, true, true, true));
                possibleDirections.add(Arrays.asList(false, false, true, false, false, false));
                possibleDirections.add(Arrays.asList(false, false, true, false, false, true));
                possibleDirections.add(Arrays.asList(false, false, true, false, true, false));
                possibleDirections.add(Arrays.asList(false, false, true, false, true, true));
                possibleDirections.add(Arrays.asList(false, false, true, true, false, false));
                possibleDirections.add(Arrays.asList(false, false, true, true, false, true));
                possibleDirections.add(Arrays.asList(false, false, true, true, true, false));
                possibleDirections.add(Arrays.asList(false, false, true, true, true, true));
                possibleDirections.add(Arrays.asList(false, true, false, false, false, false));
                possibleDirections.add(Arrays.asList(false, true, false, false, false, true));
                possibleDirections.add(Arrays.asList(false, true, false, false, true, false));
                possibleDirections.add(Arrays.asList(false, true, false, false, true, true));
                possibleDirections.add(Arrays.asList(false, true, false, true, false, false));
                possibleDirections.add(Arrays.asList(false, true, false, true, false, true));
                possibleDirections.add(Arrays.asList(false, true, false, true, true, false));
                possibleDirections.add(Arrays.asList(false, true, false, true, true, true));
                possibleDirections.add(Arrays.asList(false, true, true, false, false, false));
                possibleDirections.add(Arrays.asList(false, true, true, false, false, true));
                possibleDirections.add(Arrays.asList(false, true, true, false, true, false));
                possibleDirections.add(Arrays.asList(false, true, true, false, true, true));
                possibleDirections.add(Arrays.asList(false, true, true, true, false, false));
                possibleDirections.add(Arrays.asList(false, true, true, true, false, true));
                possibleDirections.add(Arrays.asList(false, true, true, true, true, false));
                possibleDirections.add(Arrays.asList(false, true, true, true, true, true));

                possibleDirections.add(Arrays.asList(true, false, false, false, false, false));
                possibleDirections.add(Arrays.asList(true, false, false, false, false, true));
                possibleDirections.add(Arrays.asList(true, false, false, false, true, false));
                possibleDirections.add(Arrays.asList(true, false, false, false, true, true));
                possibleDirections.add(Arrays.asList(true, false, false, true, false, false));
                possibleDirections.add(Arrays.asList(true, false, false, true, false, true));
                possibleDirections.add(Arrays.asList(true, false, false, true, true, false));
                possibleDirections.add(Arrays.asList(true, false, false, true, true, true));
                possibleDirections.add(Arrays.asList(true, false, true, false, false, false));
                possibleDirections.add(Arrays.asList(true, false, true, false, false, true));
                possibleDirections.add(Arrays.asList(true, false, true, false, true, false));
                possibleDirections.add(Arrays.asList(true, false, true, false, true, true));
                possibleDirections.add(Arrays.asList(true, false, true, true, false, false));
                possibleDirections.add(Arrays.asList(true, false, true, true, false, true));
                possibleDirections.add(Arrays.asList(true, false, true, true, true, false));
                possibleDirections.add(Arrays.asList(true, false, true, true, true, true));
                possibleDirections.add(Arrays.asList(true, true, false, false, false, false));
                possibleDirections.add(Arrays.asList(true, true, false, false, false, true));
                possibleDirections.add(Arrays.asList(true, true, false, false, true, false));
                possibleDirections.add(Arrays.asList(true, true, false, false, true, true));
                possibleDirections.add(Arrays.asList(true, true, false, true, false, false));
                possibleDirections.add(Arrays.asList(true, true, false, true, false, true));
                possibleDirections.add(Arrays.asList(true, true, false, true, true, false));
                possibleDirections.add(Arrays.asList(true, true, false, true, true, true));
                possibleDirections.add(Arrays.asList(true, true, true, false, false, false));
                possibleDirections.add(Arrays.asList(true, true, true, false, false, true));
                possibleDirections.add(Arrays.asList(true, true, true, false, true, false));
                possibleDirections.add(Arrays.asList(true, true, true, false, true, true));
                possibleDirections.add(Arrays.asList(true, true, true, true, false, false));
                possibleDirections.add(Arrays.asList(true, true, true, true, false, true));
                possibleDirections.add(Arrays.asList(true, true, true, true, true, false));
                possibleDirections.add(Arrays.asList(true, true, true, true, true, true));
                break;
            }
            default: {
                throw new InvalidKeyException("Unexpected size " + size, null);
            }
        }

        // todo remove once debug confirms correctness
        List<Boolean> direction = possibleDirections.get(0);
        for (List<Boolean> dir : possibleDirections) {
            if (direction.size() != dir.size()) {
                throw new InvalidKeyException("Irregular lengths for size: " + size);
            }
        }

        return possibleDirections;

    }
}
