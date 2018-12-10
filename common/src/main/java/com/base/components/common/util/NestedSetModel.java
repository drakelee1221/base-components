/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;



/**
 * 左右值模型计算工具
 * @author DLee
 * @date 2015年9月15日 下午11:05:14
 * @version V1.0
 *
 */
public class NestedSetModel {
	private NestedSetModel() {
    }

    /**
     * 帮助计算出按照不同关系对目标节点插入新节点是一些参数值。<p/>
     * 通过该方法获得返回值后可以根据以下 SQL 语句向数据库插入新数据。<p/>
	 * UPDATE 表名 SET Lft = Lft + 2 WHERE Lft > return[0]; <p/>
	 * UPDATE 表名 SET Rgt = Rgt + 2 WHERE Rgt > return[0]; <p/>
	 * INSERT INTO 表名(Lft, Rgt, 其它字段) VALUES(return[1], return[1] + 1, 其它值); <p/>
     * @param targetLft - 目标节点的左值。
     * @param targetRgt - 目标节点的右值。
     * @param relationship - 新节点在目标节点的那一个位置插入。
     * @return 返回一个长度为 2 的 int 类型数组，该数组在 SQL 中的使用请参考该方法的帮助。
     */
    public static int[] insert(int targetLft, int targetRgt, Relationship relationship) {
        int[] re = new int[2];
        switch(relationship.ordinal()) {
        case 0:
            re[0] = targetLft - 1;
            re[1] = targetLft;
            break;
        case 1:
            re[0] = targetLft;
            re[1] = targetLft + 1;
            break;
        case 2:
            re[0] = targetRgt - 1;
            re[1] = targetRgt;
            break;
        case 3:
            re[0] = targetRgt;
            re[1] = targetRgt + 1;
            break;
        default:
            throw new AssertionError(relationship.name());
        }

        return re;
    }

    /**
     * 帮助计算出删除目标节点的一些参数值。<p/>
	 * 通过该方法获得返回值后可以根据以下 SQL 语句向数据库删除数据。<p/>
	 * DELETE FROM 表名 WHERE Lft >= return[0] and Rgt <= return[1]; <p/>
	 * UPDATE 表名 SET Lft = Lft - (return[1] - return[0] + 1) WHERE Lft > return[0]; <p/>
 	 * UPDATE 表名 SET Rgt = Rgt - (return[1] - return[0] + 1) WHERE Rgt > return[1]; <p/>
     * @param targetLft - 目标节点的左值。
     * @param targetRgt - 目标节点的右值。
     * @return 返回一个长度为 2 的 int 类型数组，该数组在 SQL 中的使用请参考该方法的帮助。
     */
    public static int[] delete(int targetLft, int targetRgt) {
        return delete(targetLft, targetRgt, false);
    }

    /**
     * 帮助计算出删除目标节点的一些参数值。<p/>
	 * 通过该方法获得返回值后可以根据以下 SQL 语句向数据库删除数据。<p/>
	 * DELETE FROM 表名 WHERE Lft >= return[0] and Rgt <= return[1]; <p/>
	 * UPDATE 表名 SET Lft = Lft - (return[1] - return[0] + 1) WHERE Lft > return[0]; <p/>
	 * UPDATE 表名 SET Rgt = Rgt - (return[1] - return[0] + 1) WHERE Rgt > return[1]; <p/>
     * @param targetLft - 目标节点的左值。
     * @param targetRgt - 目标节点的右值。
     * @param onlyChild - 为 true 时代表保留目标节点只删除其下的子代节点，为 false 时代表删除目标节点及其所有子代节点
     * @return 返回一个长度为 2 的 int 类型数组，该数组在 SQL 中的使用请参考该方法的帮助。
     */
    public static int[] delete(int targetLft, int targetRgt, boolean onlyChild) {
        int[] re = new int[2];
        if(onlyChild) {
            re[0] = targetLft + 1;
            re[1] = targetRgt - 1;
        } else {
            re[0] = targetLft;
            re[1] = targetRgt;
        }

        return re;
    }

    /**
     * 
     * 帮助计算出更新目标节点的一些参数值。<p/>
	 * 通过该方法获得返回值后可以根据以下 SQL 语句向数据库删除数据。<p/>
	 * UPDATE 表名 SET Lft = Lft * -1, Rgt = Rgt * -1 WHERE Lft >= updateLft and Lft <= updateRgt;  <p/>
	 * UPDATE 表名 SET Lft = Lft + (return[3]) WHERE Lft > return[0] and Lft < return[1]; <p/>
	 * UPDATE 表名 SET Rgt = Rgt + (return[3]) WHERE Rgt > return[0] and Rgt < return[1]; <p/>
	 * UPDATE 表名 SET Lft = Lft * -1 + (return[2]), Rgt = Rgt * -1 + (return[2]) WHERE Lft <= updateLft * -1 and Rgt >= updateRgt * -1;
     * @param targetLft - 目标节点的左值。
     * @param targetRgt - 目标节点的右值。
     * @param updateLft - 更新节点的左值。
     * @param updateRgt - 更新节点的右值。
     * @param relationship - 更新节点改变到目标节点的那一个位置。
     * @return 返回一个长度为 4 的 int 类型数组，该数组在 SQL 中的使用请参考该方法的帮助。
     */
    public static int[] update(int targetLft, int targetRgt, int updateLft, int updateRgt, Relationship relationship) {
        if(targetLft >= 1 && targetRgt >= 1 && updateLft >= 1 && updateRgt >= 1) {
            if(targetLft == targetRgt) {
                throw new IllegalArgumentException("目标节点的左右值不能相等.");
            } else if(updateLft == updateRgt) {
                throw new IllegalArgumentException("更新节点的左右值不能相等.");
            } else if(updateLft < targetLft && updateRgt > targetRgt) {
                throw new IllegalArgumentException("目标是其子代");
            } else if(updateLft == targetLft) {
                throw new IllegalArgumentException("参数“targetLft”和“updateLft”值不能相同。");
            } else if(updateRgt == targetRgt) {
                throw new IllegalArgumentException("参数“targetRgt”和“updateRgt”值不能相同。");
            } else if((relationship == Relationship.After || relationship == Relationship.Before) && targetLft == 1) {
                throw new IllegalArgumentException("不可以改变为跟节点的兄弟节点.");
            } else {
                boolean isForward = true;
                if(updateLft < targetLft) {
                    isForward = false;
                }

                if(targetLft < updateLft && targetRgt > updateRgt) {
                    switch(relationship.ordinal()) {
                    case 0:
                    case 1:
                        isForward = true;
                        break;
                    case 2:
                    case 3:
                        isForward = false;
                        break;
                    default:
                        throw new AssertionError(relationship.name());
                    }
                }

                int[] re = new int[4];
                int finalLftValue;
                switch(relationship.ordinal()) {
                case 0:
                    if(isForward) {
                        re[0] = targetLft - 1;
                        re[1] = updateLft;
                        re[2] = targetLft - updateLft;
                        re[3] = updateRgt - updateLft + 1;
                    } else {
                        re[3] = (updateRgt - updateLft + 1) * -1;
                        finalLftValue = targetLft + re[3];
                        re[0] = updateRgt;
                        re[1] = targetLft;
                        re[2] = finalLftValue - updateLft;
                    }
                    break;
                case 1:
                    if(isForward) {
                        finalLftValue = targetLft + 1;
                        re[0] = finalLftValue - 1;
                        re[1] = updateLft;
                        re[2] = finalLftValue - updateLft;
                        re[3] = updateRgt - updateLft + 1;
                    } else {
                        re[3] = (updateRgt - updateLft + 1) * -1;
                        finalLftValue = targetLft + re[3] + 1;
                        re[0] = updateRgt;
                        re[1] = targetLft + 1;
                        re[2] = finalLftValue - updateLft;
                    }
                    break;
                case 2:
                    if(isForward) {
                        re[0] = targetRgt - 1;
                        re[1] = updateLft;
                        re[2] = targetRgt - updateLft;
                        re[3] = updateRgt - updateLft + 1;
                    } else {
                        re[3] = (updateRgt - updateLft + 1) * -1;
                        finalLftValue = targetRgt + re[3];
                        re[0] = updateRgt;
                        re[1] = targetRgt;
                        re[2] = finalLftValue - updateLft;
                    }
                    break;
                case 3:
                    if(isForward) {
                        finalLftValue = targetRgt + 1;
                        re[0] = finalLftValue - 1;
                        re[1] = updateLft;
                        re[2] = finalLftValue - updateLft;
                        re[3] = updateRgt - updateLft + 1;
                    } else {
                        re[3] = (updateRgt - updateLft + 1) * -1;
                        finalLftValue = targetRgt + re[3] + 1;
                        re[0] = updateRgt;
                        re[1] = targetRgt + 1;
                        re[2] = finalLftValue - updateLft;
                    }
                    break;
                default:
                    throw new AssertionError(relationship.name());
                }

                return re;
            }
        } else {
            throw new IllegalArgumentException("节点的左右值都必须大于 1.");
        }
    }
    
}
