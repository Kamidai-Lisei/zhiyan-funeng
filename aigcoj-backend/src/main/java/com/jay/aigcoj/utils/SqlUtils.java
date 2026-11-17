package com.jay.aigcoj.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 * 
 */
public class SqlUtils {

    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
