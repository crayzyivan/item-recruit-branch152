package com.item.controller;

import com.item.dto.background.UserBackgroundCheckDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.framework.http.Pager;
import com.item.service.BackgroundService;
import com.item.vo.BackgroundListVO;
import com.item.vo.BackgroundQueryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@RestController
@RequestMapping("/background-check")
@RequiredArgsConstructor
@Slf4j
public class BackgroundCheckController {
    private final BackgroundService backgroundService;
    @PostMapping("/result")
    public void webhook(@RequestBody String data) {
        backgroundService.backgroundPdfReportProcess(data);
    }

    /**
     * 发起背调
     * @param candidateJobId
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/trigger/{candidateJobId}")
    public void startBackgroundCheck(@PathVariable(value = "candidateJobId") Long candidateJobId) {
        backgroundService.startCheck(candidateJobId);
    }

    /**
     * 根据id获取背调结果报告
     * @param id
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/report/{id}")
    public UserBackgroundCheckDTO getBackgroundReport(@PathVariable(value = "id") Long id) {
        return backgroundService.getUserBackgroundCheckResult(id);
    }

    /**
     * 背景调查列表
     * @param pageIndex
     * @param pageSize
     * @param jobId
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/list")
    public Pager<BackgroundListVO> selectBackgroundPageList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                            @RequestParam(value = "jobId") Long jobId){
        BackgroundQueryVO backgroundQueryVO = BackgroundQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).jobId(jobId).build();
        Pager<BackgroundListVO> page =backgroundService.selectBackgroundPageList(backgroundQueryVO);
        return page;
    }
}
