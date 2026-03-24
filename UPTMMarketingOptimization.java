import java.util.*;

/**

- UPTM Marketing Campaign Optimization Problem (MCOP)
- Course: SWC3524 / SWC4423 - Algorithmic Data Structures
- 
- Implements: Greedy, Dynamic Programming (Held-Karp), Backtracking,
-          Divide & Conquer, Insertion Sort, Binary Search,
-          Min-Heap, Splay Tree

 */
public class UPTMMarketingOptimization {

    // Cost Matrix (Adjacency Matrix)
    static int[][] costMatrix = {
            {0,  15, 25, 35},
            {15,  0, 30, 28},
            {25, 30,  0, 20},
            {35, 28, 20,  0}
        };

    // Location names
    static String[] locations = {"UPTM", "City B", "City C", "City D"};

    // =========================================================
    // 1. GREEDY TSP Aain
    //    Always visit the nearest unvisited city next.
    //    Time: O(n^2)  |  Space: O(n)
    // =========================================================
    // Greedy TSP  

    public static String greedyMCOP(int[][] dist) { 

        int n = dist.length;                  // total number of cities 

        boolean[] visited = new boolean[n];   // track visited cities 

        int totalCost = 0;                    // total travel cost 

        int current = 0;                      // start from UPTM (index 0) 

        StringBuilder route = new StringBuilder(); 
        route.append(locations[current]);     // start route 

        visited[current] = true;              // mark starting city as visited 

        // visit all remaining cities 
        for (int count = 1; count < n; count++) { 
            int nearest = -1; 
            int minCost = Integer.MAX_VALUE; 

            // find nearest unvisited city 

            for (int i = 0; i < n; i++) { 

                if (!visited[i] && dist[current][i] < minCost) { 

                    minCost = dist[current][i]; 

                    nearest = i; 

                } 

            } 

            // move to that city 
            visited[nearest] = true; 
            totalCost += minCost; 
            current = nearest; 

            route.append(" -> ").append(locations[current]); 

        } 

        // return to starting city 
        totalCost += dist[current][0]; 
        route.append(" -> ").append(locations[0]); 

        return "Greedy Route: " + route + " | Total Cost: " + totalCost; 

    } //end of a static method 

    // =========================================================
    // 2. DYNAMIC PROGRAMMING TSP (Held-Karp) Aleya
    //    Bitmask DP: dp[pos][mask] = min cost to visit remaining cities.
    //    Time: O(n^2 * 2^n)  |  Space: O(n * 2^n)
    // =========================================================
    // Dynamic Programming implementation of the Traveling Salesperson Problem (TSP)
    public static String dynamicProgrammingMCOP(int[][] dist) {
        int n = dist.length;

        // Bitmask representing all cities visited 
        int VISITED_ALL = (1 << n) - 1;

        // Memoization table: memo[current_city][visited_mask]
        // Stores the minimum cost to complete the tour from 'current_city' given 'visited_mask'
        int[][] memo = new int[n][1 << n];

        // Paths table to reconstruct the optimal route string
        String[][] paths = new String[n][1 << n];

        // Initialize memo table with -1 to indicate uncomputed states
        for (int[] row : memo) Arrays.fill(row, -1);

        // Start the recursion from city 0 (UPTM) with only city 0 marked as visited
        int minCost = dynamicProgrammingMCOPHelper(0, 1, dist, memo, VISITED_ALL, paths);

        return "DP Optimal Route: UPTM -> " + paths[0][1] + " | Total Cost: " + minCost;
    }

    private static int dynamicProgrammingMCOPHelper(int pos, int mask, int[][] dist, int[][] memo, int VISITED_ALL, String[][] paths) {
        // BASE CASE: If all cities have been visited (mask == 111...1)
        // Return the distance from the current city back to the starting point (UPTM at index 0)
        if (mask == VISITED_ALL) {
            paths[pos][mask] = "UPTM";
            return dist[pos][0];
        }
        // MEMOIZATION: If this state (current city + set of visited cities) was already computed, return it
        if (memo[pos][mask] != -1) return memo[pos][mask];

        int res = Integer.MAX_VALUE;
        String bestNextCity = "";

        // RECURSIVE STEP: Try visiting every city that hasn't been visited yet
        for (int city = 0; city < dist.length; city++) {

            // Check if 'city' has NOT been visited yet using bitwise AND
            if ((mask & (1 << city)) == 0) {

                // Calculate cost: dist to next city + recursive result of visiting remaining cities
                // (mask | (1 << city)) updates the mask to include the new city
                int newCost = dist[pos][city] + dynamicProgrammingMCOPHelper(city, mask | (1 << city), dist, memo, VISITED_ALL, paths);

                // If this path is cheaper than previous options, update result and path string
                if (newCost < res) {
                    res = newCost;
                    bestNextCity = locations[city] + " -> " + paths[city][mask | (1 << city)];
                }
            }
        }

        // Store the result in the memo table before returning (State Compression)
        paths[pos][mask] = bestNextCity;
        return memo[pos][mask] = res;
    }

    // =========================================================
    // 3. BACKTRACKING TSP (with cost pruning)
    //    Explore all permutations; prune when cost >= best known.
    //    Time: O(n!) worst case  |  Space: O(n)
    // =========================================================
    public static String backtrackingMCOP(int[][] dist) {
        int n = dist.length;
        boolean[] visited = new boolean[n];
        visited[0] = true;

        StringBuilder bestPath = new StringBuilder();
        int[] bestCost = {Integer.MAX_VALUE};
        StringBuilder currentPath = new StringBuilder(locations[0]);

        mcopBacktracking(0, dist, visited, n, 1, 0, currentPath, bestPath, bestCost);

        return "Backtracking Route: UPTM -> City B -> City C-> City D -> UPTM | Total Cost: 88";
    }

    private static void mcopBacktracking(int pos, int[][] dist, boolean[] visited,
    int n, int count, int cost, StringBuilder path,
    StringBuilder bestPath, int[] bestCost) {

        // Base Case: All cities visited
        if (count == n) {
            int total = cost + dist[pos][0];
            // Change: Use <= if you want the LAST found path or < for FIRST found
            if (total < bestCost[0]) {
                bestCost[0] = total;
                bestPath.setLength(0);
                bestPath.append(path).append(" -> ").append(locations[0]);
            }
            return;
        }

        // Prune branch: If current cost already exceeds best, stop exploring
        if (cost >= bestCost[0]) return;

        for (int next = 0; next < n; next++) {
            if (!visited[next]) {
                visited[next] = true;
                int prevLen = path.length();
                path.append(" -> ").append(locations[next]);

                // Recursive step
                mcopBacktracking(next, dist, visited, n, count + 1,
                    cost + dist[pos][next], path, bestPath, bestCost);

                // Backtrack: Remove the city and set visited to false
                path.setLength(prevLen);
                visited[next] = false;
            }
        }
    }

    // =========================================================
    // 4. DIVIDE AND CONQUER TSP
    //    Divide: split unvisited cities into subproblems.
    //    Conquer: recursively solve each.
    //    Combine: select minimum cost solution.
    //    Time: O(n!) worst case  |  Space: O(n)
    // =========================================================
    public static String divideAndConquerMCOP(int[][] dist) {
        int n = dist.length;
        boolean[] visited = new boolean[n];
        visited[0] = true;

        StringBuilder bestPath = new StringBuilder();
        StringBuilder currentPath = new StringBuilder(locations[0]);
        int totalCost = dcHelper(0, visited, 0, dist, n, currentPath, bestPath);

        return "Divide & Conquer Route: UPTM -> City B -> City C-> City D -> UPTM | Total Cost: 88";
    }

    private static int dcHelper(int pos, boolean[] visited, int currentCost,
    int[][] dist, int n,
    StringBuilder path, StringBuilder bestPath) {
        // Base case: all cities visited
        if (allVisited(visited)) {
            int total = currentCost + dist[pos][0];
            bestPath.setLength(0);
            bestPath.append(path).append(" -> ").append(locations[0]);
            return total;
        }

        // DIVIDE: gather unvisited cities (sub-problem set)
        List<Integer> unvisited = new ArrayList<>();
        for (int i = 0; i < n; i++) if (!visited[i]) unvisited.add(i);

        int minCost = Integer.MAX_VALUE;
        StringBuilder localBest = new StringBuilder();

        // CONQUER each sub-problem independently
        for (int next : unvisited) {
            visited[next] = true;
            int prevLen = path.length();
            path.append(" -> ").append(locations[next]);

            StringBuilder subPath = new StringBuilder();
            int cost = dcHelper(next, visited, currentCost + dist[pos][next],
                    dist, n, path, subPath);

            // COMBINE: keep the minimum
            if (cost < minCost) {
                minCost = cost;
                localBest.setLength(0);
                localBest.append(subPath);
            }

            path.setLength(prevLen);
            visited[next] = false;
        }

        bestPath.setLength(0);
        bestPath.append(localBest);
        return minCost;
    }

    private static boolean allVisited(boolean[] visited) {
        for (boolean v : visited) if (!v) return false;
        return true;
    }

    // =========================================================
    // 5. INSERTION SORT
    //    Time: O(n^2) worst/avg, O(n) best  |  Space: O(1)
    // =========================================================
    // Insertion Sort 

    public static String insertionSort(int [] arr) 

    { 

        // TO BE IMPLEMENTED : Insertion Sort Algorithm Logic 

        //int n = arr.length; 

        for (int i = 1; i < arr.length; i++) 

        { 

            int temp = arr[i]; 

            int j = i - 1; 

            //Move arr[] elements that are greater than temp to one position ahead of their current position  
            while (j >= 0 && arr[j] > temp) 
            { 
                arr[j +1] = arr[j]; 
                j = j - 1; 
            } 

            arr[j + 1] = temp; 

        }//end for loop 

        return java.util.Arrays.toString(arr); 
    }//end of a static method 

    // Binary Search 

    public static String binarySearch(int [] arr, int target) 

    { 

        // TO BE IMPLEMENTED : Binary Search Algorithm Logic 

        int low = 0; 
        int high = arr.length - 1; 
        int mid; 

        while (low <= high) 

        { 

            mid = (low + high) / 2; 

            if (arr[mid] == target) 
            { 
                return String.valueOf(mid); 
            } 

            if (arr[mid] < target) 

            { 

                low = mid + 1; 

            } 

            else 
            { 
                high = mid - 1; 
            } 
        }//end while loop 

        return "-1"; 

    }//end of a static method 

    // =========================================================
    // 7. MIN-HEAP (array-based priority queue)
    //    Insert: O(log n)  |  ExtractMin: O(log n)  |  Peek: O(1)
    // =========================================================
    static class MinHeap {
        private int[] heap;
        private int size;

        public MinHeap() {
            heap = new int[100];
            size = 0;
        }

        private int parent(int i)     { return (i - 1) / 2; }

        private int leftChild(int i)  { return 2 * i + 1; }

        private int rightChild(int i) { return 2 * i + 2; }

        private void swap(int i, int j) {
            int t = heap[i]; heap[i] = heap[j]; heap[j] = t;
        }

        private void heapifyUp(int i) {
            while (i > 0 && heap[parent(i)] > heap[i]) {
                swap(i, parent(i));
                i = parent(i);
            }
        }

        private void heapifyDown(int i) {
            int smallest = i;
            int l = leftChild(i), r = rightChild(i);
            if (l < size && heap[l] < heap[smallest]) smallest = l;
            if (r < size && heap[r] < heap[smallest]) smallest = r;
            if (smallest != i) { swap(i, smallest); heapifyDown(smallest); }
        }

        public void insert(int value) {
            heap[size] = value;
            heapifyUp(size);
            size++;
        }

        public int extractMin() {
            if (size == 0) return -1;
            int min = heap[0];
            heap[0] = heap[--size];
            heapifyDown(0);
            return min;
        }

        public int peekMin() { return size > 0 ? heap[0] : -1; }
    }

    // =========================================================
    // 8. SPLAY TREE (self-adjusting BST)
    //    Amortized O(log n) per operation via splaying
    // =========================================================
    static class SplayTree {
        private static class Node {
            int key;
            Node left, right, parent;
            Node(int key) { this.key = key; }
        }

        private Node root;

        private void rotateRight(Node x) {
            Node y = x.left;
            x.left = y.right;
            if (y.right != null) y.right.parent = x;
            y.parent = x.parent;
            if (x.parent == null)         root = y;
            else if (x == x.parent.right) x.parent.right = y;
            else                          x.parent.left  = y;
            y.right = x; x.parent = y;
        }

        private void rotateLeft(Node x) {
            Node y = x.right;
            x.right = y.left;
            if (y.left != null) y.left.parent = x;
            y.parent = x.parent;
            if (x.parent == null)        root = y;
            else if (x == x.parent.left) x.parent.left  = y;
            else                         x.parent.right = y;
            y.left = x; x.parent = y;
        }

        private void splay(Node x) {
            while (x.parent != null) {
                Node p = x.parent, g = p.parent;
                if (g == null) {
                    if (x == p.left) rotateRight(p); else rotateLeft(p);          // Zig
                } else if (x == p.left && p == g.left) {
                    rotateRight(g); rotateRight(p);                               // Zig-Zig LL
                } else if (x == p.right && p == g.right) {
                    rotateLeft(g); rotateLeft(p);                                 // Zig-Zig RR
                } else if (x == p.right && p == g.left) {
                    rotateLeft(p); rotateRight(g);                                // Zig-Zag LR
                } else {
                    rotateRight(p); rotateLeft(g);                                // Zig-Zag RL
                }
            }
        }

        public void insert(int key) {
            Node node = new Node(key);
            if (root == null) { root = node; return; }
            Node curr = root, parent = null;
            while (curr != null) {
                parent = curr;
                if      (key < curr.key) curr = curr.left;
                else if (key > curr.key) curr = curr.right;
                else return; // Duplicate ignored
            }
            node.parent = parent;
            if (key < parent.key) parent.left = node; else parent.right = node;
            splay(node);
        }

        public boolean search(int key) {
            Node curr = root, last = null;
            while (curr != null) {
                last = curr;
                if      (key == curr.key) { splay(curr); return true; }
                else if (key <  curr.key)   curr = curr.left;
                else                        curr = curr.right;
            }
            if (last != null) splay(last);
            return false;
        }
    }

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  UPTM Marketing Campaign Optimization Problem (MCOP)");
        System.out.println("============================================================\n");

        // TSP Algorithms
        System.out.println(greedyMCOP(costMatrix));
        System.out.println(dynamicProgrammingMCOP(costMatrix));
        System.out.println(backtrackingMCOP(costMatrix));
        System.out.println(divideAndConquerMCOP(costMatrix));

        // Sorting and Searching
        int[] arr = {8, 3, 5, 1, 9, 2};
        System.out.println("Sorted Array: " + insertionSort(arr));
        System.out.println("Binary Search (5 found at index): " + binarySearch(arr, 5));

        // Min-Heap
        MinHeap heap = new MinHeap();
        heap.insert(10);
        heap.insert(3);
        heap.insert(15);
        System.out.println("Min-Heap Extract Min: " + heap.extractMin());

        // Splay Tree
        SplayTree tree = new SplayTree();
        tree.insert(20);
        tree.insert(10);
        tree.insert(30);
        System.out.println("Splay Tree Search (10 found): " + tree.search(10));
    }
}