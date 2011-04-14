//package genspace.ui;
package org.geworkbench.components.genspace;


import java.util.List;

import javax.naming.InitialContext;


import org.geworkbench.components.genspace.entity.Friend;
import org.geworkbench.components.genspace.entity.User;
import org.geworkbench.components.genspace.entity.UserNetwork;
import org.geworkbench.components.genspace.server.FriendFacadeRemote;
import org.geworkbench.components.genspace.server.NetworkFacadeRemote;
import org.geworkbench.components.genspace.server.PublicFacadeRemote;
import org.geworkbench.components.genspace.server.ToolInformationProvider;
import org.geworkbench.components.genspace.server.UsageInformationRemote;
import org.geworkbench.components.genspace.server.UserFacadeRemote;
import org.geworkbench.components.genspace.server.WorkflowRepositoryRemote;
import org.geworkbench.engine.properties.PropertiesManager;


import com.sun.appserv.security.ProgrammaticLogin;

public class LoginFactory {

	private static final String PROPERTY_GENSPACE_LOGIN_USER = "LoginUserId";

	private static User user;

	private static UserFacadeRemote userFacade;
	private static UsageInformationRemote usageFacade;
	private static FriendFacadeRemote friendFacade;
	private static NetworkFacadeRemote networkFacade;
	private static PublicFacadeRemote publicFacade;
	private static WorkflowRepositoryRemote workflowFacade;
	
	public synchronized static WorkflowRepositoryRemote getWorkflowOps()
	{
		if(workflowFacade == null)
			workflowFacade = (WorkflowRepositoryRemote) GenSpace.getRemote("WorkflowRepository");
		return workflowFacade;
	}
	public synchronized static UserFacadeRemote getUserOps()
	{
		if(userFacade == null)
			userFacade = (UserFacadeRemote) GenSpace.getRemote("UserFacade");
		return userFacade;
	}
	
	public synchronized static PublicFacadeRemote getPublicFacade()
	{
		if(publicFacade == null)
			publicFacade = (PublicFacadeRemote) GenSpace.getRemote("PublicFacade");
		return publicFacade;
	}
	
	public synchronized static UsageInformationRemote getPrivUsageFacade()
	{
		if(user == null)
			return null;
		if(usageFacade == null)
			usageFacade = (UsageInformationRemote) GenSpace.getRemote("UsageInformation");
		return usageFacade;
	}
	public synchronized static ToolInformationProvider getUsageOps()
	{
		if(user != null)
			return getPrivUsageFacade();
		else
			return getPublicFacade();
	}

	public synchronized static FriendFacadeRemote getFriendOps()
	{
		if(friendFacade == null)
			friendFacade = (FriendFacadeRemote) GenSpace.getRemote("FriendFacade");
		return friendFacade;
	}
	public synchronized static NetworkFacadeRemote getNetworkOps()
	{
		if(networkFacade == null)
			networkFacade = (NetworkFacadeRemote) GenSpace.getRemote("NetworkFacade");
		return networkFacade;
	}
	
	
	public LoginFactory() {
		super();
	}

	
	public static User getUser() {
		return user;
	}
	public static boolean userRegister(User u) {
		user = getPublicFacade().register(u);
		if(user != null)
			return true;
		return false;
	}
	static ProgrammaticLogin pm = new ProgrammaticLogin();
	public static boolean userLogin(String username, String password) {
		
		System.setProperty("java.security.auth.login.config", "login.conf");
		try {
			
			pm.login(username, password,"GELogin",true);
			InitialContext ctx = new InitialContext();
			ctx.lookup("org.geworkbench.components.genspace.server.UserFacadeRemote");
			user = getUserOps().getMe();
		} catch (Exception e) {
			return false;
		}
		

			GenSpace.getInstance().getWorkflowRepository().updateUser();

				try {
					PropertiesManager properties = PropertiesManager
							.getInstance();
//					properties.setProperty(GenSpaceLogin.class,
//							PROPERTY_GENSPACE_LOGIN_USER, user.getUsername()); //todo: probably unused
				} catch (Exception ex) {
				}

		return true;
	
	}
	public static void updateCachedUser()
	{
		user = getUserOps().getMe();
	}
	public static boolean userUpdate() {

		getUserOps().updateUser(user);
		return true;
	}

	public static List<UserNetwork> getAllNetworks() {
		return user.getNetworks();
	}

	public static String getUsername() {
		if(user == null)
			return null;
		return user.getUsername();
	}

	public static boolean isLoggedIn() {
		return user != null;
	}

	public static void logout() {
		try {
			pm.logout(true);
			userFacade = null;
			usageFacade = null;
			friendFacade = null;
			networkFacade = null;
			publicFacade = null;
			workflowFacade = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user = null;
	}

	/**
	 * Returns if the currently logged in user may view the profile of the specified user
	 * @param user2
	 * @return
	 */
	public static boolean isVisible(User user2) {
		Friend f = user2.isFriendsWith(getUser());
		if(f != null && f.isVisible())
		{
			return true;
		}
		//Check the networks
		for(UserNetwork u1 : user2.getNetworks())
		{
			if(u1.isVisible())
				for(UserNetwork u2 : getUser().getNetworks())
				{
					if(u2.getNetwork().equals(u1.getNetwork()))
						return true;
				}
		}
		return false;
	}

	
}
