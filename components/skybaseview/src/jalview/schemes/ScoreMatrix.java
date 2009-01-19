package jalview.schemes;

public class ScoreMatrix
{
  String name;
  /**
   * reference to integer score matrix
   */
  int[][] matrix;
  /**
   * 0 for Protein Score matrix. 1 for dna score matrix
   */
  int type;
  ScoreMatrix(String name, int[][] matrix, int type)
  {
    this.matrix = matrix;
    this.type = type;
  }

  public boolean isDNA()
  {
    return type == 1;
  }

  public boolean isProtein()
  {
    return type == 0;
  }

  public int[][] getMatrix()
  {
    return matrix;
  }

  /**
   *
   * @param A1
   * @param A2
   * @return score for substituting first char in A1 with first char in A2
   */
  public int getPairwiseScore(String A1, String A2)
  {
    return getPairwiseScore(A1.charAt(0), A2.charAt(0));
  }

  public int getPairwiseScore(char c, char d)
  {
    int pog = 0;

    try
    {
      int a = (type == 0) ? ResidueProperties.aaIndex[c] :
          ResidueProperties.nucleotideIndex[c];
      int b = (type == 0) ? ResidueProperties.aaIndex[d] :
          ResidueProperties.nucleotideIndex[d];

      pog = matrix[a][b];
    }
    catch (Exception e)
    {
      //System.out.println("Unknown residue in " + A1 + " " + A2);
    }

    return pog;
  }

}
