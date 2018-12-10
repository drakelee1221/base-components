/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base.condition;

/**
 * @Description 条件查询常量, 操作符和链接符
 * @author DLee
 * @date 2015年9月5日 上午11:01:12
 * @version V1.0
 */
public enum ConditionEnum {
	/**
	 * "="
	 */
	OPERATE_EQUAL,
	/**
	 * "<>"
	 */
	OPERATE_UNEQUAL,
	/**
	 * ">"
	 */
	OPERATE_GREATER,
	/**
	 * "<"
	 */
	OPERATE_LESS,
	/**
	 * ">="
	 */
	OPERATE_GREATER_EQUAL,
	/**
	 * "<="
	 */
	OPERATE_LESS_EQUAL,
	/**
	 * "in"
	 */
	OPERATE_IN,
	/**
	 * "not in"
	 */
	OPERATE_NOT_IN,
	/**
	 * "like '%***%'" ，性能低
	 */
	@Deprecated
	OPERATE_LIKE,
	/**
	 * "like '%***'"，性能低
	 */
	@Deprecated
	OPERATE_LEFT_LIKE,
	/**
	 * "like '***%'"
	 */
	OPERATE_RIGHT_LIKE,
	/**
	 * "is null"
	 */
	OPERATE_IS_NULL,
	/**
	 * "is not null"
	 */
	OPERATE_IS_NOT_NULL;


	/**
	 * 条件组连接符
	 */
	public enum Link{
		/**
		 * 连接符 and
		 */
		LINK_AND,
		/**
		 * 连接符 or
		 */
		LINK_OR;
	}


}
