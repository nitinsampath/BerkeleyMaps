import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;


/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {

//    private class Route implements Comparable<Route>{
//        private double priority;
//        private LinkedList<Long> r;
//
//        private Route(GraphDB g, ) {
//
//        }
//
//        public int compareTo(Route r) {
//            if (this.priority < r.priority) {
//                return -1;
//            } else if (this.priority > r.priority) {
//                return 1;
//            } else {
//                return 0;
//            }
//        }


    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon,
                                                double stlat, double destlon, double destlat) {

        LinkedList<Long> ans = new LinkedList<>();
        HashMap<Long, Double> distTo = new HashMap<>(); //distance it takes in the p
        Long start = g.closest(stlon, stlat);
        Long dest = g.closest(destlon, destlat);
        PriorityQueue<GraphDB.Node> pq = new PriorityQueue<>();
        GraphDB.Node init = g.nodesMap.get(start);
        GraphDB.Node end = g.nodesMap.get(dest);
        HashSet<Long> seen = new HashSet<>();
        HashMap<Long, Long> realEdgeTo = new HashMap<>();

        init.setPriority(end, 0); //priority represents euclidean distance to dest
        pq.add(init); //added the start node to the priority queue
        distTo.put(start, 0.0); //dist to represents how long it has taken
        //ans.add(start);
        LinkedList<Long> firstLL = new LinkedList<>();
        firstLL.add(start);
        //edgeTo.put(start, firstLL);
        realEdgeTo.put(start, start);

        while (!pq.isEmpty()) {


            GraphDB.Node min = pq.remove();
            long minID = min.id;
            //System.out.println(ans);
            seen.add(start);

            if (minID == dest) {
                LinkedList<Long> l = new LinkedList<>();
                Long pointer = minID;
                l.add(0, minID);
                while (!realEdgeTo.get(pointer).equals(start)) {
                    l.add(0, realEdgeTo.get(pointer));
                    pointer = realEdgeTo.get(pointer);

                }
                l.add(0, start);
                return l;
            }

            for (Long adjid : min.adjacents.keySet()) {
                if (!seen.contains(adjid)) {
                    GraphDB.Node curr = g.nodesMap.get(adjid);
                    if (distTo.get(adjid) == null) {
                        distTo.put(adjid, distTo.get(minID) + g.distance(minID, adjid));
                        curr.parent = min;

                        seen.add(adjid);
                        realEdgeTo.put(adjid, minID);
                    } else if (distTo.get(adjid) != null && distTo.get(adjid)
                            > distTo.get(minID) + g.distance(minID, adjid)) {
                        distTo.put(adjid, distTo.get(minID) + g.distance(minID, adjid));
                        //ans.add(adjid);
                        curr.parent = min;

                        realEdgeTo.put(adjid, minID);
                    }

                    curr.setPriority(end, distTo.get(adjid));
                    pq.add(curr);


                } else if (seen.contains(adjid) && distTo.get(adjid) != null
                        && distTo.get(adjid) > distTo.get(minID) + g.distance(minID, adjid)) {
                    GraphDB.Node curr = g.nodesMap.get(adjid);
                    distTo.put(adjid, distTo.get(minID) + g.distance(minID, adjid));
                    curr.parent = min;
                    curr.setPriority(end, distTo.get(adjid));
                    pq.add(curr);
                    realEdgeTo.put(adjid, minID);
                }

            }

        }

        return null;
    }

}

