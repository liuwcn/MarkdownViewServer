package vip.liuw.mdview.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.liuw.mdview.framework.component.SystemConstant;
import vip.liuw.mdview.framework.response.Res;
import vip.liuw.mdview.service.MdFileService;

@RestController
@RequestMapping("/mdfile")
public class MdFileAPI {
    @Autowired
    private MdFileService mdFileService;

    @GetMapping("/list")
    public Res list(@RequestParam(defaultValue = SystemConstant.ROOT_FILE_ID) String id,
                    @RequestParam(defaultValue = "0") int flush) {
        return mdFileService.list(id, flush);
    }

    @GetMapping("/content")
    public Res getContent(@RequestParam String id) {
        return mdFileService.getContent(id);
    }
}
