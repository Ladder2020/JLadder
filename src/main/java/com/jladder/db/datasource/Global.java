package com.jladder.db.datasource;

import com.jladder.Ladder;
import com.jladder.db.DbInfo;

/**
 * 全局的数据源工厂<br>
 * 一般情况下，一个应用默认只使用一种数据库连接池，因此维护一个全局的数据源工厂类减少判断连接池类型造成的性能浪费
 *
 * @author looly
 * @since 4.0.2
 */
public class Global {

	private static volatile DataSourceFactory factory;
	private static final Object lock = new Object();

	/*
	 * 设置在JVM关闭时关闭所有数据库连接
	 */
	static {
		// JVM关闭时关闭所有连接池
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (null != factory) {
					factory.destroy();
					//StaticLog.debug("DataSource: [{}] destroyed.", factory.dataSourceName);
					factory = null;
				}
			}
		});
	}
	public static DataSourceFactory get(){
		return get(null);
	}
	/***
	 *
	 * @return
	 */
	public static DataSourceFactory get(DbInfo info) {
		if (null == factory) {
			synchronized (lock) {
				if (null == factory) {
					factory = info == null ?DataSourceFactory.create() :  DataSourceFactory.create(info);
				}
			}
		}else{
			if(info!=null) Ladder.Settings().getDatabase().put(info.getName(),info);
		}

		return factory;
	}

	/**
	 * 设置全局的数据源工厂<br>
	 * 在项目中存在多个连接池库的情况下，我们希望使用低优先级的库时使用此方法自定义之<br>
	 * 重新定义全局的数据源工厂此方法可在以下两种情况下调用：
	 *
	 * <pre>
	 * 1. 在get方法调用前调用此方法来自定义全局的数据源工厂
	 * 2. 替换已存在的全局数据源工厂，当已存在时会自动关闭
	 * </pre>
	 *
	 * @param customDSFactory 自定义数据源工厂
	 * @return 自定义的数据源工厂
	 */
	public static DataSourceFactory set(DataSourceFactory customDSFactory) {
		synchronized (lock) {
			if (null != factory) {
				if (factory.equals(customDSFactory)) {
					return factory;// 数据源工厂不变时返回原数据源工厂
				}
				// 自定义数据源工厂前关闭之前的数据源
				factory.destroy();
			}

//			StaticLog.debug("Custom use [{}] DataSource.", customDSFactory.dataSourceName);
			factory = customDSFactory;
		}
		return factory;
	}
}
