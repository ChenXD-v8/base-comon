package com.cxd.basecommon.excelimport;


import java.lang.annotation.*;

/**
 * <p>
 * excel导入column
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/09/28 14:51
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelImportColumn {

    /**
     * 列序号
     *
     */
    int index() default 0;
    String columnName() default "";
}
