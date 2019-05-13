package dto;

import lombok.Data;

import java.util.List;

/**
 * Description:
 * Designer: jack
 * Date: 2019-05-13
 * Version: 1.0.0
 */

@Data
public class FormatData<T> {
    private String id;
    private String parentId;
    private T data;
    private List<FormatData<T>> children;
}