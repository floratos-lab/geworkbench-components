package org.geworkbench.components.alignment.synteny;

/**
 * DotMatrixObj.java
 * A class to hold information about pairwise dot matrix.
 *<br>
 * Currently this class can only be instantiated by the <code> DotsParser </code> class.
 */
public class DotMatrixObj {

    /* The horizontal size of matrix in pixels. */
        int pixX;
    /* The vertical size of matrix in pixels. */
        int pixY;
    /* The name of the sequence along the horizontal (X) axis. */
        String nameX;
    /* The name of the sequence along the vertical (Y) axis. */
        String nameY;
    /* The description of the sequence along the horizontal (X) axis. */
        String descriptionX;
    /* The description of the sequence along the vertical (Y) axis. */
        String descriptionY;
    /* The first position in analysis in horizontal sequence. */
        int startX;
    /* The last position in analysis in horizontal sequence. */
        int endX;
    /* The first position in analysis in vertical sequence. */
        int startY;
    /* The last position in analysis in vertical sequence. */
        int endY;
    /*  The matrix as a huge single row of 0 and 1.
      * todo: to make it a binary, i.e. by bits. */
        char DotMatrixDirect[];
        char DotMatrixInvert[];

    /* whether this DotMatrixObj is included in the MSA analysis */
        Boolean include= new Boolean(false);

    /* marking coordinates */
        int[] markLinesX;
        int[] markLinesY;
        int markLinesXnum;
        int markLinesYnum;
        int max_mark_lines;

    /* Constructor: is blank. */
        public DotMatrixObj() {
        max_mark_lines = 40;
        markLinesX=new int[max_mark_lines];
        markLinesY=new int[max_mark_lines];
        markLinesXnum=0;
        markLinesYnum=0;
        }

        /* Get methods for class variables.*/
        /**
         * Returns the horizontal DotMatrix size in pixels in this DotMatrixObj.
         *
         * @return		the width as an integer.
         */
        public int getPixX() {
                return pixX;
        }

        /**
         * Returns the vertical DotMatrix size in pixels stored in this DotMatrixObj.
         *
         * @return		the height as an integer.
         */
        public int getPixY() {
                return pixY;
        }

        /**
         * Returns the horizontal sequence name stored in this DotMatrixObj.
         *
         * @return		the name as a String.
         */
        public String getNameX() {
                return nameX;
        }

        /**
         * Returns the vertical sequence name stored in this DotMatrixObj.
         *
         * @return		the name as a String.
         */
        public String getNameY() {
                return nameY;
        }

        /**
         * Returns the horizontal sequence description stored in this DotMatrixObj.
         *
         * @return		the description as a String..
         */
        public String getDescriptionX() {
                return descriptionX;
        }

        /**
         * Returns the horizontal sequence description stored in this DotMatrixObj.
         *
         * @return		the description as a String..
         */
        public String getDescriptionY() {
                return descriptionY;
        }

        /**
         * Returns the start position of horizontal sequence in this DotMatrixObj.
         *
         * @return		the start as an int.
         */
        public int getStartX() {
                return startX;
        }

        /**
         * Returns the start position of vertical sequence in this DotMatrixObj.
         *
         * @return		the start as an int.
         */
        public int getStartY() {
                return startY;
        }

        /**
         * Returns the last position of horizontal sequence in this DotMatrixObj.
         *
         * @return		the end as an int.
         */
        public int getEndX() {
                return endX;
        }

        /**
         * Returns the last position of vertical sequence in this DotMatrixObj.
         *
         * @return		the end as an int.
         */
        public int getEndY() {
                return endY;
        }

        /**
         * Returns the DotMatrix stored in this DotMatrixObj.
         *
         * @return		dotmatrix value as an array of integers.
         */
        public char[] getDotMatrixDirect() {
                return DotMatrixDirect;
        }
        public char[] getDotMatrixInvert() {
                return DotMatrixInvert;
        }

        /**
         * Returns single pixel stored in this DotMatrixObj.
         *
         * @return		dotmatrix value as an array of integers.
         */
        public char getDirectPixel(int i,int j){
          return DotMatrixDirect[i*pixX+j];
        }
        public char getInvertedPixel(int i,int j){
          return DotMatrixInvert[i*pixX+j];
        }
            /* Set methods for class variables */

        /**
         * Sets the horizontal DotMatrix size in pixels in this DotMatrixObj.
         *
         * the width as an integer.
         */
        public void setPixX(int pX) {
                pixX=pX;
        }

        public int getMarkLinesXnum() {
            return markLinesXnum;
        }

        public int getMarkLinesYnum() {
            return markLinesYnum;
        }


        public void addMarkX(int pos){
            if(markLinesXnum+1 >= max_mark_lines) return;
            markLinesX[markLinesXnum++]=((pos-startX)*pixX)/(endX-startX+1);
        }
        public void addMarkY(int pos){
            if(markLinesYnum+1 >= max_mark_lines) return;
            markLinesY[markLinesYnum++]=((pos-startY)*pixY)/(endY-startY+1);
        }
        /**
         * Sets the vertical DotMatrix size in pixels stored in this DotMatrixObj.
         *
         * the height as an integer.
         */
        public void setPixY(int pY) {
                pixY=pY;
        }

        /**
         * Set the horizontal sequence name in this DotMatrixObj.
         *
         * the name as a String.
         */
        public void setNameX(String nm) {
                nameX=nm;
        }

        /**
         * Sets the vertical sequence name in this DotMatrixObj.
         *
         * the name as a String.
         */
        public void setNameY(String nm) {
                nameY=nm;
        }

        /**
         * Sets the horizontal sequence description in this DotMatrixObj.
         *
         * the description as a String..
         */
        public void setDescriptionX(String desc) {
                descriptionX=desc;
        }

        /**
         * Sets the horizontal sequence description in this DotMatrixObj.
         *
         * the description as a String..
         */
        public void setDescriptionY(String desc) {
                descriptionY=desc;
        }

        /**
         * Sets the start position of horizontal in this DotMatrixObj.
         *
         * the start as an int.
         */
        public void setStartX(int st) {
                startX=st;
        }

        /**
         * Sets the start position of vertical sequence in this DotMatrixObj.
         *
         * the start as an int.
         */
        public void setStartY(int st) {
                startY=st;
        }

        /**
         * Sets the last position of horizontal sequence in this DotMatrixObj.
         *
         * the end as an int.
         */
        public void setEndX(int en) {
                endX=en;
        }

        /**
         * Sets the last position of vertical sequence in this DotMatrixObj.
         *
         * the end as an int.
         */
        public void setEndY(int en) {
                endY=en;
        }

        /**
         * Sets the DotMatrix values in this DotMatrixObj.
         *
         * the dotmatrix as an array of char.
         */
        public void setDotMatrixDirect(char[] dm) {
                DotMatrixDirect=dm;
        }
        public void setDotMatrixInvert(char[] im) {
                DotMatrixInvert=im;
        }
}
