package it.cnr.si.repo.jscript;

import it.cnr.si.repo.jscript.exception.TransformationException;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ScriptUtils extends BaseScopableProcessorExtension implements ApplicationContextAware{

	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		this.applicationContext = arg0;
	}
	
	public Object getBean(String beanName){
		return applicationContext.getBean(beanName);
	}
	
	public String[] getBeanNamesForType(String className) throws ClassNotFoundException {
		Class<?> myClass = Class.forName(className);
		return applicationContext.getBeanNamesForType(myClass);
	}


	/* reflection utility methods */

	/**
	 * 
	 * Class.forName wrapper
	 * 
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> getClass(String className) throws ClassNotFoundException {
		return Class.forName(className);
	}

	/**
	 * 
	 * Execute a static method with provided arguments
	 * 
	 * @param qname
	 *            the qualified name of the method to invoke
	 * @param params
	 *            the array of parameters
	 * @return the result of the invokation
	 * 
	 * @throws Exception
	 */
	public static Object executeStatic(String qname, Object... params)
			throws Exception {
		int separator = qname.lastIndexOf('.');
		String className = qname.substring(0, separator);
		String method = qname.substring(separator + 1);

		Class[] parameterTypes = new Class[params.length];
		int index = 0;
		for (Object o : params) {
			Class c = null;
			if (o.getClass().equals(Integer.class)) {
				c = int.class;
			} else if (o.getClass().equals(Double.class)) {
				c = double.class;
			} else if (o.getClass().equals(Boolean.class)) {
				c = boolean.class;
			} else {
				c = o.getClass();
			}
			parameterTypes[index] = c;
			++index;
		}

		return Class.forName(className).getMethod(method, parameterTypes)
				.invoke(null, params);

	}

	/**
	 * Get a static class field / constant
	 * 
	 * @param qname
	 *            the qualified name of the constant to retrieve
	 * @return the constant
	 * @throws Exception
	 */
	public static Object constant(String qname) throws Exception {
		int separator = qname.lastIndexOf('.');
		String className = qname.substring(0, separator);
		String field = qname.substring(separator + 1);
		return Class.forName(className).getField(field).get(null);
	}

	/**
	 * Transform a generic Map into a Map whose keys are String. Note that it
	 * could fail if two keys returns the same toString() value.
	 * 
	 * @param map
	 *            the map to transform
	 * @return
	 * @throws it.cnr.si.repo.jscript.exception.TransformationException
	 *             if two keys returns the same toString() value.
	 */
	public static Map<String, Object> transformMap(Map map)
			throws it.cnr.si.repo.jscript.exception.TransformationException {
		Map<String, Object> flat = new HashMap<String, Object>();
		for (Object k : map.keySet()) {
			flat.put(k.toString(), map.get(k));
		}

		if (flat.keySet().size() != map.keySet().size()) {
			throw new TransformationException(
					"unable to transform map: two or more keys produce the same toString() value.");
		}

		return flat;
	}

}