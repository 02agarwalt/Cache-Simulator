/*
Author: Tanay Agarwal
JHED: tagarwa2
Class: CSF
Assignment: 7
*/

import java.util.Scanner;
import java.util.BitSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

/**
CacheSimulator class for problem 2.
*/
public final class CacheSimulator {
    /** Hexadecimal constant. */
    static final int HEX = 16;
    
    /** Percentage constant. */
    static final int PERCENT = 100;
    
    /** Number of bits in memory addresses. */
    static final int MEM_BITS = 32;
    
    /** Constructor for checkstyle compliance. */
    private CacheSimulator() {
        
    }
    
    /** 
    Main. 
    
    @param args command line arguments.
    */
    public static void main(String[] args) {
        if (args.length != 2 + 2 + 2 + 1) {
            System.out.println("Not enough command-line arguments.");
            return;
        }
        
        try {
            int x = 0;
            int arg0 = Integer.parseInt(args[x++]);
            int arg1 = Integer.parseInt(args[x++]);
            int arg2 = Integer.parseInt(args[x++]);
            int arg3 = Integer.parseInt(args[x++]);
            int arg4 = Integer.parseInt(args[x++]);
            int arg5 = Integer.parseInt(args[x++]);
            
            if (!paramsValid(arg0, arg1, arg2, arg3, arg4, arg5)) {
                System.out.println("Invalid command-line arguments.");
                return;   
            }
            
            File file = new File(args[x]);
            Scanner input = new Scanner(file);
            
            performSimulation(arg0, arg1, arg2, arg3, arg4, arg5, input);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid command-line arguments.");
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
        }
    }
    
    /**
    Selects which cache to simulate.
    
    @param sets number of sets in cache.
    @param blocks number of blocks in each set.
    @param bytes number of bytes in each block.
    @param wa write allocate or not.
    @param wt write through or not (write-back).
    @param evic eviction policy.
    @param input input Scanner.
    */
    public static void performSimulation(int sets, int blocks, int bytes,
        int wa, int wt, int evic, Scanner input) {
        
        if (blocks == 1) {
            if (wt == 1) {
                directMappedWT(sets, bytes, wa, input);
            } else {
                directMappedWB(sets, bytes, input);
            }
        } else {
            if (wt == 1) {
                if (evic == 0) {
                    saFifoWT(sets, blocks, bytes, wa, input);
                } else {
                    saLruWT(sets, blocks, bytes, wa, input);
                }
            }
            if (wt == 0) {
                if (evic == 0) {
                    saFifoWB(sets, blocks, bytes, input);
                } else {
                    saLruWB(sets, blocks, bytes, input);
                }
            }
        }
           
    }
    
    /**
    Checks validity of command-line arguments.
    Made a new function for checkstyle compliance.
    
    @param sets number of sets in cache.
    @param blocks number of blocks in each set.
    @param bytes number of bytes in each block.
    @param wa write allocate or not.
    @param wt write through or not (write-back).
    @param evic eviction policy.
    @return true if valid, false if invalid.
    */
    public static boolean paramsValid(int sets, int blocks, int bytes,
        int wa, int wt, int evic) {
        
        int[] arr = new int[2 + 2 + 2];
        int x = 0;
        arr[x++] = sets;
        arr[x++] = blocks;
        arr[x++] = bytes;
        arr[x++] = wa;
        arr[x++] = wt;
        arr[x++] = evic;
        
        for (int i = 0; i < 2 + 1; i++) {
            if (!((arr[i] & (arr[i] - 1)) == 0)) {
                return false;
            }
            if (!(arr[i] > 0)) {
                return false;
            }
            if (!(arr[2] >= 2 + 2)) {
                return false;
            }
        }
        
        for (int i = 2 + 1; i < arr.length; i++) {
            if (!(arr[i] == 0 || arr[i] == 1)) {
                return false;
            }
        }
        
        if (wt == 0 && wa == 0) {
            return false;
        }
        
        return true;
    }
    
    /**
    Creates a 2d array of BitSets.
    
    @param rows number of rows.
    @param cols number of columns.
    @param bits number of bits in each BitSet.
    @return 2d array of BitSet objects.
    */
    public static BitSet[][] newBitSetArray(int rows, int cols, int bits) {
        BitSet[][] output = new BitSet[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                output[r][c] = new BitSet(bits);
            }
        }
        return output;
    }
    
    /**
    Direct-mapped cache, write-back.
    
    @param sets number of rows in cache.
    @param bytes number of bytes in each row.
    @param input input Scanner.
    */
    public static void directMappedWB(int sets, int bytes, Scanner input) {
        int setBits = (int) (Math.log(sets) / Math.log(2));
        int byteBits = (int) (Math.log(bytes) / Math.log(2));
        int tagBits = MEM_BITS - setBits - byteBits;
        int multiplier = bytes / (2 * 2);
        
        BitSet[] cache = new BitSet[sets];
        for (int i = 0; i < sets; i++) {
            cache[i] = new BitSet(tagBits + 2);
        }
        
        long loadTot = 0;
        long storeTot = 0;
        long loadHits = 0;
        long storeHits = 0;
        long loadMisses = 0;
        long storeMisses = 0;
        long cycleTot = 0;
        
        while (input.hasNext()) {
            String ls = input.next();
            String addr = input.next();
            long address = Long.parseLong(addr.substring(2), HEX);
            input.next();
            
            int setIndex = (int) (address >>> byteBits) & (sets - 1);
            
            boolean hit = false;
            
            if (cache[setIndex].get(tagBits) //valid bit
                && value(cache[setIndex].get(0, tagBits)) 
                == (address >>> (byteBits + setBits))) {
                
                if (ls.equals("l")) {
                    loadTot++;
                    loadHits++;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeHits++;
                    cycleTot++;
                    cache[setIndex].set(tagBits + 1);
                }
            } else {
                if (ls.equals("l")) {
                    loadTot++;
                    loadMisses++;
                    
                    if (cache[setIndex].get(tagBits + 1)) {
                        cycleTot = cycleTot + PERCENT * multiplier;
                        cache[setIndex].clear(tagBits + 1);
                    }
                    
                    int tag = (int) (address >>> (setBits + byteBits));
                    cache[setIndex] = newBitSet(tagBits + 2, tag);
                    cache[setIndex].set(tagBits);
                    
                    cycleTot = cycleTot + PERCENT * multiplier;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeMisses++;
                    
                    if (cache[setIndex].get(tagBits + 1)) {
                        cycleTot = cycleTot + PERCENT * multiplier;
                        cache[setIndex].clear(tagBits + 1);
                    }
                        
                    int tag = (int) (address >>> (setBits + byteBits));
                    cache[setIndex] = newBitSet(tagBits + 2, tag);
                    cache[setIndex].set(tagBits);
                    cache[setIndex].set(tagBits + 1);
                    cycleTot = cycleTot + PERCENT * multiplier;
                        
                    cycleTot++;
                    cache[setIndex].set(tagBits + 1);
                }
            }
        }
        input.close();
        
        System.out.println("Total loads: " + loadTot);
        System.out.println("Total stores: " + storeTot);
        System.out.println("Load hits: " + loadHits);
        System.out.println("Load misses: " + loadMisses);
        System.out.println("Store hits: " + storeHits);
        System.out.println("Store misses: " + storeMisses);
        System.out.println("Total cycles: " + cycleTot);
    }
    
    /**
    Converts BitSet to integer.
    
    @param set the BitSet to convert.
    @return the int equivalent of set.
    */
    public static int value(BitSet set) {
        int output = 0;
        
        int length = set.length();
        
        for (int i = 0; i < length; i++) {
            if (set.get(i)) {
                output = output + (int) Math.pow(2, i);
            }
        }
        
        return output;
    }
    
    /**
    Creates BitSet with specified value.
    
    @param bits number of bits in new BitSet.
    @param val the value of the created BitSet.
    @return new BitSet with desired value.
    */
    public static BitSet newBitSet(int bits, int val) {
        BitSet output = new BitSet(bits);
        
        for (int i = 0; i < bits; i++) {
            if ((val & ((int) Math.pow(2, i))) == ((int) Math.pow(2, i))) {
                output.set(i);
            }
        }
        
        return output;
    }
    
    /**
    Direct-mapped cache, write-through.
    
    @param sets number of rows in cache.
    @param bytes number of bytes in each row.
    @param wa write-allocate or not.
    @param input input Scanner.
    */
    public static void directMappedWT(int sets, int bytes, int wa, 
        Scanner input) {
        int setBits = (int) (Math.log(sets) / Math.log(2));
        int byteBits = (int) (Math.log(bytes) / Math.log(2));
        int tagBits = MEM_BITS - setBits - byteBits;
        int multiplier = bytes / (2 * 2);
        
        BitSet[] cache = new BitSet[sets];
        for (int i = 0; i < sets; i++) {
            cache[i] = new BitSet(tagBits + 2);
        }
        
        long loadTot = 0;
        long storeTot = 0;
        long loadHits = 0;
        long storeHits = 0;
        long loadMisses = 0;
        long storeMisses = 0;
        long cycleTot = 0;
        
        while (input.hasNext()) {
            String ls = input.next();
            String addr = input.next();
            long address = Long.parseLong(addr.substring(2), HEX);
            input.next();
            
            int setIndex = (int) (address >>> byteBits) & (sets - 1);
            
            boolean hit = false;
            
            if (cache[setIndex].get(tagBits) //valid bit
                && value(cache[setIndex].get(0, tagBits)) 
                == (address >>> (byteBits + setBits))) {
                
                if (ls.equals("l")) {
                    loadTot++;
                    loadHits++;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeHits++;
                    cycleTot++;
                    cycleTot = cycleTot + PERCENT; //write-through
                }
            } else {
                if (ls.equals("l")) {
                    loadTot++;
                    loadMisses++;
                    
                    int tag = (int) (address >>> (setBits + byteBits));
                    cache[setIndex] = newBitSet(tagBits + 2, tag);
                    cache[setIndex].set(tagBits);
                    
                    cycleTot = cycleTot + PERCENT * multiplier;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeMisses++;
                    
                    if (wa == 1) {
                        int tag = (int) (address >>> (setBits + byteBits));
                        cache[setIndex] = newBitSet(tagBits + 2, tag);
                        cache[setIndex].set(tagBits);
                        cycleTot = cycleTot + PERCENT * multiplier;
                        
                        cycleTot++;
                        cycleTot = cycleTot + PERCENT;
                    } else {
                        cycleTot = cycleTot + PERCENT;
                    }
                }
            }
        }
        input.close();
        
        System.out.println("Total loads: " + loadTot);
        System.out.println("Total stores: " + storeTot);
        System.out.println("Load hits: " + loadHits);
        System.out.println("Load misses: " + loadMisses);
        System.out.println("Store hits: " + storeHits);
        System.out.println("Store misses: " + storeMisses);
        System.out.println("Total cycles: " + cycleTot);
    }
    
    /**
    Returns array of LinkedLists.
    
    @param sets number of LinkedLists.
    @return LinkedList<BitSet>[]
    */
    public static LinkedList<BitSet>[] newLinkedList(int sets) {
        @SuppressWarnings("unchecked")
        LinkedList<BitSet>[] cache = new LinkedList[sets];
        for (int i = 0; i < sets; i++) {
            cache[i] = new LinkedList<BitSet>();
        }
        return cache;
    }
    /**
    Finds blockIndex.
    
    @param cache array of linkedlists.
    @param setIndex the set index.
    @param tagBits number of tag bits.
    @param address memory address.
    @param byteBits number of byte bits.
    @param setBits number of set bits.
    @return block index.
    */
    public static int getBlockIndex(LinkedList<BitSet>[] cache, int setIndex, 
        int tagBits, long address, int byteBits, int setBits) {
        
        for (int i = 0; i < cache[setIndex].size(); i++) {
            BitSet temp = cache[setIndex].get(i);
                
            if (temp.get(tagBits) && value(temp.get(0, tagBits))
                == (address >>> (byteBits + setBits))) {
                  
                return i;
            }
        }
        return -1;
    }
    
    /**
    Set-associative cache, write-through, FIFO.
    
    @param sets number of rows in cache.
    @param blocks number of blocks in each set.
    @param bytes number of bytes in each row.
    @param wa write-allocate or not.
    @param input input Scanner.
    */
    public static void saFifoWT(int sets, int blocks, int bytes, int wa, 
        Scanner input) {
        int setBits = (int) (Math.log(sets) / Math.log(2));
        int byteBits = (int) (Math.log(bytes) / Math.log(2));
        int tagBits = MEM_BITS - setBits - byteBits;
        int multiplier = bytes / (2 * 2);
        
        LinkedList<BitSet>[] cache = newLinkedList(sets);
        
        long loadTot = 0;
        long storeTot = 0;
        long loadHits = 0;
        long storeHits = 0;
        long loadMisses = 0;
        long storeMisses = 0;
        long cycleTot = 0;
        
        while (input.hasNext()) {
            String ls = input.next();
            String addr = input.next();
            long address = Long.parseLong(addr.substring(2), HEX);
            input.next();
            
            int setIndex = (int) (address >>> byteBits) & (sets - 1);
            
            boolean hit = false;
            
            int blockIndex = getBlockIndex(cache, setIndex, tagBits,
                address, byteBits, setBits);
            
            if (blockIndex != -1) {
                
                if (ls.equals("l")) {
                    loadTot++;
                    loadHits++;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeHits++;
                    cycleTot++;
                    cycleTot = cycleTot + PERCENT; //write-through
                }
            } else {
                if (ls.equals("l")) {
                    loadTot++;
                    loadMisses++;
                    
                    int tag = (int) (address >>> (setBits + byteBits));
                    BitSet temp = newBitSet(tagBits + 2, tag);
                    temp.set(tagBits);
                    cache[setIndex].addFirst(temp);
                    
                    if (cache[setIndex].size() > blocks) {
                        cache[setIndex].removeLast();
                    }
                    
                    cycleTot = cycleTot + PERCENT * multiplier;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeMisses++;
                    
                    if (wa == 1) {
                        int tag = (int) (address >>> (setBits + byteBits));
                        BitSet temp = newBitSet(tagBits + 2, tag);
                        temp.set(tagBits);
                        cache[setIndex].addFirst(temp);
                        cycleTot = cycleTot + PERCENT * multiplier;
                        
                        if (cache[setIndex].size() > blocks) {
                            cache[setIndex].removeLast();
                        }
                        
                        cycleTot++;
                        cycleTot = cycleTot + PERCENT;
                    } else {
                        cycleTot = cycleTot + PERCENT;
                    }
                }
            }
        }
        input.close();
        
        System.out.println("Total loads: " + loadTot);
        System.out.println("Total stores: " + storeTot);
        System.out.println("Load hits: " + loadHits);
        System.out.println("Load misses: " + loadMisses);
        System.out.println("Store hits: " + storeHits);
        System.out.println("Store misses: " + storeMisses);
        System.out.println("Total cycles: " + cycleTot);
    }
    
    /**
    Set-associative cache, write-back, FIFO.
    
    @param sets number of rows in cache.
    @param blocks number of blocks in each set.
    @param bytes number of bytes in each row.
    @param input input Scanner.
    */
    public static void saFifoWB(int sets, int blocks, int bytes, 
        Scanner input) {
        int setBits = (int) (Math.log(sets) / Math.log(2));
        int byteBits = (int) (Math.log(bytes) / Math.log(2));
        int tagBits = MEM_BITS - setBits - byteBits;
        int multiplier = bytes / (2 * 2);
        
        LinkedList<BitSet>[] cache = newLinkedList(sets);
        
        long loadTot = 0;
        long storeTot = 0;
        long loadHits = 0;
        long storeHits = 0;
        long loadMisses = 0;
        long storeMisses = 0;
        long cycleTot = 0;
        
        while (input.hasNext()) {
            String ls = input.next();
            String addr = input.next();
            long address = Long.parseLong(addr.substring(2), HEX);
            input.next();
            
            int setIndex = (int) (address >>> byteBits) & (sets - 1);
            
            boolean hit = false;
            
            int blockIndex = getBlockIndex(cache, setIndex, tagBits,
                address, byteBits, setBits);
            
            if (blockIndex != -1) {
                
                if (ls.equals("l")) {
                    loadTot++;
                    loadHits++;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeHits++;
                    cycleTot++;
                    
                    BitSet temp = cache[setIndex].get(blockIndex);
                    temp.set(tagBits + 1);
                    cache[setIndex].set(blockIndex, temp);
                }
            } else {
                if (ls.equals("l")) {
                    loadTot++;
                    loadMisses++;
                    
                    int tag = (int) (address >>> (setBits + byteBits));
                    BitSet temp = newBitSet(tagBits + 2, tag);
                    temp.set(tagBits);
                    cache[setIndex].addFirst(temp);
                    cycleTot = cycleTot + PERCENT * multiplier;
                    
                    if (cache[setIndex].size() > blocks) {
                        BitSet temp2 = cache[setIndex].getLast();
                        if (temp2.get(tagBits + 1)) {
                            cycleTot = cycleTot + PERCENT * multiplier;
                        }
                        
                        cache[setIndex].removeLast();
                    }
                    
                    
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeMisses++;
                    
                    //write-allocate
                    int tag = (int) (address >>> (setBits + byteBits));
                    BitSet temp = newBitSet(tagBits + 2, tag);
                    temp.set(tagBits);
                    temp.set(tagBits + 1); //write-back
                    cache[setIndex].addFirst(temp);
                    cycleTot = cycleTot + PERCENT * multiplier;
                    
                    if (cache[setIndex].size() > blocks) {
                        BitSet temp2 = cache[setIndex].getLast();
                        if (temp2.get(tagBits + 1)) {
                            cycleTot = cycleTot + PERCENT * multiplier;
                        }
                        
                        cache[setIndex].removeLast();
                    }
                        
                    cycleTot++;
                }
            }
        }
        input.close();
        
        System.out.println("Total loads: " + loadTot);
        System.out.println("Total stores: " + storeTot);
        System.out.println("Load hits: " + loadHits);
        System.out.println("Load misses: " + loadMisses);
        System.out.println("Store hits: " + storeHits);
        System.out.println("Store misses: " + storeMisses);
        System.out.println("Total cycles: " + cycleTot);
    }
    
    /**
    Set-associative cache, write-through, LRU.
    
    @param sets number of rows in cache.
    @param blocks number of blocks in each set.
    @param bytes number of bytes in each row.
    @param wa write-allocate or not.
    @param input input Scanner.
    */
    public static void saLruWT(int sets, int blocks, int bytes, int wa, 
        Scanner input) {
        int setBits = (int) (Math.log(sets) / Math.log(2));
        int byteBits = (int) (Math.log(bytes) / Math.log(2));
        int tagBits = MEM_BITS - setBits - byteBits;
        int multiplier = bytes / (2 * 2);
        
        LinkedList<BitSet>[] cache = newLinkedList(sets);
        
        long loadTot = 0;
        long storeTot = 0;
        long loadHits = 0;
        long storeHits = 0;
        long loadMisses = 0;
        long storeMisses = 0;
        long cycleTot = 0;
        
        while (input.hasNext()) {
            String ls = input.next();
            String addr = input.next();
            long address = Long.parseLong(addr.substring(2), HEX);
            input.next();
            
            int setIndex = (int) (address >>> byteBits) & (sets - 1);
            
            boolean hit = false;
            
            int blockIndex = getBlockIndex(cache, setIndex, tagBits,
                address, byteBits, setBits);
            
            if (blockIndex != -1) {
                
                if (ls.equals("l")) {
                    loadTot++;
                    loadHits++;
                    cycleTot++;
                    
                    BitSet temp = cache[setIndex].remove(blockIndex);
                    cache[setIndex].addFirst(temp);
                
                } else {
                    storeTot++;
                    storeHits++;
                    cycleTot++;
                    cycleTot = cycleTot + PERCENT; //write-through
                    
                    BitSet temp = cache[setIndex].remove(blockIndex);
                    cache[setIndex].addFirst(temp);
                }
            } else {
                if (ls.equals("l")) {
                    loadTot++;
                    loadMisses++;
                    
                    int tag = (int) (address >>> (setBits + byteBits));
                    BitSet temp = newBitSet(tagBits + 2, tag);
                    temp.set(tagBits);
                    cache[setIndex].addFirst(temp);
                    
                    if (cache[setIndex].size() > blocks) {
                        cache[setIndex].removeLast();
                    }
                    
                    cycleTot = cycleTot + PERCENT * multiplier;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeMisses++;
                    
                    if (wa == 1) {
                        int tag = (int) (address >>> (setBits + byteBits));
                        BitSet temp = newBitSet(tagBits + 2, tag);
                        temp.set(tagBits);
                        cache[setIndex].addFirst(temp);
                        cycleTot = cycleTot + PERCENT * multiplier;
                        
                        if (cache[setIndex].size() > blocks) {
                            cache[setIndex].removeLast();
                        }
                        
                        cycleTot++;
                        cycleTot = cycleTot + PERCENT;
                    } else {
                        cycleTot = cycleTot + PERCENT;
                    }
                }
            }
        }
        input.close();
        
        System.out.println("Total loads: " + loadTot);
        System.out.println("Total stores: " + storeTot);
        System.out.println("Load hits: " + loadHits);
        System.out.println("Load misses: " + loadMisses);
        System.out.println("Store hits: " + storeHits);
        System.out.println("Store misses: " + storeMisses);
        System.out.println("Total cycles: " + cycleTot);
    }
    
    /**
    Set-associative cache, write-back, LRU.
    
    @param sets number of rows in cache.
    @param blocks number of blocks in each set.
    @param bytes number of bytes in each row.
    @param input input Scanner.
    */
    public static void saLruWB(int sets, int blocks, int bytes, 
        Scanner input) {
        int setBits = (int) (Math.log(sets) / Math.log(2));
        int byteBits = (int) (Math.log(bytes) / Math.log(2));
        int tagBits = MEM_BITS - setBits - byteBits;
        int multiplier = bytes / (2 * 2);
        
        LinkedList<BitSet>[] cache = newLinkedList(sets);
        
        long loadTot = 0;
        long storeTot = 0;
        long loadHits = 0;
        long storeHits = 0;
        long loadMisses = 0;
        long storeMisses = 0;
        long cycleTot = 0;
        
        while (input.hasNext()) {
            String ls = input.next();
            String addr = input.next();
            long address = Long.parseLong(addr.substring(2), HEX);
            input.next();
            
            int setIndex = (int) (address >>> byteBits) & (sets - 1);
            
            boolean hit = false;
            
            int blockIndex = getBlockIndex(cache, setIndex, tagBits,
                address, byteBits, setBits);
            
            if (blockIndex != -1) {
                
                if (ls.equals("l")) {
                    loadTot++;
                    loadHits++;
                    cycleTot++;
                    
                    BitSet temp = cache[setIndex].remove(blockIndex);
                    cache[setIndex].addFirst(temp);
                
                } else {
                    storeTot++;
                    storeHits++;
                    cycleTot++;
                    
                    BitSet temp = cache[setIndex].remove(blockIndex);
                    temp.set(tagBits + 1);
                    cache[setIndex].addFirst(temp);
                }
            } else {
                if (ls.equals("l")) {
                    loadTot++;
                    loadMisses++;
                    
                    int tag = (int) (address >>> (setBits + byteBits));
                    BitSet temp = newBitSet(tagBits + 2, tag);
                    temp.set(tagBits);
                    cache[setIndex].addFirst(temp);
                    
                    if (cache[setIndex].size() > blocks) {
                        BitSet temp2 = cache[setIndex].getLast();
                        if (temp2.get(tagBits + 1)) {
                            cycleTot = cycleTot + PERCENT * multiplier;
                        }
                        
                        cache[setIndex].removeLast();
                    }
                    
                    cycleTot = cycleTot + PERCENT * multiplier;
                    cycleTot++;
                
                } else {
                    storeTot++;
                    storeMisses++;
                    
                    //write-allocate
                    int tag = (int) (address >>> (setBits + byteBits));
                    BitSet temp = newBitSet(tagBits + 2, tag);
                    temp.set(tagBits);
                    temp.set(tagBits + 1); //write-back
                    cache[setIndex].addFirst(temp);
                    cycleTot = cycleTot + PERCENT * multiplier;
                    
                    if (cache[setIndex].size() > blocks) {
                        BitSet temp2 = cache[setIndex].getLast();
                        if (temp2.get(tagBits + 1)) {
                            cycleTot = cycleTot + PERCENT * multiplier;
                        }
                        
                        cache[setIndex].removeLast();
                    }
                    
                    
                        
                    cycleTot++;
                }
            }
        }
        input.close();
        
        System.out.println("Total loads: " + loadTot);
        System.out.println("Total stores: " + storeTot);
        System.out.println("Load hits: " + loadHits);
        System.out.println("Load misses: " + loadMisses);
        System.out.println("Store hits: " + storeHits);
        System.out.println("Store misses: " + storeMisses);
        System.out.println("Total cycles: " + cycleTot);
    }
}
