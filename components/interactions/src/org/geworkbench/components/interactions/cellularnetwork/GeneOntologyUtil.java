package org.geworkbench.components.interactions.cellularnetwork;

import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;

import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Feb 9, 2007
 * Time: 10:39:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneOntologyUtil {
    public static final String KINASE = "K";
    public static final String TF = "TF";
    public static final String PHOSPATASE = "P";
    private String KINASEGOTERMSTR = "16301";
    private String TFGOTERMSTR = "3700";
    private String PHOSPATASEGOTERMSTR = "4721";
    private static GeneOntologyUtil geneOntologyUtil = new GeneOntologyUtil();
    private static GeneOntologyTree tree = new GeneOntologyTree();

    static {
        try {
            tree = new GeneOntologyTree();
            tree.parseOBOFile("data/gene_ontology.obo");

        } catch (Exception x) {
            x.printStackTrace();

        }
    }

    public Set<GOTerm> getAllGOTerms(DSGeneMarker dsGeneMarker) {
        String geneId = dsGeneMarker.getLabel();
        String[] goTerms = AnnotationParser.getInfo(geneId, AnnotationParser.GOTERM);
        if (goTerms != null) {
            Set<GOTerm> set = new HashSet<GOTerm>();
            for (String goTerm : goTerms) {
                String goIdStr = goTerm.split("/")[0].trim();
                if (!goIdStr.equalsIgnoreCase("---")) {
                    int goId = new Integer(goIdStr);
                    if(tree.getTerm(goId)!=null)
                    set.add(tree.getTerm(goId));
                }
            }
            return set;
        }

        return null;
    }

    /**
     * create the GO tree.
     */
    public GeneOntologyUtil() {

    }

    public Set<GOTerm> getGOFunctionTerms(DSGeneMarker dsGeneMarker) {
        String geneId = dsGeneMarker.getLabel();
        String[] goTerms = AnnotationParser.getInfo(geneId, AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION);

        Set<GOTerm> set = new HashSet<GOTerm>();

        if (goTerms != null) {
            for (String goTerm : goTerms) {
                String goIdStr = goTerm.split("/")[0].trim();
                //      System.out.println("(" + goIdStr + ")" + dsGeneMarker + "getGOF");
                try {
                    if (!goIdStr.equalsIgnoreCase("---")) {
                        Integer goId = new Integer(goIdStr);
                        if (goId != null) {
                            set.addAll(tree.getAncestors(goId));
                        }
                    }
                } catch (NumberFormatException ne) {
                    ne.printStackTrace();
                }

            }
        }
        return set;
    }

    public TreeMap<String, List<GOTerm>> getAllGoTerms(DSGeneMarker dsGeneMarker, String catagory) {
        String geneId = dsGeneMarker.getLabel();
        String[] goTerms = AnnotationParser.getInfo(geneId, catagory);

        Set<GOTerm> set = new HashSet<GOTerm>();
        TreeMap<String, List<GOTerm>> treeMap = new TreeMap<String, List<GOTerm>>();
        if (goTerms != null) {

            for (String goTerm : goTerms) {
                String goIdStr = goTerm.split("/")[0].trim();
                //      System.out.println("(" + goIdStr + ")" + dsGeneMarker + "getGOF");
                try {
                    if (!goIdStr.equalsIgnoreCase("---")) {
                        Integer goId = new Integer(goIdStr);
                        if (goId != null) {
                            //set.addAll(tree.getAncestors(goId));
                            treeMap.put(goTerm, tree.getAncestorsTreeNodes(goId));
                        }
                    }
                } catch (NumberFormatException ne) {
                    ne.printStackTrace();
                }

            }
        }
        return treeMap;
    }

    public String matchFucntions(Set<GOTerm> set) {
        if (set == null || set.size() == 0) {
            return "";
        }
        for (GOTerm goterm : set) {
            if (new Integer(KINASEGOTERMSTR).equals(goterm.getId())) {
                return KINASE;
            }
            if (new Integer(TFGOTERMSTR).equals(goterm.getId())) {
                return TF;
            }
            if (new Integer(PHOSPATASEGOTERMSTR).equals(goterm.getId())) {
                return PHOSPATASE;
            }
        }
        return "";
    }

    public String checkMarkerFunctions(DSGeneMarker dsGeneMarker) {
        return matchFucntions(getGOFunctionTerms(dsGeneMarker));
    }

    public static GeneOntologyUtil getOntologyUtil() {
        if (geneOntologyUtil == null) {
            geneOntologyUtil = new GeneOntologyUtil();
        }
        return geneOntologyUtil;
    }

}
