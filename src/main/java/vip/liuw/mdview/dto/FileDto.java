package vip.liuw.mdview.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import vip.liuw.mdview.framework.component.SystemConstant;
import vip.liuw.mdview.service.MdFileService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileDto {
    public static final int FILE_TYPE_FILE = 1;
    public static final int FILE_TYPE_DIR = 2;

    private String name;
    /**
     * 1：文件夹，2：文本
     */
    private int type;

    private String id;

    private String parentId;

    private List<FileDto> children;
}
