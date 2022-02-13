package com;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

public class SymbolSequence {
    public static final int[][] morphism = {
            { 0, 4, 9, 0, 8, 9, 0},
            { 1, 5,10, 1, 6,10, 1},
            { 2, 3,11, 2, 7,11, 2},
            { 3, 2, 6, 3,10, 6, 3},
            { 4, 0, 7, 4,11, 7, 4},
            { 5, 1, 8, 5, 9, 8, 5},
            { 6, 3, 2, 6, 3,10, 6},
            { 7, 4, 0, 7, 4,11, 7},
            { 8, 5, 1, 8, 5, 9, 8},
            { 9, 0, 4, 9, 0, 8, 9},
            {10, 1, 5,10, 1, 6,10},
            {11, 2, 3,11, 2, 7,11},
    };
    private static final int NUM_SYMBOLS = morphism.length;
    public static final char[] vectorMap = {'i', 'j', 'k', 'i', 'j', 'k', 'i', 'j', 'k', 'i', 'j', 'k'};
    public ArrayList<Integer> sequence;

    public SymbolSequence(int sequenceLength) {
        sequence = new ArrayList<>(sequenceLength);
        int index = 0;
        int appended = 0;
        while (appended < sequenceLength) {
            int currMorphismRule = 0;
            if (index != 0)
                currMorphismRule = sequence.get(index);
            int ruleIndex = 0;
            while (appended < sequenceLength
                    && ruleIndex < morphism[currMorphismRule].length) {
                sequence.add(morphism[currMorphismRule][ruleIndex]);
                appended++;
                ruleIndex++;
            }
            index++;
        }
    }

    public void extendSequenceToLength(int sequenceLength) {
        if (sequence.size()>=sequenceLength) {
            return;
        }
        sequence = new ArrayList<>(sequenceLength);
        int index = 0;
        int appended = 0;
        while (appended < sequenceLength) {
            int currMorphismRule = 0;
            if (index != 0)
                currMorphismRule = sequence.get(index);
            int ruleIndex = 0;
            while (appended < sequenceLength
                    && ruleIndex < morphism[currMorphismRule].length) {
                sequence.add(morphism[currMorphismRule][ruleIndex]);
                appended++;
                ruleIndex++;
            }
            index++;
        }
    }

    public int earliestSubwordMatch(int startIndex, int subwordLength) {
        extendSequenceToLength(startIndex + subwordLength + 1);
        for (int i=0; i<=startIndex; i++) {
            boolean match = true;
            for (int j=0; j<subwordLength; j++) {
                if (sequence.get(i + j) != (int)sequence.get(startIndex + j)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return startIndex;
    }

    /**
     * Get the index of the last new symbol.
     *
     * This assumes that it occurs in the first 1000 indices and that there is a known
     * set of 12 symbols, of which all will eventually appear.
     * @return the index of the last new symbol.
     */
    private int indexOfLastNewSymbol() {
        extendSequenceToLength(1000);
        HashSet<Integer> symbolsSeen = new HashSet<>();
        for (int i=0; i < sequence.size(); i++) {
            symbolsSeen.add(sequence.get(i));
            if (symbolsSeen.size() == morphism.length) {
                return i;
            }
        }
        throw new RuntimeException("Symbol sequence too short. Did not find one of the symbols.");
    }

    /**
     * Get the start index of the last new pair of symbols.
     *
     * This assumes that all subwords of length 2 in the images of the morphism will appear
     * and that they will appear in the first 1000 symbols.
     * @return the index of the last new pair of symbols.
     */
    private int indexOfLastNewSymbolPair() {
        extendSequenceToLength(1000);
        HashSet<Integer> expectedPairs = new HashSet<>();
        for (int[] morphismImage : morphism) {
            for (int j = 0; j < morphismImage.length - 1; j++) {
                int symbol1 = morphismImage[j];
                int symbol2 = morphismImage[j + 1];
                expectedPairs.add(symbol1 * morphism.length + symbol2);
            }
        }
        for (int i=0; i<sequence.size(); i++) {
            int symbol1 = sequence.get(i);
            int symbol2 = sequence.get(i + 1);
            int pair = symbol1 * morphism.length + symbol2;
            if (expectedPairs.contains(pair)) {
                expectedPairs.remove(pair);
            }
            if (expectedPairs.size() == 0) {
                return i;
            }
        }
        throw new RuntimeException("Symbol sequence too short. Did not find an expected pair of symbols.");
    }

    /**
     * Get the start index of the last new subword produced by iterating the morphism.
     *
     * @param wordLength: the length of the subword.
     * @return the index of the last new subword.
     */
    public int indexOfLastNewSubword(int wordLength) {
        return indexOfLastNewSubword(wordLength, null);
    }

    /**
     * Get the index of the last new subword produced by iterating the morphism.
     *
     * @param wordLength: the length of the subword.
     * @param dp: the Dynamic Programming memoization array.
     * @return the index of the last new subword.
     */
    private int indexOfLastNewSubword(int wordLength, Integer[] dp) {
        if (dp==null) {
            dp = new Integer[wordLength + 1];
        }
        if (dp[wordLength] != null) {
            return dp[wordLength];
        }
        if (wordLength == 1) {
            int index = indexOfLastNewSymbol();
            dp[wordLength] = index;
            return index;
        } else if (wordLength == 2) {
            int index = indexOfLastNewSymbolPair();
            dp[wordLength] = index;
            return index;
        }
        int prevSubwordLength = (int)Math.ceil((double) wordLength / 7.0) + 1;
        int maxCheckIndex = 7 * (indexOfLastNewSubword(prevSubwordLength, dp) + prevSubwordLength);
        if (maxCheckIndex + wordLength + 1 > sequence.size()) {
            extendSequenceToLength(maxCheckIndex + wordLength + 2);
        }
        System.out.printf("Working on word length: %d\n", wordLength);

        /*
        HashMap<Integer, ArrayList<Integer>> wordHashes = new HashMap<>();
        int lastNewSubwordIndex = 0;
        for (int i=0; i<=maxCheckIndex; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j=0; j<wordLength; j++) {
                sb.append('a'+sequence.get(i+j));
            }
            int wordHash = sb.toString().hashCode();
            if (wordHashes.containsKey(wordHash)) {
                boolean hasMatch = false;
                for (int otherStartIndex: wordHashes.get(wordHash)) {
                    boolean isMatch = true;
                    for (int j=0; j<wordLength; j++) {
                        if (sequence.get(i+j) != (int)sequence.get(otherStartIndex + j)) {
                            isMatch = false;
                            break;
                        }
                    }
                    if (isMatch) {
                        hasMatch = true;
                        break;
                    }
                }
                if (!hasMatch) {
                    wordHashes.get(wordHash).add(i);
                    lastNewSubwordIndex = i;
                }
            } else {
                ArrayList<Integer> indexList = new ArrayList<>();
                indexList.add(i);
                wordHashes.put(wordHash, indexList);
                lastNewSubwordIndex = i;
            }
        }

         */

        HashSet<BigInteger> wordSet = new HashSet<>();
        BigInteger base = new BigInteger("" + NUM_SYMBOLS);
        BigInteger maxPow = base.pow(wordLength-1);
        BigInteger word = BigInteger.ZERO;
        for (int i=0; i < wordLength; i++) {
            word = word.multiply(base);
            BigInteger addend = new BigInteger("" + sequence.get(i));
            word = word.add(addend);
        }
        wordSet.add(word);
        int lastNewSubwordIndex = 0;
        for (int j=wordLength; j < maxCheckIndex + wordLength; j++) {
            if ((j-wordLength) % 1000000 == 0) {
                System.out.printf("\tConsidering index %d/%d\n", j-wordLength, maxCheckIndex);
            }
            BigInteger subtractor = maxPow.multiply(new BigInteger("" + sequence.get(j - wordLength)));
            word = word.subtract(subtractor);
            word = word.multiply(base);
            BigInteger addend = new BigInteger("" + sequence.get(j));
            word = word.add(addend);
            int prevSize = wordSet.size();
            wordSet.add(word);
            if (wordSet.size() > prevSize) {
                lastNewSubwordIndex = j - wordLength + 1;
            }
        }

        dp[wordLength] = lastNewSubwordIndex;
        return lastNewSubwordIndex;
    }

    public int indexOfLastNewVectorSequence(int sequenceLength) {
        int upperBoundIndex = indexOfLastNewSubword(sequenceLength) + sequenceLength;
        if (upperBoundIndex + sequenceLength > sequence.size()) {
            extendSequenceToLength(upperBoundIndex + sequenceLength);
        }
        HashSet<String> vectorSequences = new HashSet<>();
        int lastNewIndex = 0;
        for (int i=0; i<upperBoundIndex; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j=0; j<sequenceLength; j++) {
                sb.append(vectorMap[sequence.get(i+j)]);
            }
            String vectorSequence = sb.toString();
            if (!vectorSequences.contains(vectorSequence)) {
                vectorSequences.add(vectorSequence);
                lastNewIndex = i;
            }
        }
        return lastNewIndex;
    }
}
