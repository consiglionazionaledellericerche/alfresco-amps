package it.cnr.si.inca.ldap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.ldap.LDAPAuthenticationComponentImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LDAPCustomAuthenticationComponentImpl extends LDAPAuthenticationComponentImpl
{

  private String InitialContextFactory		= "com.sun.jndi.ldap.LdapCtxFactory";
  private String SecurityProtocol			= "";
  private String ProviderUrl				= "ldap://127.0.0.1";
  private String SecurityAuthentication		= "simple";
  private String SecurityPrincipal			= "cn=mastercnrapp8,ou=account,o=cnr,c=it";
  private String SecurityCredentials		= "pippa";
  private String LdapCnrApp					= "cnrapp8";

  private String InitialContextFactoryMaster	= "com.sun.jndi.ldap.LdapCtxFactory";
  private String SecurityProtocolMaster			= "ssl";
  private String ProviderUrlMaster				= "ldaps://phpadmin.cnr.it";
  private String SecurityAuthenticationMaster	= "simple";
  private String SecurityPrincipalMaster		= "cn=mastercnrapp8,ou=account,o=cnr,c=it";
  private String SecurityCredentialsMaster		= "pippa";
  private String LdapCnrAppMaster				= "cnrapp8";
  
  Properties users = new Properties();

  
  protected final static Log log = LogFactory.getLog(LDAPCustomAuthenticationComponentImpl.class);

  public LDAPCustomAuthenticationComponentImpl() {
	  super();
	  loadProperties();
  }

  protected void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
	  log.warn("ProviderUrlMaster: "+ProviderUrlMaster);
	  if (users.get(userName)!=null) {
	  		String password1 = users.get(userName).toString();
	  		String password2 = (new StringBuffer().append(password)).toString();
	  		if (password1.equals(password2)) {
	            setCurrentUser(escapeUserName(userName, true));
	  			return;
	  		}
	  		else
	  			throw new AuthenticationException("Login Failed");
	  	}
        InitialDirContext ctx = null;
        try
        {
        	String sInitialContextFactory = InitialContextFactory;
        	String sSecurityProtocol = SecurityProtocol;
        	String sProviderUrl = ProviderUrl;
        	String sSecurityAuthentication = SecurityAuthentication;
        	String sSecurityPrincipal = SecurityPrincipal;
        	String sSecurityCredentials = SecurityCredentials;
        	String sLdapCnrApp = LdapCnrApp; 

        	// Set up the environment for creating the initial context
            Hashtable env = new Hashtable();

            env.put(Context.INITIAL_CONTEXT_FACTORY, sInitialContextFactory);
            if (!sSecurityProtocol.isEmpty())
            	env.put(Context.SECURITY_PROTOCOL, sSecurityProtocol);
            env.put(Context.PROVIDER_URL, sProviderUrl);
            env.put(Context.SECURITY_AUTHENTICATION, sSecurityAuthentication);
            env.put(Context.SECURITY_PRINCIPAL, sSecurityPrincipal);
            env.put(Context.SECURITY_CREDENTIALS, sSecurityCredentials);
            try
            {
            	ctx = new InitialDirContext(env);

            } catch (NamingException ex) {
            	sInitialContextFactory = InitialContextFactoryMaster;
            	sSecurityProtocol = SecurityProtocolMaster;
            	sProviderUrl = ProviderUrlMaster;
            	sSecurityAuthentication = SecurityAuthenticationMaster;
            	sSecurityPrincipal = SecurityPrincipalMaster;
            	sSecurityCredentials = SecurityCredentialsMaster;
            	sLdapCnrApp = LdapCnrAppMaster; 

                Hashtable env2 = new Hashtable();

                // Set up the environment for creating the initial context
                env2.put(Context.INITIAL_CONTEXT_FACTORY, sInitialContextFactory);
                if (!sSecurityProtocol.isEmpty())
                	env2.put(Context.SECURITY_PROTOCOL, sSecurityProtocol);
                env2.put(Context.PROVIDER_URL, sProviderUrl);
                env2.put(Context.SECURITY_AUTHENTICATION, sSecurityAuthentication);
                env2.put(Context.SECURITY_PRINCIPAL, sSecurityPrincipal);
                env2.put(Context.SECURITY_CREDENTIALS, sSecurityCredentials);

            	ctx = new InitialDirContext(env2);
            }

            Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope (SearchControls.SUBTREE_SCOPE);
            NamingEnumeration answer = null;

            //answer = ctx.search(sProviderUrl+"/o=cnr, c=it", "(uid="+userName +")", constraints);
            answer = ctx.search("o=cnr, c=it", "(uid="+userName +")", constraints);
            String cienne = "";
            String cnrapp = "";
            if (answer.hasMoreElements()) {
            	SearchResult srtmp = (SearchResult) answer.nextElement();
            	cienne = srtmp.getName();

            	Attributes attrs = srtmp.getAttributes();
            	Attribute t1 = attrs.get(sLdapCnrApp);
            	if (t1 != null) cnrapp = (String) t1.get();
            	
    	        //log.warn("CNRAPP "+sLdapCnrApp+": "+ cnrapp);
            } else {
            	clearCurrentSecurityContext();
            	throw new AuthenticationException("Login Failed");
            }		 

            if (!cnrapp.trim().equalsIgnoreCase("si")) {
                clearCurrentSecurityContext();
                throw new AuthenticationException("Login Failed");

            }
            cienne=cienne + ",o=cnr,c=it";
            String passi = new String(password);
            
            Hashtable env2 = new Hashtable();

            env2.put(Context.INITIAL_CONTEXT_FACTORY, sInitialContextFactory);
            if (sSecurityProtocol!=null)
            	env2.put(Context.SECURITY_PROTOCOL, sSecurityProtocol);
            env2.put(Context.PROVIDER_URL,  sProviderUrl);
            env2.put(Context.SECURITY_AUTHENTICATION, sSecurityAuthentication);
            env2.put(Context.SECURITY_PRINCIPAL, cienne);
            env2.put(Context.SECURITY_CREDENTIALS, passi);

            ctx.close();
            ctx = new InitialDirContext(env2);

            setCurrentUser(escapeUserName(userName, true));

        } catch (NamingException e) {
          e.printStackTrace();
          clearCurrentSecurityContext();
          throw new AuthenticationException("Failed to close connection", e);
        } finally {
            if (ctx != null)
            {
                try
                {
                    ctx.close();
                }
                catch (NamingException e)
                {
                    clearCurrentSecurityContext();
                    throw new AuthenticationException("Failed to close connection", e);
                }
            }
        }
    }
    private static String escapeUserName(String userName, boolean escape)
    {
        if (escape)
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < userName.length(); i++)
            {
                char c = userName.charAt(i);
                if (c == ',')
                {
                    sb.append('\\');
                }
                sb.append(c);
            }
            return sb.toString();

        }
        else
        {
            return userName;
        }

    }
	public void loadProperties() {
        String className = this.getClass().getPackage().getName();
        className = className.replace('$', '-');
        className = className.replace('.', '/');
		try {
			//Properties  prop = new Properties();

			//prop.load(new FileInputStream(new File("ldap-custom-cnr.properties")));

	        String propertiesUrl = className + "/ldap-custom-cnr.properties";

	        URL url = getClass().getClassLoader().getResource(propertiesUrl);
	        InputStream is = null;
	        if(url != null)
	        	is = url.openStream();
            if(is != null) {
                Properties prop = new Properties();
                prop.load(is);

    			InitialContextFactory = prop.getProperty("ldap.authentication.java.naming.factory.initial");
    			SecurityProtocol = prop.getProperty("ldap.authentication.java.naming.security.protocol");
    			ProviderUrl = prop.getProperty("ldap.authentication.java.naming.provider.url");
    			SecurityAuthentication = prop.getProperty("ldap.authentication.java.naming.security.authentication");
    			SecurityPrincipal = prop.getProperty("ldap.authentication.java.naming.security.principal");
    			SecurityCredentials = prop.getProperty("ldap.authentication.java.naming.security.credentials");
    			LdapCnrApp = prop.getProperty("ldap.authentication.cnrapp");
    			
    			InitialContextFactoryMaster = prop.getProperty("ldap.authentication.java.naming.factory.initial.master");
    			SecurityProtocolMaster = prop.getProperty("ldap.authentication.java.naming.security.protocol.master");
    			ProviderUrlMaster = prop.getProperty("ldap.authentication.java.naming.provider.url.master");
    			SecurityAuthenticationMaster = prop.getProperty("ldap.authentication.java.naming.security.authentication.master");
    			SecurityPrincipalMaster = prop.getProperty("ldap.authentication.java.naming.security.principal.master");
    			SecurityCredentialsMaster = prop.getProperty("ldap.authentication.java.naming.security.credentials.master");
    			LdapCnrAppMaster = prop.getProperty("ldap.authentication.cnrapp.master");
            }

		} catch (FileNotFoundException e) {
	        //log.error("File ldap-custom-cnr.properties non trovato.", e);
		} catch (IOException e) {
	        log.error("Errore nella lettura del file ldap-custom-cnr.properties.", e);
		} catch (Exception e) {
	        log.error("Errore generico nella lettura del file ldap-custom-cnr.properties.", e);
		}
		try {
	        String usersUrl = className + "/ldap-users-cnr.properties";

	        URL url = getClass().getClassLoader().getResource(usersUrl);
	        InputStream isu = null;
	        if(url != null)
	        	isu = url.openStream();
            if(isu != null) {
                users.load(isu);
            }
			
		} catch (FileNotFoundException e) {
	        //log.error("File ldap-custom-cnr.properties non trovato.", e);
		} catch (IOException e) {
	        log.error("Errore nella lettura del file ldap-users-cnr.properties.", e);
		} catch (Exception e) {
	        log.error("Errore generico nella lettura del file ldap-users-cnr.properties.", e);
		}
	}
}
