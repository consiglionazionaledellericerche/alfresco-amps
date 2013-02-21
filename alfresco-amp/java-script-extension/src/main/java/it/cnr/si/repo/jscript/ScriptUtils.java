package it.cnr.si.repo.jscript;

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
}