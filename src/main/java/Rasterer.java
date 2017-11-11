import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Comparator;
import java.util.Iterator;


/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.


        private class QuadTreeNode implements Comparable<QuadTreeNode> {
            private String fileName;
            private double ulLon;
            private double ulLat;
            private double lrLon;
            private double lrLat;
            private int depth;
            private double lonDPP;

            private QuadTreeNode NE;
            private QuadTreeNode NW;
            private QuadTreeNode SW;
            private QuadTreeNode SE;

            //Constructor recursively builds quadtree by adding to NE, NW, SW, SE
            private QuadTreeNode(String fileName, double ulLon, double ulLat,
                                 double lrLon, double lrLat, int depth) {
                this.fileName = fileName;
                this.ulLon = ulLon;
                this.ulLat = ulLat;
                this.lrLon = lrLon;
                this.lrLat = lrLat;
                this.depth = depth;

                if (depth < 7) {
                    makeChildren(this);
                }

            }

            private void makeChildren(QuadTreeNode x) {
                if (x.depth < 7) {
                    String name = null;
                    if (this.fileName.equals("root")) {
                        name = "";
                    } else {
                        name = x.fileName;
                    }
                    double centerlon = (x.lrLon + x.ulLon) / 2;
                    double centerlat = (x.lrLat + x.ulLat) / 2;

                    x.NW = new QuadTreeNode(name + "1", x.ulLon, x.ulLat, centerlon,
                            centerlat, depth + 1);
                    x.NE = new QuadTreeNode(name + "2", centerlon, ulLat, lrLon,
                            centerlat, depth + 1);
                    x.SW = new QuadTreeNode(name + "3", ulLon, centerlat,
                            centerlon, lrLat, depth + 1);
                    x.SE = new QuadTreeNode(name + "4", centerlon,
                            centerlat, lrLon, lrLat, depth + 1);
                }
            }



            public int compareTo(QuadTreeNode qtn) {
                if (this.ulLon < qtn.ulLon) {
                    if (this.ulLat >= qtn.ulLat) {
                        return -1;
                    }
                } else if (this.ulLat > qtn.ulLat) {
                    if (this.ulLon >= qtn.ulLon) {
                        return -1;
                    }
                }
                return 1;
            }


        }




//        private QuadTree() {
//            root = new QuadTreeNode("root", MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT,
//                    MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT, 0);
//        }




    private QuadTreeNode q;


    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgroot) {
        q = new Rasterer.QuadTreeNode("root", MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT,
                    MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT, 0);


    }

    public class QuadTreeNodeComparator implements Comparator<QuadTreeNode> {
        public int compare(QuadTreeNode n1, QuadTreeNode n2) {
            if (n1.ulLon < n2.ulLon) {
                if (n1.ulLat >= n2.ulLat) {
                    return -1;
                }
            } else if (n1.ulLat > n2.ulLat) {
                if (n1.ulLon >= n2.ulLon) {
                    return -1;
                }
            }
            return 1;
        }
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {

        Map<String, Object> results = new HashMap<>();

        double ullon = params.get("ullon");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double lrlon = params.get("lrlon");
        if (!checkInput(ullon, ullat, lrlon, lrlat)) {
            results.put("depth", 0.0);
            results.put("raster_ul_lon", 0.0);
            results.put("raster_lr_lon", 0.0);
            results.put("query_success", false);
            results.put("raster_lr_lat", 0.0);
            results.put("raster_ul_lat", 0.0);
            results.put("render_grid", null);
            return results;

        }

        ArrayList<QuadTreeNode> ql = new ArrayList<>();
        getFiles(q, params.get("lrlon"), params.get("ullon"), params.get("ullat"),
                params.get("lrlat"), params.get("w"), ql);

        Collections.sort(ql);
        results.put("depth", ql.get(0).depth);
        results.put("raster_ul_lon", ql.get(0).ulLon);
        results.put("raster_lr_lon", ql.get(ql.size() - 1).lrLon);
        results.put("query_success", true);
        results.put("raster_lr_lat", ql.get(ql.size() - 1).lrLat);
        results.put("raster_ul_lat", ql.get(0).ulLat);
        results.put("render_grid", imageArray(ql));

        return results;
    }

    public boolean checkInput (double ullon, double ullat, double lrlon, double lrlat) {
        if (ullon > lrlon || lrlat > ullat) {
            return false;
        } if (lrlon < MapServer.ROOT_ULLON || lrlat > MapServer.ROOT_ULLAT || ullon > MapServer.ROOT_LRLON
                || ullat < MapServer.ROOT_LRLAT
                ) {
            return false;
        }
        return true;
    }

    public String[][] imageArray(ArrayList<QuadTreeNode> arr) {
        int index = 1;
        double firstLat = arr.get(0).ulLat;
        while (arr.get(index).ulLat == firstLat) {
            index += 1;
        }



        Iterator<QuadTreeNode> it = arr.iterator();
        String[][] out = new String[arr.size() / index][index];
//        for (int f = 0; f < out.length; f += 1) {
//            for (int s = 0; s < out[0].length; s += 1) {
//                String pngFile = "img/" + arr.get(counter).fileName + ".png";
//                out[f][s] = pngFile;
//                counter += 1;
//            }
//        }
        int counter = 0;
        while (it.hasNext()) {
            QuadTreeNode q = it.next();
            int row = counter / index;
            int col = counter % index;
            out[row][col] = "img/" + q.fileName + ".png";
            counter += 1;
        }
        //sort through the array list and add to 2d array accordingly

        return out;
    }


    //try 2d array list and add to that list, use .get.get to add to 2d array
    public void getFiles(QuadTreeNode quad, double lrlon, double ullon,
                         double ullat, double lrlat, double width,
                         ArrayList<QuadTreeNode> ql) {
        double queryLonDPP = getLonDPP(lrlon, ullon, width);
        double nodeLonDPP = getLonDPP(quad.lrLon, quad.ulLon, MapServer.TILE_SIZE);

        if (intersect(ullon, ullat, lrlon, lrlat, quad.ulLon,
                quad.ulLat, quad.lrLon, quad.lrLat)) {
            if (quad.depth == 7) {
                ql.add(quad);
            } else if (nodeLonDPP <= queryLonDPP) {
                ql.add(quad);
            } else {
                getFiles(quad.NE, lrlon, ullon, ullat, lrlat, width, ql);
                getFiles(quad.NW, lrlon, ullon, ullat, lrlat, width, ql);
                getFiles(quad.SE, lrlon, ullon, ullat, lrlat, width, ql);
                getFiles(quad.SW, lrlon, ullon, ullat, lrlat, width, ql);
            }

        }
    }

    public double getLonDPP(double lrLon, double ulLon, double width) {
        double dPP = (lrLon - ulLon) / width;
        return dPP;
    }

    //Citation: Project 3 FAQ - stack overflow post
    //query is rectangle A and picture is rectangle B
    public boolean intersect(double queryULlon, double queryULlat,
                             double queryLRlon, double queryLRlat,
                             double rUllon, double rULlat, double rLRlon, double rLRlat) {
        if (queryULlon < rLRlon && queryLRlon > rUllon
                && queryULlat > rLRlat && queryLRlat < rULlat) {
            return true;
        }
        return false;
    }



}
