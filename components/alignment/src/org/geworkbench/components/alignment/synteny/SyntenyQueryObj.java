package org.geworkbench.components.alignment.synteny;

/**
 * SyntenyQueryObj.java
 * A class to hold information about query for submittion to the synteny computation program
 * one or two queries now are accepted.
 *
 *<br>
 */
public class SyntenyQueryObj {

    /** Query type - local files submittion or URLS's  */
      String qtype;
    /** Number of queries in request */
        int queriesNum;
    /** The query names strings */
        String nameX;
        String nameY;
    /** The query strings */
        String queryX;
        String queryY;
    /** The first positions in queries. */
        int startX;
        int startY;
    /** The last position in queries. */
        int endX;
        int endY;

    /* Get methods for class variables.*/
        /** Get query type - local files submittion or URLS's  */
            String getQType(){return qtype;}
        /** Get number of queries in request */
            int getQueriesNum(){ return queriesNum;}
        /** Get the query names strings */
            String getNameX(){ return nameX;}
            String getNameY(){ return nameY;}
        /** Get the query strings */
            String getQueryX(){ return queryX;}
            String getQueryY(){ return queryY;}
        /** Get the first positions in queries. */
            int getStartX(){ return startX;}
            int getStartY(){ return startY;}
        /** Get the last position in queries. */
            int getEndX(){ return endX;}
            int getEndY(){ return endY;}


      /* Set methods for class variables */
      /** Query type - local files submittion or URLS's  */
          void setQType(String qt){qtype=qt;}
      /** Number of queries in request */
          void setQueriesNum(int qnum){queriesNum=qnum;}
      /** The query names strings */
          void setNameX(String nmx){nameX=nmx;}
          void setNameY(String nmy){nameY=nmy;}
      /** The query strings */
          void setQueryX(String qx){queryX=qx;}
          void setQueryY(String qy){queryY=qy;}
      /** The first positions in queries. */
          void setStartX(int sx){startX=sx;}
          void setStartY(int sy){startY=sy;}
      /** The last position in queries. */
          void setEndX(int ex){endX=ex;}
          void setEndY(int ey){endY=ey;}

}
