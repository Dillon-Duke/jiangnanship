package com.caidao.controller.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tom
 */
@RestController
public class FileController {

    @GetMapping("/file")
    public String file() {
        System.out.println("tijiao");
        return "/fileUploade";
    }
    }
