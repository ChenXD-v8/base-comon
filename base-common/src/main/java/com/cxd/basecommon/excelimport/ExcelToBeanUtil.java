package com.cxd.basecommon.excelimport;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * excel数据转bean
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/09/28 15:05
 */
@Slf4j
public class ExcelToBeanUtil {

    public static <T> List<T> generateDataList(MultipartFile file, Class<T> clazz){
        return generateDataList(file,0,clazz);
    }

    /**
     * 从excel中遍历数据 封装到 model中
     * @param file excel文件
     * @param sheetIndex sheet页索引
     * @param clazz 返回类型
     * @param <T> T
     * @return List<T>
     */
    public static <T> List<T> generateDataList(MultipartFile file, int sheetIndex,Class<T> clazz) {
        List<T> list= new ArrayList<>();
        // 根据 class 获取set方法与 属性在excel 列号 生成map
        Map<Integer, Method> methodMap = getMethodMapFromClass(clazz);
        InputStream fileIo = null;
        Workbook workBook = null;
        try {
            // 获取输入流
            fileIo = file.getInputStream();
            workBook   = WorkbookFactory.create(fileIo);
            // 获取sheet
            Sheet sheet = workBook.getSheetAt(sheetIndex);
            int rowSize = sheet.getLastRowNum();
            if (rowSize < 1){
                return list;
            }
            // 遍历excel 行信息
            for (int i = 1; i <= rowSize; i++) {
                Row row = sheet.getRow(i);
                // new Object
                T t = clazz.newInstance();
                // 遍历行每一列
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    // 获取cell value
                    String value = handleCellDataType(row.getCell(j));
                    // 如果第一列值为空 则返回 不继续遍历
                    if (j == 0 && StringUtils.isEmpty(value)){
                        return list;
                    }
                    // map中包含列号 则调用set方法复制
                    if (methodMap.containsKey(j)){
                        Method method = methodMap.get(j);
                        method.invoke(t,value);
                    }
                }
                list.add(t);
            }
        } catch (Exception e) {
            log.error("convert excel to model[{}] error: {}",clazz.getName(),e.getMessage());
            e.printStackTrace();
        }finally {
            // 关闭流
            closeStream(workBook,fileIo);
        }
        return list;
    }


    public static Map<Integer,Method> getMethodMapFromClass(Class clazz){
        Map<Integer,Method> methodMap = new HashMap<>();
        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                if (field.isAnnotationPresent(ExcelImportColumn.class)) {
                    ExcelImportColumn annotation = field.getAnnotation(ExcelImportColumn.class);
                    int index = annotation.index();
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
                    Method method = propertyDescriptor.getWriteMethod();
                    methodMap.put(index,method);
                }
            }
        }catch (Exception e){
            log.error("get method and columnIndex from class [{}] error: {}",clazz.getName(),e.getMessage());
        }
        return methodMap;
    }

    public static void closeStream(Workbook workbook,InputStream inputStream){
        if (workbook != null && inputStream != null){
            try {
                workbook.close();
                inputStream.close();
            } catch (IOException e) {
                log.error("流关闭失败：{}",e.getMessage());
            }
        }
    }

    public static String handleCellDataType(Cell cell) {
        if (null == cell) {
            return "";
        }
        String value = "";
        // 格式化number
        DecimalFormat df = new DecimalFormat("#.#########");
        // 日期格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 格式化数字
        DecimalFormat df2 = new DecimalFormat("0");
        switch (cell.getCellType()) {
            case NUMERIC:
                String format = cell.getCellStyle().getDataFormatString();
                if ("General".equals(format)) {
                    value = df.format(cell.getNumericCellValue());
                } else if ("m/d/yy".equals(format)) {
                    value = sdf.format(cell.getDateCellValue());
                } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    value = sdf.format(cell.getDateCellValue());
                } else {
                    value = df2.format(cell.getNumericCellValue());
                }
                break;
            case STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            case FORMULA:
                try {
                    value = String.valueOf(cell.getStringCellValue());
                } catch (IllegalStateException e) {
                    value = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            default:
                break;
        }
        return value;
    }

}
