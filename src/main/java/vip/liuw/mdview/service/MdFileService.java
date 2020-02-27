package vip.liuw.mdview.service;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vip.liuw.mdview.dto.FileDto;
import vip.liuw.mdview.framework.component.SystemConstant;
import vip.liuw.mdview.framework.response.Res;
import vip.liuw.mdview.utils.IDUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MdFileService {
    @Value("${myconfig.root-dir}")
    private String rootDir;

    @Value("${myconfig.base-url}")
    private String baseUrl;

    @Value("${myconfig.exclude-dir}")
    private String excludeDir;


    //markdown中的文件
    private Pattern imagePattern = Pattern.compile("!\\[(.*)\\]\\((.*)\\)");
    //常用的远程文件协议
    private Pattern remoteFilePattern = Pattern.compile("^(http://|https://|ftp://)");
    //上一级目录
    private Pattern preDirPattern = Pattern.compile("\\.\\./");


    private FileDto rootFile = new FileDto();
    private Map<String, FileDto> allFile = Collections.synchronizedMap(new HashMap<>());

    @PostConstruct
    public void init() {
        File root = new File(rootDir);
        if (!root.exists()) {
            throw new RuntimeException("根文件不存在");
        }
        rootFile.setId(SystemConstant.ROOT_FILE_ID);
        rootFile.setName(root.getName());
        rootFile.setType(FileDto.FILE_TYPE_DIR);
        allFile.put(rootFile.getId(), rootFile);
    }

    /**
     * 刷新当前目录及子目录
     *
     * @param parent
     */
    public void cleanCache(FileDto parent) {
        FileDto tmp = parent;
        if (tmp.getChildren() != null) {
            for (FileDto fileDto : tmp.getChildren()) {
                allFile.remove(fileDto.getId());
                cleanCache(fileDto);
            }
            tmp.setChildren(null);
        }
    }

    /**
     * 列出当前目录下的目录和文件
     * 已排除指定的目录和非.md文件
     *
     * @param id
     * @param flush 是否刷新当前目录
     * @return
     */
    public Res list(String id, int flush) {
        FileDto parent = allFile.get(id);
        List<FileDto> fileList = null;
        if (flush == 1) {
            cleanCache(parent);
        } else {
            fileList = parent.getChildren();
        }
        if (fileList == null) {
            fileList = new ArrayList<>();
            parent.setChildren(fileList);
            File parentFile = new File(getAbsolutePath(parent));
            if (!parentFile.exists()) {
                return Res.error("路径不存在");
            }
            if (!parentFile.isDirectory()) {
                return Res.error("路径非文件夹");
            }

            for (File file : parentFile.listFiles()) {
                FileDto dto = new FileDto();
                //排除部分文件和目录
                if (file.isFile()) {
                    if (!file.getName().endsWith(".md")) {
                        continue;
                    }
                    dto.setType(FileDto.FILE_TYPE_FILE);
                } else {
                    if (Pattern.matches(excludeDir, file.getName())) {
                        continue;
                    }
                    dto.setType(FileDto.FILE_TYPE_DIR);
                }
                dto.setName(file.getName());
                dto.setId(IDUtils.getUUID());
                dto.setParentId(parent.getId());
                allFile.put(dto.getId(), dto);
                fileList.add(dto);
            }
        }
        return Res.ok(fileList);
    }

    /**
     * 读取文件内容
     *
     * @param id
     * @return
     */
    public Res getContent(String id) {
        FileDto fileDto = allFile.get(id);
        File file = new File(getAbsolutePath(fileDto));
        String content = "";
        StringBuilder sb = new StringBuilder();
        try {
            FileReader reader = new FileReader(file);
            char[] bs = new char[2048];
            int len;
            while ((len = reader.read(bs)) > -1) {
                sb.append(ArrayUtils.subarray(bs, 0, len));
            }
            content = sb.toString();
            Matcher matcher = imagePattern.matcher(content);
            //替换本地图片的相对路径为项目http路径
            while (matcher.find()) {
                //图片路径
                String fileInfo = matcher.group(0);
                String name = matcher.group(1);
                String path = matcher.group(2);
                //跳过非本地文件
                if (!remoteFilePattern.matcher(path).find()) {
                    //当前文件的相对目录
                    String relativePath = getRelativePath(fileDto).replace(File.separator + fileDto.getName(), "")
                            .replaceAll("\\\\", "/");
                    //文件在上级目录的计算目录级数
                    if (path.startsWith("../")) {
                        Matcher preDirMather = preDirPattern.matcher(path);
                        int count = preDirMather.groupCount();
                        //图片在文件根目录之外的不管
                        if (count > relativePath.split("/").length) {
                            continue;
                        }
                        while (count > 0) {
                            relativePath = relativePath.substring(0, relativePath.lastIndexOf("/"));
                            count--;
                        }
                    } else {
                        path = path.replaceFirst("\\./", "");
                    }
                    //拼凑本项目的http图片路径
                    content = content.replace(fileInfo, String.format("![%s](%s%s)", name, baseUrl,
                            relativePath + "/" + path));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Res.error("文件不存在");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Res.ok().data(content);
    }

    public String getAbsolutePath(FileDto fileDto) {
        return rootDir + getRelativePath(fileDto);
    }

    public String getRelativePath(FileDto fileDto) {
        if (SystemConstant.ROOT_FILE_ID.equals(fileDto.getId())) {
            return "";
        }
        List<String> pathList = new ArrayList<>();
        pathList.add(fileDto.getName());
        FileDto tmp = fileDto;
        while (tmp.getParentId() != null && !SystemConstant.ROOT_FILE_ID.equals(tmp.getParentId())) {
            tmp = allFile.get(tmp.getParentId());
            pathList.add(tmp.getName());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = pathList.size(); i > 0; i--) {
            sb.append(File.separator).append(pathList.get(i - 1));
        }
        return sb.toString();
    }


}
