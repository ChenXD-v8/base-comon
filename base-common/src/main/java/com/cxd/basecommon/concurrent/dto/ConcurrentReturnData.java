package com.cxd.basecommon.concurrent.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


/**
 * <p>
 * 并遍历执行返回结果
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/09/27 15:34
 */
@Data
@AllArgsConstructor
@ToString
public class ConcurrentReturnData<K> {
    /**
     * 执行成功条数
     */
    private int successElements;
    /**
     * 执行失败条数
     */
    private int errorElements;

    /**
     * 执行总条数
     */
    private int totalElements;
    /**
     * 失败list
     */
    private List<K> errorList;
    /**
     * 错误消息
     */
    private List<String> errorMessages;

    /**
     * 合并返回结果
     *
     * @param returnData 返回结果1
     * @return this
     */
    public ConcurrentReturnData<K> add(ConcurrentReturnData<K> returnData) {
        this.errorElements = this.errorElements + returnData.errorElements;
        this.successElements = this.successElements + returnData.successElements;
        this.errorList.addAll(returnData.getErrorList());
        this.errorMessages.addAll(returnData.getErrorMessages());
        this.totalElements = this.totalElements + returnData.totalElements;
        return this;
    }


    public void addSuccessElements(int addSize) {
        this.successElements += addSize;
    }

    public void addErrorElements(int addSize) {
        this.errorElements += addSize;
    }
    public void addErrorMessage(String message) {
        this.errorMessages.add(message);
    }
    public void addErrorList(List<K> list){
        if (this.errorList == null){
            this.errorList = new ArrayList<>();
        }else {
            this.errorList.addAll(list);
        }

    }
}
