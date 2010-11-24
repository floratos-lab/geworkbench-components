package org.geworkbench.components.idea;

import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Erf ;
import java.util.Arrays;


/**
 * Mutual Information caculation
 * 
 * @author zm2165
 * @version $id$
 *
 */
public class MutualInfo {
	
	private double [] x;	//array of x for mutual information calculation	
	private double [] y;	//array of y
	private double h;		//parameter in mutual information
	private int n;			//length of array of x or array of y which should be the same	
	private double mi;		//result
	
	public MutualInfo(double[] x, double [] y, double h){
		this.x=x;
		this.y=y;
		this.h=h;
		n=x.length;
	}
	
	public MutualInfo(double[] x, double [] y) throws MathException{
		this.x=x;
		this.y=y;
		//double a = 0.364119;
		//double b = -0.151931;
		double a = 0.52477;
		double b = -0.24;
		
		n=x.length;
		h = a*Math.pow(n, b);
		cacuMutualInfo();
	}
	
	private void cacuMutualInfo() throws MathException{
		double [][] probTable=new double [n][n];
		double [] normTable1D=new double [n];
		double [][] normTable2D=new double [n][n];
		double [] xCopy = new double [n];
		double [] yCopy = new double [n];
		int [] rt_x = new int [n];
		int [] rt_y = new int [n];
		double [] xx = new double [n];
		double [] yy = new double [n];
		double ss;
		
		final double MINDOUBLE=0.000000001;		
		final double SQRT2=1.414213562373;	
		
		for(int i=0;i<n;i++){
			double k=(double)i/(n-1);
			normTable1D[i]=0.5*(Erf.erf((1-k)/(h*SQRT2))-Erf.erf((-1)*k/(h*SQRT2)));
		}		
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				normTable2D[i][j]=normTable1D[i]*normTable1D[j];
				probTable[i][j]=-1.0;
			}
		}
		
		int [] repeat=new int[n];
		for (int i=0;i<n;i++){
			xCopy[i]=x[i];
			yCopy[i]=y[i];
			repeat[i]=0;
		}
		
		Arrays.sort(xCopy);
		Arrays.sort(yCopy);
		
		for (int i=0;i<n;i++){	//process rt_x, regular binarySearch may return the same int when value repeats
								//which is the reason I don't use it.
			boolean aFlag=false;
			for(int j=0;j<n;j++){
				if ((x[i]-xCopy[j])<MINDOUBLE){
					if(!aFlag){
						rt_x[i]=j+repeat[j];
						repeat[j]++;
						aFlag=true;
					}
					
				}
			}
		}
		
		for (int i=0;i<n;i++) repeat[i]=0;
		
		for (int i=0;i<n;i++){//process rt_y			
			boolean aFlag=false;
			for(int j=0;j<n;j++){
				if ((y[i]-yCopy[j])<MINDOUBLE){
					if(!aFlag){
						rt_y[i]=j+repeat[j];
						repeat[j]++;
						aFlag=true;
					}
					
				}
			}
		}
		
		
		for (int i=0;i<n;i++){
			xx[i]=((double) rt_x[i])/(n-1);//rt_x[i]-1)/(n-1) in matlab
			yy[i]=((double) rt_y[i])/(n-1);
		}
		
		
		ss=0;
		for(int i=0;i<n;i++){
			double fxy=0;
			double fx=0;
			double fy=0;
			for(int j=0;j<n;j++){
				int ix=Math.abs(rt_x[i]-rt_x[j]);
				int iy=Math.abs(rt_y[i]-rt_y[j]);
				double dx=xx[i]-xx[j];
				double dy=yy[i]-yy[j];
				
				if(Math.abs(probTable[ix][iy]+1.0)<MINDOUBLE){
					if(Math.abs(probTable[ix][0]+1.0)<MINDOUBLE){
						probTable[ix][0]=Math.exp(-(dx*dx)/(2*h*h));
						probTable[0][ix]=probTable[ix][0];
					}
					if(Math.abs(probTable[0][iy]+1.0)<MINDOUBLE){
						probTable[0][iy]=Math.exp(-(dy*dy)/(2*h*h));
						probTable[iy][0]=probTable[0][iy];
					}
					probTable[ix][iy]=Math.exp(-(dx*dx+dy*dy)/(2*h*h));
					probTable[iy][ix]=probTable[ix][iy];					
				}
				fx+=probTable[ix][0]/normTable1D[rt_x[j]];
				fy+=probTable[0][iy]/normTable1D[rt_y[j]];
				fxy+=probTable[ix][iy]/normTable2D[rt_x[j]][rt_y[j]];				
			}//inner for
			ss+=Math.log(n*fxy/(fx*fy));			
		}//outer for
		mi=Math.max(ss/n, 0);
	}
	
	public double getMI(){
		return mi;
	}
	
	

}
