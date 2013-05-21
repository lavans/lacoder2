package jp.co.sbisec.iris.common.di;

import static org.testng.Assert.assertTrue;
import jp.co.sbisec.iris.common.di.mock.Bean;
import jp.co.sbisec.iris.common.di.mock.BeanImpl;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.FileUtils;

public class BeanManagerTest {
	@BeforeTest
	public void setup() {
		BeanManager.load(FileUtils.makeResourceFileName(this.getClass()));
	}

	@Test
	public void getBeanClassT() {
		Bean bean = BeanManager.getBean(Bean.class);
		assertTrue(bean instanceof BeanImpl);
	}

	@Test
	public void getBeanString() {
		Bean bean = BeanManager.getBean(Bean.class.getName());
		assertTrue(bean instanceof BeanImpl);
	}

	@Test
	public void getBeanClassString() {
		Class<? extends Bean> beanClass = BeanManager.getBeanClass(Bean.class.getName());
		assertTrue(beanClass.getName().equals(BeanImpl.class.getName()));
	}
	
	@Test
	public void getBeanFromGroup() {
		// load config
		BeanManager.init();
		BeanManager.load(FileUtils.makeResourceFileName(this.getClass(), "group"));
		
		// check bean
		Bean bean = BeanManager.getBean("mock", "Bean");
		assertTrue(bean instanceof BeanImpl);
	}


	@Test
	public void getBeanInfo() {
//		BeanManager.getbeani
//		throw new RuntimeException("Test not implemented");
	}
}
