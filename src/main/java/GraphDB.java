import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    ArrayList<Long> nList = new ArrayList<>();
    HashMap<Long, Node> nodesMap = new HashMap<>();

    public static class Node implements Comparable<Node> {
        boolean seen;
        Node parent;
        long id;
        double lat;
        double lon;
        double priority;
        HashMap<String, String> info;
        HashMap<Long, Double> adjacents; //long is the id of an adjacent node and double is the

        public Node(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            info = new HashMap<>();
            //nList.add(this.id);
            //nodesMap.put(this.id, this);
            adjacents = new HashMap<>();
            seen = false;
        }

        public void setPriority(Node dest, double prev) { //sets the
            double sum = Math.pow(this.lon - dest.lon, 2) + Math.pow(this.lat - dest.lat, 2);
            this.priority = Math.sqrt(sum) + prev;

        }

        @Override
        public int compareTo(Node n) {
            if (this.priority - n.priority < 0) {
                return -1;
            } else if (this.priority - n.priority > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }


    HashMap<Long, Way> potentialWays = new HashMap<>();

    public static class Way {
        long id;
        boolean valid = true;
        ArrayList<Node> path;
        String name;


        public Way(long id, boolean valid) {
            this.id = id;
            this.valid = valid;
            path = new ArrayList<>();
        }
    }

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }







    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        Iterator<Long> x = vertices().iterator();
        while (x.hasNext()) {
            if (nodesMap.get(x.next()).adjacents.size() == 0) {
                x.remove();
            }
        }
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return nList;
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        //Node n = nodesMap.get(v);
        return nodesMap.get(v).adjacents.keySet();
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        //Node vN = nodesMap.get(v);
        //Node wN = nodesMap.get(w);

        return distanceCalc(nodesMap.get(v).lon, nodesMap.get(w).lon,
                nodesMap.get(v).lat, nodesMap.get(w).lat);
    }

    double distanceCalc(double lon1, double lon2, double lat1, double lat2) {
        double sum = Math.pow(lon1 - lon2, 2) + Math.pow(lat1 - lat2, 2);
        return Math.sqrt(sum);
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        long minID = nList.get(0);
        Node least = nodesMap.get(minID);

        for (Long ids: nList) {
            Node x = nodesMap.get(ids);
            if (distanceCalc(x.lon, lon, x.lat, lat)
                    < distanceCalc(least.lon, lon, least.lat, lat)) {
                minID = x.id;
                least = x;
            }
        }


        return minID;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return nodesMap.get(v).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return nodesMap.get(v).lat;
    }

    void addEdge(long id1, long id2) {
        //Node n1 = nodesMap.get(id1);
        //Node n2 = nodesMap.get(id2);

        nodesMap.get(id1).adjacents.put(id2, distance(id1, id2));
        nodesMap.get(id2).adjacents.put(id1, distance(id1, id2));

    }
}
