package org.geworkbench.components.genspace;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceRef;

import org.geworkbench.components.genspace.entity.Workflow;
import org.geworkbench.components.genspace.server.PublicFacadeRemote;
import org.geworkbench.components.genspace.server.ToolInformationProvider;

public class SpeedTest {
	
//	@WebServiceRef(wsdlLocation="http://boris.cs.columbia.edu:8080/genspace-ejbWAR/GenericUsageInformationServiceService?wsdl")
//	static ToolInformationProvider svc;
	
	@EJB
	private static PublicFacadeRemote toolInfo;
	public static void main(String[] args) {
		InitialContext ctx;
		try {
			System.out.println("Looking for IC");
			ctx = new InitialContext();
			System.out.println("Have IC");
			PublicFacadeRemote facade = (PublicFacadeRemote) ctx.lookup("org.geworkbench.components.genspace.server.PublicFacadeRemote");
			System.out.println("Have facade");
			System.out.println(facade.getAllTools());
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		toolInfo.getMostPopularWFHeads();
//		List<Workflow> ret = svc.getWorkflowsByPopularity();
//		LoginFactory.getUsageOps().getAllTools();
////		
//		long start  =System.currentTimeMillis();
//		List<Workflow> ret = LoginFactory.getUsageOps().getWorkflowsByPopularity();
//		System.out.println(GenSpace.getObjectSize((Serializable) ret) );
//		System.out.println("Time: " + (System.currentTimeMillis() - start)/1000);
////		
//		start  =System.currentTimeMillis();
//		ret = LoginFactory.getUsageOps().getWorkflowsByPopularity();
//		System.out.println(GenSpace.getObjectSize((Serializable) ret) );
//		System.out.println("Time: " + (System.currentTimeMillis() - start)/1000);
//		System.exit(0);
	}
}
