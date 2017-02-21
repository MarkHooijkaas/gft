package org.kisst.gft.admin;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class SimpleLdapAuthentication {
	private String domain="ibgroep.local";
	private String host="grndc010";
	private String dn="DC=IBGroep,DC=local";
	
	
    public boolean authenticateUser(String username, String password) {
        String returnedAtts[] ={ "sn", "givenName", "mail" };
        String searchFilter = "(&(objectClass=user)(sAMAccountName=" + username + "))";
        //Create the search controls

        SearchControls searchCtls = new SearchControls();
        searchCtls.setReturningAttributes(returnedAtts);
        //Specify the search scope

        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = dn;
        Hashtable<String,String> environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        //Using starndard Port, check your instalation

        environment.put(Context.PROVIDER_URL, "ldap://" + host + ":389");
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");

        environment.put(Context.SECURITY_PRINCIPAL, username + "@" + domain);
        environment.put(Context.SECURITY_CREDENTIALS, password);
        LdapContext ctxGC = null;
        try
        {
            ctxGC = new InitialLdapContext(environment, null);
            NamingEnumeration<?> answer = ctxGC.search(searchBase, searchFilter, searchCtls);
            if (answer==null)
            	throw new RuntimeException("no user found, but could authenticate");
            /*
            while (answer.hasMoreElements())
            {
                SearchResult sr = (SearchResult)answer.next();
                Attributes attrs = sr.getAttributes();
                if (attrs!=null) { 
                     NamingEnumeration<String> it = attrs.getIDs();
                     while (it.hasMoreElements()) { 
                         String id = it.nextElement();
                         System.out.println(id + " = "+attrs.get(id));
                     }
                     System.out.print("****");
                }
            }
            */
            return true;

        }
        catch (NamingException e) { e.printStackTrace(); return false;}
    }
}
