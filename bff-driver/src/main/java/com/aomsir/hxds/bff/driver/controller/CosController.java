package com.aomsir.hxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.aomsir.hxds.common.exception.HxdsException;
import com.aomsir.hxds.common.util.CosUtil;
import com.aomsir.hxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;

/**
 * @Author: Aomsir
 * @Date: 2023/1/5
 * @Description:
 * @Email: info@say521.cn
 * @GitHub: https://github.com/aomsir
 */

@RestController
@RequestMapping("/cos")
@Tag(name = "CosController", description = "对象存储Web接口")
public class CosController {

    private static final Logger log = LoggerFactory.getLogger(CosController.class);
    @Resource
    private CosUtil cosUtil;

    @PostMapping("/uploadCosPrivateFile")
    @SaCheckLogin
    @Operation(summary = "上传文件")
    public R uploadCosPrivateFile(@Param("file") MultipartFile file,
                                   @Param("module") String module) {
        if (file.isEmpty()) {
            throw new HxdsException("上传文件不能为空");
        }


        try {
            String path = null;
            if ("driverAuth".equals(module)) {
                path = "/driver/auth/";   // 私有存储桶的上传目录
            } else {
                throw new HxdsException("module错误");
            }

            HashMap map = this.cosUtil.uploadPrivateFile(file, path);
            return R.ok(map);
        } catch (IOException e) {
            log.error("文件上传到腾讯云错误：", e);
            throw new HxdsException("文件上传到腾讯云错误");
        }
    }



    @PostMapping("/deleteCosPrivateFile")
    @SaCheckLogin
    @Operation(summary = "删除文件")
    public R deleteCosPrivateFile() {

    }
}
